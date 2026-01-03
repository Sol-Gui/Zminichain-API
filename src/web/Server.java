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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Server {
  private final int endpoint;
  private final int connectionBacklog;
  private HttpServer server;
  private final Map<String, HttpHandler> routes = new TreeMap<>(); // Referência a rota à uma instância do
  private final ExecutorService executor;

  public Server(int endpoint, int connectionBacklog, int workerThreads) {
    if (connectionBacklog < 0) {
      throw new IllegalArgumentException("connectionBacklog must be >= 0");
    }

    if (workerThreads < 0) {
      throw new IllegalArgumentException("workerThreads must be >= 0");
    }

    this.endpoint = endpoint;
    this.connectionBacklog = connectionBacklog;

    if (workerThreads == 0) {
      this.executor = null;
    } else {
      this.executor = Executors.newFixedThreadPool(workerThreads);
    }
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
    this.server = HttpServer.create(new InetSocketAddress(endpoint), connectionBacklog);

    routes.forEach(server::createContext);

    if (executor != null) {
      server.setExecutor(executor);
    }

    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));

    server.start();
  }

  public void stop() {
    if (server != null) {
      server.stop(0);
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
      throw e;
    }
  }

  private void verifyIsEndedResponse(Response res) throws IOException {
    if (!res.isEnded()) {
      res.end();
    }
  }

  private void verifyMethod(HttpExchange exchange, String method) throws IOException {
    if (!exchange.getRequestMethod().equalsIgnoreCase(method)) {
      exchange.sendResponseHeaders(405, -1);
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
          verifyMethod(exchange, "GET");

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
}