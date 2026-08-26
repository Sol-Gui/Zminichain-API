package web;

import web.rest.annotations.*;
import controllers.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class Main {

  @RestController
  public static class startController {

    @Get("/")
    public void generalContext(Response res, Request req) throws IOException {
      res.status(200).json("message", "Default Route!");
      req.debugPathParams("GET");
    }

    @Get("/servidor")
    public void HelloWorld(Response res, Request req) throws IOException {
      res.status(200).send("{\"message\":\"Hello World!\", \"joao\":\"Eduardo!\"}");
      req.debugPathParams("GET");
    }

    @Get("/joao")
    public void Joao(Response res, Request req) throws IOException {
      req.debugPathParams("GET");
      res.status(200).json(
          "message", "Hello João!",
          "Eduardo", "Novo",
          "Testando", "Json"
      );  
    }

    @Post("/new-message")
    public void NewMessage(Response res, Request req) throws IOException {
      String body = req.getBody();
      req.debugPathParams("POST");

      res.status(200).json(
          "message", "Mensagem recebida via POST",
          "body", body.isEmpty() ? "Nenhum corpo enviado" : body,
          "method", "POST"
      );
    }
  }

  public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
    int port = 3000;
    Server server = new Server(port, 50, 2);
    server.addGlobalHeaders("Access-Control-Allow-Origin", "http://localhost:5173");
    WebSocket io = new WebSocket(server);

    server.use(startController.class);
    server.use(AddBlock.addBlockController.class);

    io.use(TestWebSocket.webSocketController.class);

    server.run();
    System.out.println("Servidor rodando em http://localhost:" + port);
  }
}
