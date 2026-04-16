package com.deadline.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubmissionService {

    public void submitTask(int playerId, String title, String status) {
        String query = "INSERT INTO submissions (player_id, title, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, playerId);
            pstmt.setString(2, title);
            pstmt.setString(3, status);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getSubmissionsByPlayer(int playerId) {
        List<Map<String, Object>> submissions = new ArrayList<>();
        String query = "SELECT title, status, submitted_at FROM submissions WHERE player_id = ? ORDER BY submitted_at DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, playerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("title", rs.getString("title"));
                    record.put("status", rs.getString("status"));
                    record.put("submitted_at", rs.getTimestamp("submitted_at"));
                    submissions.add(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return submissions;
    }
}
