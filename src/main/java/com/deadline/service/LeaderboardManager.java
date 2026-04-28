package com.deadline.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.deadline.backend.ScoreService;

public class LeaderboardManager {

    public static class PlayerScore {
        public String name;
        public int score;
        public int timeSeconds;
        public String avatarPath;

        public PlayerScore(String name, int score, int timeSeconds, String avatarPath) {
            this.name = name;
            this.score = score;
            this.timeSeconds = timeSeconds;
            this.avatarPath = avatarPath;
        }
    }

    public static void saveScore(String name, int score, int timeSeconds, String avatarPath) {
        // Simpan ke database
        ScoreService scoreService = new ScoreService();
        scoreService.saveScore(name, score, timeSeconds);
    }

    public static List<PlayerScore> loadScores() {
        List<PlayerScore> scores = new ArrayList<>();
        ScoreService scoreService = new ScoreService();
        List<Map<String, Object>> dbScores = scoreService.getAllScores();

        for (Map<String, Object> record : dbScores) {
            String name = (String) record.get("player_name");
            int score = (int) record.get("score");
            int time = (int) record.get("survival_time");
            // Default avatar path or logic if needed
            String avatarPath = "/assets/default.png";
            scores.add(new PlayerScore(name, score, time, avatarPath));
        }
        return scores;
    }
}
