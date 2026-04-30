package com.deadline.main;

import com.deadline.ui.*;
import com.deadline.game.*;
import com.deadline.audio.*;
import com.deadline.backend.DatabaseManager;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static final String DASHBOARD = "DASHBOARD";
    public static final String INPUT_PLAYER = "INPUT_PLAYER";
    public static final String LEADERBOARD = "LEADERBOARD";
    public static final String GAME = "GAME";
    public static final String LOADING = "LOADING";

    private static JFrame frame;
    private static JPanel mainPanel;
    private static CardLayout cardLayout;
    private static GamePanel gamePanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Initialize database on startup
            DatabaseManager.initializeDatabase();

            frame = new JFrame("23:59 — SUBMIT OR DIE");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // FIXED RESOLUTION (4:3 Ratio to match background)
            int width = 1024;
            int height = 768;
            
            mainPanel = new JPanel(cardLayout = new CardLayout());
            mainPanel.setPreferredSize(new Dimension(width, height));
            
            frame.setResizable(false);

            mainPanel.add(new DashboardPanel(), DASHBOARD);
            mainPanel.add(new InputPlayerPanel(), INPUT_PLAYER);
            mainPanel.add(new SurvivorRankingUI(), LEADERBOARD);
            mainPanel.add(new LoadingPage(), LOADING);

            gamePanel = new GamePanel();
            mainPanel.add(gamePanel, GAME);

            frame.add(mainPanel);
            frame.pack(); // Adjust window to fit preferred size
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);

            switchPage(DASHBOARD);
        });
    }

    public static void switchPage(String pageName) {
        if (cardLayout != null && mainPanel != null) {
            cardLayout.show(mainPanel, pageName);

            // 🎵 Putar musik menu jika sedang di Dashboard atau Input Player
            if (pageName.equals(DASHBOARD) || pageName.equals(INPUT_PLAYER)) {
                SoundManager.playBackgroundMusic("/sound/Midnight Focus Loop.wav", -5.0f);
            }

            // Fokus ke GamePanel biar input keyboard jalan
            if (pageName.equals(GAME)) {
                for (Component comp : mainPanel.getComponents()) {
                    if (comp instanceof GamePanel) {
                        comp.requestFocusInWindow();
                    }
                }
            }
        }
    }

    public static void goToLeaderboardWithLoading() {
        setLoadingTarget(LEADERBOARD);
        switchPage(LOADING);
    }

    public static void goToGameWithLoading(int playerId, String playerName, String avatarPath) {
        SoundManager.stopBackgroundMusicWithFade(); 
        if (gamePanel != null) {
            gamePanel.resetGame(playerId, playerName, avatarPath);
        }
        setLoadingTarget(GAME);
        switchPage(LOADING);
    }

    private static void setLoadingTarget(String target) {
        for (Component comp : mainPanel.getComponents()) {
            if (comp instanceof LoadingPage) {
                ((LoadingPage) comp).setTargetPage(target);
                break;
            }
        }
    }
}