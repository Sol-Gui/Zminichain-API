package web;

import com.sun.net.httpserver.HttpExchange;

public class Request {
  HttpExchange exchange;

  public Request(HttpExchange exchange) {
    this.exchange = exchange;
  }
}
