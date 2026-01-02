package web;

import com.sun.net.httpserver.HttpExchange;
import web.annotations.*;

import java.io.IOException;

public class Main {

  @RestController
  static class Hello {

    /**
    @Get("/testes")
    public String Testando() {
      return "Bom dia!";
    }

     */

    @Get("/servidor")
    public void HelloWorld(Response res) throws IOException {
      res.status(200).send("{\"message\":\"Hello World!\"}");

      res.end();
    }

    @Get("/joao")
    public void Joao(Response res) throws IOException {
      res.status(200).send("João");

      res.end();
    }

    @Post("/new-message")
    public void NewMessage(String Message) {

    }
  }

  public static void main(String[] args) throws IOException {
    int port = 3000;
    Server server = new Server(port, 0, null);

    server.use(Hello.class);
    server.run();
    System.out.println("Servidor rodando em http://localhost:" + port);
  }
}
