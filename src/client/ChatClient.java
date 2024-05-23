package client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "34.0.250.190"; // Adres IP serwera
    private static final int SERVER_PORT = 12346; // Port serwera
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chat Client");
    private JTextField textField = new JTextField(40);
    private JButton sendButton = new JButton("Send");
    private JPanel messagePanel = new JPanel();
    private String userName;

    public ChatClient() {
        // Show login/registration dialog
        showLoginDialog();

        // Konfiguracja interfejsu graficznego
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        frame.setLayout(new BorderLayout());

        // Nagłówek
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(58, 89, 152)); // Facebook Messenger Blue
        headerPanel.setPreferredSize(new Dimension(400, 50));
        JLabel headerLabel = new JLabel("Komunikator Szymonka");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(headerLabel);

        // Pole wiadomości
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        JScrollPane messageScrollPane = new JScrollPane(messagePanel);

        // Pole wprowadzania tekstu
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(messageScrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Obsługa wysyłania wiadomości
        ActionListener sendAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(userName + ": " + textField.getText());
                textField.setText("");
            }
        };

        textField.addActionListener(sendAction);
        sendButton.addActionListener(sendAction);

        frame.setVisible(true);
    }

    private void showLoginDialog() {
        JFrame loginFrame = new JFrame("Login");
        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel("Email (register only):"));
        loginPanel.add(new JTextField(20));  // Dummy field for alignment
        loginPanel.add(loginButton);
        loginPanel.add(registerButton);

        loginFrame.add(loginPanel);
        loginFrame.pack();
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);

        loginButton.addActionListener(e -> {
            userName = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticate("login", userName, password)) {
                loginFrame.dispose();
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Login failed");
            }
        });

        registerButton.addActionListener(e -> {
            userName = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticate("register", userName, password)) {
                loginFrame.dispose();
                frame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Registration failed");
            }
        });
    }

    private boolean authenticate(String type, String username, String password) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println(type);
            out.println(username);
            out.println(password);

            String response = in.readLine();
            return "login_success".equals(response) || "register_success".equals(response);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void run() throws IOException {
        textField.setEditable(true);

        // Wysłanie nazwy użytkownika na początku połączenia
        out.println(userName);

        // Odbieranie wiadomości od serwera
        while (true) {
            String message = in.readLine();
            if (message == null) {
                break;
            }
            addMessageToPanel(message);
        }
    }

    private void addMessageToPanel(String message) {
        // Utworzenie dymka czatu
        String[] parts = message.split(": ", 2);
        if (parts.length < 2) {
            return; // Niepoprawny format wiadomości, pomiń
        }
        String sender = parts[0];
        String text = parts[1];

        JLabel messageLabel = new JLabel("<html><b>" + sender + ":</b> " + text + "</html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        JPanel messageBubble = new JPanel();
        messageBubble.setLayout(new BorderLayout());
        messageBubble.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (sender.equals(userName)) {
            messageBubble.setBackground(new Color(220, 248, 198)); // Lighter color for the current user's messages
        } else {
            messageBubble.setBackground(new Color(240, 240, 240)); // Different color for other users' messages
        }

        messageBubble.add(messageLabel, BorderLayout.CENTER);

        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        if (sender.equals(userName)) {
            outerPanel.add(messageBubble, BorderLayout.LINE_END);
        } else {
            outerPanel.add(messageBubble, BorderLayout.LINE_START);
        }

        messagePanel.add(outerPanel);
        messagePanel.revalidate();
        messagePanel.repaint();
    }

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.run();
    }
}
