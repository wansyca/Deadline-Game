package com.deadline.audio;

import java.net.URL;
import javax.sound.sampled.*;

/**
 * SoundManager — centralized audio controller for Deadline Horror Game.
 *
 * Sound files live in:  src/main/resources/sound/
 *   - hal&input.wav   → looping menu / splash / input-player BGM
 *   - click.wav        → UI button SFX
 *   - ambilbuku.wav    → book-collected SFX
 *   - Gameover.wav     → game-over sting (plays once, no loop)
 *
 * Volume design:
 *   BGM (menu)  ≈ 25 % → VOL_MENU = -12 dB  (soft ambience, not dominant)
 *   Click SFX   ≈ 60 % → VOL_CLICK = -5 dB
 *   Book SFX    ≈ 70 % → VOL_BOOK  =  0 dB
 *   Game-over   ≈ 100% → VOL_GAMEOVER = 0 dB
 *
 * Usage:
 *   SoundManager.playMenuMusic();        // from Splash / Dashboard / InputPlayer
 *   SoundManager.stopMenuMusicFade();    // when entering game
 *   SoundManager.playClickSound();       // every button press
 *   SoundManager.playBookSound();        // on book collect
 *   SoundManager.playGameOverSound();    // on game over
 *   SoundManager.stopAll();             // emergency full stop
 */
public class SoundManager {

    // ─── Resource paths ─────────────────────────────────────────────────────
    private static final String PATH_MENU     = "/sound/awal&input.wav";
    private static final String PATH_CLICK    = "/sound/click.wav";
    private static final String PATH_BOOK     = "/sound/ambilbuku.wav";
    private static final String PATH_GAMEOVER = "/sound/Gameover.wav";

    // ─── Volume defaults (dB) ────────────────────────────────────────────────
    // Menu BGM dinaikkan agar terdengar jelas di halaman awal & input player.
    // SFX tetap keras agar feedback player selalu jelas.
    private static final float VOL_MENU     = 0.0f;  // ~25% — soft horror ambience
    private static final float VOL_CLICK    =  -5.0f;  // ~60% — crisp UI feedback
    private static final float VOL_BOOK     =   -5.0f;  // ~70% — satisfying collect
    private static final float VOL_GAMEOVER =   0.0f;  // 100% — dramatic sting

    // ─── Background music clip (menu / input) ───────────────────────────────
    private static Clip bgClip   = null;
    private static String bgPath = null;  // track which file is loaded

    // ─── Fade thread reference (to cancel early) ────────────────────────────
    private static Thread fadeThread = null;

    // ────────────────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ────────────────────────────────────────────────────────────────────────

    /** Play (or resume) the menu BGM — looping. Safe to call repeatedly. */
    public static synchronized void playMenuMusic() {
        // Already playing the same track → do nothing
        if (bgClip != null && bgClip.isRunning() && PATH_MENU.equals(bgPath)) return;

        // Cancel any in-progress fade
        cancelFade();

        // Stop whatever was playing before
        stopBgClip();

        bgClip = loadClip(PATH_MENU, VOL_MENU);
        if (bgClip != null) {
            bgPath = PATH_MENU;
            bgClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgClip.start();
        }
    }

    /** Fade out menu BGM then stop. Call when entering gameplay. */
    public static void stopMenuMusicFade() {
        if (bgClip == null || !bgClip.isRunning()) return;

        final Clip target = bgClip;
        fadeThread = new Thread(() -> {
            try {
                FloatControl gain = (FloatControl) target.getControl(FloatControl.Type.MASTER_GAIN);
                float vol = gain.getValue();
                while (vol > gain.getMinimum() + 1f) {
                    if (Thread.currentThread().isInterrupted()) return;
                    vol -= 1.5f;
                    gain.setValue(Math.max(vol, gain.getMinimum()));
                    Thread.sleep(25);
                }
                target.stop();
                target.close();
            } catch (Exception e) {
                target.stop();
                target.close();
            }
        }, "BGM-Fade");
        fadeThread.setDaemon(true);
        fadeThread.start();

        bgClip = null;
        bgPath = null;
    }

    /** Stop all audio immediately (emergency stop). */
    public static synchronized void stopAll() {
        cancelFade();
        stopBgClip();
    }

    // ─── SFX shortcuts ──────────────────────────────────────────────────────

    /** Play UI click sound — very fast, non-blocking. */
    public static void playClickSound() {
        playSfx(PATH_CLICK, VOL_CLICK);
    }

    /** Play book-collected sound — instant, can overlap. */
    public static void playBookSound() {
        playSfx(PATH_BOOK, VOL_BOOK);
    }

    /**
     * Play game-over sting — stops all other audio first,
     * then plays Gameover.wav once with slight dramatic delay.
     */
    public static void playGameOverSound() {
        // Stop menu music hard (no fade — dramatic effect)
        stopAll();

        new Thread(() -> {
            try {
                Thread.sleep(200); // tiny breath before the sting
            } catch (InterruptedException ignored) {}
            playSfx(PATH_GAMEOVER, VOL_GAMEOVER);
        }, "GameOver-SFX").start();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  LEGACY COMPATIBILITY  (called from older code that hasn't been updated)
    // ────────────────────────────────────────────────────────────────────────

    /** @deprecated Use {@link #playMenuMusic()} instead. */
    @Deprecated
    public static void playBackgroundMusic(String ignoredPath, float ignoredVol) {
        playMenuMusic();
    }

    /** @deprecated Use {@link #stopMenuMusicFade()} instead. */
    @Deprecated
    public static void stopBackgroundMusicWithFade() {
        stopMenuMusicFade();
    }

    // ────────────────────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Play a one-shot SFX clip on a daemon thread so it never blocks the EDT.
     * Each call creates its own Clip instance so sounds can overlap freely.
     */
    private static void playSfx(String path, float volumeDb) {
        new Thread(() -> {
            Clip clip = loadClip(path, volumeDb);
            if (clip == null) return;
            clip.start();
            // Auto-close when done
            clip.addLineListener(ev -> {
                if (ev.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });
        }, "SFX").start();
    }

    /**
     * Load and configure a Clip from the classpath resource folder.
     * Returns null (silently) if the resource is missing — no console spam.
     */
    private static Clip loadClip(String resourcePath, float volumeDb) {
        try {
            URL url = SoundManager.class.getResource(resourcePath);
            if (url == null) {
                // Try without leading slash via class loader
                String rel = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                url = SoundManager.class.getClassLoader().getResource(rel);
            }
            if (url == null) {
                System.err.println("⚠ Sound not found: " + resourcePath);
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            try {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // Clamp to supported range
                float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb));
                gain.setValue(clamped);
            } catch (IllegalArgumentException ignored) {
                // Volume control not supported on this system — just play at default
            }

            return clip;
        } catch (Exception e) {
            System.err.println("⚠ Error loading sound [" + resourcePath + "]: " + e.getMessage());
            return null;
        }
    }

    private static synchronized void stopBgClip() {
        if (bgClip != null) {
            try {
                bgClip.stop();
                bgClip.close();
            } catch (Exception ignored) {}
            bgClip = null;
            bgPath = null;
        }
    }

    private static void cancelFade() {
        if (fadeThread != null && fadeThread.isAlive()) {
            fadeThread.interrupt();
            fadeThread = null;
        }
    }
}
