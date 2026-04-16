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

    public void saveScore(int playerId, int score, int survivalTime) {
        String query = "INSERT INTO scores (player_id, score, survival_time) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, score);
            pstmt.setInt(3, survivalTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getTopScores(int limit) {
        List<Map<String, Object>> scores = new ArrayList<>();
        String query = "SELECT p.username, s.score, s.survival_time FROM scores s " +
                       "JOIN players p ON s.player_id = p.id " +
                       "ORDER BY s.score DESC, s.survival_time DESC LIMIT ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("username", rs.getString("username"));
                    record.put("score", rs.getInt("score"));
                    record.put("survival_time", rs.getInt("survival_time"));
                    scores.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }

    public Map<String, Object> getBestScoreByPlayer(int playerId) {
        String query = "SELECT score, survival_time FROM scores WHERE player_id = ? ORDER BY score DESC, survival_time DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, playerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("score", rs.getInt("score"));
                    record.put("survival_time", rs.getInt("survival_time"));
                    return record;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
