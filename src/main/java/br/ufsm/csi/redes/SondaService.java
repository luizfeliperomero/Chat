package br.ufsm.csi.redes;

import br.ufsm.csi.redes.model.Sonda;
import br.ufsm.csi.redes.model.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SondaService {
    private String username;
    public static final Set<Usuario> usuarios = new HashSet<>();
    private ChatClientSwing chatClientSwing;

    public SondaService(String username, ChatClientSwing chatClientSwing) {
       start();
       this.username = username;
       this.chatClientSwing = chatClientSwing;
    }

    public void start() {
        new Thread(new Receive()).start();
        new Thread(new Send()).start();
        new Thread(new UpdateUsers()).start();
    }

    public class UpdateUsers implements Runnable {

        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                Thread.sleep(8000);
                synchronized (usuarios) {
                    List<Usuario> afkUsers = usuarios.stream()
                            .filter(u -> (System.currentTimeMillis() - u.getTimestamp()) > 30000)
                            .toList();
                    afkUsers.stream().forEach(u -> usuarios.remove(u));
                }
            }
        }
    }

    public class Send implements Runnable {

        @SneakyThrows
        @Override
        public void run() {
            DatagramSocket socket = new DatagramSocket();
            Sonda sonda = new Sonda("sonda", username, Usuario.StatusUsuario.DISPONIVEL );
            byte[] packetBytes = new ObjectMapper().writeValueAsString(sonda).getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(packetBytes, 0, packetBytes.length, InetAddress.getByName("255.255.255.255"), 8080);
            while (true) {
                socket.send(packet);
                Thread.sleep(5000);
            }
        }
    }

    public class Receive implements Runnable {

        @SneakyThrows
        @Override
        public void run() {
            DatagramSocket socket = new DatagramSocket(8080);
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String packetStr = new String(buffer, 0, packet.getLength(), StandardCharsets.UTF_8);
                Sonda sonda = new ObjectMapper().readValue(packetStr, Sonda.class);
                Usuario user = new Usuario(sonda.getUsername(), System.currentTimeMillis(),sonda.getStatus(), packet.getAddress());
                synchronized (usuarios) {
                    if(!usuarios.remove(user)) {
                        chatClientSwing.getDfListModel().addElement(user);
                    }
                    usuarios.add(user);
                }
            }
        }
    }


}
