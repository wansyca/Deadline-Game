package com.deadline.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.deadline.backend.ScoreService;

public class LeaderboardManager {

    // ========== FULL PLAYER SCORE MODEL ==========
    public static class PlayerScore {
        public String name;
        public String avatarPath;   // "cowo" or "cewe" (folder name)
        public int score;
        public int booksCollected;
        public int level;
        public int survivalTime;    // in seconds
        public String status;       // "WIN" or "GAME OVER"

        public PlayerScore(String name, String avatarPath, int score,
                int booksCollected, int level, int survivalTime, String status) {
            this.name = name;
            this.avatarPath = avatarPath != null ? avatarPath : "cowo";
            this.score = score;
            this.booksCollected = booksCollected;
            this.level = Math.max(1, level);
            this.survivalTime = survivalTime;
            this.status = status != null ? status : "GAME OVER";
        }

        /** Format survival time as MM:SS */
        public String getFormattedTime() {
            int m = survivalTime / 60;
            int s = survivalTime % 60;
            return String.format("%02d:%02d", m, s);
        }
    }

    // ========== SAVE (full signature) ==========
    public static void saveScore(String name, String avatarPath, int score,
            int booksCollected, int level, int survivalTime, String status) {
        new ScoreService().saveScore(name, avatarPath, score, booksCollected, level, survivalTime, status);
    }

    // ========== LEGACY wrapper for old callers ==========
    public static void saveScore(String name, int score, int timeSeconds, String avatarPath) {
        // Called from old GamePanel code — map to new signature
        new ScoreService().saveScore(name, avatarPath, score, 0, 1, timeSeconds, "GAME OVER");
    }

    // ========== LOAD ALL SCORES ==========
    public static List<PlayerScore> loadScores() {
        List<PlayerScore> list = new ArrayList<>();
        List<Map<String, Object>> rows = new ScoreService().getAllScores();

        for (Map<String, Object> row : rows) {
            String name    = (String) row.get("player_name");
            String avatar  = (String) row.get("avatar_path");
            int score      = toInt(row.get("score"));
            int books      = toInt(row.get("books_collected"));
            int level      = Math.max(1, toInt(row.get("level")));
            int time       = toInt(row.get("survival_time"));
            String status  = row.get("status") != null ? (String) row.get("status") : "GAME OVER";

            list.add(new PlayerScore(name, avatar, score, books, level, time, status));
        }
        return list;
    }

    private static int toInt(Object val) {
        if (val == null) return 0;
        return ((Number) val).intValue();
    }
}
