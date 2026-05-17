package com.deadline.ui;

import com.deadline.audio.SoundManager;
import com.deadline.main.Main;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * SplashScreen — cinematic horror intro screen for "23:59".
 *
 * Background = bg.png (identik dengan DashboardPanel) sehingga transisi terasa seamless.
 * Di atasnya: partikel debu putih jatuh perlahan, vignette tepi, logo fade-in halus.
 */
public class SplashScreen extends JPanel {

    // ─── Assets ─────────────────────────────────────────────────────────────
    private BufferedImage logo;
    private Image         bgImage;      // sama dengan bg.png di DashboardPanel
    private int targetW, targetH;

    // ─── Animation state ────────────────────────────────────────────────────
    private Timer animTimer;
    private long  startTime;

    /** Alpha keseluruhan scene: 0 = transparan, 1 = penuh */
    private float sceneAlpha = 0f;

    /** Zoom cinematic sangat halus pada logo: mulai 1.05 → berakhir 1.0 */
    private float logoScale  = 1.05f;

    // ─── Timing (ms) ────────────────────────────────────────────────────────
    private static final int FADE_IN_MS  = 1200;   // logo fade-in
    private static final int HOLD_MS     = 2200;   // hold di opacity penuh
    private static final int FADE_OUT_MS = 900;    // fade-out smooth ke Dashboard
    private static final int TOTAL_MS    = FADE_IN_MS + HOLD_MS + FADE_OUT_MS; // 4300ms

    // ─── Partikel debu/abu ───────────────────────────────────────────────────
    private static final int PARTICLE_COUNT = 120;
    private float[] px, py, pspdX, pspdY, psize;
    private int[]   palpha;
    private boolean particlesInited = false;

