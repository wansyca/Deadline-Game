package com.deadline;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

/**
 * SoundManager handles sound effects playback for the game.
 */
public class SoundManager {
    public static void playClickSound() {
        playSound("/assets/sound/click.wav", "click sound", -15.0f);
    }

    public static void playBookSound() {
        playSound("/assets/sound/book.wav", "book collection sound", 0.0f);
    }

    public static void playGameOverSound() {
        playSound("/assets/sound/Gameover.wav", "game over sound", 0.0f);
    }

    /**
     * Internal helper to play sound effects using javax.sound.sampled.Clip.
     */
    private static void playSound(String path, String description, float volume) {
        try {
            // Load the sound file from resources
            InputStream is = SoundManager.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Sound file not found: " + path);
                return;
            }

            // Wrap in BufferedInputStream for better compatibility
            InputStream bis = new BufferedInputStream(is);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bis);

            // Get a clip resource (Always new clip to avoid shared volume state)
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Set volume using FloatControl
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume);
            } catch (IllegalArgumentException e) {
                System.err.println("Volume control not supported for " + description);
            }

            // Start playing the clip
            clip.start();

            // LineListener to close the clip after it finishes playing
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

        } catch (Exception e) {
            System.err.println("Error playing " + description + ": " + e.getMessage());
        }
    }
}
