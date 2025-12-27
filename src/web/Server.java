package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

public class Server {
  private final int endpoint;
  private final int threadPool;
  private final Map<String, HttpHandler> routes = new TreeMap<>();
  private final ExecutorService executor;

  public Server(int endpoint, int threadPool, ExecutorService executor) {
    this.endpoint = endpoint;
    this.threadPool = threadPool;
    this.executor = executor;
  }

  public void run() throws IOException {
    HttpServer server = HttpServer.create(new InetSocketAddress(endpoint), threadPool);

    routes.forEach(server::createContext);
    server.setExecutor(executor);

    server.start();
  }

  public void shutdown() {
    if (executor != null) {
      executor.shutdown();
    }
  }

  public void use(String route, HttpHandler handler) {
    routes.put(route, handler);
  }

  static class HelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
      String response = "{\"message\": \"Hello Blockchain\"}";
      exchange.getResponseHeaders().add("Content-Type", "application/json");
      exchange.sendResponseHeaders(200, response.length());

      OutputStream os = exchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }

  public static void main(String[] args) throws IOException {
    int port = 3000;
    Server server = new Server(port, 0, null);

    server.use("/hello", new HelloHandler());
    server.run();
    System.out.println("Servidor rodando em http://localhost:" + port);
  }
}