package com.deadline;

import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {

    private Image bg;

    private JLabel title;
    private JLabel subtitle;
    private JButton start, leaderboard, exit;

    public DashboardPanel() {
        setLayout(null);

        bg = new ImageIcon(getClass().getResource("/assets/bg.png")).getImage();

        // HEADER (23.59)
        title = new JLabel("23.59", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 64));
        title.setForeground(new Color(255, 50, 50));
        add(title);

        // SUBTITLE (SUBMIT OR DIE)
        subtitle = new JLabel("SUBMIT OR DIE", SwingConstants.CENTER);
        subtitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        subtitle.setForeground(new Color(255, 80, 80));
        add(subtitle);

        // BUTTONS
        start = createButton("START GAME");
        start.addActionListener(e -> Main.switchPage(Main.INPUT_PLAYER));
        add(start);

        leaderboard = createButton("LEADERBOARD");
        leaderboard.addActionListener(e -> Main.goToLeaderboardWithLoading());
        add(leaderboard);

        exit = createButton("EXIT");
        exit.addActionListener(e -> System.exit(0));
        add(exit);

        // RESPONSIVE
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                layoutComponents();
            }
        });
    }

    private void layoutComponents() {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // ===== TITLE & SUBTITLE =====
        int titleY = 80;
        int titleHeight = 80;
        int subtitleHeight = 40;
        int spacing = 25; // 🔥 ini jarak antar title & subtitle

        title.setBounds(centerX - 200, titleY, 400, titleHeight);
        subtitle.setBounds(centerX - 200, titleY + titleHeight + spacing, 400, subtitleHeight);

        // ===== BUTTONS =====
        int startY = centerY - 50;

        start.setBounds(centerX - 130, startY, 260, 60);
        leaderboard.setBounds(centerX - 130, startY + 80, 260, 60);
        exit.setBounds(centerX - 130, startY + 160, 260, 60);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(new Color(100, 0, 0));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(200, 0, 0));
                } else {
                    g2.setColor(new Color(150, 0, 0));
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bg != null) {
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        }

        // overlay gelap biar aesthetic
        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}