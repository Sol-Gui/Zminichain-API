package web;

import web.annotations.*;

import java.io.IOException;

public class Main {

  @RestController
  static class Hello {

    @Get("/servidor")
    public void HelloWorld(Response res) throws IOException {
      res.status(200).send("{\"message\":\"Hello World!\"}");
    }

    @Get("/joao")
    public void Joao(Response res) throws IOException {
      res.status(200).json("message", "Hello João!");
    }

    @Post("/new-message")
    public void NewMessage(Response res, Request req) {
      res.addHeader("Access-Control-Allow-Origin", "http://localhost:3001");
    }
  }

  public static void main(String[] args) throws IOException {
    int port = 3000;
    Server server = new Server(port, 50, 0);

    server.use(Hello.class);

    server.run();
    System.out.println("Servidor rodando em http://localhost:" + port);
  }
}
