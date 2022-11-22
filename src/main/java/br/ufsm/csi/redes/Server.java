package br.ufsm.csi.redes;

import br.ufsm.csi.redes.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {
    private final ServerSocket serverSocket;

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    @SneakyThrows
    public Server() {
        this.serverSocket = new ServerSocket(8081);
    }

    @SneakyThrows
    public void start() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String serverStartedOutput = "Server Started on port " + serverSocket.getLocalPort() + " " + dtf.format(LocalDateTime.now());
        String serverStartedOutputFormatted = "\u001B[32m" + "Server Started on port " + serverSocket.getLocalPort() + " " + "\u001B[37m" + dtf.format(LocalDateTime.now()) + "\u001B[0m";
        String dash = "-";
        String title = "Chat-Server";
        int topDashesHalfLength = (serverStartedOutput.length() - title.length())/2;
        System.out.println(dash.repeat(topDashesHalfLength) + title + dash.repeat(topDashesHalfLength));
        System.out.println(serverStartedOutputFormatted);
        System.out.println(dash.repeat(serverStartedOutput.length()));

        new Thread(new Session(serverSocket.accept())).start();
    }


    public class Session implements Runnable{
        private Socket socket;

        @SneakyThrows
        public Session(Socket socket) {
            this.socket = socket;
        }

        @SneakyThrows
        @Override
        public void run() {
            System.out.println("Session Started From " +socket.getInetAddress());
            while (true) {
                byte[] buffer = new byte[1024];
                int size = socket.getInputStream().read(buffer);
                Message message = new ObjectMapper().readValue(new String(buffer, 0, size), Message.class);

                byte[] messageBytes = new ObjectMapper().writeValueAsString(message).getBytes(StandardCharsets.UTF_8);
            }
        }
    }

}
