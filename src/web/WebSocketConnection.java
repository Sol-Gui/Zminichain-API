package web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WebSocketConnection {
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;
    private final String route;

    public WebSocketConnection(Socket socket, String route) throws IOException {
        this.socket = socket;
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
        this.route = route;
    }

    public void send(String message) throws IOException {
        byte[] payload = message.getBytes("UTF-8");
        writeFrame(0x1, payload);
    }

    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            writeFrame(0x8, new byte[0]);
            socket.close();
        }
    }

    public String getRoute() {
        return route;
    }

    public boolean isOpen() {
        return socket != null && !socket.isClosed();
    }

    InputStream getInputStream() {
        return in;
    }

    OutputStream getOutputStream() {
        return out;
    }

    Socket getSocket() {
        return socket;
    }

    private void writeFrame(int opcode, byte[] payload) throws IOException {
        ByteArrayOutputStream frame = new ByteArrayOutputStream();

        frame.write(0x80 | opcode);

        if (payload.length < 126) {
            frame.write(payload.length);
        } else if (payload.length <= 65535) {
            frame.write(126);
            frame.write((payload.length >> 8) & 0xFF);
            frame.write(payload.length & 0xFF);
        } else {
            frame.write(127);
            for (int i = 7; i >= 0; i--) {
                frame.write((int) ((payload.length >> (8 * i)) & 0xFF));
            }
        }

        frame.write(payload);

        byte[] rawFrame = frame.toByteArray();
        out.write(rawFrame);
        out.flush();
    }
}
