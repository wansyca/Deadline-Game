package com.deadline.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_NAME = "db_deadline";
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_URL = BASE_URL + DB_NAME
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL tidak ditemukan!");
            throw new SQLException(e);
        } catch (SQLException e) {
            System.out.println("❌ Koneksi Gagal: " + e.getMessage());
            throw e;
        }
    }

    public static void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL tidak ditemukan!");
            return;
        }

        String rootUrl = BASE_URL + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(rootUrl, USER, PASS);
                Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME
                    + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("✅ Database '" + DB_NAME + "' siap.");

            try (Connection dbConn = getConnection();
                    Statement dbStmt = dbConn.createStatement()) {

                // ✅ Leaderboard table — new full schema
                String createLeaderboard = "CREATE TABLE IF NOT EXISTS leaderboard ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY, "
                        + "player_name VARCHAR(50), "
                        + "avatar_path VARCHAR(255), "
                        + "score INT, "
                        + "books_collected INT, "
                        + "level INT, "
                        + "survival_time INT, "
                        + "status VARCHAR(20), "
                        + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                        + ")";
                dbStmt.executeUpdate(createLeaderboard);
                System.out.println("✅ Tabel 'leaderboard' siap.");

                // Migration: add missing columns to old tables if they exist
                String[] alterCols = {
                    "ALTER TABLE leaderboard ADD COLUMN IF NOT EXISTS avatar_path VARCHAR(255)",
                    "ALTER TABLE leaderboard ADD COLUMN IF NOT EXISTS books_collected INT DEFAULT 0",
                    "ALTER TABLE leaderboard ADD COLUMN IF NOT EXISTS level INT DEFAULT 1",
                    "ALTER TABLE leaderboard ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'GAME OVER'"
                };
                for (String sql : alterCols) {
                    try { dbStmt.executeUpdate(sql); } catch (Exception ignored) {}
                }

                System.out.println("✅ Database initialized successfully.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Gagal Inisialisasi Database: " + e.getMessage());
        }
    }
}
