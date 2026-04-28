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
        g.fillOval(x + 16, y + height - 10, width - 32, 10);

        // Draw Pixel Book
        g.drawImage(PixelAssets.imgBook, x, (int) (y + hover), width, height, null);
    }
}
