package com.deadline.audio;

import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

/**
 * SoundManager handles sound effects playback for the game.
 */
public class SoundManager {
    public static void playClickSound() {
        playSound("/assets/sound/click.wav", "click sound", -15.0f);
    }

    public static void playBookSound() {
        playSound("/assets/sound/collect.wav", "book collection sound", 0.0f);
    }

    public static void playGameOverSound() {
        playSound("/assets/sound/Gameover.wav", "game over sound", 0.0f);
    }

    private static Clip backgroundClip;

    /**
     * Plays background music in a loop. If already playing, does nothing.
     */
    public static void playBackgroundMusic(String path, float volume) {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            return;
        }

        try {
            URL url = SoundManager.class.getResource(path);
            if (url == null) {
                // Fallback attempt without leading slash if using relative class loader
                url = SoundManager.class.getClassLoader().getResource(path.startsWith("/") ? path.substring(1) : path);
            }

            if (url == null) {
                System.err.println("Music file not found: " + path);
                return;
            }
            System.out.println("Playing background music from: " + url.getPath());

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(ais);

            FloatControl gainControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(volume);

            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
        } catch (Exception e) {
            System.err.println("Error playing music: " + e.getMessage());
        }
    }

    /**
     * Stops the background music with a smooth fade-out effect.
     */
    public static void stopBackgroundMusicWithFade() {
        if (backgroundClip == null || !backgroundClip.isRunning())
            return;

        new Thread(() -> {
            try {
                FloatControl gainControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
                float currentVolume = gainControl.getValue();
                for (float v = currentVolume; v > -60.0f; v -= 1.0f) {
                    if (backgroundClip == null)
                        break;
                    gainControl.setValue(v);
                    Thread.sleep(30);
                }
                if (backgroundClip != null) {
                    backgroundClip.stop();
                    backgroundClip.close();
                    backgroundClip = null;
                }
            } catch (Exception e) {
                if (backgroundClip != null) {
                    backgroundClip.stop();
                    backgroundClip.close();
                }
                backgroundClip = null;
            }
        }).start();
    }

    /**
     * Internal helper to play sound effects using javax.sound.sampled.Clip.
     */
    private static void playSound(String path, String description, float volume) {
        try {
            // Load the sound file from resources
            URL url = SoundManager.class.getResource(path);
            if (url == null) {
                // Silently return to avoid console spam for missing SFX
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(url);

            // Get a clip resource
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Set volume
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume);
            } catch (IllegalArgumentException e) {
                // Ignore if volume control not supported
            }

            clip.start();
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (Exception e) {
            // Log only real errors, not missing files
        }
    }
}
