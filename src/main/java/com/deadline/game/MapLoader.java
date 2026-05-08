package com.deadline.game;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MapLoader {

    public static int[][] loadMap(String path, int rows, int cols) {
        int[][] map = new int[rows][cols];
        try {
            InputStream is = MapLoader.class.getResourceAsStream(path);
            if (is == null) {
                // Fallback to absolute file path if not in resources
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    is = new java.io.FileInputStream(file);
                }
            }

            if (is == null) {
                System.err.println("❌ Could not find map file: " + path);
                return map;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            for (int r = 0; r < rows; r++) {
                String line = br.readLine();
                if (line == null) break;
                String[] values = line.trim().split("\\s+");
                for (int c = 0; c < cols && c < values.length; c++) {
                    try {
                        map[r][c] = Integer.parseInt(values[c]);
                    } catch (NumberFormatException e) {
                        // Handle char-based maps if needed
                        map[r][c] = (int) values[c].charAt(0);
                    }
                }
            }
            br.close();
        } catch (Exception e) {
            System.err.println("❌ Error loading map: " + e.getMessage());
        }
        return map;
    }

    public static List<String> loadTextMap(String path) {
        List<String> lines = new ArrayList<>();
        try {
            InputStream is = MapLoader.class.getResourceAsStream(path);
            if (is == null) {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    is = new java.io.FileInputStream(file);
                }
            }

            if (is == null) return lines;

            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }
}
