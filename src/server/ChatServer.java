package server;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12346;
    private static Set<PrintWriter> clientWriters = new HashSet<>();

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

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Handle login/registration
                String requestType = in.readLine();
                if ("register".equalsIgnoreCase(requestType)) {
                    String username = in.readLine();
                    String password = in.readLine();
                    boolean success = registerUser(username, password);
                    out.println(success ? "register_success" : "register_fail");
                } else if ("login".equalsIgnoreCase(requestType)) {
                    String username = in.readLine();
                    String password = in.readLine();
                    boolean success = authenticateUser(username, password);
                    out.println(success ? "login_success" : "login_fail");
                }

                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println("Received: " + message);
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println(message);
                        }
                    }
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
                    clientWriters.remove(out);
                }
            }
        }

        private boolean registerUser(String username, String password) {
            String url = "jdbc:mysql://localhost:3306/baza?useSSL=false&requireSSL=false";
            String dbUser = "root";
            String dbPassword = "/MVk9+\",BMpn>?m}";

            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
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
            String url = "jdbc:mysql://localhost:3306/baza?useSSL=false&requireSSL=false";
            String dbUser = "root";
            String dbPassword = "/MVk9+\",BMpn>?m}";

            try (Connection connection = DriverManager.getConnection(url, dbUser, dbPassword);
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
    }
}
