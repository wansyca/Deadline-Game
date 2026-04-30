package com.deadline.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import com.deadline.main.Main;

public class LoadingPage extends JPanel {

    private int progress = 0;
    private int dots = 0;
    private Image bgImage;
    private String targetPage = Main.DASHBOARD;
    
    // Very low resolution to force extreme pixelation
    private static final int VIRTUAL_WIDTH = 320;
    private static final int VIRTUAL_HEIGHT = 240;

    public LoadingPage() {
        try {
            java.net.URL url = getClass().getResource("/assets/bg.png");
            if (url != null) bgImage = new ImageIcon(url).getImage();
        } catch (Exception e) {}

        // Progress Timer (roughly 3 seconds to reach 100)
        Timer progressTimer = new Timer(30, e -> {
            if (progress < 100) {
                progress += 1;
                repaint();
            } else {
                ((Timer) e.getSource()).stop();
                // Switch to the target page ONLY when 100% is reached
                Main.switchPage(targetPage);
            }
        });

        Timer dotsTimer = new Timer(500, e -> {
            dots = (dots + 1) % 4;
            repaint();
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent evt) {
                progress = 0;
                dots = 0;
                progressTimer.restart();
                dotsTimer.restart();
            }
        });
    }

    public void setTargetPage(String page) {
        this.targetPage = page;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Render to a tiny buffer
        BufferedImage buffer = new BufferedImage(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buffer.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // 1. BACKGROUND
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT, null);
        }
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        int centerX = VIRTUAL_WIDTH / 2;
        int centerY = VIRTUAL_HEIGHT / 2;

        // 2. TITLE (Lowered font size for low-res)
        g2.setFont(new Font("Monospaced", Font.BOLD, 20));
        String title = "23:59 — SUBMIT OR DIE";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        
        g2.setColor(new Color(100, 0, 0));
        g2.drawString(title, centerX - (titleWidth / 2) + 1, centerY - 40 + 1);
        g2.setColor(new Color(255, 0, 0));
        g2.drawString(title, centerX - (titleWidth / 2), centerY - 40);

        // 3. LOADING TEXT
        g2.setFont(new Font("Monospaced", Font.BOLD, 9));
        StringBuilder sb = new StringBuilder("Menyiapkan berkas deadline");
        for(int i=0; i<dots; i++) sb.append(".");
        String displayStatus = sb.toString() + " " + progress + "%";
        int statusWidth = g2.getFontMetrics().stringWidth("Menyiapkan berkas deadline... 100%");
        
        g2.setColor(Color.WHITE);
        g2.drawString(displayStatus, centerX - (statusWidth / 2), centerY - 10);

        // 4. PROGRESS BAR
        int barW = 200;
        int barH = 10;
        int barX = centerX - (barW / 2);
        int barY = centerY + 5;

        g2.setColor(new Color(60, 60, 60));
        g2.drawRect(barX - 1, barY - 1, barW + 1, barH + 1);
        
        int fillW = (int) (barW * (progress / 100.0));
        g2.setColor(new Color(255, 30, 30));
        g2.fillRect(barX, barY, fillW, barH);
        
        // Grid pattern
        g2.setColor(new Color(0, 0, 0, 80));
        for (int i = 0; i < barW; i += 6) {
            g2.drawLine(barX + i, barY, barX + i, barY + barH);
        }

        g2.dispose();

        // Stretch the tiny buffer to the screen with Nearest Neighbor
        Graphics2D gFinal = (Graphics2D) g;
        gFinal.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        gFinal.drawImage(buffer, 0, 0, getWidth(), getHeight(), null);
    }
}