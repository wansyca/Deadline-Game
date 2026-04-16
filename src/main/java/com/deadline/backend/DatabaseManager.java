package com.deadline.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/deadline_game";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASS);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS deadline_game");
            
            try (Connection dbConn = getConnection();
                 Statement dbStmt = dbConn.createStatement()) {
                
                String createPlayers = "CREATE TABLE IF NOT EXISTS players (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50), " +
                        "avatar VARCHAR(100), " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
                dbStmt.executeUpdate(createPlayers);

                String createScores = "CREATE TABLE IF NOT EXISTS scores (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "player_id INT, " +
                        "score INT, " +
                        "survival_time INT, " +
                        "played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")";
                dbStmt.executeUpdate(createScores);

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
            e.printStackTrace();
        }
    }
}
