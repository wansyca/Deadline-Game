package com.deadline;

import java.io.*;
import java.util.*;
import com.deadline.backend.ScoreService;

public class LeaderboardManager {
    private static final String FILE_NAME = "leaderboard.txt";

    public static class PlayerScore {
        public String name;
        public int score;
        public int timeSeconds;

        public PlayerScore(String name, int score, int timeSeconds) {
            this.name = name;
            this.score = score;
            this.timeSeconds = timeSeconds;
        }
    }


    public static void saveScore(String name, int score, int timeSeconds) {
        // Now handled by GamePanel calling ScoreService directly.
        // We keep this empty or keep basic file IO for backup if desired, 
        // but requirement asks to use database.
    }

    public static List<PlayerScore> loadScores() {
        List<PlayerScore> scores = new ArrayList<>();
        
        ScoreService scoreService = new ScoreService();
        List<Map<String, Object>> dbScores = scoreService.getTopScores(10);
        
        for (Map<String, Object> record : dbScores) {
            String name = (String) record.get("username");
            int score = (int) record.get("score");
            int time = (int) record.get("survival_time");
            scores.add(new PlayerScore(name, score, time));
        }

        // Add dummy data only if we have fewer than 3 scores (maintain existing logic, but UI will fetch from DB)
        // Note: the prompt says "SurvivorRankingUI tampilkan data dari database (bukan dummy)", 
        // so we remove dummy data logic.
        return scores;
    }
}
