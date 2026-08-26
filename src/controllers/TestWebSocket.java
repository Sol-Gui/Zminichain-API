package controllers;

import web.WebSocketConnection;
import web.websocket.annotations.*;

import java.io.IOException;

public class TestWebSocket {

    @WsController("/ws/teste")
    public static class webSocketController {
        @OnOpen
        public void helloMessage(WebSocketConnection ws) throws IOException {
            System.out.println("A rota Ws foi Aberta!");
            ws.send("Bem-vindo ao WebSocket!");
        }

        @OnMessage
        public void onMessage(WebSocketConnection ws, String message) throws IOException {
            System.out.println("Recebi: " + message);
            ws.send("Echo: " + message);
        }

        @OnClose
        public void onClose(WebSocketConnection ws) {
            System.out.println("Conexão fechada: " + ws.getRoute());
        }

        @OnError
        public void onError(WebSocketConnection ws, Exception e) {
            e.printStackTrace();
        }
    }
}
