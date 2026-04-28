package com.deadline.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerService {

    public int createPlayer(String username, String avatar) {
        // 🚫 Cegah duplicate
        int existingId = getPlayerIdByUsername(username);
        if (existingId != -1) {
            return existingId;
        }

        String query = "INSERT INTO players (username, avatar) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, username);
            pstmt.setString(2, avatar);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public Map<String, Object> getPlayerById(int id) {
        String query = "SELECT * FROM players WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> player = new HashMap<>();
                    player.put("id", rs.getInt("id"));
                    player.put("username", rs.getString("username"));
                    player.put("avatar", rs.getString("avatar"));
                    player.put("created_at", rs.getTimestamp("created_at"));
                    return player;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Map<String, Object>> getAllPlayers() {
        List<Map<String, Object>> players = new ArrayList<>();
        String query = "SELECT * FROM players";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> player = new HashMap<>();
                player.put("id", rs.getInt("id"));
                player.put("username", rs.getString("username"));
                player.put("avatar", rs.getString("avatar"));
                player.put("created_at", rs.getTimestamp("created_at"));
                players.add(player);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM players WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getPlayerIdByUsername(String username) {
        String query = "SELECT id FROM players WHERE LOWER(username) = LOWER(?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
