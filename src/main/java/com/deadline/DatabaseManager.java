package com.deadline;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:game.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        // Load the driver to ensure it's registered
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found!");
            e.printStackTrace();
            return;
        }

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            
            // 1. Table players
            String createPlayers = "CREATE TABLE IF NOT EXISTS players (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT NOT NULL UNIQUE," +
                    "avatar TEXT," +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            stmt.execute(createPlayers);
            
            // 2. Table scores
            String createScores = "CREATE TABLE IF NOT EXISTS scores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_id INTEGER," +
                    "score INTEGER," +
                    "survival_time INTEGER," +
                    "played_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(player_id) REFERENCES players(id)" +
                    ")";
            stmt.execute(createScores);

            // 3. Table submissions
            String createSubmissions = "CREATE TABLE IF NOT EXISTS submissions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "player_id INTEGER," +
                    "title TEXT," +
                    "status TEXT," +
                    "submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(player_id) REFERENCES players(id)" +
                    ")";
            stmt.execute(createSubmissions);
            
            System.out.println("Database initialized successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize database: " + e.getMessage());
        }
    }
}
