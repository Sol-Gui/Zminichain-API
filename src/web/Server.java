package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import web.annotations.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class Server {
  private final int endpoint;
  private final int threadPool;
  private final Map<String, HttpHandler> routes = new TreeMap<>(); // Referência a rota à uma instância do
  private final ExecutorService executor;

  public Server(int endpoint, int threadPool, ExecutorService executor) {
    this.endpoint = endpoint;
    this.threadPool = threadPool;
    this.executor = executor;
  }

  private static void validateHttpAnnotations(Method method) {
    long count = Arrays.stream(method.getAnnotations())
        .map(Annotation::annotationType)
        .filter(a -> a.isAnnotationPresent(HttpMethod.class))
        .count();

    if (count > 1) {
      throw new IllegalStateException(
          "Method " + method.getName() + " cannot have multiple HTTP method annotations"
      );
    }
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

  public void use(Class<?> controller) {
    try {
      boolean annotationPresent = controller.isAnnotationPresent(RestController.class);

      if (annotationPresent) {
        Method[] methods = controller.getDeclaredMethods();

        Map<Class<? extends Annotation>, Consumer<Method>> handlers = new HashMap<>();

        handlers.put(Get.class, this::processGetMethod);
        handlers.put(Post.class, this::processPostMethod);
        handlers.put(Put.class, this::processPutMethod);
        handlers.put(Delete.class, this::processDeleteMethod);


        for (Method method : methods) {

          validateHttpAnnotations(method);

          for (Annotation annotation : method.getAnnotations()) {

            Consumer<Method> handler = handlers.get(annotation.annotationType());

            if (handler != null) {
              handler.accept(method);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace(); // mudar depois
    }
  }

  private void verifyIsEndedResponse(Response res) throws IOException {
    if (!res.isEnded()) {
      res.end();
    }
  }

  private void processGetMethod(Method method) {
    Get getAnnotation = method.getAnnotation(Get.class);
    String route = getAnnotation.value();

    class DefaultHandler implements HttpHandler {
      @Override
      public void handle(HttpExchange exchange) throws IOException {
        Response res = new Response(exchange);
        Request req;
        try {
          //Object result;
          int i;

          Class<?>[] types = method.getParameterTypes();
          Object[] args = new Object[types.length];

          for (i = 0; i < types.length; i++) {
            if (types[i] == Response.class) {
              args[i] = res;
            }

            if (types[i] == Request.class) {
              req = new Request(exchange);
              args[i] = req;
            }
          }

          method.invoke(method.getDeclaringClass()
              .getDeclaredConstructor()
              .newInstance(), args);

          verifyIsEndedResponse(res);

        } catch (Exception e) {
          e.printStackTrace();
          res.status(500)
              .send("{\"error\":\"Internal Server Error\"}")
              .end();
        }
      }
    }

    routes.put(route, new DefaultHandler());
  }

  private void processPostMethod(Method method) {
    System.out.println("Executou Post em " + method.getName());
  }

  private void processPutMethod(Method method) {
    System.out.println("Executou Put em " + method.getName());
  }

  private void processDeleteMethod(Method method) {
    System.out.println("Executou Delete em " + method.getName());
  }

  /**
  @RestController
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
  */
}