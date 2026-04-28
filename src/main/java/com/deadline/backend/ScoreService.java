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

    public void saveScore(String nama, int score, int waktu) {
        String insertQuery = "INSERT INTO leaderboard (nama, score, waktu) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("ERROR: Koneksi database gagal, tidak dapat menyimpan skor.");
                return;
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, nama);
                insertStmt.setInt(2, score);
                insertStmt.setInt(3, waktu);

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("DATA BERHASIL MASUK (Jumlah: " + rowsAffected + ")");
                } else {
                    System.out.println("ERROR: Data gagal masuk.");
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR SQL saat simpan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getAllScores() {
        List<Map<String, Object>> scores = new ArrayList<>();
        String query = "SELECT * FROM leaderboard ORDER BY score DESC, waktu DESC";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null)
                return scores;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> record = new HashMap<>();
                        String name = rs.getString("nama");
                        int score = rs.getInt("score");
                        int time = rs.getInt("waktu");

                        record.put("player_name", name);
                        record.put("score", score);
                        record.put("survival_time", time);
                        scores.add(record);

                        System.out.println("Fetched from DB: " + name + " | " + score + " | " + time);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("ERROR SQL saat ambil data: " + e.getMessage());
            e.printStackTrace();
        }
        return scores;
    }

    public boolean isUsernameInLeaderboard(String username) {
        String query = "SELECT COUNT(*) FROM leaderboard WHERE LOWER(nama) = LOWER(?)";
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null)
                return false;
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
