package web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Response {
  private final OutputStream output;

  private final Map<String, List<String>> headers =
          new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  private final ByteArrayOutputStream body =
          new ByteArrayOutputStream();

  private int status = 200;
  private boolean isEnded;

  public Response(OutputStream output) {
    this.output = output;
  }

  public Response send(String body) throws IOException {
    verifyNotEnded();

    this.body.write(
        body.getBytes(StandardCharsets.UTF_8)
    );

    return this;
  }

  public Response status(int status) {
    verifyNotEnded();

    setHeader(
        "Content-Type",
        "application/json"
    );

    this.status = status;
    return this;
  }

  public Response addHeader(
      String key,
      String value
  ) {
    verifyNotEnded();

    headers
        .computeIfAbsent(key, ignored -> new ArrayList<>())
        .add(value);

    return this;
  }

  public Response setHeader(
      String key,
      String value
  ) {
    verifyNotEnded();

    List<String> values = new ArrayList<>();
    values.add(value);

    headers.put(key, values);

    return this;
  }

  public Response json(String... map)
      throws IOException {
    verifyNotEnded();

    if (map.length < 2 || map.length % 2 != 0) {
      throw new IllegalArgumentException(
          "json() expects key/value pairs"
      );
    }

    setHeader(
        "Content-Type",
        "application/json"
    );

    StringBuilder response =
        new StringBuilder("{");

    for (int i = 0; i < map.length; i += 2) {
      if (i > 0) {
        response.append(",");
      }

      response.append("\"")
          .append(escapeJson(map[i]))
          .append("\":\"")
          .append(escapeJson(map[i + 1]))
          .append("\"");
    }

    response.append("}");

    body.write(
        response.toString()
            .getBytes(StandardCharsets.UTF_8)
    );

    return this;
  }

  private String escapeJson(String value) {
    if (value == null) {
      return "null";
    }

    StringBuilder sb = new StringBuilder();

    for (char c : value.toCharArray()) {
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\b' -> sb.append("\\b");
        case '\f' -> sb.append("\\f");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");

        default -> {
          if (c < 32) {
            sb.append(
                String.format("\\u%04x", (int) c)
            );
          } else {
            sb.append(c);
          }
        }
      }
    }

    return sb.toString();
  }

  public void end() throws IOException {
    if (isEnded) {
      return;
    }

    byte[] responseBody = body.toByteArray();

    setHeader(
        "Content-Length",
        String.valueOf(responseBody.length)
    );

    if (!headers.containsKey("Connection")) {
      setHeader("Connection", "close");
    }

    StringBuilder responseHeaders =
        new StringBuilder();

    responseHeaders
        .append("HTTP/1.1 ")
        .append(status)
        .append(" ")
        .append(getReasonPhrase(status))
        .append("\r\n");

    headers.forEach((key, values) -> {
      for (String value : values) {
        responseHeaders
            .append(key)
            .append(": ")
            .append(value)
            .append("\r\n");
      }
    });

    responseHeaders.append("\r\n");

    output.write(
        responseHeaders.toString()
            .getBytes(StandardCharsets.US_ASCII)
    );

    output.write(responseBody);
    output.flush();

    isEnded = true;
  }

  public boolean isEnded() {
    return isEnded;
  }

  private void verifyNotEnded() {
    if (isEnded) {
      throw new IllegalStateException(
          "Response has already ended"
      );
    }
  }

  private String getReasonPhrase(int status) {
    return switch (status) {
      case 100 -> "Continue";
      case 101 -> "Switching Protocols";
      case 200 -> "OK";
      case 201 -> "Created";
      case 202 -> "Accepted";
      case 204 -> "No Content";
      case 301 -> "Moved Permanently";
      case 302 -> "Found";
      case 304 -> "Not Modified";
      case 400 -> "Bad Request";
      case 401 -> "Unauthorized";
      case 403 -> "Forbidden";
      case 404 -> "Not Found";
      case 405 -> "Method Not Allowed";
      case 409 -> "Conflict";
      case 415 -> "Unsupported Media Type";
      case 422 -> "Unprocessable Entity";
      case 500 -> "Internal Server Error";
      case 501 -> "Not Implemented";
      case 503 -> "Service Unavailable";
      default -> "";
    };
  }
}