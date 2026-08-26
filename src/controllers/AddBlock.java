package controllers;

import java.io.IOException;
import java.util.Map;

import web.Response;
import web.Request;
import web.rest.annotations.Get;
import web.rest.annotations.RestController;

public class AddBlock {

  @RestController
  public static class addBlockController {
    
    @Get("/addBlock")
    public void addBlock(Response res, Request req) throws IOException {
      res.status(200).json("message", "Adicionar bloco");
      req.debugPathParams("GET");
    }

    @Get("/teste")
    public void teste(Response res, Request req) throws IOException {
      res.status(200).json("message", "teste");
      req.debugPathParams("GET");
    }
  }
}