    public SplashScreen() {
        setBackground(Color.BLACK);   // fallback jika bg.png gagal load
        loadAssets();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Asset loading
    // ────────────────────────────────────────────────────────────────────────

    private void loadAssets() {
        // Logo universitas
        try {
            logo = ImageIO.read(getClass().getResourceAsStream("/assets/ui/panels/logo.png"));
        } catch (Exception e) {
            System.err.println("❌ Splash: logo.png tidak ditemukan");
        }

        // Background — sama persis dengan DashboardPanel
        try {
            java.net.URL url = getClass().getResource("/assets/ui/panels/bg.png");
            if (url != null) bgImage = new ImageIcon(url).getImage();
        } catch (Exception e) {
            System.err.println("❌ Splash: bg.png tidak ditemukan");
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Inisialisasi partikel & ukuran logo (setelah panel punya ukuran nyata)
    // ────────────────────────────────────────────────────────────────────────

    private void initScene() {
        int sw = getWidth();
        int sh = getHeight();
        if (sw <= 0 || sh <= 0) return;

        // Ukuran logo: 32% lebar layar
        if (logo != null) {
            double ratio = (double) logo.getWidth() / logo.getHeight();
            targetW = (int) (sw * 0.32);
            targetH = (int) (targetW / ratio);
        }

        // Init partikel
        px     = new float[PARTICLE_COUNT];
        py     = new float[PARTICLE_COUNT];
        pspdX  = new float[PARTICLE_COUNT];
        pspdY  = new float[PARTICLE_COUNT];
        psize  = new float[PARTICLE_COUNT];
        palpha = new int  [PARTICLE_COUNT];

        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            resetParticle(i, sw, sh, rnd, true);
        }
        particlesInited = true;

        startAnimation();
    }

    private void resetParticle(int i, int sw, int sh, java.util.Random rnd, boolean scattered) {
        px[i]    = rnd.nextFloat() * sw;
        py[i]    = scattered ? rnd.nextFloat() * sh : -rnd.nextFloat() * 40f;
        pspdX[i] = (rnd.nextFloat() - 0.5f) * 0.35f;    // sedikit drift horizontal
        pspdY[i] = 0.25f + rnd.nextFloat() * 0.55f;      // jatuh ke bawah perlahan
        psize[i] = 1.0f + rnd.nextFloat() * 2.0f;        // 1–3 px
        palpha[i] = 55 + rnd.nextInt(110);                // 55–165 (lembut)
    }

    private void updateParticles() {
        int sw = getWidth();
        int sh = getHeight();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            px[i] += pspdX[i];
            py[i] += pspdY[i];
            if (py[i] > sh + 5)              resetParticle(i, sw, sh, rnd, false);
            if (px[i] < -5 || px[i] > sw + 5) px[i] = rnd.nextFloat() * sw;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Loop animasi
    // ────────────────────────────────────────────────────────────────────────

    private void startAnimation() {
        startTime = System.currentTimeMillis();

        animTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - startTime;

            if (particlesInited) updateParticles();

            // Fase 1: Fade-in
            if (elapsed < FADE_IN_MS) {
                float t  = (float) elapsed / FADE_IN_MS;
                sceneAlpha = easeInOut(t);
                logoScale  = 1.05f - 0.05f * easeInOut(t);   // zoom: 1.05 → 1.0

            // Fase 2: Hold
            } else if (elapsed < FADE_IN_MS + HOLD_MS) {
                sceneAlpha = 1.0f;
                logoScale  = 1.0f;

            // Fase 3: Fade-out
            } else if (elapsed < TOTAL_MS) {
                float t    = (float) (elapsed - FADE_IN_MS - HOLD_MS) / FADE_OUT_MS;
                sceneAlpha = 1.0f - easeInOut(t);

            // Selesai → pindah ke Dashboard
            } else {
                ((Timer) e.getSource()).stop();
                Main.switchPage(Main.DASHBOARD);
                return;
            }

            repaint();
        });
        animTimer.start();
    }

    /** Cubic ease-in-out */
    private static float easeInOut(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Lifecycle
    // ────────────────────────────────────────────────────────────────────────

    @Override
    public void addNotify() {
        super.addNotify();
        SoundManager.playMenuMusic();   // mulai ambience segera saat splash tampil
    }

    // ────────────────────────────────────────────────────────────────────────
    //  Painting
    // ────────────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (logo == null) return;

        // Tunda init sampai panel punya ukuran nyata
        if (!particlesInited) {
            initScene();
            return;
        }

        int w = getWidth();
        int h = getHeight();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,  RenderingHints.VALUE_ANTIALIAS_OFF);

        // ── 1. bg.png — IDENTIK dengan DashboardPanel, tampil penuh ──────────
        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, w, h, null);
        } else {
            // fallback jika bg.png gagal load
            g2.setColor(new Color(10, 5, 5));
            g2.fillRect(0, 0, w, h);
        }

        // ── 2. Partikel debu/abu putih jatuh perlahan ────────────────────────
        if (particlesInited && sceneAlpha > 0.01f) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sceneAlpha * 0.85f));
            for (int i = 0; i < PARTICLE_COUNT; i++) {
                g2.setColor(new Color(220, 210, 210, palpha[i]));
                int ps = (int) Math.max(1, psize[i]);
                g2.fillRect((int) px[i], (int) py[i], ps, ps);
            }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── 3. Logo universitas — fade-in + zoom cinematic halus ─────────────
        if (sceneAlpha > 0.01f && targetW > 0) {
            int drawW = (int) (targetW * logoScale);
            int drawH = (int) (targetH * logoScale);
            int drawX = (w - drawW) / 2;
            int drawY = (h - drawH) / 2;

            // Soft glow merah di belakang logo (sangat redup, tidak dominan)
            long elapsed  = System.currentTimeMillis() - startTime;
            float pulse   = 0.5f + 0.5f * (float) Math.sin(elapsed / 1800.0);
            int glowAlpha = (int) (18 + 10 * pulse);
            int glowR     = (int) (drawW * 1.6);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RadialGradientPaint glow = new RadialGradientPaint(
                w / 2f, h / 2f, glowR / 2f,
                new float[]{0f, 1f},
                new Color[]{new Color(100, 0, 0, glowAlpha), new Color(0, 0, 0, 0)}
            );
            g2.setPaint(glow);
            g2.fillRect(0, 0, w, h);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            // Logo dengan sedikit fade transparency agar menyatu dengan bg
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, sceneAlpha * 0.90f));
            g2.drawImage(logo, drawX, drawY, drawW, drawH, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // ── 4. Vignette gelap tipis di tepi layar ────────────────────────────
        drawVignette(g2, w, h);
    }

    private void drawVignette(Graphics2D g2, int w, int h) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RadialGradientPaint vignette = new RadialGradientPaint(
            w / 2f, h / 2f,
            (float) Math.sqrt(w * w + h * h) / 2f,
            new float[]{0.45f, 1.0f},
            new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 160)}
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, w, h);
    }
}
