package com.deadline.ui;

import com.deadline.main.Main;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SplashScreen extends JPanel {
    private BufferedImage logo;
    private LogoPiece[] pieces;
    private int targetW, targetH;
    private Timer animTimer;
    private long startTime;
    private float alpha = 1.0f;
    private boolean completed = false;
    private int shakeOffset = 0;
    
    private final int GRID_SIZE = 4; // 4x4 = 16 pieces
    private final int DURATION_MS = 3800; // Total duration
    private final int ASSEMBLY_TIME = 1200; // 1.2s to assemble

    private class LogoPiece {
        int startX, startY;
        int targetX, targetY;
        int srcX, srcY, srcW, srcH;
        int currentX, currentY;

        LogoPiece(int startX, int startY, int targetX, int targetY, int srcX, int srcY, int srcW, int srcH) {
            this.startX = startX;
            this.startY = startY;
            this.targetX = targetX;
            this.targetY = targetY;
            this.srcX = srcX;
            this.srcY = srcY;
            this.srcW = srcW;
            this.srcH = srcH;
        }

        void update(float t) {
            // Cubic ease out
            float easeT = 1 - (float)Math.pow(1 - t, 3);
            currentX = (int) (startX + (targetX - startX) * easeT);
            currentY = (int) (startY + (targetY - startY) * easeT);
        }
    }

    public SplashScreen() {
        setBackground(Color.BLACK);
        loadLogo();
    }

    private void loadLogo() {
        try {
            logo = ImageIO.read(getClass().getResourceAsStream("/assets/logo.png"));
        } catch (Exception e) {
            System.err.println("❌ Error loading splash logo: assets/logo.png");
        }
    }

    private void initPieces() {
        int screenW = getWidth();
        int screenH = getHeight();
        if (screenW <= 0 || logo == null) return;

        double logoRatio = (double) logo.getWidth() / logo.getHeight();
        targetW = (int) (screenW * 0.35);
        targetH = (int) (targetW / logoRatio);
        int centerX = (screenW - targetW) / 2;
        int centerY = (screenH - targetH) / 2;

        pieces = new LogoPiece[GRID_SIZE * GRID_SIZE];
        int pW = logo.getWidth() / GRID_SIZE;
        int pH = logo.getHeight() / GRID_SIZE;
        int dw = targetW / GRID_SIZE;
        int dh = targetH / GRID_SIZE;

        java.util.Random rnd = new java.util.Random();
        int idx = 0;
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int targetX = centerX + c * dw;
                int targetY = centerY + r * dh;
                
                // Random start positions (often off-screen)
                int startX = rnd.nextInt(screenW + 400) - 200;
                int startY = rnd.nextInt(screenH + 400) - 200;
                
                pieces[idx++] = new LogoPiece(startX, startY, targetX, targetY, 
                                            c * pW, r * pH, pW, pH);
            }
        }
        
        startAnimation();
    }

    private void startAnimation() {
        startTime = System.currentTimeMillis();
        animTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            
            if (elapsed < ASSEMBLY_TIME) {
                float t = (float) elapsed / ASSEMBLY_TIME;
                for (LogoPiece p : pieces) p.update(t);
                completed = false;
            } else if (elapsed < ASSEMBLY_TIME + 1800) {
                // Stay assembled
                for (LogoPiece p : pieces) {
                    p.currentX = p.targetX;
                    p.currentY = p.targetY;
                }
                // IMPACT SHAKE
                if (elapsed < ASSEMBLY_TIME + 100) {
                    shakeOffset = (int)(Math.random() * 8 - 4);
                } else {
                    shakeOffset = 0;
                }
                completed = true;
            } else if (elapsed < DURATION_MS) {
                float t = (float) (elapsed - (ASSEMBLY_TIME + 1800)) / 800f;
                alpha = 1.0f - t;
            } else {
                animTimer.stop();
                Main.switchPage(Main.DASHBOARD);
            }
            repaint();
        });
        animTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (logo == null) return;
        if (pieces == null) {
            initPieces();
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));

        int dw = targetW / GRID_SIZE;
        int dh = targetH / GRID_SIZE;

        for (LogoPiece p : pieces) {
            g2.drawImage(logo, 
                p.currentX, p.currentY + (completed ? shakeOffset : 0), p.currentX + dw, p.currentY + dh + (completed ? shakeOffset : 0),
                p.srcX, p.srcY, p.srcX + p.srcW, p.srcY + p.srcH, null);
        }
    }
}
