package com.deadline;

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("23:59 — SUBMIT OR DIE");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // FULLSCREEN MODE
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            // frame.setUndecorated(true); // aktifkan kalau mau tanpa border

            frame.setLocationRelativeTo(null);
            frame.setResizable(true);

            cardLayout = new CardLayout();
            mainPanel = new JPanel(cardLayout);

            mainPanel.add(new DashboardPanel(), DASHBOARD);
            mainPanel.add(new InputPlayerPanel(), INPUT_PLAYER);
            mainPanel.add(new SurvivorRankingUI(), LEADERBOARD);
            mainPanel.add(new LoadingPage(), LOADING);
            mainPanel.add(new GamePanel(), GAME);

            frame.add(mainPanel);
            frame.setVisible(true);

            switchPage(DASHBOARD);
        });
    }

    public static void switchPage(String pageName) {
        if (cardLayout != null && mainPanel != null) {
            cardLayout.show(mainPanel, pageName);

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
        switchPage(LOADING);

        Timer timer = new Timer(2000, e -> switchPage(LEADERBOARD));
        timer.setRepeats(false);
        timer.start();
    }

    public static void goToGameWithLoading(String playerName, String avatarPath) {
        switchPage(LOADING);

        Timer timer = new Timer(2000, e -> switchPage(GAME));
        timer.setRepeats(false);
        timer.start();
    }
}