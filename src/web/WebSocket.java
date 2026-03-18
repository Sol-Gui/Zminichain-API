package web;

import java.net.InetSocketAddress;
import  java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketAddress;

public class WebSocket {
  private ServerSocket serversocket;
  private Socket socket;
  private SocketAddress socketAddress;

  public Server(String hostname, int port) {
    serversocket = new ServerSocket(port);
    socketAddress = new InetSocketAddress(hostname, port);

  }
  try {
    ServerSocket serverSocket = new ServerSocket(3000);
    Socket socket = serverSocket.accept();
    SocketAddress socketAddress = new InetSocketAddress("localhost", 3000);

  } catch (IOException e) {
    throw new IOException("Invalid params requests: ", e);
  }
}
