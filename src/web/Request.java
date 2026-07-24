package web;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Request {
  private final String method;
  private final String requestTarget;
  private final String path;
  private final String httpVersion;

  private final Map<String, String> queryParams =
      new HashMap<>();

  private final Map<String, String> pathParams =
      new HashMap<>();

  private final Map<String, String> headers =
      new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  private final byte[] bodyBytes;
  private String body;

  public Request(InputStream input) throws IOException {
    String requestLine = readLine(input);

    if (requestLine == null || requestLine.isBlank()) {
      throw new EOFException("Empty HTTP request");
    }

    String[] requestParts = requestLine.split("\\s+", 3);

    if (requestParts.length != 3) {
      throw new IOException("Invalid HTTP request line");
    }

    method = requestParts[0];
    requestTarget = requestParts[1];
    httpVersion = requestParts[2];

    URI uri;

    try {
      uri = URI.create(requestTarget);
    } catch (IllegalArgumentException e) {
      throw new IOException("Invalid request URI", e);
    }

    String requestPath = uri.getPath();

    if (requestPath == null || requestPath.isBlank()) {
      requestPath = "/";
    }

    path = requestPath;

    parseQueryParams(uri.getRawQuery());

    String headerLine;

    while ((headerLine = readLine(input)) != null) {
      if (headerLine.isEmpty()) {
        break;
      }

      int separatorIndex = headerLine.indexOf(':');

      if (separatorIndex <= 0) {
        throw new IOException("Invalid HTTP header");
      }

      String key = headerLine
          .substring(0, separatorIndex)
          .trim();

      String value = headerLine
          .substring(separatorIndex + 1)
          .trim();

      headers.merge(
          key,
          value,
          (currentValue, newValue) ->
              currentValue + ", " + newValue
      );
    }

    int contentLength = getContentLength();

    bodyBytes = input.readNBytes(contentLength);

    if (bodyBytes.length != contentLength) {
      throw new EOFException(
          "Incomplete HTTP request body"
      );
    }
  }

  private int getContentLength() throws IOException {
    String contentLengthHeader =
        headers.get("Content-Length");

    if (contentLengthHeader == null) {
      return 0;
    }

    try {
      int contentLength =
          Integer.parseInt(contentLengthHeader);

      if (contentLength < 0) {
        throw new IOException(
            "Content-Length cannot be negative"
        );
      }

      return contentLength;

    } catch (NumberFormatException e) {
      throw new IOException(
          "Invalid Content-Length",
          e
      );
    }
  }

  private void parseQueryParams(String rawQuery) {
    if (rawQuery == null || rawQuery.isBlank()) {
      return;
    }

    String[] params = rawQuery.split("&");

    for (String param : params) {
      String[] keyValue = param.split("=", 2);

      String key = URLDecoder.decode(
          keyValue[0],
          StandardCharsets.UTF_8
      );

      String value = keyValue.length == 2
          ? URLDecoder.decode(
              keyValue[1],
              StandardCharsets.UTF_8
          )
          : "";

      queryParams.put(key, value);
    }
  }

  private static String readLine(InputStream input)
      throws IOException {
    ByteArrayOutputStream buffer =
        new ByteArrayOutputStream();

    int currentByte;

    while ((currentByte = input.read()) != -1) {
      if (currentByte == '\n') {
        break;
      }

      if (currentByte != '\r') {
        buffer.write(currentByte);
      }
    }

    if (currentByte == -1 && buffer.size() == 0) {
      return null;
    }

    return buffer.toString(
        StandardCharsets.UTF_8
    );
  }

  public String getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  public String getRequestTarget() {
    return requestTarget;
  }

  public String getHttpVersion() {
    return httpVersion;
  }

  public Map<String, String> getQueryParams() {
    return queryParams;
  }

  public Map<String, String> getPathParams(
      String method
  ) {
    pathParams.put(method, requestTarget);
    return pathParams;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public String getBody() {
    if (body == null) {
      body = new String(
          bodyBytes,
          StandardCharsets.UTF_8
      );
    }

    return body;
  }
}