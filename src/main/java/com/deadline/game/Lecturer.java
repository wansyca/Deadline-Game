package com.deadline.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
            down1 = ImageIO.read(getClass().getResourceAsStream(path + "down_1.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream(path + "right_1.png"));
            
            // Fallback for frame 2: Use frame 1 if frame 2 is missing
            try { up2 = ImageIO.read(getClass().getResourceAsStream(path + "up_2.png")); } catch(Exception e) { up2 = up1; }
            try { down2 = ImageIO.read(getClass().getResourceAsStream(path + "down_2.png")); } catch(Exception e) { down2 = down1; }
            try { right2 = ImageIO.read(getClass().getResourceAsStream(path + "right_2.png")); } catch(Exception e) { right2 = right1; }
            
            if (up2 == null) up2 = up1;
            if (down2 == null) down2 = down1;
            if (right2 == null) right2 = right1;

            // Auto-flip for Left consistency
            left1 = flipImage(right1);
            left2 = flipImage(right2);
            
            System.out.println("✅ Loaded and fixed assets for Lecturer: " + folder);
        } catch (Exception e) {
            System.err.println("❌ Critical error loading lecturer assets: " + folder);
        }
    }

    private BufferedImage flipImage(BufferedImage src) {
        if (src == null) return null;
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = dest.createGraphics();
        g2.drawImage(src, w, 0, 0, h, 0, 0, w, h, null);
        g2.dispose();
        return dest;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return this.speed;
    }

    @Override
    public void update() {
        // AI Logic directly in update() to ensure it's called by GamePanel
        Player player = null;
        java.util.List<Lecturer> lecturers = null;

        // Since we need references, we'll keep updateAI for now but call it from update() 
        // IF we had a reference. However, GamePanel calls updateAI directly.
        // The real problem is update1() vs update().
    }

    public void updateAI(Player player, java.util.List<Lecturer> lecturers) {
        double dx = player.getX() - exactX;
        double dy = player.getY() - exactY;
        double dist = Math.sqrt(dx * dx + dy * dy);

        double targetDx = 0;
        double targetDy = 0;

        if (dist > 2) {
            targetDx = dx / dist;
            targetDy = dy / dist;
            isMoving = true;

            if (Math.abs(dx) >= Math.abs(dy)) {
                direction = (dx > 0) ? "right" : "left";
            } else {
                direction = (dy > 0) ? "down" : "up";
            }

            spriteCounter++;
            if (spriteCounter >= 8) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        } else {
            isMoving = false;
            spriteNum = 1;
            spriteCounter = 0;
        }

        exactX += targetDx * speed;
        exactY += targetDy * speed;

        // Separation
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
    public void draw(Graphics2D g) {
        BufferedImage image = null;
        switch (direction) {
            case "up": image = (spriteNum == 1) ? up1 : up2; break;
            case "down": image = (spriteNum == 1) ? down1 : down2; break;
            case "left": image = (spriteNum == 1) ? left1 : left2; break;
            case "right": image = (spriteNum == 1) ? right1 : right2; break;
        }

        if (image == null) return;

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        // NORMALIZE SIZE: Scale everything to 96px height regardless of PNG resolution
        int targetHeight = 96;
        int imgH = (image != null) ? image.getHeight() : 16;
        int imgW = (image != null) ? image.getWidth() : 16;
        
        double scaleRatio = (double) targetHeight / Math.max(1, imgH);
        int drawW = (int) (imgW * scaleRatio);
        int drawH = targetHeight;
        
        // Center horizontally in the 96px box, align feet to bottom
        int drawX = x + (width - drawW) / 2;
        int drawY = y + (height - drawH);

        // SHADOW
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x + 12, y + height - 10, width - 24, 8);

        if (image != null) {
            // BOUNCE EFFECT
            int finalY = drawY;
            if (isMoving && spriteNum == 2) {
                finalY += 4;
            }
            g.drawImage(image, drawX, finalY, drawW, drawH, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 20, y + 40, width - 40, height - 44);
    }

    public boolean intersects(Player p) {
        return getBounds().intersects(p.getBounds());
    }
}
