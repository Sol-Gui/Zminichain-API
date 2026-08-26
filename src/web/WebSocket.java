package web;

import web.websocket.annotations.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.TreeMap;
import java.util.Map;

public class WebSocket {
  private static final String MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

  private final Server server;
  private final Map<String, WsRouteHandlers> wsRouteMap = new TreeMap<>();

  private static class WsRouteHandlers {
    Method onOpen;
    Method onMessage;
    Method onClose;
    Method onError;
    Class<?> controllerClass;
  }

  private static class WebSocketFrame {
    int opcode;
    byte[] payload;
  }

  public WebSocket(Server server) {
    this.server = server;
  }

  public void use(Class<?> controller) {
    WsController wsAnnotation = controller.getAnnotation(WsController.class);

    if (wsAnnotation != null) {
      registerRoute(controller, wsAnnotation);
      return;
    }

    for (Class<?> innerClass : controller.getDeclaredClasses()) {
      WsController innerAnnotation = innerClass.getAnnotation(WsController.class);
      if (innerAnnotation == null) continue;
      registerRoute(innerClass, innerAnnotation);
    }
  }

  private void registerRoute(Class<?> clazz, WsController annotation) {
    String route = annotation.value();
    WsRouteHandlers handlers = new WsRouteHandlers();
    handlers.controllerClass = clazz;

    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(OnOpen.class)) {
        handlers.onOpen = method;
      } else if (method.isAnnotationPresent(OnMessage.class)) {
        handlers.onMessage = method;
      } else if (method.isAnnotationPresent(OnClose.class)) {
        handlers.onClose = method;
      } else if (method.isAnnotationPresent(OnError.class)) {
        handlers.onError = method;
      }
    }

    wsRouteMap.put(route, handlers);
    server.addWebSocketRoute(route, (client, req) -> handleUpgrade(client, req, handlers));
  }

  private void handleUpgrade(Socket client, Request req, WsRouteHandlers handlers) throws Exception {
    performHandshake(client, req);

    WebSocketConnection connection = new WebSocketConnection(client, req.getPath());

    if (handlers.onOpen != null) {
      invokeHandler(handlers.onOpen, handlers.controllerClass, connection, null, null, null);
    }

    try {
      InputStream in = client.getInputStream();

      while (!client.isClosed()) {
        WebSocketFrame frame = readFrame(in);

        if (frame == null) break;

        switch (frame.opcode) {
          case 0x1 -> {
            String message = new String(frame.payload, "UTF-8");
            if (handlers.onMessage != null) {
              invokeHandler(handlers.onMessage, handlers.controllerClass, connection, message, null, null);
            }
          }
          case 0x2 -> {
            if (handlers.onMessage != null) {
              invokeHandler(handlers.onMessage, handlers.controllerClass, connection, null, frame.payload, null);
            }
          }
          case 0x8 -> {
            if (handlers.onClose != null) {
              invokeHandler(handlers.onClose, handlers.controllerClass, connection, null, null, null);
            }
            client.close();
          }
          case 0x9 -> {
            ByteArrayOutputStream pong = new ByteArrayOutputStream();
            pong.write(0x80 | 0xA);
            pong.write(0);
            client.getOutputStream().write(pong.toByteArray());
            client.getOutputStream().flush();
          }
        }
      }
    } catch (IOException e) {
      if (handlers.onError != null) {
        invokeHandler(handlers.onError, handlers.controllerClass, connection, null, null, e);
      }
      client.close();
    }
  }

  private WebSocketFrame readFrame(InputStream in) throws IOException {
    int b1 = in.read();
    if (b1 == -1) return null;
    int opcode = b1 & 0x0F;

    int b2 = in.read();
    if (b2 == -1) return null;
    boolean masked = (b2 & 0x80) != 0;
    long payloadLength = b2 & 0x7F;

    if (payloadLength == 126) {
      payloadLength = ((in.read() & 0xFF) << 8) | (in.read() & 0xFF);
    } else if (payloadLength == 127) {
      payloadLength = 0;
      for (int i = 0; i < 8; i++) {
        payloadLength = (payloadLength << 8) | (in.read() & 0xFF);
      }
    }

    byte[] mask = null;
    if (masked) {
      mask = new byte[4];
      in.read(mask);
    }

    byte[] payload = new byte[(int) payloadLength];
    int totalRead = 0;
    while (totalRead < payload.length) {
      int read = in.read(payload, totalRead, payload.length - totalRead);
      if (read == -1) throw new IOException("Connection closed");
      totalRead += read;
    }

    if (masked) {
      for (int i = 0; i < payload.length; i++) {
        payload[i] = (byte) (payload[i] ^ mask[i % 4]);
      }
    }

    WebSocketFrame frame = new WebSocketFrame();
    frame.opcode = opcode;
    frame.payload = payload;
    return frame;
  }

  private void performHandshake(Socket client, Request req) throws IOException, NoSuchAlgorithmException {
    String wsKey = req.getHeaders().get("Sec-WebSocket-Key");

    if (wsKey == null) {
      throw new IOException("Missing Sec-WebSocket-Key");
    }

    String acceptKey = Base64.getEncoder().encodeToString(
        MessageDigest.getInstance("SHA-1")
            .digest((wsKey + MAGIC).getBytes("UTF-8"))
    );

    String response = "HTTP/1.1 101 Switching Protocols\r\n"
        + "Upgrade: websocket\r\n"
        + "Connection: Upgrade\r\n"
        + "Sec-WebSocket-Accept: " + acceptKey + "\r\n"
        + "\r\n";

    client.getOutputStream().write(response.getBytes("UTF-8"));
    client.getOutputStream().flush();
  }

  private void invokeHandler(Method method, Class<?> controllerClass,
                             WebSocketConnection connection,
                             String message, byte[] binaryData, Exception error) throws Exception {
    Class<?>[] paramTypes = method.getParameterTypes();
    Object[] args = new Object[paramTypes.length];

    for (int i = 0; i < paramTypes.length; i++) {
      if (paramTypes[i] == WebSocketConnection.class) {
        args[i] = connection;
      } else if (paramTypes[i] == String.class) {
        args[i] = message;
      } else if (paramTypes[i] == byte[].class) {
        args[i] = binaryData;
      } else if (Exception.class.isAssignableFrom(paramTypes[i])) {
        args[i] = error;
      }
    }

    Object instance = controllerClass.getDeclaredConstructor().newInstance();
    method.invoke(instance, args);
  }
}
