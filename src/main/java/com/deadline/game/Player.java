package com.deadline.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Player extends GameObject {

    private String name = "Mahasiswa";
    private String avatarFolder; // cewe or cowo

    private BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    private String direction = "down";
    private int spriteCounter = 0;
    private int spriteNum = 1;

    private double prevExactX, prevExactY;
    private double exactX, exactY;
    private double velX = 0, velY = 0;
    private int dX, dY;

    private int carriedAssignments = 0;
    private final int MAX_CARRY = 3;
    private int collectedBooks = 0;
    private boolean isMoving = false;

    // Size configuration
    private static final int SCALE = 6;
    private static final int TILE_SIZE = 16 * SCALE; // 96x96

    public Player(int x, int y) {
        super(x, y, TILE_SIZE, TILE_SIZE);
        this.exactX = x;
        this.exactY = y;
        this.prevExactX = x;
        this.prevExactY = y;
    }

    public void setAvatar(String folder) {
        this.avatarFolder = folder;
        loadImages();
    }

    private void loadImages() {
        try {
            String path = "/assets/player/" + avatarFolder + "/";
            
            up1 = ImageIO.read(getClass().getResourceAsStream(path + "up_1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream(path + "up_2.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream(path + "down_1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream(path + "down_2.png"));
            
            // For Right
            right1 = ImageIO.read(getClass().getResourceAsStream(path + "right_1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream(path + "right_2.png"));
            
            // For Left: GUARANTEE consistency by flipping the right sprites
            // This prevents the character from 'changing' if left_1.png is a different asset.
            left1 = flipImage(right1);
            left2 = flipImage(right2);
            
            System.out.println("✅ Loaded and mirrored assets for: " + avatarFolder);
        } catch (Exception e) {
            System.err.println("❌ Critical error loading assets: " + avatarFolder);
            e.printStackTrace();
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

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void setX(int x) {
        super.setX(x);
        this.exactX = x;
    }

    @Override
    public void setY(int y) {
        super.setY(y);
        this.exactY = y;
    }

    public void setDirection(int dx, int dy) {
        this.dX = dx;
        this.dY = dy;
    }

    @Override
    public void update() {
        double speedLimit = 6.0;

        double inputX = dX;
        double inputY = dY;
        if (dX != 0 && dY != 0) {
            double length = Math.sqrt(dX * dX + dY * dY);
            inputX = dX / length;
            inputY = dY / length;
        }

        double targetVelX = inputX * speedLimit;
        double targetVelY = inputY * speedLimit;

        velX += (targetVelX - velX) * 0.3;
        velY += (targetVelY - velY) * 0.3;

        double speed = Math.sqrt(velX * velX + velY * velY);
        if (speed > 0.2) { // More sensitive movement detection
            isMoving = true;
            
            // Determine direction - prioritizing horizontal for 'right/left' feel
            if (Math.abs(velX) >= Math.abs(velY)) {
                direction = (velX > 0) ? "right" : "left";
            } else {
                direction = (velY > 0) ? "down" : "up";
            }

            // Animation logic (8 ticks for 'mulus' feel)
            spriteCounter++;
            if (spriteCounter >= 8) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        } else {
            isMoving = false;
            spriteNum = 1; // IDLE
            spriteCounter = 0;
            velX = 0;
            velY = 0;
        }
    }

    public void applyMoveX() {
        prevExactX = exactX;
        exactX += velX;
        x = (int) Math.round(exactX);
    }

    public void applyMoveY() {
        prevExactY = exactY;
        exactY += velY;
        y = (int) Math.round(exactY);
    }

    public void rollbackX() {
        exactX = prevExactX;
        x = (int) Math.round(exactX);
        velX = 0;
    }

    public void rollbackY() {
        exactY = prevExactY;
        y = (int) Math.round(exactY);
        velY = 0;
    }

    @Override
    public Rectangle getBounds() {
        // Adjusted hitbox for 96x96 scale
        return new Rectangle(x + 20, y + 40, width - 40, height - 44);
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage image = null;

        switch (direction) {
            case "up":
                image = (spriteNum == 1) ? up1 : up2;
                break;
            case "down":
                image = (spriteNum == 1) ? down1 : down2;
                break;
            case "left":
                image = (spriteNum == 1) ? left1 : left2;
                break;
            case "right":
                image = (spriteNum == 1) ? right1 : right2;
                break;
        }

        if (image == null) return;

        // PIXEL RENDERING HINT
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

        // NAME TAG
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        int textWidth = g.getFontMetrics().stringWidth(name);
        g.drawString(name, x + (width - textWidth) / 2, y - 10);

        // ITEM HELD UI
        if (carriedAssignments > 0) {
            g.setColor(new Color(255, 215, 0));
            g.fillRoundRect(x + width - 12, y - 5, 18, 18, 5, 5);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Monospaced", Font.BOLD, 12));
            g.drawString("" + carriedAssignments, x + width - 8, y + 8);
        }
    }

    public boolean canCarryMore() {
        return carriedAssignments < MAX_CARRY;
    }

    public void collectAssignment() {
        carriedAssignments++;
    }

    public int getCarriedAssignments() {
        return carriedAssignments;
    }

    public void resetCarriedAssignments() {
        carriedAssignments = 0;
    }

    public int getCollectedBooks() {
        return collectedBooks;
    }

    public void incrementCollectedBooks() {
        collectedBooks++;
    }

    public void setCollectedBooks(int count) {
        this.collectedBooks = count;
    }

    public String getAvatar() {
        return avatarFolder;
    }
}