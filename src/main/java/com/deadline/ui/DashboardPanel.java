package com.deadline.ui;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.deadline.audio.SoundManager;
import com.deadline.main.Main;

public class DashboardPanel extends JPanel {

    private Image bgImage;
    private JLabel titleLabel;
    private JLabel startBtn, leaderboardBtn, exitBtn;

    public DashboardPanel() {
        setLayout(null);

        // 1. BACKGROUND
        try {
            java.net.URL bgUrl = getClass().getResource("/assets/bg.png");
            if (bgUrl != null) {
                bgImage = new ImageIcon(bgUrl).getImage();
            } else {
                System.err.println("❌ Dashboard Background not found at /assets/bg.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. JUDUL GAME
        titleLabel = new JLabel();
        try {
            java.net.URL judulUrl = getClass().getResource("/assets/judul.png");
            if (judulUrl != null) {
                titleLabel.setIcon(new ImageIcon(judulUrl));
            } else {
                System.err.println("❌ Title image not found at /assets/judul.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        add(titleLabel);

        // 3 & 4. BUTTONS
        startBtn = createButton(
                "/assets/button/btn_start_normal.png",
                "/assets/button/btn_start_hover.png",
                "/assets/button/btn_start_presed.png",
                () -> {
                    SoundManager.playClickSound();
                    Main.switchPage(Main.INPUT_PLAYER);
                }
        );
        add(startBtn);

        leaderboardBtn = createButton(
                "/assets/button/btn_laeder_normal.png",
                "/assets/button/btn_laeder_hover.png",
                "/assets/button/btn_laeder_presed.png",
                () -> {
                    SoundManager.playClickSound();
                    Main.goToLeaderboardWithLoading();
                }
        );
        add(leaderboardBtn);

        exitBtn = createButton(
                "/assets/button/btn_exit_normal.png",
                "/assets/button/btn_exit_hover.png",
                "/assets/button/btn_exit_presed.png",
                () -> {
                    SoundManager.playClickSound();
                    System.exit(0);
                }
        );
        add(exitBtn);

        // 6. LAYOUT
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                layoutComponents();
            }
        });
    }

    private void layoutComponents() {
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // TITLE - Posisi: tengah atas (center)
        if (titleLabel.getIcon() != null) {
            int titleWidth = titleLabel.getIcon().getIconWidth();
            int titleHeight = titleLabel.getIcon().getIconHeight();
            titleLabel.setBounds(centerX - (titleWidth / 2), 80, titleWidth, titleHeight);
        } else {
            titleLabel.setBounds(centerX - 150, 80, 300, 100);
        }

        // BUTTONS - Susunan vertikal: Start Game, Leaderboard, Exit
        int spacing = 20;
        int startY = centerY - 50; // Adjust starting Y position so they look centered

        // Helper to position a button
        positionButton(startBtn, centerX, startY);
        startY += (startBtn.getIcon() != null ? startBtn.getIcon().getIconHeight() : 60) + spacing;

        positionButton(leaderboardBtn, centerX, startY);
        startY += (leaderboardBtn.getIcon() != null ? leaderboardBtn.getIcon().getIconHeight() : 60) + spacing;

        positionButton(exitBtn, centerX, startY);
    }

    private void positionButton(JLabel btn, int centerX, int y) {
        if (btn.getIcon() != null) {
            int w = btn.getIcon().getIconWidth();
            int h = btn.getIcon().getIconHeight();
            btn.setBounds(centerX - (w / 2), y, w, h);
        } else {
            btn.setBounds(centerX - 100, y, 200, 60);
        }
    }

    private JLabel createButton(String normalPath, String hoverPath, String pressedPath, Runnable onClick) {
        JLabel btn = new JLabel();
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Load Icons
        ImageIcon normalIcon = loadIcon(normalPath);
        ImageIcon hoverIcon = loadIcon(hoverPath);
        ImageIcon pressedIcon = loadIcon(pressedPath);

        if (normalIcon != null) btn.setIcon(normalIcon);

        // 5. INTERAKSI BUTTON
        btn.addMouseListener(new MouseAdapter() {
            boolean isPressed = false;

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isPressed && hoverIcon != null) {
                    btn.setIcon(hoverIcon);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isPressed = false;
                if (normalIcon != null) {
                    btn.setIcon(normalIcon);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                if (pressedIcon != null) {
                    btn.setIcon(pressedIcon);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isPressed) {
                    isPressed = false;
                    if (btn.contains(e.getPoint())) {
                        if (hoverIcon != null) btn.setIcon(hoverIcon);
                        if (onClick != null) onClick.run();
                    } else {
                        if (normalIcon != null) btn.setIcon(normalIcon);
                    }
                }
            }
        });

        return btn;
    }

    private ImageIcon loadIcon(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url);
            } else {
                System.err.println("❌ Asset not found: " + path);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 7. STYLE PIXEL
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // RENDER BACKGROUND FULL PANEL NO DISTORTION
        if (bgImage != null) {
            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = bgImage.getWidth(null);
            int imgH = bgImage.getHeight(null);

            if (imgW > 0 && imgH > 0) {
                double scaleX = (double) panelW / imgW;
                double scaleY = (double) panelH / imgH;
                double scale = Math.max(scaleX, scaleY);

                int drawW = (int) (imgW * scale);
                int drawH = (int) (imgH * scale);
                int drawX = (panelW - drawW) / 2;
                int drawY = (panelH - drawH) / 2;

                g2.drawImage(bgImage, drawX, drawY, drawW, drawH, null);
            } else {
                g2.drawImage(bgImage, 0, 0, panelW, panelH, null);
            }
        } else {
            g2.setColor(new java.awt.Color(20, 20, 30));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. DARK OVERLAY & VIGNETTE
        g2.setColor(new java.awt.Color(0, 0, 0, 140)); // Base Overlay
        g2.fillRect(0, 0, getWidth(), getHeight());

        // RADIAL VIGNETTE (Manual with gradient)
        int w = getWidth();
        int h = getHeight();
        float[] dist = { 0.0f, 1.0f };
        java.awt.Color[] colors = { new java.awt.Color(0, 0, 0, 0), new java.awt.Color(0, 0, 0, 230) };
        java.awt.RadialGradientPaint p = new java.awt.RadialGradientPaint(
            new java.awt.geom.Point2D.Double(w/2.0, h/2.0), Math.max(w, h), dist, colors);
        g2.setPaint(p);
        g2.fillRect(0, 0, w, h);
    }
}