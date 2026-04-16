package com.deadline;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubmissionService {

    public void submitTask(int playerId, String title, String status) {
        String query = "INSERT INTO submissions(player_id, title, status) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, playerId);
            pstmt.setString(2, title);
            pstmt.setString(3, status);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error submitting task: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public List<Map<String, Object>> getSubmissionsByPlayer(int playerId) {
        String query = "SELECT * FROM submissions WHERE player_id = ? ORDER BY submitted_at DESC";
        List<Map<String, Object>> submissions = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
             
            pstmt.setInt(1, playerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("id", rs.getInt("id"));
                    record.put("player_id", rs.getInt("player_id"));
                    record.put("title", rs.getString("title"));
                    record.put("status", rs.getString("status"));
                    record.put("submitted_at", rs.getString("submitted_at"));
                    submissions.add(record);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting submissions by player: " + e.getMessage());
            e.printStackTrace();
        }
        return submissions;
    }
}
