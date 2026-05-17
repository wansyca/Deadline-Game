package com.deadline.ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.deadline.audio.SoundManager;
import com.deadline.main.Main;

/**
 * DashboardPanel — Main menu of "23:59".
 *
 * Visual language is identical to SplashScreen so the transition feels seamless:
 *  - Same dark red-black gradient background (no extra bg image overlay needed)
 *  - 100 white/grey ash particles drifting slowly downward
 *  - Edge vignette for cinematic depth
 *  - Existing bg.png drawn first for game-world flavour, then dark tint + particles on top
 */
public class DashboardPanel extends JPanel {

    // ─── Assets ─────────────────────────────────────────────────────────────
    private Image bgImage;
    private Image titleImage;
    private Image taglineImage;
    private ImageButton start, leaderboard, exit;

    private static final float TITLE_SCALE   = 0.75f;
    private static final float TAGLINE_SCALE = 0.25f;

    private static final int BTN_WIDTH  = 250;
    private static final int BTN_HEIGHT = 80;

    // (Background is bg.png — same as before, rendered at full opacity)

    // ─── Particles ──────────────────────────────────────────────────────────
    private static final int   PARTICLE_COUNT = 100;
    private float[] px, py, pspdX, pspdY, psize;
    private int[]   palpha;
    private boolean particlesInited = false;

    // ─── Animation timer ────────────────────────────────────────────────────
    private Timer particleTimer;

    // ─── Scene fade-in (for smooth entry from splash) ───────────────────────
    private float sceneAlpha  = 0f;
    private long  showTime    = -1;
    private static final int FADE_IN_MS = 600;

