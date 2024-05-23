package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String jdbcUrl = "jdbc:mysql://34.116.187.113:3306/baza?useSSL=false&requireSSL=false";
        String username = "root";
        String password = "/MVk9+\",BMpn>?m}";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Ensure the MySQL driver is loaded
            Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
            if (conn != null) {
                System.out.println("Connected to the database!");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }
    }
}
