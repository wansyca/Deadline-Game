package com.deadline.model;

import java.awt.Color;
import java.awt.Graphics2D;

import com.deadline.game.GameObject;
import com.deadline.ui.PixelAssets;

public class Assignment extends GameObject {

    private int animTick = 0;

    public Assignment(int x, int y) {
        // Use 64x64 for the pixel art size
        super(x, y, 64, 64);
    }

    @Override
    public void update() {
        animTick++;
    }

    @Override
    public void draw(Graphics2D g) {
        // Floating animation
        double hover = Math.sin(animTick * 0.1) * 5;

        // Shadow
        g.setColor(new Color(0, 0, 0, 40));
        g.fillOval(x + 10, y + height - 5, width - 20, 8);

        // Orange Glow
        float[] dist = {0.0f, 0.8f, 1.0f};
        Color[] colors = {new Color(255, 180, 0, 220), new Color(255, 120, 0, 100), new Color(255, 100, 0, 0)};
        java.awt.RadialGradientPaint rgp = new java.awt.RadialGradientPaint(
            new java.awt.geom.Point2D.Double(x + width/2, y + height/2 + hover), 
            (float)width * 1.5f, dist, colors);
        g.setPaint(rgp);
        g.fillOval(x - width/2, (int)(y - height/2 + hover), width * 2, height * 2);

        // Draw Pixel Book
        g.drawImage(PixelAssets.imgBook, x, (int) (y + hover), width, height, null);
    }
}