    public DashboardPanel() {
        setLayout(null);
        setOpaque(false);

        // Load assets
        bgImage     = loadImage("/assets/ui/panels/bg.png");
        titleImage  = loadImage("/assets/ui/panels/judul.png");
        taglineImage= loadImage("/assets/ui/panels/tagline.png");

        // Buttons
        start = new ImageButton(
            "/assets/ui/buttons/btn_start_normal.png",
            "/assets/ui/buttons/btn_start_hover.png",
            "/assets/ui/buttons/btn_start_pressed.png",
            BTN_WIDTH, BTN_HEIGHT
        );
        start.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.switchPage(Main.INPUT_PLAYER);
        });
        add(start);

        leaderboard = new ImageButton(
            "/assets/ui/buttons/btn_laeder_normal.png",
            "/assets/ui/buttons/btn_laeder_hover.png",
            "/assets/ui/buttons/btn_laeder_pressed.png",
            BTN_WIDTH, BTN_HEIGHT
        );
        leaderboard.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.goToLeaderboardWithLoading();
        });
        add(leaderboard);

        exit = new ImageButton(
            "/assets/ui/buttons/btn_exit_normal.png",
            "/assets/ui/buttons/btn_exit_hover.png",
            "/assets/ui/buttons/btn_exit_pressed.png",
            BTN_WIDTH, BTN_HEIGHT
        );
        exit.addActionListener(e -> {
            SoundManager.playClickSound();
            System.exit(0);
        });
        add(exit);

        // Responsive layout
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent evt) {
                layoutComponents();
            }
        });
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Panel lifecycle — start/stop particle timer
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void addNotify() {
        super.addNotify();
        showTime = System.currentTimeMillis();
        initParticles();
        startParticleTimer();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (particleTimer != null) {
            particleTimer.stop();
            particleTimer = null;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Particles
    // ────────────────────────────────────────────────────────────────────────

    private void initParticles() {
        int sw = Math.max(getWidth(),  800);
        int sh = Math.max(getHeight(), 600);

        px     = new float[PARTICLE_COUNT];
        py     = new float[PARTICLE_COUNT];
        pspdX  = new float[PARTICLE_COUNT];
        pspdY  = new float[PARTICLE_COUNT];
        psize  = new float[PARTICLE_COUNT];
        palpha = new int  [PARTICLE_COUNT];

        Random rnd = new Random();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            resetParticle(i, sw, sh, rnd, true);
        }
        particlesInited = true;
    }

    private void resetParticle(int i, int sw, int sh, Random rnd, boolean scattered) {
        px[i]    = rnd.nextFloat() * sw;
        py[i]    = scattered ? rnd.nextFloat() * sh : -rnd.nextFloat() * 30f;
        pspdX[i] = (rnd.nextFloat() - 0.5f) * 0.35f;
        pspdY[i] = 0.25f + rnd.nextFloat() * 0.55f;
        psize[i] = 1.0f + rnd.nextFloat() * 2.0f;
        palpha[i] = 55 + rnd.nextInt(100);
    }

    private void startParticleTimer() {
        if (particleTimer != null) particleTimer.stop();
        particleTimer = new Timer(16, e -> {
            if (!particlesInited) return;
            int sw = getWidth();
            int sh = getHeight();
            Random rnd = new Random();
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                px[i] += pspdX[i];
                py[i] += pspdY[i];
                if (py[i] > sh + 5) resetParticle(i, sw, sh, rnd, false);
                if (px[i] < -5 || px[i] > sw + 5) px[i] = rnd.nextFloat() * sw;
            }

            // Fade-in progress
            if (showTime > 0) {
                long elapsed = System.currentTimeMillis() - showTime;
                sceneAlpha = Math.min(1f, (float) elapsed / FADE_IN_MS);
            }

            repaint();
        });
        particleTimer.start();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Layout
    // ────────────────────────────────────────────────────────────────────────

    private void layoutComponents() {
        if (titleImage == null || taglineImage == null) return;
        int centerX = getWidth() / 2;

        int titleY = 80;
        int tw = (int) (titleImage.getWidth(null)    * TITLE_SCALE);
        int th = (int) (titleImage.getHeight(null)   * TITLE_SCALE);

        int taglineY = titleY + th - 22;
        int tgw = (int) (taglineImage.getWidth(null)  * TAGLINE_SCALE);
        int tgh = (int) (taglineImage.getHeight(null) * TAGLINE_SCALE);

        int buttonY  = taglineY + tgh + 60;
        int spacing  = 12;

        start.setBounds      (centerX - BTN_WIDTH / 2, buttonY,                          BTN_WIDTH, BTN_HEIGHT);
        leaderboard.setBounds(centerX - BTN_WIDTH / 2, buttonY + BTN_HEIGHT + spacing,   BTN_WIDTH, BTN_HEIGHT);
        exit.setBounds       (centerX - BTN_WIDTH / 2, buttonY + (BTN_HEIGHT + spacing) * 2, BTN_WIDTH, BTN_HEIGHT);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Painting
    // ────────────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_OFF);

        // ── 1. bg.png — tampil penuh persis seperti aslinya ─────────────────
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, w, h, null);
        }

        // ── 2. Partikel debu/abu jatuh perlahan ─────────────────────────────
        if (particlesInited && sceneAlpha > 0f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sceneAlpha * 0.85f));
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                g2.setColor(new Color(220, 210, 210, palpha[i]));
                int ps = (int) Math.max(1, psize[i]);
                g2.fillRect((int) px[i], (int) py[i], ps, ps);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── 3. Title & Tagline ───────────────────────────────────────────────
        if (titleImage != null) {
            int tw = (int) (titleImage.getWidth(null)  * TITLE_SCALE);
            int th = (int) (titleImage.getHeight(null) * TITLE_SCALE);
            int tx = (w - tw) / 2;
            int ty = 80;
            g2.drawImage(titleImage, tx, ty, tw, th, null);

            if (taglineImage != null) {
                int tgw = (int) (taglineImage.getWidth(null)  * TAGLINE_SCALE);
                int tgh = (int) (taglineImage.getHeight(null) * TAGLINE_SCALE);
                int tgx = (w - tgw) / 2;
                int tgy = ty + th - 22;
                g2.drawImage(taglineImage, tgx, tgy, tgw, tgh, null);
            }
        }

        // ── 4. Vignette tipis di tepi layar ─────────────────────────────────
        drawVignette(g2, w, h);

        g2.dispose();
    }

    private void drawVignette(Graphics2D g2, int w, int h) {
        RadialGradientPaint vignette = new RadialGradientPaint(
            w / 2f, h / 2f,
            (float) Math.sqrt(w * w + h * h) / 2f,
            new float[]{0.45f, 1.0f},
            new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 175)}
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, w, h);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────────────────

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) return new ImageIcon(url).getImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}