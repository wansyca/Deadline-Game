package com.deadline.ui;

import com.deadline.main.Main;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

public class SplashScreen extends JPanel {
    private BufferedImage logo;
    private LogoPiece[] pieces;
    private Particle[] particles;
    private int targetW, targetH;
    private Timer animTimer;
    private long startTime;
    private float alpha = 1.0f;
    private boolean completed = false;
    private int shakeOffset = 0;
    
    private final int GRID_SIZE = 4; // 4x4 = 16 pieces
    private final int DURATION_MS = 3000; // 3 seconds total
    private final int ASSEMBLY_TIME = 1000; // 1s to assemble

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

    private class Particle {
        float x, y;
        float speedX, speedY;
        float size;
        int alpha;
        
        Particle(int w, int h) {
            x = (float) (Math.random() * w);
            y = (float) (Math.random() * h);
            speedX = (float) (Math.random() * 0.5 - 0.25);
            speedY = (float) (Math.random() * -1.0 - 0.2); // move up slowly
            size = (float) (Math.random() * 3 + 1);
            alpha = (int) (Math.random() * 100 + 50);
        }
        
        void update(int w, int h) {
            x += speedX;
            y += speedY;
            if (y < 0) {
                y = h;
                x = (float) (Math.random() * w);
            }
        }
    }

    public SplashScreen() {
        setBackground(Color.BLACK);
        loadLogo();
    }

    private void loadLogo() {
        try {
            logo = ImageIO.read(getClass().getResourceAsStream("/assets/ui/panels/logo.png"));
        } catch (Exception e) {
            System.err.println("❌ Error loading splash logo: /assets/ui/panels/logo.png");
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

        particles = new Particle[80];
        for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle(screenW, screenH);
        }
        
        startAnimation();
    }

    private void startAnimation() {
        startTime = System.currentTimeMillis();
        animTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            
            if (particles != null) {
                for (Particle p : particles) p.update(getWidth(), getHeight());
            }

            if (elapsed < ASSEMBLY_TIME) {
                float t = (float) elapsed / ASSEMBLY_TIME;
                for (LogoPiece p : pieces) p.update(t);
                completed = false;
            } else if (elapsed < DURATION_MS - 500) {
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
                float t = (float) (elapsed - (DURATION_MS - 500)) / 500f;
                alpha = Math.max(0.0f, 1.0f - t);
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

        int w = getWidth();
        int h = getHeight();
        long elapsed = System.currentTimeMillis() - startTime;

        // --- CINEMATIC BACKGROUND ---
        
        // 1. Dark Gradient (Black to Deep Red)
        GradientPaint gp = new GradientPaint(0, 0, Color.BLACK, 0, h, new Color(30, 0, 0));
        g2.setPaint(gp);
        g2.fillRect(0, 0, w, h);

        // 2. Animated Fog
        float fogOffset = (elapsed % 10000) / 10000f; 
        g2.setColor(new Color(60, 10, 10, 15));
        for (int i = 0; i < 3; i++) {
            int ovalW = (int) (w * 1.5);
            int ovalH = (int) (h * 1.5);
            int ox = (int) (Math.sin(fogOffset * Math.PI * 2 + i) * 150) - (ovalW - w) / 2;
            int oy = (int) (Math.cos(fogOffset * Math.PI * 2 + i) * 100) - (ovalH - h) / 2;
            g2.fillOval(ox, oy, ovalW, ovalH);
        }

        // 3. Floating Dust Particles
        if (particles != null) {
            for (Particle p : particles) {
                g2.setColor(new Color(255, 180, 150, p.alpha));
                g2.fillRect((int)p.x, (int)p.y, (int)p.size, (int)p.size);
            }
        }

        // 4. Soft Red Glow around Logo
        if (completed) {
            int glowSize = (int) (targetW * 1.5);
            float glowPulse = (float) Math.abs(Math.sin(elapsed / 600.0));
            RadialGradientPaint rgp = new RadialGradientPaint(
                new Point(w / 2, h / 2), glowSize / 2f, 
                new float[]{0f, 1f}, 
                new Color[]{new Color(150, 0, 0, (int)(30 + 15 * glowPulse)), new Color(0, 0, 0, 0)}
            );
            g2.setPaint(rgp);
            g2.fillRect(0, 0, w, h);
        }

        // --- DRAW LOGO ---
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));

        int dw = targetW / GRID_SIZE;
        int dh = targetH / GRID_SIZE;
        
        // Gentle Floating Animation
        int floatY = 0;
        if (completed) {
            floatY = (int) (Math.sin(elapsed / 400.0) * 8);
        }

        for (LogoPiece p : pieces) {
            int finalY = p.currentY + floatY + (completed ? shakeOffset : 0);
            g2.drawImage(logo, 
                p.currentX, finalY, p.currentX + dw, finalY + dh,
                p.srcX, p.srcY, p.srcX + p.srcW, p.srcY + p.srcH, null);
        }
    }
}
