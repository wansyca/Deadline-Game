package com.deadline.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import com.deadline.audio.SoundManager;
import com.deadline.main.Main;
import com.deadline.service.LeaderboardManager;

public class SurvivorRankingUI extends JPanel {

    // ── Virtual canvas ──────────────────────────────────────────────────────
    private static final int VW = 900, VH = 620;

    // ── Palette (Horror Academia - Red/Black/Gold) ────────────────────────
    private static final Color BG       = new Color(12, 2, 2);
    private static final Color GOLD     = new Color(255, 195, 0);
    private static final Color SILVER   = new Color(200, 205, 215);
    private static final Color BRONZE   = new Color(190, 100, 50);
    private static final Color GOLD_DIM = new Color(255, 195, 0, 50);
    private static final Color SIL_DIM  = new Color(200, 205, 215, 30);
    private static final Color BRZ_DIM  = new Color(190, 100, 50, 30);
    private static final Color ROW_N    = new Color(20, 5, 5, 200);
    private static final Color ROW_HOVER= new Color(50, 8, 12, 230);
    private static final Color TEXT     = new Color(245, 220, 220); // Pale dull white/pink
    private static final Color MUTED    = new Color(140, 60, 60);
    private static final Color ACCENT   = new Color(180, 20, 30);   // Dark blood red

    // ── State ────────────────────────────────────────────────────────────────
    private List<LeaderboardManager.PlayerScore> scores;
    private final Map<String, BufferedImage> avatarCache = new HashMap<>();
    private ImageButton backBtn;
    
    // Animations
    private float fadeAlpha = 0f;
    private float podiumAnim = 0f;
    private float listAnim = 0f;
    private float pulseTime = 0f;
    private Timer animTimer;
    
    // Scrolling & Hover
    private double scrollY = 0;
    private double targetScrollY = 0;
    private int hoveredRow = -1;
    private Font pixelFont;
    
    // Particles
    private static class Particle {
        float x, y, speedY, speedX, size;
        int alpha;
    }
    private final List<Particle> particles = new ArrayList<>();

    // List geometry (virtual coords)
    private static final int LIST_Y  = 410;
    private static final int ROW_H   = 46;
    private static final int LIST_X  = 60;
    private static final int LIST_W  = VW - 120;

    public SurvivorRankingUI() {
        setLayout(null);
        setBackground(BG);
        loadPixelFont();
        buildBackButton();
        initParticles();
        hookInput();
        addComponentListener(new ComponentAdapter() {
            @Override public void componentShown(ComponentEvent e) {
                placeButton();
                startAnimations();
                refreshAsync();
            }
            @Override public void componentResized(ComponentEvent e) { placeButton(); }
        });
        refreshAsync();
    }

