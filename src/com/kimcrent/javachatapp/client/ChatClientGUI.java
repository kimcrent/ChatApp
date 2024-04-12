import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.function.Consumer;



public class ChatClientGUI extends JFrame {
    private JTextArea messageArea;
    private JTextField textField;
    private ChatClient client;

    public ChatClientGUI(){
        super("Chat Application");
        setSize(400, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        textField = new JTextField();
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                client.sendMessage(textField.getText());
                textField.setText("");
            }
        });
        add(textField, BorderLayout.SOUTH);

        // Intialize and start the ChatClient
        try {
            this.client = new ChatClient("127.0.0.1", 5000, this::onMessageReceived);
            client.startClient();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error connecting to the server", "Connection error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

    }
    
    private void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message + "\n"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI().setVisible(true);
        });
    }
    
}

public class ChatClient {
    private Socket socket;
    private BufferReader in;
    private PrintWriter out;
    private Consumer<String> onMessageReceived;

    public ChatClient(String serverAddress, int serverPort, Consumer<String> onMessageReceived) throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.onMessageReceived = onMessageReceived;
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public void startClient() {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine())!= null) {
                    onMessageReceived.accept(line);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
