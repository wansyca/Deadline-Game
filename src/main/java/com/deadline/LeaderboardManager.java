package com.deadline;

import java.io.*;
import java.util.*;

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
        List<PlayerScore> scores = loadScores();
        scores.add(new PlayerScore(name, score, timeSeconds));
        
        // Sort descending by score
        Collections.sort(scores, (a, b) -> b.score - a.score);

        // Keep top 10
        if (scores.size() > 10) {
            scores = scores.subList(0, 10);
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (PlayerScore ps : scores) {
                out.println(ps.name + "," + ps.score + "," + ps.timeSeconds);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<PlayerScore> loadScores() {
        List<PlayerScore> scores = new ArrayList<>();
        
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        scores.add(new PlayerScore(
                            parts[0], 
                            Integer.parseInt(parts[1]), 
                            Integer.parseInt(parts[2])
                        ));
                    }
                }
            } catch (IOException | NumberFormatException e) {
                e.printStackTrace();
            }
        }
        
        // Add dummy data only if we have fewer than 3 scores
        if (scores.size() < 3) {
            if (!containsName(scores, "Fahmi")) scores.add(new PlayerScore("Fahmi", 200, 120));
            if (!containsName(scores, "Iqbal") && scores.size() < 2) scores.add(new PlayerScore("Iqbal", 150, 90));
            if (!containsName(scores, "Rizky") && scores.size() < 3) scores.add(new PlayerScore("Rizky", 100, 60));
        }

        // Final sort and limit
        Collections.sort(scores, (a, b) -> b.score - a.score);
        if (scores.size() > 10) {
            scores = scores.subList(0, 10);
        }
        
        return scores;
    }

    private static boolean containsName(List<PlayerScore> scores, String name) {
        for (PlayerScore ps : scores) {
            if (ps.name.equalsIgnoreCase(name)) return true;
        }
        return false;
    }
}
