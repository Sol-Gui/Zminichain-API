package controllers;

import web.websocket.annotations.*;

public class TestWebSocket {
  @WsController("/testingWSconnection")
  public static class TestWebSocket {
    
    @OnOpen
  }
}
