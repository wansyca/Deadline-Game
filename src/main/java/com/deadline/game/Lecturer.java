package com.deadline.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.deadline.ui.PixelAssets;

public class Lecturer extends GameObject {

    private double speed;
    private double exactX, exactY;
    private int animTick = 0;
    private boolean facingRight = true;
    private int type; // 0 = Tua, 1 = Muda, 2 = Cewe (used for color variations if needed, or just
                      // behavior)

    public Lecturer(int x, int y, double speed, int type) {
        // Pixel grid size: 64x64
        super(x, y, 64, 64);
        this.speed = speed;
        this.exactX = x;
        this.exactY = y;
        this.type = type;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void updateAI(Player player, java.util.List<Lecturer> lecturers) {
        double dx = player.getX() - exactX;
        double dy = player.getY() - exactY;
        double dist = Math.sqrt(dx * dx + dy * dy);

        double targetDx = 0;
        double targetDy = 0;

        if (dist > 5) {
            targetDx = dx / dist;
            targetDy = dy / dist;

            animTick++;
            if (dx > 0)
                facingRight = true;
            else if (dx < 0)
                facingRight = false;
        } else {
            animTick = 0;
        }

        exactX += targetDx * speed;
        exactY += targetDy * speed;

        exactX += (Math.random() - 0.5) * 0.5;
        exactY += (Math.random() - 0.5) * 0.5;

        for (Lecturer other : lecturers) {
            if (other != this) {
                double diffX = this.exactX - other.exactX;
                double diffY = this.exactY - other.exactY;
                double distance = Math.sqrt(diffX * diffX + diffY * diffY);

                if (distance < 50) { // Reduced distance for 64x64 scale
                    exactX += diffX * 0.05;
                    exactY += diffY * 0.05;
                }
            }
        }

        this.x = (int) Math.round(exactX);
        this.y = (int) Math.round(exactY);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics2D g) {
        int frameIndex = (animTick / 5) % PixelAssets.imgLecturerWalk.length;
        BufferedImage frame = (animTick == 0) ? PixelAssets.imgLecturerIdle : PixelAssets.imgLecturerWalk[frameIndex];

        // SHADOW
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x + 10, y + height - 8, width - 20, 10);

        // DOSEN IMAGE
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x + width / 2, y + height / 2);

        if (!facingRight) {
            g2.scale(-1, 1);
        }

        // Slightly tint based on type (just an example of variation)
        g2.drawImage(frame, -width / 2, -height / 2, width, height, null);
        g2.dispose();
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 16, y + 16, width - 32, height - 16);
    }

    public boolean intersects(Player p) {
        return getBounds().intersects(p.getBounds());
    }
}
