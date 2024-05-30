package server;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class ChatServer {

    private static String URL = "jdbc:mysql://localhost:3306/baza?useSSL=false&requireSSL=false";
    private static String DB_USER = "root";
    private static String DB_PASSWORD = "/MVk9+\",BMpn>?m}";
    private static final int PORT = 12346;
    private static Map<String, PrintWriter> clientWriters = new HashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Chat server started...");
        ServerSocket serverSocket = new ServerSocket(PORT);

        try {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String requestType = in.readLine();
                System.out.println("Received: " + requestType);
                if ("register".equalsIgnoreCase(requestType)) {
                    username = in.readLine();
                    String password = in.readLine();
                    boolean success = registerUser(username, password);
                    out.println(success ? "register_success" : "register_fail");
                    System.out.println("User registered: " + username);
                } else if ("login".equalsIgnoreCase(requestType)) {
                    username = in.readLine();
                    String password = in.readLine();
                    boolean success = authenticateUser(username, password);
                    if (success) {
                        out.println("login_success");
                        sendFriendList(username);
                        System.out.println("User logged in: " + username);
                    } else {
                        out.println("login_fail");
                        System.out.println("Login failed for user: " + username);
                    }
                } else if ("add_friend".equalsIgnoreCase(requestType)) {
                    username = in.readLine();
                    String friendUsername = in.readLine();
                    boolean success = addFriend(username, friendUsername);
                    out.println(success ? "add_friend_success" : "add_friend_fail");
                    System.out.println(username + " added friend: " + friendUsername);
                } else if ("remove_friend".equalsIgnoreCase(requestType)) {
                    username = in.readLine();
                    String friendUsername = in.readLine();
                    boolean success = removeFriend(username, friendUsername);
                    out.println(success ? "remove_friend_success" : "remove_friend_fail");
                    System.out.println(username + " removed friend: " + friendUsername);
                } else if ("send_message".equalsIgnoreCase(requestType)) {
                    String sender = in.readLine();
                    String receiver = in.readLine();
                    String message = in.readLine();
                    boolean success = storeMessage(sender, receiver, message);
                    out.println(success ? "send_message_success" : "send_message_fail");

                    if (success) {
                        PrintWriter receiverWriter = clientWriters.get(receiver);
                        if (receiverWriter != null) {
                            receiverWriter.println(sender + ": " + message);
                        }
                        out.println(sender + ": " + message);
                        System.out.println(sender + " sent message to " + receiver + ": " + message);
                    }
                } else if ("get_chat_history".equalsIgnoreCase(requestType)) {
                    String user1 = in.readLine();
                    String user2 = in.readLine();
                    sendChatHistory(user1, user2);
                    System.out.println("Chat history requested between " + user1 + " and " + user2);
                } else if ("get_friends".equalsIgnoreCase(requestType)) {
                    username = in.readLine();
                    sendFriendList(username);
                    System.out.println("Friend list requested for user: " + username);
                }

                if (username != null) {
                    synchronized (clientWriters) {
                        clientWriters.put(username, out);
                    }
                }
                while ((requestType = in.readLine()) != null) {
                    System.out.println("Received: " + requestType);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clientWriters) {
                    clientWriters.values().remove(out);
                }
            }
        }

        private boolean registerUser(String username, String password) {


            try (Connection connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean authenticateUser(String username, String password) {


            try (Connection connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean addFriend(String username, String friendUsername) {


            try (Connection connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD)) {
                int userId = getUserId(username, connection);
                int friendId = getUserId(friendUsername, connection);

                if (userId == -1 || friendId == -1) {
                    return false;
                }

                PreparedStatement stmt = connection.prepareStatement("INSERT INTO friends (user_id, friend_id) VALUES (?, ?)");
                stmt.setInt(1, userId);
                stmt.setInt(2, friendId);
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean removeFriend(String username, String friendUsername) {


            try (Connection connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD)) {
                int userId = getUserId(username, connection);
                int friendId = getUserId(friendUsername, connection);

                if (userId == -1 || friendId == -1) {
                    return false;
                }

                PreparedStatement stmt = connection.prepareStatement("DELETE FROM friends WHERE user_id = ? AND friend_id = ?");
                stmt.setInt(1, userId);
                stmt.setInt(2, friendId);
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        private boolean storeMessage(String sender, String receiver, String message) {


            try (Connection connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD)) {
                int senderId = getUserId(sender, connection);
                int receiverId = getUserId(receiver, connection);

                if (senderId == -1 || receiverId == -1) {
                    return false;
                }

                PreparedStatement stmt = connection.prepareStatement("INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, ?, ?)");
                stmt.setInt(1, senderId);
                stmt.setInt(2, receiverId);
                stmt.setString(3, message);
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        private int getUserId(String username, Connection connection) throws SQLException {
            PreparedStatement stmt = connection.prepareStatement("SELECT id FROM users WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        }

        private void sendFriendList(String username) throws IOException {


            try (Connection connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD)){
                int userId = getUserId(username, connection);

                if (userId == -1) {
                    return;
                }

                PreparedStatement stmt = connection.prepareStatement(
                        "SELECT u.username FROM users u " +
                                "JOIN friends f ON u.id = f.friend_id " +
                                "WHERE f.user_id = ?");
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();

                out.println("friend_list_start");
                while (rs.next()) {
                    out.println(rs.getString("username"));
                }
                out.println("friend_list_end");
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void sendChatHistory(String user1, String user2) throws IOException {


            try (Connection connection = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD)) {
                int user1Id = getUserId(user1, connection);
                int user2Id = getUserId(user2, connection);

                if (user1Id == -1 || user2Id == -1) {
                    return;
                }

                PreparedStatement stmt = connection.prepareStatement(
                        "SELECT u1.username AS sender, u2.username AS receiver, m.message, m.timestamp " +
                                "FROM messages m " +
                                "JOIN users u1 ON m.sender_id = u1.id " +
                                "JOIN users u2 ON m.receiver_id = u2.id " +
                                "WHERE (m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?) " +
                                "ORDER BY m.timestamp ASC");
                stmt.setInt(1, user1Id);
                stmt.setInt(2, user2Id);
                stmt.setInt(3, user2Id);
                stmt.setInt(4, user1Id);
                ResultSet rs = stmt.executeQuery();

                out.println("chat_history_start");
                while (rs.next()) {
                    String sender = rs.getString("sender");
                    String message = rs.getString("message");
                    out.println(sender + ": " + message);
                    System.out.println("Sending chat history: " + sender + ": " + message);
                }
                out.println("chat_history_end");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
