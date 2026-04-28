package com.deadline.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.deadline.audio.SoundManager;
import com.deadline.main.Main;

public class DashboardPanel extends JPanel {

    private JLabel title;
    private JLabel subtitle;
    private JButton start, leaderboard, exit;

    public DashboardPanel() {
        setLayout(null);
        setBackground(new Color(20, 20, 30)); // Dark pixel background

        // HEADER (23.59)
        title = new JLabel("23.59", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 80));
        title.setForeground(new Color(255, 50, 50));
        add(title);

        // SUBTITLE (SUBMIT OR DIE)
        subtitle = new JLabel("SUBMIT OR DIE", SwingConstants.CENTER);
        subtitle.setFont(new Font("Monospaced", Font.BOLD, 32));
        subtitle.setForeground(new Color(255, 200, 50));
        add(subtitle);

        // BUTTONS
        start = createButton("START GAME", new Color(40, 150, 40));
        start.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.switchPage(Main.INPUT_PLAYER);
        });
        add(start);

        leaderboard = createButton("LEADERBOARD", new Color(150, 100, 40));
        leaderboard.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.goToLeaderboardWithLoading();
        });
        add(leaderboard);

        exit = createButton("EXIT", new Color(150, 40, 40));
        exit.addActionListener(e -> {
            SoundManager.playClickSound();
            System.exit(0);
        });
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
        int spacing = 25;

        title.setBounds(centerX - 200, titleY, 400, titleHeight);
        subtitle.setBounds(centerX - 200, titleY + titleHeight + spacing, 400, subtitleHeight);

        // ===== BUTTONS =====
        int startY = centerY - 20;

        start.setBounds(centerX - 130, startY, 260, 60);
        leaderboard.setBounds(centerX - 130, startY + 80, 260, 60);
        exit.setBounds(centerX - 130, startY + 160, 260, 60);
    }

    private JButton createButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                // NO AA for pixel look
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                Color current = baseColor;
                if (getModel().isPressed()) {
                    current = baseColor.darker().darker();
                } else if (getModel().isRollover()) {
                    current = baseColor.brighter();
                }

                // Pixel button style: Shadow below, main block, bright border top/left
                g2.setColor(current.darker());
                g2.fillRect(4, 4, getWidth() - 4, getHeight() - 4);

                g2.setColor(current);
                g2.fillRect(0, 0, getWidth() - 4, getHeight() - 4);

                g2.setColor(Color.WHITE);
                g2.drawRect(0, 0, getWidth() - 5, getHeight() - 5);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Monospaced", Font.BOLD, 22));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        // Background pattern for pixel feel
        g2.setColor(new Color(30, 30, 40));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(25, 25, 35));
        for (int i = 0; i < getWidth(); i += 32) {
            for (int j = 0; j < getHeight(); j += 32) {
                if ((i + j) % 64 == 0) {
                    g2.fillRect(i, j, 32, 32);
                }
            }
        }
    }
}