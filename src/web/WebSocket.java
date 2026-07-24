package web;

public class WebSocket {
  private static final String MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
  private Server server;

  public WebSocket(Server server) {
    this.server = server;
  }

  public void performHandshake() {
    
  }
}