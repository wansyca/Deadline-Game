package com.deadline.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreService {

    // ========== SAVE SCORE (Full) ==========
    public void saveScore(String playerName, String avatarPath, int score,
            int booksCollected, int level, int survivalTime, String status) {

        String sql = "INSERT INTO leaderboard "
                + "(player_name, avatar_path, score, books_collected, level, survival_time, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, playerName);
            ps.setString(2, avatarPath != null ? avatarPath : "cowo");
            ps.setInt(3, score);
            ps.setInt(4, booksCollected);
            ps.setInt(5, level);
            ps.setInt(6, survivalTime);
            ps.setString(7, status != null ? status : "GAME OVER");

            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Score saved: " + playerName
                        + " | " + score + " pts | LVL " + level
                        + " | " + booksCollected + " books | " + status);
            }

        } catch (SQLException e) {
            System.out.println("❌ ERROR SQL simpan score: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ========== LEGACY (backward compat) ==========
    public void saveScore(String nama, int score, int waktu) {
        saveScore(nama, "cowo", score, 0, 1, waktu, "GAME OVER");
    }

    // ========== LOAD ALL SCORES ==========
    public List<Map<String, Object>> getAllScores() {
        List<Map<String, Object>> scores = new ArrayList<>();
        String sql = "SELECT * FROM leaderboard ORDER BY score DESC, survival_time DESC";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("player_name", rs.getString("player_name"));
                row.put("avatar_path", rs.getString("avatar_path"));
                row.put("score", rs.getInt("score"));
                row.put("books_collected", rs.getInt("books_collected"));
                row.put("level", rs.getInt("level"));
                row.put("survival_time", rs.getInt("survival_time"));
                row.put("status", rs.getString("status"));
                scores.add(row);
            }

        } catch (SQLException e) {
            System.out.println("❌ ERROR SQL ambil scores: " + e.getMessage());
            e.printStackTrace();
        }
        return scores;
    }

    // ========== CHECK USERNAME ==========
    public boolean isUsernameInLeaderboard(String username) {
        String sql = "SELECT COUNT(*) FROM leaderboard WHERE LOWER(player_name) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
