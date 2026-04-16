package com.deadline;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PlayerService {

    public int createPlayer(String username, String avatar) {
        // Handle duplication: if username exists, return existing ID
        int existingId = getPlayerIdByUsername(username);
        if (existingId != -1) {
            return existingId;
        }

        String query = "INSERT INTO players(username, avatar) VALUES(?, ?)";
        int playerId = -1;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
             
            pstmt.setString(1, username);
            pstmt.setString(2, avatar);
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    playerId = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating player: " + e.getMessage());
            e.printStackTrace();
        }
        return playerId;
    }
    
    public Map<String, Object> getPlayerById(int id) {
        String query = "SELECT * FROM players WHERE id = ?";
        Map<String, Object> player = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    player.put("id", rs.getInt("id"));
                    player.put("username", rs.getString("username"));
                    player.put("avatar", rs.getString("avatar"));
                    player.put("created_at", rs.getString("created_at"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting player by id: " + e.getMessage());
            e.printStackTrace();
        }
        return player.isEmpty() ? null : player;
    }
    
    public List<Map<String, Object>> getAllPlayers() {
        String query = "SELECT * FROM players";
        List<Map<String, Object>> players = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
             
            while (rs.next()) {
                Map<String, Object> player = new HashMap<>();
                player.put("id", rs.getInt("id"));
                player.put("username", rs.getString("username"));
                player.put("avatar", rs.getString("avatar"));
                player.put("created_at", rs.getString("created_at"));
                players.add(player);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all players: " + e.getMessage());
            e.printStackTrace();
        }
        return players;
    }

    public int getPlayerIdByUsername(String username) {
        String query = "SELECT id FROM players WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting player ID by username: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }
}
