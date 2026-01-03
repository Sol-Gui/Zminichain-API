package web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class Response {
  private final HttpExchange exchange;
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

  public Response addHeader(String key, String value) {
    exchange.getResponseHeaders().add(key, value);
    return this;
  }

  public Response setHeader(String key, String value) {
    exchange.getResponseHeaders().set(key, value);
    return this;
  }

  public Response json(String key, String value) throws IOException {
    String response;

    response = String.format("{\"%s\":\"%s\"}", key, value);

    exchange.sendResponseHeaders(status, response.getBytes().length);
    exchange.getResponseBody().write(response.getBytes());

    return this;
  }

  public void end() throws IOException {
    exchange.getResponseBody().close();
    isEnded = true;
  }

  public boolean isEnded() {
    return isEnded;
  }
}