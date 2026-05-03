package com.deadline.ui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * A custom JButton that uses images for its states (normal, hover, pressed).
 * Optimized for pixel art using NEAREST_NEIGHBOR interpolation.
 */
public class ImageButton extends JButton {
    private ImageIcon normalIcon;
    private ImageIcon hoverIcon;
    private ImageIcon pressedIcon;

    public ImageButton(String normalPath, String hoverPath, String pressedPath, float scale) {
        init(normalPath, hoverPath, pressedPath, -1, -1, scale);
    }

    public ImageButton(String normalPath, String hoverPath, String pressedPath, int width, int height) {
        init(normalPath, hoverPath, pressedPath, width, height, 1.0f);
    }

    private void init(String normalPath, String hoverPath, String pressedPath, int w, int h, float scale) {
        this.normalIcon = loadIcon(normalPath, w, h, scale);
        this.hoverIcon = loadIcon(hoverPath, w, h, scale);
        this.pressedIcon = loadIcon(pressedPath, w, h, scale);

        setIcon(normalIcon);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (hoverIcon != null) setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (normalIcon != null) setIcon(normalIcon);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (pressedIcon != null) setIcon(pressedIcon);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (getBounds().contains(e.getPoint())) {
                    if (hoverIcon != null) setIcon(hoverIcon);
                } else {
                    if (normalIcon != null) setIcon(normalIcon);
                }
            }
        });
    }

    private ImageIcon loadIcon(String path, int targetW, int targetH, float scale) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) {
                System.err.println("❌ Asset not found: " + path);
                return null;
            }
            ImageIcon icon = new ImageIcon(url);
            int w = (targetW > 0) ? targetW : (int) (icon.getIconWidth() * scale);
            int h = (targetH > 0) ? targetH : (int) (icon.getIconHeight() * scale);

            java.awt.image.BufferedImage scaledImg = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = scaledImg.createGraphics();
            
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            
            g2.drawImage(icon.getImage(), 0, 0, w, h, null);
            g2.dispose();

            return new ImageIcon(scaledImg);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (normalIcon != null) {
            return new Dimension(normalIcon.getIconWidth(), normalIcon.getIconHeight());
        }
        return super.getPreferredSize();
    }
}

