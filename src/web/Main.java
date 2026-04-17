package web;

import web.rest.annotations.Get;
import web.rest.annotations.Post;
import web.rest.annotations.RestController;
import controllers.*;

import java.io.IOException;

public class Main {

  @RestController
  public static class startController {

    @Get("/")
    public void generalContext(Response res) throws IOException {
      res.status(200).json("message", "Tem nada aqui não parcero");
    }

    @Get("/servidor")
    public void HelloWorld(Response res) throws IOException {
      res.status(200).send("{\"message\":\"Hello World!\", \"joao\":\"Eduardo!\"}");
    }

    @Get("/joao")
    public void Joao(Response res) throws IOException {
      res.status(200).json(
          "message", "Hello João!",
          "Eduardo", "Novo",
          "Testando", "Json"
      );  
    }

    @Post("/new-message")
    public void NewMessage(Response res, Request req) throws IOException {
      String body = req.getBody();

      res.status(200).json(
          "message", "Mensagem recebida via POST",
          "body", body.isEmpty() ? "Nenhum corpo enviado" : body,
          "method", "POST"
      );
    }
  }

  public static void main(String[] args) throws IOException {
    int port = 3000;
    Server server = new Server(port, 50, 0);
    server.addGlobalHeaders("Access-Control-Allow-Origin", "http://localhost:5173");
    //WebSocket io = new

    server.use(startController.class);
    server.use(AddBlock.addBlockController.class);

    server.run();
    System.out.println("Servidor rodando em http://localhost:" + port);
  }
}
