package controllers;

import java.io.IOException;

import web.Response;
import web.rest.annotations.Get;
import web.rest.annotations.RestController;

public class AddBlock {

  @RestController
  public static class addBlockController {
    
    @Get("/addBlock")
    public void addBlock(Response res) throws IOException {
      res.status(200).json("message", "Adicionar bloco");
    }

    @Get("/teste")
    public void teste(Response res) throws IOException {
      res.status(200).json("message", "teste");
    }
  }
}
