package com.deadline;

import java.util.*;
import java.io.File;
import com.deadline.backend.ScoreService;

public class LeaderboardManager {
    private static final String FILE_NAME = "leaderboard.txt";

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
        // 1. Ambil semua data leaderboard dari file LOCAL (sebagai backup)
        List<PlayerScore> scores = loadScoresFromFile();

        // 2. Hapus data lama dengan nama yang sama (Case-Insensitive) - KONSEP: HANYA 1 DATA PER PLAYER
        scores.removeIf(s -> s.name.equalsIgnoreCase(name));

        // 3. Tambahkan score terbaru (Selalu OVERWRITE meskipun lebih rendah)
        scores.add(new PlayerScore(name, score, timeSeconds,avatarPath));


        // 4. Sort Score (Urutkan dari terbesar untuk tampilan)
        Collections.sort(scores, (a, b) -> b.score - a.score);

        // 5. Sinkronisasi ke file
        syncToFile(scores);
    }

    private static List<PlayerScore> loadScoresFromFile() {
        List<PlayerScore> scores = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return scores;

        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().split(",");
                if (parts.length == 3) {
                    scores.add(new PlayerScore(
                        parts[0],
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]),
                        "/assets/default.png"
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scores;
    }

    private static void syncToFile(List<PlayerScore> scores) {
        try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(FILE_NAME))) {
            for (PlayerScore s : scores) {
                out.println(s.name + "," + s.score + "," + s.timeSeconds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<PlayerScore> loadScores(int limit) {
        List<PlayerScore> scores = new ArrayList<>();
        ScoreService scoreService = new ScoreService();
        List<Map<String, Object>> dbScores = scoreService.getAllScores(limit);
        
        for (Map<String, Object> record : dbScores) {
            String name = (String) record.get("username");
            int score = (int) record.get("score");
            int time = (int) record.get("survival_time");
            String avatarPath = (String) record.get("avatar");
            scores.add(new PlayerScore(name, score, time, avatarPath));
        }
        return scores;
    }
}
