package web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

  public Response json(String... map) throws IOException {
    if (map.length < 2 || map.length % 2 != 0) {
      throw new IllegalArgumentException("json() expects key/value pairs");
    }

    StringBuilder response = new StringBuilder("{");

    for (int i = 0; i < map.length; i+=2) {
      if (i > 0) response.append(",");

      response.append("\"")
          .append(escapeJson(map[i]))
          .append("\":\"")
          .append(escapeJson(map[i + 1]))
          .append("\"");
    }
    response = response.append("}");

    byte[] responseBytes = response.toString().getBytes();

    exchange.sendResponseHeaders(status, responseBytes.length);
    exchange.getResponseBody().write(responseBytes);

    return this;
  }

  private String escapeJson(String value) {
    if (value == null) return "null";

    StringBuilder sb = new StringBuilder();

    for (char c : value.toCharArray()) {
      switch (c) {
        case '"':  sb.append("\\\""); break;
        case '\\': sb.append("\\\\"); break;
        case '\b': sb.append("\\b"); break;
        case '\f': sb.append("\\f"); break;
        case '\n': sb.append("\\n"); break;
        case '\r': sb.append("\\r"); break;
        case '\t': sb.append("\\t"); break;
        default:
          if (c < 32) {
            sb.append(String.format("\\u%04x", (int) c));
          } else {
            sb.append(c);
          }
      }
    }
    return sb.toString();
  }


  public void end() throws IOException {
    exchange.getResponseBody().close();
    isEnded = true;
  }

  public boolean isEnded() {
    return isEnded;
  }
}