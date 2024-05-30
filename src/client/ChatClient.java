package client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
    private static final String SERVER_ADDRESS = "34.0.250.190"; // Server IP address
    private static final int SERVER_PORT = 12346; // Server port
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Chat Client");
    private JTextField textField = new JTextField(40);
    private JButton sendButton = new JButton("Send");
    private JButton addFriendButton = new JButton("Add Friend");
    private JButton removeFriendButton = new JButton("Remove Friend");
    private JPanel messagePanel = new JPanel();
    private String userName;
    private JComboBox<String> friendsComboBox = new JComboBox<>();

    public ChatClient() {
        // Show login/registration dialog
        showLoginDialog();

        // GUI setup
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);
        frame.setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(58, 89, 152)); // Facebook Messenger Blue
        headerPanel.setPreferredSize(new Dimension(400, 50));
        JLabel headerLabel = new JLabel("Komunikator Szymonka");
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 24));
        headerPanel.add(headerLabel);

        // Friends list
        JPanel friendsPanel = new JPanel();
        friendsPanel.setLayout(new BorderLayout());
        friendsPanel.add(new JLabel("Friends:"), BorderLayout.WEST);
        friendsPanel.add(friendsComboBox, BorderLayout.CENTER);
        friendsPanel.add(addFriendButton, BorderLayout.EAST);
        friendsPanel.add(removeFriendButton, BorderLayout.SOUTH);

        // Message panel
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        JScrollPane messageScrollPane = new JScrollPane(messagePanel);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        inputPanel.add(textField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(headerPanel, BorderLayout.NORTH);
        frame.add(friendsPanel, BorderLayout.NORTH);
        frame.add(messageScrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Send message action
        ActionListener sendAction = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String friend = (String) friendsComboBox.getSelectedItem();
                if (friend != null) {
                    out.println("send_message");
                    out.println(userName);
                    out.println(friend);
                    out.println(textField.getText());
                    System.out.println("send_message "+userName+" "+friend + "textField.getText()");
                    textField.setText("");
                }
            }
        };

        textField.addActionListener(sendAction);
        sendButton.addActionListener(sendAction);

        // Add friend action
        addFriendButton.addActionListener(e -> {
            String friendUsername = JOptionPane.showInputDialog("Enter friend's username:");
            if (friendUsername != null && !friendUsername.isEmpty()) {
                addFriend(userName, friendUsername);
            }
        });

        // Remove friend action
        removeFriendButton.addActionListener(e -> {
            String friendUsername = (String) friendsComboBox.getSelectedItem();
            if (friendUsername != null && !friendUsername.isEmpty()) {
                removeFriend(userName, friendUsername);
            }
        });

        // Load chat history when friend is selected
        friendsComboBox.addActionListener(e -> {
            String friend = (String) friendsComboBox.getSelectedItem();
            if (friend != null) {
                loadChatHistory(userName, friend);
            }
        });

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
                initializeChat();
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
                initializeChat();
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
            if ("login_success".equals(response) || "register_success".equals(response)) {
                if ("login_success".equals(response)) {
                    loadFriendsList();
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void addFriend(String username, String friendUsername) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("add_friend");
            out.println(username);
            out.println(friendUsername);

            String response = in.readLine();
            if ("add_friend_success".equals(response)) {
                JOptionPane.showMessageDialog(frame, "Friend added successfully");
                friendsComboBox.addItem(friendUsername);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to add friend");
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeFriend(String username, String friendUsername) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("remove_friend");
            out.println(username);
            out.println(friendUsername);

            String response = in.readLine();
            if ("remove_friend_success".equals(response)) {
                JOptionPane.showMessageDialog(frame, "Friend removed successfully");
                friendsComboBox.removeItem(friendUsername);
            } else {
                JOptionPane.showMessageDialog(frame, "Failed to remove friend");
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFriendsList() {
        try {
            out.println("get_friends");
            out.println(userName);

            String response;
            while (!(response = in.readLine()).equals("friend_list_end")) {
                if (!response.equals("friend_list_start")) {
                    friendsComboBox.addItem(response);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatHistory(String user1, String user2) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println("get_chat_history");
            out.println(user1);
            out.println(user2);

            messagePanel.removeAll();

            String response;
            while (!(response = in.readLine()).equals("chat_history_end")) {
                if (!response.equals("chat_history_start")) {
                    addMessageToPanel(response);
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeChat() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(() -> {
                try {
                    run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void run() throws IOException {
        while (true) {
            if (in != null) {
                String message = in.readLine();
                if (message == null) {
                    break;
                }
                addMessageToPanel(message);
            }
        }
    }

    private void addMessageToPanel(String message) {
        // Create chat bubble
        String[] parts = message.split(": ", 2);
        if (parts.length < 2) {
            return; // Invalid message format, skip
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