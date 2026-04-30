package com.deadline.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.Box;
import javax.swing.JPanel;
import com.deadline.audio.SoundManager;
import com.deadline.main.Main;
import com.deadline.service.LeaderboardManager;

public class SurvivorRankingUI extends JPanel {
    private List<LeaderboardManager.PlayerScore> scores;
    private ImageButton backBtn;
    
    // Virtual resolution for pixelation
    private static final int VIRT_W = 640;
    private static final int VIRT_H = 480;

    public SurvivorRankingUI() {
        setLayout(null); // Manual positioning for the buffer technique
        setBackground(new Color(10, 10, 15));

        // BACK BUTTON (Don't change implementation)
        backBtn = new ImageButton(
                "/assets/buttons/btn_backto_normal.png",
                "/assets/buttons/btn_backto_normal.png",
                "/assets/buttons/btn_backto_normal.png",
                180, 50);
        backBtn.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.switchPage(Main.DASHBOARD);
        });
        add(backBtn);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData();
            }
            
            @Override
            public void componentResized(ComponentEvent e) {
                // Keep button at top left
                backBtn.setBounds(30, 20, 180, 50);
            }
        });

        refreshData();
    }

    private void refreshData() {
        scores = LeaderboardManager.loadScores();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 1. Create Pixel Buffer
        BufferedImage buffer = new BufferedImage(VIRT_W, VIRT_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buffer.createGraphics();

        // PIXEL SETTINGS
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // BACKGROUND & GRID PATTERN
        g2.setColor(new Color(10, 10, 15));
        g2.fillRect(0, 0, VIRT_W, VIRT_H);
        
        g2.setColor(new Color(255, 255, 255, 5));
        for(int x=0; x<VIRT_W; x+=16) g2.drawLine(x, 0, x, VIRT_H);
        for(int y=0; y<VIRT_H; y+=16) g2.drawLine(0, y, VIRT_W, y);

        // HEADER
        g2.setFont(new Font("Monospaced", Font.BOLD, 28));
        String title = "HALL OF SURVIVORS";
        g2.setColor(Color.WHITE);
        g2.drawString(title, (VIRT_W - g2.getFontMetrics().stringWidth(title))/2, 50);

        if (scores == null || scores.isEmpty()) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
            String empty = "NO DATA FOUND...";
            g2.setColor(new Color(150, 0, 0));
            g2.drawString(empty, (VIRT_W - g2.getFontMetrics().stringWidth(empty))/2, VIRT_H/2);
        } else {
            drawPodiums(g2);
            drawList(g2);
        }

        g2.dispose();

        // 2. Render buffer to screen
        Graphics2D gFinal = (Graphics2D) g;
        gFinal.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        gFinal.drawImage(buffer, 0, 0, getWidth(), getHeight(), null);
    }

    private void drawPodiums(Graphics2D g2) {
        int podiumY = 180;
        int cardW = 100;
        int gap = 20;
        int startX = (VIRT_W - (cardW * 3 + gap * 2)) / 2;

        LeaderboardManager.PlayerScore p1 = scores.size() > 0 ? scores.get(0) : null;
        LeaderboardManager.PlayerScore p2 = scores.size() > 1 ? scores.get(1) : null;
        LeaderboardManager.PlayerScore p3 = scores.size() > 2 ? scores.get(2) : null;

        // Order: 2ND, 1ST, 3RD
        drawCard(g2, "2ND", p2, startX, podiumY + 20, cardW, 100, new Color(160, 160, 160));
        drawCard(g2, "1ST", p1, startX + cardW + gap, podiumY, cardW, 120, new Color(255, 215, 0));
        drawCard(g2, "3RD", p3, startX + (cardW + gap) * 2, podiumY + 40, cardW, 80, new Color(205, 127, 50));
    }

    private void drawCard(Graphics2D g2, String rank, LeaderboardManager.PlayerScore ps, int x, int y, int w, int h, Color color) {
        // Pixel Box
        g2.setColor(new Color(30, 30, 40));
        g2.fillRect(x, y, w, h);
        
        g2.setColor(color);
        g2.drawRect(x, y, w, h);
        g2.drawRect(x+1, y+1, w-2, h-2); // Thick outline

        // Rank Header
        g2.fillRect(x, y, w, 25);
        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2.setColor(Color.BLACK);
        g2.drawString(rank, x + (w - g2.getFontMetrics().stringWidth(rank))/2, y + 18);

        // Name & Score
        if (ps != null) {
            g2.setFont(new Font("Monospaced", Font.BOLD, 11));
            g2.setColor(Color.WHITE);
            String name = ps.name.length() > 10 ? ps.name.substring(0, 8) + ".." : ps.name;
            g2.drawString(name, x + (w - g2.getFontMetrics().stringWidth(name))/2, y + 45);
            
            g2.setColor(color);
            String pts = ps.score + " PTS";
            g2.drawString(pts, x + (w - g2.getFontMetrics().stringWidth(pts))/2, y + 65);
        } else {
            g2.setFont(new Font("Monospaced", Font.BOLD, 10));
            g2.setColor(new Color(100, 100, 100));
            g2.drawString("---", x + (w - g2.getFontMetrics().stringWidth("---"))/2, y + 55);
        }
    }

    private void drawList(Graphics2D g2) {
        if (scores.size() <= 3) return;

        int listY = 320;
        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.setColor(new Color(150, 150, 170));
        
        for (int i = 3; i < Math.min(scores.size(), 8); i++) {
            LeaderboardManager.PlayerScore ps = scores.get(i);
            String line = "#" + (i + 1) + " " + String.format("%-12s", ps.name) + " " + ps.score + " PTS";
            g2.drawString(line, (VIRT_W - g2.getFontMetrics().stringWidth(line))/2, listY + (i - 3) * 20);
        }
    }
}
