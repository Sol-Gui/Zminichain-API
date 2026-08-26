package web;

import web.rest.annotations.*;
import web.websocket.annotations.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Server {
  @FunctionalInterface
  protected interface WebSocketUpgradeHandler {
    void handle(Socket client, Request req) throws Exception;
  }

  @FunctionalInterface
  private interface RouteHandler {
    void handle(Request req, Response res) throws Exception;
  }
  private final int endpoint;
  private final int connectionBacklog;

  protected ServerSocket server;
  private Thread acceptThread;
  private volatile boolean running;

  private final Map<String, RouteHandler> routes = new TreeMap<>();
  private final Map<String, WebSocketUpgradeHandler> wsRoutes = new TreeMap<>();

  private final ExecutorService executor;
  private final Map<String, String> globalHeaders = new TreeMap<>();

  public Server(int endpoint, int connectionBacklog, int workerThreads) {
    if (connectionBacklog < 0) {
      throw new IllegalArgumentException(
          "connectionBacklog must be >= 0"
      );
    }

    if (workerThreads < 0) {
      throw new IllegalArgumentException(
          "workerThreads must be >= 0"
      );
    }

    this.endpoint = endpoint;
    this.connectionBacklog = connectionBacklog;

    if (workerThreads == 0) {
      this.executor = null;
    } else {
      this.executor = Executors.newFixedThreadPool(workerThreads);
    }
  }

  public void addGlobalHeaders(String key, String value) {
    globalHeaders.put(key, value);
  }

  private void useGlobalHeaders(Response response) {
    globalHeaders.forEach(response::addHeader);
  }

  protected static void validateAnnotations(Method method, Class<? extends Annotation> GenericAnnotationClass) {
    long count = Arrays.stream(method.getAnnotations())
        .map(Annotation::annotationType)
        .filter(annotation ->
            annotation.isAnnotationPresent(GenericAnnotationClass)
        )
        .count();

    if (count > 1) {
      throw new IllegalStateException(
          "Method "
              + method.getName()
              + " cannot have multiple method annotations"
      );
    }
  }

  public synchronized void run() throws IOException {
    if (running) {
      throw new IllegalStateException("Server is already running");
    }

    server = new ServerSocket();
    server.setReuseAddress(true);

    server.bind(
        new InetSocketAddress(endpoint),
        connectionBacklog
    );

    running = true;

    acceptThread = new Thread(
        this::acceptConnections,
        "server-accept-thread"
    );

    acceptThread.start();

    Runtime.getRuntime().addShutdownHook(
        new Thread(this::stop)
    );
  }

  private void acceptConnections() {
    while (running) {
      try {
        Socket client = server.accept();

        Runnable requestTask = () -> handleClient(client);

        if (executor != null) {
          executor.execute(requestTask);
        } else {
          new Thread(requestTask).start();
        }

      } catch (SocketException e) {
        if (running) {
          e.printStackTrace();
        }

      } catch (IOException e) {
        if (running) {
          e.printStackTrace();
        }
      }
    }
  }

  private void handleClient(Socket client) {
    try {
      Response res = new Response(client.getOutputStream());
      useGlobalHeaders(res);

      Request req;

      try {
        req = new Request(client.getInputStream());
      } catch (Exception e) {
        res.status(400)
            .send("{\"error\":\"Bad Request\"}")
            .end();
        client.close();
        return;
      }

      WebSocketUpgradeHandler wsHandler = wsRoutes.get(req.getPath());

      if (wsHandler != null && isWebSocketUpgrade(req)) {
        wsHandler.handle(client, req);
        return;
      }

      RouteHandler handler = routes.get(req.getPath());

      if (handler == null) {
        res.status(404)
            .send("{\"error\":\"Not Found\"}")
            .end();
        client.close();
        return;
      }

      try {
        handler.handle(req, res);
        verifyIsEndedResponse(res);
      } catch (Exception e) {
        e.printStackTrace();

        if (!res.isEnded()) {
          res.status(500)
              .send("{\"error\":\"Internal Server Error\"}")
              .end();
        }
      }

      client.close();

    } catch (Exception e) {
      e.printStackTrace();
      try { client.close(); } catch (IOException ignored) {}
    }
  }

  private boolean isWebSocketUpgrade(Request req) {
    String upgrade = req.getHeaders().get("Upgrade");
    String connection = req.getHeaders().get("Connection");
    return "websocket".equalsIgnoreCase(upgrade)
        && connection != null
        && connection.toLowerCase().contains("upgrade");
  }

  public synchronized void stop() {
    running = false;

    if (server != null && !server.isClosed()) {
      try {
        server.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    if (executor != null) {
      executor.shutdown();

      try {
        if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
          executor.shutdownNow();
        }
      } catch (InterruptedException e) {
        executor.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }

  public void use(Class<?> controller) {
    try {
      boolean annotationPresent =
          controller.isAnnotationPresent(RestController.class);

      if (annotationPresent) {
        Method[] methods = controller.getMethods();

        Map<Class<? extends Annotation>, Consumer<Method>> handlers =
            new HashMap<>();

        handlers.put(Get.class, this::processGetMethod);
        handlers.put(Post.class, this::processPostMethod);
        handlers.put(Put.class, this::processPutMethod);
        handlers.put(Delete.class, this::processDeleteMethod);

        for (Method method : methods) {
          validateAnnotations(method, HttpMethod.class);

          for (Annotation annotation : method.getAnnotations()) {
            Consumer<Method> handler =
                handlers.get(annotation.annotationType());

            if (handler != null) {
              handler.accept(method);
            }
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

  private void verifyIsEndedResponse(Response res)
      throws IOException {
    if (!res.isEnded()) {
      res.end();
    }
  }

  private boolean verifyMethod(
      Request req,
      Response res,
      String method
  ) throws IOException {
    if (!req.getMethod().equalsIgnoreCase(method)) {
      res.status(405)
          .send("{\"error\":\"Method Not Allowed\"}")
          .end();

      return false;
    }

    return true;
  }

  private void handleRequestResponse(
      Request req,
      Response res,
      Method method
  ) throws Exception {
    Class<?>[] types = method.getParameterTypes();
    Object[] args = new Object[types.length];

    for (int i = 0; i < types.length; i++) {
      if (types[i] == Response.class) {
        args[i] = res;
      }

      if (types[i] == Request.class) {
        args[i] = req;
      }
    }

    Object controller = method
        .getDeclaringClass()
        .getDeclaredConstructor()
        .newInstance();

    method.invoke(controller, args);
  }

  public void addWebSocketRoute(String path, WebSocketUpgradeHandler handler) {
    wsRoutes.put(path, handler);
  }


    private void processGetMethod(Method method) {
    Get getAnnotation = method.getAnnotation(Get.class);
    String route = getAnnotation.value();

    routes.put(route, (req, res) -> {
      if (!verifyMethod(req, res, "GET")) {
        return;
      }

      handleRequestResponse(req, res, method);
      verifyIsEndedResponse(res);
    });
  }

  private void processPostMethod(Method method) {
    Post postAnnotation = method.getAnnotation(Post.class);
    String route = postAnnotation.value();

    routes.put(route, (req, res) -> {
      if (!verifyMethod(req, res, "POST")) {
        return;
      }

      handleRequestResponse(req, res, method);
      verifyIsEndedResponse(res);
    });
  }

  private void processPutMethod(Method method) {
    System.out.println(
        "Executou Put em " + method.getName()
    );
  }

  private void processDeleteMethod(Method method) {
    System.out.println(
        "Executou Delete em " + method.getName()
    );
  }
}