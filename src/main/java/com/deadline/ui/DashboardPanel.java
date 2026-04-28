package com.deadline.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
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
    private Image bgImage;

    public DashboardPanel() {
        setLayout(null);

        // LOAD BACKGROUND
        try {
            java.net.URL bgUrl = getClass().getResource("/assets/bg.png");
            if (bgUrl != null) {
                bgImage = new ImageIcon(bgUrl).getImage();
            } else {
                System.err.println("❌ Dashboard Background not found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TIMER (23:59) - BOLD CRIMSON PIXEL
        title = new JLabel("23:59", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                
                // Solid Pixel Shadow
                g2.setFont(getFont());
                g2.setColor(new Color(0, 0, 0, 150));
                g2.drawString(getText(), 4, 74);
                
                // Main Crimson Text
                g2.setColor(new Color(220, 40, 40)); 
                g2.drawString(getText(), 0, 70);
                g2.dispose();
            }
        };
        title.setFont(new Font("Monospaced", Font.BOLD, 110));
        add(title);

        // TITLE (SUBMIT OR DIE) - RED & CLEAN
        subtitle = new JLabel("SUBMIT OR DIE", SwingConstants.CENTER);
        subtitle.setFont(new Font("Monospaced", Font.BOLD, 28));
        subtitle.setForeground(new Color(220, 40, 40));
        add(subtitle);

        // BUTTONS - DEEP RED PIXEL STYLE
        Color crimsonRed = new Color(160, 0, 0);
        start = createButton("START GAME", crimsonRed);
        start.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.switchPage(Main.INPUT_PLAYER);
        });
        add(start);

        leaderboard = createButton("LEADERBOARD", crimsonRed);
        leaderboard.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.goToLeaderboardWithLoading();
        });
        add(leaderboard);

        exit = createButton("EXIT", crimsonRed);
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

                // 1. CHUNKY PIXEL SHADOW (Bottom-Right)
                g2.setColor(new Color(0, 0, 0, 150));
                g2.fillRect(6, 6, getWidth() - 6, getHeight() - 6);

                // 2. MAIN BUTTON BODY
                g2.setColor(current);
                g2.fillRect(0, 0, getWidth() - 6, getHeight() - 6);

                // 3. PIXEL HIGHLIGHT (Top-Left)
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillRect(0, 0, getWidth() - 6, 4); // Top
                g2.fillRect(0, 0, 4, getHeight() - 6); // Left

                // 4. THICK PIXEL BORDER (Darker outline)
                g2.setColor(current.darker().darker());
                g2.setStroke(new java.awt.BasicStroke(4));
                g2.drawRect(2, 2, getWidth() - 10, getHeight() - 10);

                g2.dispose();
                
                // TEXT
                Graphics2D gt = (Graphics2D) g.create();
                gt.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                gt.setFont(getFont());
                gt.setColor(Color.WHITE);
                
                int tw = gt.getFontMetrics().stringWidth(getText());
                gt.drawString(getText(), (getWidth() - tw) / 2 - 3, getHeight() / 2 + 6);
                gt.dispose();
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

        // PIXEL STYLE RENDERING
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // RENDER BACKGROUND FULL (PAS KARENA RESOLUSI TETAP)
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g2.setColor(new Color(20, 20, 30));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. DARK OVERLAY & VIGNETTE
        g2.setColor(new Color(0, 0, 0, 140)); // Base Overlay
        g2.fillRect(0, 0, getWidth(), getHeight());

        // RADIAL VIGNETTE (Manual with gradient)
        int w = getWidth();
        int h = getHeight();
        float[] dist = { 0.0f, 1.0f };
        Color[] colors = { new Color(0, 0, 0, 0), new Color(0, 0, 0, 230) };
        java.awt.RadialGradientPaint p = new java.awt.RadialGradientPaint(
            new java.awt.geom.Point2D.Double(w/2, h/2), w, dist, colors);
        g2.setPaint(p);
        g2.fillRect(0, 0, w, h);
    }
}