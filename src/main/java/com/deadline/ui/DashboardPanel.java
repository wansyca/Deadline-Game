package com.deadline.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.deadline.audio.SoundManager;
import com.deadline.main.Main;

public class DashboardPanel extends JPanel {

    private Image bgImage;
    private Image titleImage;
    private Image taglineImage;
    private ImageButton start, leaderboard, exit;
    
    private static final float TITLE_SCALE = 0.75f;
    private static final float TAGLINE_SCALE = 0.25f;
    
    private static final int BTN_WIDTH = 250;
    private static final int BTN_HEIGHT = 80;

    public DashboardPanel() {
        setLayout(null);

        // LOAD ASSETS
        bgImage = loadImage("/assets/bg.png");
        titleImage = loadImage("/assets/judul.png");
        taglineImage = loadImage("/assets/tagline.png");

        // BUTTONS (Main Menu size: 250x80)
        start = new ImageButton(
            "/assets/buttons/btn_start_normal.png",
            "/assets/buttons/btn_start_hover.png",
            "/assets/buttons/btn_start_pressed.png",
            BTN_WIDTH, BTN_HEIGHT
        );
        start.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.switchPage(Main.INPUT_PLAYER);
        });
        add(start);

        leaderboard = new ImageButton(
            "/assets/buttons/btn_laeder_normal.png",
            "/assets/buttons/btn_laeder_hover.png",
            "/assets/buttons/btn_laeder_pressed.png",
            BTN_WIDTH, BTN_HEIGHT
        );
        leaderboard.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.goToLeaderboardWithLoading();
        });
        add(leaderboard);

        exit = new ImageButton(
            "/assets/buttons/btn_exit_normal.png",
            "/assets/buttons/btn_exit_hover.png",
            "/assets/buttons/btn_exit_pressed.png",
            BTN_WIDTH, BTN_HEIGHT
        );
        exit.addActionListener(e -> {
            SoundManager.playClickSound();
            System.exit(0);
        });
        add(exit);

        // RESPONSIVE LAYOUT
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                layoutComponents();
            }
        });
    }

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void layoutComponents() {
        int centerX = getWidth() / 2;

        // ===== POSITIONS =====
        int titleY = 80;
        
        int tw = (int)(titleImage.getWidth(null) * TITLE_SCALE);
        int th = (int)(titleImage.getHeight(null) * TITLE_SCALE);
        
        int taglineY = titleY + th - 22; // Even closer
        int tgw = (int)(taglineImage.getWidth(null) * TAGLINE_SCALE);
        int tgh = (int)(taglineImage.getHeight(null) * TAGLINE_SCALE);

        // ===== BUTTONS (VERTICAL STACK) =====
        int buttonY = taglineY + tgh + 60; // Increased gap to move buttons lower
        int spacing = 12;

        start.setBounds(centerX - (BTN_WIDTH / 2), buttonY, BTN_WIDTH, BTN_HEIGHT);
        leaderboard.setBounds(centerX - (BTN_WIDTH / 2), buttonY + BTN_HEIGHT + spacing, BTN_WIDTH, BTN_HEIGHT);
        exit.setBounds(centerX - (BTN_WIDTH / 2), buttonY + (BTN_HEIGHT + spacing) * 2, BTN_WIDTH, BTN_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        }

        if (titleImage != null) {
            int tw = (int)(titleImage.getWidth(null) * TITLE_SCALE);
            int th = (int)(titleImage.getHeight(null) * TITLE_SCALE);
            int tx = (getWidth() - tw) / 2;
            int ty = 80;
            g2.drawImage(titleImage, tx, ty, tw, th, null);

            if (taglineImage != null) {
                int tgw = (int)(taglineImage.getWidth(null) * TAGLINE_SCALE);
                int tgh = (int)(taglineImage.getHeight(null) * TAGLINE_SCALE);
                int tgx = (getWidth() - tgw) / 2;
                int tgy = ty + th - 22; // Match layoutComponents
                g2.drawImage(taglineImage, tgx, tgy, tgw, tgh, null);
            }
        }

        g2.dispose();
    }
}