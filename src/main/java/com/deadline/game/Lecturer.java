package com.deadline.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Lecturer extends GameObject {

    private int type;
    private double speed;
    private double exactX, exactY;
    
    private BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    private String direction = "down";
    private int spriteCounter = 0;
    private int spriteNum = 1;
    private boolean isMoving = false;

    // Size configuration
    private static final int SCALE = 6;
    private static final int TILE_SIZE = 16 * SCALE; // 96x96

    public Lecturer(int x, int y, double speed, int type) {
        super(x, y, TILE_SIZE, TILE_SIZE);
        this.speed = speed;
        this.type = type;
        this.exactX = x;
        this.exactY = y;
        loadImages();
    }

    private void loadImages() {
        String folder = "";
        switch (type) {
            case 0: folder = "dosen_tua"; break;
            case 1: folder = "domu_cowo"; break;
            case 2: folder = "dosen_cewe"; break;
            default: folder = "dosen_tua";
        }

        try {
            String path = "/assets/player/" + folder + "/";
            up1 = ImageIO.read(getClass().getResourceAsStream(path + "up_1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream(path + "up_2.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream(path + "down_1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream(path + "down_2.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream(path + "left_1.png"));
            try { left2 = ImageIO.read(getClass().getResourceAsStream(path + "left_2.png")); } catch (Exception e) { left2 = left1; }
            right1 = ImageIO.read(getClass().getResourceAsStream(path + "right_1.png"));
            try { right2 = ImageIO.read(getClass().getResourceAsStream(path + "right_2.png")); } catch (Exception e) { right2 = right1; }
        } catch (IOException | NullPointerException e) {
            System.err.println("❌ Failed to load lecturer assets for: " + folder);
        }
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
            isMoving = true;

            // Determine direction
            if (Math.abs(dx) > Math.abs(dy)) {
                direction = (dx > 0) ? "right" : "left";
            } else {
                direction = (dy > 0) ? "down" : "up";
            }

            // Animation
            spriteCounter++;
            if (spriteCounter > 15) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        } else {
            isMoving = false;
            spriteNum = 1;
        }

        exactX += targetDx * speed;
        exactY += targetDy * speed;

        // Separation logic
        for (Lecturer other : lecturers) {
            if (other != this) {
                double diffX = this.exactX - other.exactX;
                double diffY = this.exactY - other.exactY;
                double distance = Math.sqrt(diffX * diffX + diffY * diffY);

                if (distance < 70) {
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
        BufferedImage image = null;

        switch (direction) {
            case "up": image = (spriteNum == 1) ? up1 : up2; break;
            case "down": image = (spriteNum == 1) ? down1 : down2; break;
            case "left": image = (spriteNum == 1) ? left1 : left2; break;
            case "right": image = (spriteNum == 1) ? right1 : right2; break;
        }

        if (image == null) return;

        // PIXEL RENDERING HINT
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // SHADOW
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x + 8, y + height - 8, width - 16, 10);

        // DRAW
        g.drawImage(image, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 20, y + 40, width - 40, height - 44);
    }

    public boolean intersects(Player p) {
        return getBounds().intersects(p.getBounds());
    }
}
