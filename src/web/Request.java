package web;

import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class Request {
    private final HttpExchange exchange;
    private Map<String, String> queryParams = new java.util.HashMap<>();
    private Map<String, String> pathParams = new java.util.HashMap<>();
    private Map<String, String> headers = new java.util.HashMap<>();
    private String body = null;

    public Request(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}