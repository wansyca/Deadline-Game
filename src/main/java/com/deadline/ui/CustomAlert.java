package com.deadline.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class CustomAlert {

    /**
     * Menampilkan alert peringatan (Warning)
     */
    public static void showWarning(Component parent, String title, String message) {
        showAlert(parent, title, message, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Menampilkan alert kesalahan (Error)
     */
    public static void showError(Component parent, String title, String message) {
        showAlert(parent, title, message, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Menampilkan alert informasi (Info)
     */
    public static void showInfo(Component parent, String title, String message) {
        showAlert(parent, title, message, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Menampilkan dialog konfirmasi dengan custom button
     */
    public static int showConfirm(Component parent, String title, String message, String[] options) {
        applyStyles();

        JPanel panel = createPanel(title, message);

        return JOptionPane.showOptionDialog(
                parent,
                panel,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[options.length - 1]);
    }

    private static void showAlert(Component parent, String title, String message, int messageType) {
        applyStyles();
        JPanel panel = createPanel(title, message);

        JOptionPane.showMessageDialog(
                parent,
                panel,
                title,
                JOptionPane.PLAIN_MESSAGE // Gunakan PLAIN agar icon default tidak ganggu layout custom kita
        );
    }

    private static Font pixelFont;

    private static void loadPixelFont() {
        if (pixelFont != null) return;
        try {
            java.io.InputStream is = CustomAlert.class.getResourceAsStream("/assets/font/pixel.ttf");
            if (is != null) {
                pixelFont = Font.createFont(Font.TRUETYPE_FONT, is);
            } else {
                pixelFont = new Font("Monospaced", Font.BOLD, 20);
            }
        } catch (Exception e) {
            pixelFont = new Font("Monospaced", Font.BOLD, 20);
        }
    }

    private static void applyStyles() {
        loadPixelFont();
        // Background Dialog & Panel
        UIManager.put("OptionPane.background", new Color(30, 30, 30));
        UIManager.put("Panel.background", new Color(30, 30, 30));

        // Teks Pesan (jika masih pake default label)
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.messageFont", pixelFont.deriveFont(Font.BOLD, 16));

        // Styling Button agar sesuai tema dark
        UIManager.put("Button.background", new Color(40, 40, 50));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", pixelFont.deriveFont(Font.BOLD, 14));
        UIManager.put("Button.border", BorderFactory.createLineBorder(Color.WHITE, 2));
    }

    private static JPanel createPanel(String titleText, String messageText) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.dispose();
            }
        };
        panel.setBackground(new Color(25, 25, 30));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(titleText.toUpperCase());
        title.setForeground(new Color(255, 0, 0));
        title.setFont(pixelFont.deriveFont(Font.BOLD, 22));
        title.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        // Styling message dengan HTML agar bisa ganti baris dan rata tengah
        JLabel msg = new JLabel("<html><div style='text-align: center; width: 300px;'>"
                + messageText.replace("\n", "<br>") + "</div></html>");
        msg.setForeground(Color.WHITE);
        msg.setFont(pixelFont.deriveFont(Font.BOLD, 16));
        msg.setAlignmentX(JLabel.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalStrut(15));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));
        panel.add(msg);
        panel.add(Box.createVerticalStrut(15));

        return panel;
    }
}
