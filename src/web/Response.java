package web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class Response {
  private HttpExchange exchange;
  private int status;
  private boolean isEnded;

  public Response(HttpExchange exchange) {
    this.exchange = exchange;
  }

  public Response send(String body) throws IOException {
    exchange.sendResponseHeaders(status, body.getBytes().length);
    exchange.getResponseBody().write(body.getBytes());
    return this;
  }

  public Response status(int status) {
    exchange.getResponseHeaders().add("Content-Type", "application/json");
    this.status = status;
    return this;
  }

  public void json() {

  }

  public void end() throws IOException {
    exchange.getResponseBody().close();
    isEnded = true;
  }

  public boolean isEnded() {
    return isEnded;
  }
}