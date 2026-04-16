package com.deadline;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreService {

    public void saveScore(int playerId, int score, int survivalTime) {
        String query = "INSERT INTO scores(player_id, score, survival_time) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, playerId);
            pstmt.setInt(2, score);
            pstmt.setInt(3, survivalTime);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving score: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<Map<String, Object>> getTopScores(int limit) {
        String query = "SELECT s.score, s.survival_time, p.username " +
                       "FROM scores s " +
                       "JOIN players p ON s.player_id = p.id " +
                       "ORDER BY s.score DESC, s.survival_time DESC " +
                       "LIMIT ?";
                       
        List<Map<String, Object>> topScores = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("score", rs.getInt("score"));
                    record.put("survival_time", rs.getInt("survival_time"));
                    record.put("username", rs.getString("username"));
                    topScores.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting top scores: " + e.getMessage());
            e.printStackTrace();
        }
        return topScores;
    }
    
    public Map<String, Object> getBestScoreByPlayer(int playerId) {
        String query = "SELECT * FROM scores WHERE player_id = ? ORDER BY score DESC, survival_time DESC LIMIT 1";
        Map<String, Object> bestScore = new HashMap<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, playerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    bestScore.put("id", rs.getInt("id"));
                    bestScore.put("player_id", rs.getInt("player_id"));
                    bestScore.put("score", rs.getInt("score"));
                    bestScore.put("survival_time", rs.getInt("survival_time"));
                    bestScore.put("played_at", rs.getString("played_at"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting best score by player: " + e.getMessage());
            e.printStackTrace();
        }
        return bestScore.isEmpty() ? null : bestScore;
    }
}