    // ── Setup ────────────────────────────────────────────────────────────────
    private void loadPixelFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/assets/ui/fonts/pixel.ttf");
            pixelFont = (is != null) ? Font.createFont(Font.TRUETYPE_FONT, is) : null;
        } catch (Exception ignored) {}
    }

    private Font px(float sz) {
        return (pixelFont != null) ? pixelFont.deriveFont(Font.BOLD, sz)
                                   : new Font("Monospaced", Font.BOLD, (int) sz);
    }

    private void buildBackButton() {
        backBtn = new ImageButton(
                "/assets/ui/buttons/btn_backto_normal.png",
                "/assets/ui/buttons/btn_backto_normal.png",
                "/assets/ui/buttons/btn_backto_normal.png",
                180, 50);
        backBtn.addActionListener(e -> { SoundManager.playClickSound(); Main.switchPage(Main.DASHBOARD); });
        add(backBtn);
        placeButton();
    }

    private void placeButton() { backBtn.setBounds(28, 18, 180, 50); }
    
    private void initParticles() {
        Random rnd = new Random();
        for (int i = 0; i < 40; i++) {
            Particle p = new Particle();
            p.x = rnd.nextInt(VW);
            p.y = rnd.nextInt(VH);
            p.speedY = -0.2f - rnd.nextFloat() * 0.5f;
            p.speedX = (rnd.nextFloat() - 0.5f) * 0.3f;
            p.size = 1f + rnd.nextFloat() * 2f;
            p.alpha = 20 + rnd.nextInt(50);
            particles.add(p);
        }
    }

    private void startAnimations() {
        fadeAlpha = 0f;
        podiumAnim = 0f;
        listAnim = 0f;
        pulseTime = 0f;
        targetScrollY = 0;
        scrollY = 0;
        if (animTimer != null) animTimer.stop();
        animTimer = new Timer(16, e -> {
            fadeAlpha = Math.min(1f, fadeAlpha + 0.04f);
            if (fadeAlpha > 0.5f) podiumAnim = Math.min(1f, podiumAnim + 0.03f);
            if (podiumAnim > 0.5f) listAnim = Math.min(1f, listAnim + 0.04f);
            
            pulseTime += 0.05f;
            
            // Smooth scroll
            scrollY += (targetScrollY - scrollY) * 0.2;
            
            // Particles
            for (Particle p : particles) {
                p.y += p.speedY;
                p.x += p.speedX;
                if (p.y < -10) {
                    p.y = VH + 10;
                    p.x = (float)(Math.random() * VW);
                }
            }
            repaint();
        });
        animTimer.start();
    }

    private void hookInput() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) {
                int vy = toVY(e.getY());
                int newH = hitRow(vy);
                if (newH != hoveredRow) { hoveredRow = newH; repaint(); }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { hoveredRow = -1; repaint(); }
        });
        addMouseWheelListener(e -> {
            if (scores == null || scores.size() <= 3) return;
            int maxRows = scores.size() - 3;
            double maxScroll = Math.max(0, (maxRows * ROW_H) - (VH - LIST_Y - 20));
            targetScrollY += e.getPreciseWheelRotation() * 30;
            targetScrollY = Math.max(0, Math.min(maxScroll, targetScrollY));
            repaint();
        });
    }

    private int toVY(int screenY) {
        return (getHeight() > 0) ? (int)(screenY * VH / (double) getHeight()) : screenY;
    }

    private int hitRow(int vy) {
        if (scores == null || vy < LIST_Y) return -1;
        int listVy = (int)(vy - LIST_Y + scrollY);
        int idx = listVy / ROW_H;
        if (idx >= 0 && idx < scores.size() - 3) {
            return idx + 3; // +3 because rank 1,2,3 are on podium
        }
        return -1;
    }

    private void refreshAsync() {
        new Thread(() -> {
            List<LeaderboardManager.PlayerScore> loaded = LeaderboardManager.loadScores();
            SwingUtilities.invokeLater(() -> { scores = loaded; cacheAvatars(); repaint(); });
        }).start();
    }

    private void cacheAvatars() {
        if (scores == null) return;
        for (LeaderboardManager.PlayerScore ps : scores) {
            String key = ps.avatarPath;
            if (key != null && !avatarCache.containsKey(key)) {
                BufferedImage img = loadAvatar(key);
                if (img != null) avatarCache.put(key, img);
            }
        }
    }

    private BufferedImage loadAvatar(String folder) {
        String[] paths = {
            "/assets/ui/icons/avatar_" + folder + ".png",
            "/assets/player/" + folder + "/down/down_1.png",
            "/assets/player/" + folder + "/down_1.png"
        };
        for (String p : paths) {
            try {
                InputStream is = getClass().getResourceAsStream(p);
                if (is != null) return ImageIO.read(is);
            } catch (Exception ignored) {}
        }
        return null;
    }

    // ── Paint ────────────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        BufferedImage buf = new BufferedImage(VW, VH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = buf.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBG(g2);
        drawParticles(g2);
        drawTitle(g2);

        if (scores == null || scores.isEmpty()) {
            drawEmpty(g2);
        } else {
            // Apply podium slide up animation
            Graphics2D podiumG = (Graphics2D) g2.create();
            float pyOffset = (1f - podiumAnim) * 80f;
            podiumG.translate(0, pyOffset);
            podiumG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, podiumAnim));
            drawPodium(podiumG);
            podiumG.dispose();

            if (scores.size() > 3) {
                Graphics2D listG = (Graphics2D) g2.create();
                float lyOffset = (1f - listAnim) * 50f;
                listG.translate(0, lyOffset);
                listG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, listAnim));
                drawList(listG);
                listG.dispose();
            }
        }

        // Screen Fade overlay
        if (fadeAlpha < 1f) {
            g2.setColor(new Color(0, 0, 0, (int)((1f - fadeAlpha) * 255)));
            g2.fillRect(0, 0, VW, VH);
        }
        g2.dispose();

        Graphics2D gs = (Graphics2D) g;
        gs.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        gs.drawImage(buf, 0, 0, getWidth(), getHeight(), null);
    }

    // ── Background ───────────────────────────────────────────────────────────
    private void drawBG(Graphics2D g) {
        // Base fill - dark red black
        g.setColor(BG);
        g.fillRect(0, 0, VW, VH);

        // Radial Vignette
        RadialGradientPaint vp = new RadialGradientPaint(
            VW/2f, VH/2f, VW*0.8f,
            new float[]{0f, 1f},
            new Color[]{new Color(50, 5, 5, 40), new Color(0, 0, 0, 230)}
        );
        g.setPaint(vp);
        g.fillRect(0, 0, VW, VH);

        // Thin Grid
        g.setColor(new Color(255, 0, 0, 5));
        for (int x = 0; x < VW; x += 30) g.drawLine(x, 0, x, VH);
        for (int y = 0; y < VH; y += 30) g.drawLine(0, y, VW, y);

        // Scanlines
        g.setColor(new Color(0, 0, 0, 30));
        for (int y = 0; y < VH; y += 2) g.drawLine(0, y, VW, y);
    }
    
    private void drawParticles(Graphics2D g) {
        for (Particle p : particles) {
            g.setColor(new Color(255, 100, 100, p.alpha));
            g.fillOval((int)p.x, (int)p.y, (int)p.size, (int)p.size);
        }
    }

    // ── Title ────────────────────────────────────────────────────────────────
    private void drawTitle(Graphics2D g) {
        String t = "HALL OF SURVIVORS";
        g.setFont(px(32));
        FontMetrics fm = g.getFontMetrics();
        int tx = (VW - fm.stringWidth(t)) / 2;
        int ty = 54;

        float sinPulse = (float)Math.sin(pulseTime) * 0.5f + 0.5f; // 0 to 1

        // Glow
        for (int r = 12; r >= 1; r -= 2) {
            int alpha = (int)(15 + 10 * sinPulse);
            g.setColor(new Color(200, 20, 20, alpha));
            g.drawString(t, tx - r/2, ty + r/3);
            g.drawString(t, tx + r/2, ty + r/3);
        }
        
        // Shadow
        g.setColor(new Color(0, 0, 0, 220));
        g.drawString(t, tx + 2, ty + 2);
        
        // Main text (pale pinkish)
        g.setColor(TEXT);
        g.drawString(t, tx, ty);

        // Decorative line
        int lw = 380;
        GradientPaint lp = new GradientPaint(VW/2-lw/2, 0, new Color(200,20,20,0),
                                              VW/2, 0, new Color(200,20,20,200));
        g.setPaint(lp);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(VW/2 - lw/2, ty+14, VW/2, ty+14);
        GradientPaint rp = new GradientPaint(VW/2, 0, new Color(200,20,20,200),
                                              VW/2+lw/2, 0, new Color(200,20,20,0));
        g.setPaint(rp);
        g.drawLine(VW/2, ty+14, VW/2+lw/2, ty+14);
        
        // Diamonds
        g.setColor(ACCENT);
        int[] dx = {VW/2 - lw/2 - 10, VW/2 + lw/2 + 10};
        for (int x : dx) {
            Polygon p = new Polygon();
            p.addPoint(x, ty+14 - 6);
            p.addPoint(x+6, ty+14);
            p.addPoint(x, ty+14 + 6);
            p.addPoint(x-6, ty+14);
            g.fillPolygon(p);
        }
    }

    // ── Empty State ──────────────────────────────────────────────────────────
    private void drawEmpty(Graphics2D g) {
        g.setFont(px(16));
        String s = "NO SURVIVORS YET...";
        g.setColor(MUTED);
        g.drawString(s, (VW - g.getFontMetrics().stringWidth(s)) / 2, VH / 2);
    }

    // ── Podium (Quizizz Style) ───────────────────────────────────────────────
    private void drawPodium(Graphics2D g) {
        int baseY  = 380;
        int blockW = 145; // Wider for more modern look
        int gap    = 12;
        int cx     = VW / 2;

        int h1 = 120, h2 = 80, h3 = 55;

        int x1 = cx - blockW / 2;
        int x2 = x1 - blockW - gap;
        int x3 = x1 + blockW + gap;

        LeaderboardManager.PlayerScore p1 = scores.size() > 0 ? scores.get(0) : null;
        LeaderboardManager.PlayerScore p2 = scores.size() > 1 ? scores.get(1) : null;
        LeaderboardManager.PlayerScore p3 = scores.size() > 2 ? scores.get(2) : null;

        float sinPulse = (float)Math.sin(pulseTime * 1.5f) * 0.5f + 0.5f;

        // Draw ambient glow behind entire podium
        drawGlow(g, cx, baseY - 50, 160, new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), (int)(30 + 15 * sinPulse)));

        // Draw blocks + cards
        drawPodiumBlock(g, x2, baseY - h2, blockW, h2, SILVER, "2", p2);
        drawPodiumBlock(g, x3, baseY - h3, blockW, h3, BRONZE, "3", p3);
        // Rank 1 is rendered last so it's in front
        drawPodiumBlock(g, x1, baseY - h1, blockW, h1, GOLD,   "1", p1);

        // Draw player info above
        if (p2 != null) drawPodiumCard(g, x2, baseY - h2, blockW, p2, SILVER, 58, 2);
        if (p3 != null) drawPodiumCard(g, x3, baseY - h3, blockW, p3, BRONZE, 54, 3);
        if (p1 != null) drawPodiumCard(g, x1, baseY - h1, blockW, p1, GOLD,   74, 1); // Biggest avatar
    }

    private void drawPodiumBlock(Graphics2D g, int x, int y, int w, int h, Color color, String rank, LeaderboardManager.PlayerScore ps) {
        // Modern 3D/Cinematic gradient block
        GradientPaint gp = new GradientPaint(x, y, new Color(color.getRed(), color.getGreen(), color.getBlue(), 60),
                                             x, y + h, new Color(10, 2, 2, 230));
        g.setPaint(gp);
        g.fillRoundRect(x, y, w, h, 12, 12);
        
        // Top highlight
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120));
        g.setStroke(new BasicStroke(2f));
        g.drawLine(x+6, y+1, x+w-6, y+1);

        // Block border
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(x, y, w, h, 12, 12);

        // Inner rank number
        g.setFont(px(40));
        String label = rank;
        FontMetrics fm = g.getFontMetrics();
        // Shadow
        g.setColor(new Color(0,0,0,150));
        g.drawString(label, x + (w - fm.stringWidth(label))/2 + 2, y + h/2 + 16 + 2);
        // Text
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 160));
        g.drawString(label, x + (w - fm.stringWidth(label))/2, y + h/2 + 16);
    }

    private void drawPodiumCard(Graphics2D g, int blockX, int blockY, int blockW,
                                 LeaderboardManager.PlayerScore ps, Color color, int avatarSz, int rank) {
        int cx = blockX + blockW / 2;
        int cardY = blockY - 100 - (rank == 1 ? 20 : 0);

        // Avatar Hexagon/Circle
        BufferedImage av = avatarCache.get(ps.avatarPath);
        int ax = cx - avatarSz/2;
        int ay = cardY;
        
        // Avatar Glow
        drawGlow(g, cx, ay + avatarSz/2, avatarSz, new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));

        if (av != null) {
            Shape oldClip = g.getClip();
            g.setClip(new java.awt.geom.Ellipse2D.Float(ax, ay, avatarSz, avatarSz));
            g.drawImage(av, ax, ay, avatarSz, avatarSz, null);
            g.setClip(oldClip);
            
            g.setColor(color);
            g.setStroke(new BasicStroke(rank == 1 ? 3f : 2f));
            g.drawOval(ax, ay, avatarSz, avatarSz);
        } else {
            g.setColor(new Color(30, 10, 10));
            g.fillOval(ax, ay, avatarSz, avatarSz);
            g.setColor(color);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(ax, ay, avatarSz, avatarSz);
            g.setFont(px(18));
            String fl = ps.name.substring(0, 1).toUpperCase();
            g.drawString(fl, cx - g.getFontMetrics().stringWidth(fl)/2, ay + avatarSz/2 + 6);
        }

        // Rank Badge overlapping avatar
        g.setColor(color);
        g.fillRoundRect(cx - 12, ay + avatarSz - 8, 24, 16, 6, 6);
        g.setColor(Color.BLACK);
        g.setFont(px(10));
        g.drawString("#"+rank, cx - g.getFontMetrics().stringWidth("#"+rank)/2, ay + avatarSz + 3);

        // Name
        int iy = ay + avatarSz + 24;
        g.setFont(px(rank == 1 ? 14 : 12));
        String name = ps.name.length() > 11 ? ps.name.substring(0, 9) + ".." : ps.name;
        FontMetrics fm = g.getFontMetrics();
        g.setColor(TEXT);
        // text shadow
        g.drawString(name, cx - fm.stringWidth(name)/2, iy);

        // Score
        iy += 18;
        g.setFont(px(14));
        String sc = ps.score + " PTS";
        fm = g.getFontMetrics();
        g.setColor(color);
        g.drawString(sc, cx - fm.stringWidth(sc)/2, iy);

        // Level
        iy += 18;
        String lvl = "LVL " + ps.level;
        g.setFont(px(9));
        fm = g.getFontMetrics();
        int bw = fm.stringWidth(lvl) + 12, bh = 16;
        int bx = cx - bw/2;
        g.setColor(new Color(20, 5, 5, 180));
        g.fillRoundRect(bx, iy - 12, bw, bh, 6, 6);
        g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 120));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(bx, iy - 12, bw, bh, 6, 6);
        g.setColor(color);
        g.drawString(lvl, bx + 6, iy);
    }

    // ── Rank List (4th and below) ─────────────────────────────────────────────
    private void drawList(Graphics2D g) {
        // Header
        g.setFont(px(11));
        g.setColor(MUTED);
        g.drawString("RANK", LIST_X + 12, LIST_Y - 12);
        g.drawString("PLAYER", LIST_X + 94, LIST_Y - 12);
        g.drawString("SCORE", LIST_X + 350, LIST_Y - 12);
        g.drawString("LVL", LIST_X + 500, LIST_Y - 12);
        g.drawString("BOOKS", LIST_X + 650, LIST_Y - 12);
        
        g.setColor(new Color(150, 40, 40, 80));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(LIST_X, LIST_Y - 6, LIST_X + LIST_W, LIST_Y - 6);

        // Clip area for scrolling
        Shape oldClip = g.getClip();
        g.clipRect(LIST_X - 5, LIST_Y, LIST_W + 10, VH - LIST_Y);

        for (int i = 3; i < scores.size(); i++) {
            LeaderboardManager.PlayerScore ps = scores.get(i);
            int ry = LIST_Y + (i - 3) * ROW_H - (int)scrollY;
            
            // Skip rendering if outside clip
            if (ry + ROW_H < LIST_Y || ry > VH) continue;

            boolean hovered = (i == hoveredRow);

            // Row bg
            g.setColor(hovered ? ROW_HOVER : ROW_N);
            g.fillRoundRect(LIST_X, ry + 3, LIST_W, ROW_H - 6, 8, 8);
            
            if (hovered) {
                g.setColor(ACCENT);
                g.setStroke(new BasicStroke(1.2f));
                g.drawRoundRect(LIST_X, ry + 3, LIST_W, ROW_H - 6, 8, 8);
                // Hover glow
                g.setColor(new Color(200, 20, 30, 15));
                g.fillRoundRect(LIST_X, ry + 3, LIST_W, ROW_H - 6, 8, 8);
            } else {
                g.setColor(new Color(100, 20, 20, 50));
                g.setStroke(new BasicStroke(1f));
                g.drawRoundRect(LIST_X, ry + 3, LIST_W, ROW_H - 6, 8, 8);
            }

            int ty = ry + ROW_H/2 + 4;

            // Rank number
            g.setFont(px(14));
            String rk = "#" + (i + 1);
            g.setColor(hovered ? TEXT : MUTED);
            g.drawString(rk, LIST_X + 12, ty);

            // Mini avatar
            int avSz = 26;
            int avX = LIST_X + 54, avY = ry + (ROW_H - avSz)/2;
            BufferedImage av = avatarCache.get(ps.avatarPath);
            if (av != null) {
                Shape clip = g.getClip();
                g.setClip(new java.awt.geom.Ellipse2D.Float(avX, avY, avSz, avSz));
                g.drawImage(av, avX, avY, avSz, avSz, null);
                g.setClip(clip);
                g.setColor(ACCENT);
                g.setStroke(new BasicStroke(1.5f));
                g.drawOval(avX, avY, avSz, avSz);
            } else {
                g.setColor(new Color(40, 10, 10));
                g.fillOval(avX, avY, avSz, avSz);
                g.setColor(MUTED);
                g.setFont(px(10));
                g.drawString(ps.name.substring(0,1).toUpperCase(), avX + 9, avY + 17);
            }

            // Name
            g.setFont(px(12));
            g.setColor(hovered ? Color.WHITE : TEXT);
            String nm = ps.name.length() > 14 ? ps.name.substring(0,12)+".." : ps.name;
            g.drawString(nm, LIST_X + 94, ty);

            // Score
            g.setFont(px(13));
            g.setColor(GOLD);
            g.drawString(String.valueOf(ps.score), LIST_X + 350, ty);

            // Level
            g.setFont(px(12));
            g.setColor(new Color(250, 150, 150));
            g.drawString("LV" + ps.level, LIST_X + 500, ty);

            // Books
            g.setColor(new Color(200, 200, 200));
            g.drawString("" + ps.booksCollected, LIST_X + 650, ty);

            // Time and Status removed per request
        }
        
        g.setClip(oldClip);
        
        // Scrollbar indicator
        if (scores.size() > 3) {
            int maxRows = scores.size() - 3;
            double maxScroll = Math.max(0, (maxRows * ROW_H) - (VH - LIST_Y - 20));
            if (maxScroll > 0) {
                int barH = (int)((VH - LIST_Y) * ((VH - LIST_Y) / (double)(maxRows * ROW_H)));
                barH = Math.max(20, barH);
                int barY = LIST_Y + (int)((scrollY / maxScroll) * (VH - LIST_Y - barH));
                g.setColor(new Color(100, 20, 20, 100));
                g.fillRoundRect(LIST_X + LIST_W + 5, barY, 4, barH, 4, 4);
            }
        }
    }

    // ── Glow helper ──────────────────────────────────────────────────────────
    private void drawGlow(Graphics2D g, int cx, int cy, int r, Color c) {
        float[] dist = {0f, 1f};
        Color[] cols = {c, new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)};
        RadialGradientPaint rg = new RadialGradientPaint(cx, cy, r, dist, cols);
        g.setPaint(rg);
        g.fillOval(cx - r, cy - r, r*2, r*2);
    }
}
