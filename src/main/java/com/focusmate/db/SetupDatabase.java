package com.focusmate.db;

import java.sql.*;

public class SetupDatabase {
    private static final String ROOT_URL = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
    private static final String DB_NAME = "focusmate";
    private static final String USER = "root"; // keep in sync with DB.java if you change it
    private static final String PASS = "MARS*1979"; // keep in sync with DB.java if you change it

    public static void main(String[] args) throws Exception {
        // 1) Ensure database exists
        try (Connection rootConn = DriverManager.getConnection(ROOT_URL, USER, PASS);
             Statement st = rootConn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("Database ensured: " + DB_NAME);
        }

        // 2) Ensure table exists
        try (Connection dbConn = DB.get();
             Statement st = dbConn.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "name VARCHAR(255), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );
            System.out.println("Table ensured: users");

            // 3) Seed a couple of rows (ignore duplicates)
            try (PreparedStatement ps = dbConn.prepareStatement(
                "INSERT IGNORE INTO users(email, name) VALUES (?, ?)")) {
                ps.setString(1, "test@example.com");
                ps.setString(2, "Test User");
                ps.executeUpdate();

                ps.setString(1, "alice@example.com");
                ps.setString(2, "Alice");
                ps.executeUpdate();
            }
            System.out.println("Seeded sample data.");

            // 4) Print current rows
            try (ResultSet rs = st.executeQuery("SELECT id, email, name, created_at FROM users ORDER BY id")) {
                System.out.println("\nCurrent users:");
                while (rs.next()) {
                    System.out.printf("id=%d, email=%s, name=%s, created_at=%s%n",
                        rs.getInt("id"), rs.getString("email"), rs.getString("name"), rs.getTimestamp("created_at"));
                }
            }
        }
    }
}
