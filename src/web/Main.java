package web;

import web.annotations.Get;
import web.annotations.Post;
import web.annotations.RestController;

import java.io.IOException;

public class Main {

  @RestController
  static class Hello {
    String message = "Nozes";

    @Get("/server")
    public String HelloMsg() {
      return message;
    }

    @Get("/testes")
    public String Testando() {
      return "Bom dia!";
    }

    @Get("/servidor")
    public String HelloWorld() {
      return message;
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
