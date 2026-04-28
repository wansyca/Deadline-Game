package com.deadline.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/nama_database";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            if (conn != null) {
                System.out.println("Koneksi Berhasil ke database: nama_database");
            }
            return conn;
        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL tidak ditemukan!");
            e.printStackTrace();
            throw new SQLException(e);
        } catch (SQLException e) {
            System.out.println("Koneksi Gagal: " + e.getMessage());
            throw e;
        }
    }

    public static void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver MySQL tidak ditemukan!");
            e.printStackTrace();
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASS);
                Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS nama_database");
            System.out.println("Database nama_database siap.");

            try (Connection dbConn = getConnection();
                    Statement dbStmt = dbConn.createStatement()) {

                // Pastikan tabel leaderboard sesuai request: nama, score, waktu
                String createLeaderboard = "CREATE TABLE IF NOT EXISTS leaderboard (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "nama VARCHAR(50), " +
                        "score INT, " +
                        "waktu INT, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
                dbStmt.executeUpdate(createLeaderboard);

                String createPlayers = "CREATE TABLE IF NOT EXISTS players (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50), " +
                        "avatar VARCHAR(100), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
                dbStmt.executeUpdate(createPlayers);

                String createSubmissions = "CREATE TABLE IF NOT EXISTS submissions (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "player_id INT, " +
                        "title VARCHAR(100), " +
                        "status VARCHAR(20), " +
                        "submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
                dbStmt.executeUpdate(createSubmissions);

                System.out.println("Database initialized successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Gagal Inisialisasi Database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
