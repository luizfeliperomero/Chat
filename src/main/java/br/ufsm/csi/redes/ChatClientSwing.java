package br.ufsm.csi.redes;

import br.ufsm.csi.redes.model.Message;
import br.ufsm.csi.redes.model.Usuario;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * User: Rafael
 * Date: 13/10/14
 * Time: 10:28
 *
 */
public class ChatClientSwing extends JFrame {

    private Usuario meuUsuario;
    private final String endBroadcast = "255.255.255.255";
    private JList listaChat;
    private DefaultListModel dfListModel;
    private JTabbedPane tabbedPane = new JTabbedPane();
    private Set<Usuario> chatsAbertos = new HashSet<>();
    private Socket socket;

    public ChatClientSwing() throws UnknownHostException {
        setLayout(new GridBagLayout());
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Status");

        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(Usuario.StatusUsuario.DISPONIVEL.name());
        rbMenuItem.setSelected(true);
        rbMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ChatClientSwing.this.meuUsuario.setStatus(Usuario.StatusUsuario.DISPONIVEL);
            }
        });
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem(Usuario.StatusUsuario.NAO_PERTURBE.name());
        rbMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ChatClientSwing.this.meuUsuario.setStatus(Usuario.StatusUsuario.NAO_PERTURBE);
            }
        });
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem(Usuario.StatusUsuario.VOLTO_LOGO.name());
        rbMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                ChatClientSwing.this.meuUsuario.setStatus(Usuario.StatusUsuario.VOLTO_LOGO);
            }
        });
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        menuBar.add(menu);
        this.setJMenuBar(menuBar);

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.getButton() == MouseEvent.BUTTON3) {
                    JPopupMenu popupMenu =  new JPopupMenu();
                    final int tab = tabbedPane.getUI().tabForCoordinate(tabbedPane, e.getX(), e.getY());
                    JMenuItem item = new JMenuItem("Fechar");
                    item.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            PainelChatPVT painel = (PainelChatPVT) tabbedPane.getTabComponentAt(tab);
                            tabbedPane.remove(tab);
                            chatsAbertos.remove(painel.getUsuario());
                        }
                    });
                    popupMenu.add(item);
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
        add(tabbedPane, new GridBagConstraints(1, 0, 1, 1, 1, 1, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        setSize(800, 600);
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        final int x = (screenSize.width - this.getWidth()) / 2;
        final int y = (screenSize.height - this.getHeight()) / 2;
        this.setLocation(x, y);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Chat P2P - Redes de Computadores");
        String nomeUsuario = JOptionPane.showInputDialog(this, "Digite seu nome de usuário: ");
        new SondaService(nomeUsuario);
        new Thread(new Receiver()).start();
        try {
            Thread.sleep(1000);
            add(new JScrollPane(criaLista()), new GridBagConstraints(0, 0, 1, 1, 0.1, 1, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            setVisible(true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private JComponent criaLista() {
        dfListModel = new DefaultListModel();
        SondaService.usuarios.stream().forEach(u -> {
            dfListModel.addElement(u);
        });
        listaChat = new JList(dfListModel);
        listaChat.addMouseListener(new MouseAdapter() {
            @SneakyThrows
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() == 2) {
                    int index = list.locationToIndex(evt.getPoint());
                    Usuario user = (Usuario) list.getModel().getElementAt(index);
                    socket = new Socket(user.getEndereco(), 8081);
                    if (chatsAbertos.add(user)) {
                        tabbedPane.add(user.toString(), new PainelChatPVT(user));
                    }
                }
            }
        });
        return listaChat;
    }


    class PainelChatPVT extends JPanel {

        public static JTextArea areaChat;
        JTextField campoEntrada;
        Usuario usuario;

        PainelChatPVT(Usuario usuario) {

            meuUsuario = SondaService.usuarios.stream().filter(u -> {
                try {
                    return InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()).equals(u.getEndereco());
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
            }).findFirst().orElseThrow();

            setLayout(new GridBagLayout());
            areaChat = new JTextArea();
            this.usuario = usuario;
            areaChat.setEditable(false);
            campoEntrada = new JTextField();
            campoEntrada.addActionListener(new ActionListener() {
                @SneakyThrows
                @Override
                public void actionPerformed(ActionEvent e) {
                    ((JTextField) e.getSource()).setText("");
                    areaChat.append("Você" + "> " + e.getActionCommand() + "\n");
                    Message message = new Message(e.getActionCommand(), meuUsuario);
                    byte[] messageBytes = new ObjectMapper().writeValueAsString(message).getBytes(StandardCharsets.UTF_8);
                    socket.getOutputStream().write(messageBytes, 0, messageBytes.length);
                }
            });
            add(new JScrollPane(areaChat), new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
            add(campoEntrada, new GridBagConstraints(0, 1, 1, 1, 1, 0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        }

        public Usuario getUsuario() {
            return usuario;
        }

        public void setUsuario(Usuario usuario) {
            this.usuario = usuario;
        }

    }

    public static void main(String[] args) throws UnknownHostException {
        new ChatClientSwing();

    }

    @Data
    @AllArgsConstructor
    public class Receiver implements Runnable {
        ServerSocket serverSocket;

        @SneakyThrows
        public Receiver() {
            this.serverSocket = new ServerSocket(8081);
        }

        @SneakyThrows
        @Override
        public void run() {
            while (true) {
                new Thread(new Session(this.serverSocket.accept())).start();
            }
        }


        public class Session implements Runnable {
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
                    Usuario sender = message.getSender();
                    if(chatsAbertos.add(sender)) {
                        tabbedPane.add(sender.toString(), new PainelChatPVT(sender));
                    }
                    if(!sender.getEndereco().equals(meuUsuario.getEndereco())) {
                        PainelChatPVT.areaChat.append(sender.getNome() + ": " + message.getText() + "\n");
                    }
                }
            }
        }
    }



}
