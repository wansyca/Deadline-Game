package com.deadline.game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import com.deadline.ui.PixelAssets;

public class Player extends GameObject {

    private String name = "Mahasiswa";
    private String avatarPath;

    private double prevExactX, prevExactY;
    private double exactX, exactY;
    private double velX = 0, velY = 0;
    private int dX, dY;

    private int carriedAssignments = 0;
    private final int MAX_CARRY = 3;

    private int collectedBooks = 0;
    private int animTick = 0;
    private int direction = 0; // 0=Depan, 1=Belakang, 2=Kiri, 3=Kanan
    private boolean isMoving = false;

    public Player(int x, int y) {
        // Enlarge player to 64x64
        super(x, y, 64, 64);
        this.exactX = x;
        this.exactY = y;
        this.prevExactX = x;
        this.prevExactY = y;
    }

    public void setAvatar(String path) {
        this.avatarPath = path;
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
        dX = dx;
        dY = dy;
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
        if (speed > 0.5) {
            isMoving = true;
            animTick++;

            // Logic arah gerak
            if (Math.abs(velX) > Math.abs(velY)) {
                direction = (velX > 0) ? 3 : 2; // Kanan : Kiri
            } else {
                direction = (velY > 0) ? 0 : 1; // Depan : Belakang
            }
        } else {
            isMoving = false;
            // Keep last direction for idle
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
        // Adjusted hitbox for 64x64 scale
        return new Rectangle(x + 12, y + 12, width - 24, height - 16);
    }

    @Override
    public void draw(Graphics2D g) {
        BufferedImage frame = null;

        // Logic arah gerak (depan, belakang, kiri, kanan)
        switch (direction) {
            case 0:
                frame = PixelAssets.imgPlayerDepan;
                break;
            case 1:
                frame = PixelAssets.imgPlayerBelakang;
                break;
            case 2:
                frame = (isMoving && (animTick / 10) % 2 == 1) ? PixelAssets.imgPlayerJalanKiri
                        : PixelAssets.imgPlayerKiri;
                break;
            case 3:
                frame = (isMoving && (animTick / 10) % 2 == 1) ? PixelAssets.imgPlayerJalanKanan
                        : PixelAssets.imgPlayerKanan;
                break;
        }

        if (frame == null)
            frame = PixelAssets.imgPlayerDepan; // Fallback

        // SHADOW KECIL
        g.setColor(new Color(0, 0, 0, 50));
        g.fillOval(x + 10, y + height - 10, width - 20, 8);

        // Draw Player
        g.drawImage(frame, x, y, width, height, null);

        // NAME TAG
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        int textWidth = g.getFontMetrics().stringWidth(name);
        g.drawString(name, x + (width - textWidth) / 2, y - 5);

        // ITEM HELD
        if (carriedAssignments > 0) {
            g.setColor(new Color(241, 196, 15));
            g.fillRect(x + width - 15, y, 15, 15);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Monospaced", Font.BOLD, 12));
            g.drawString("" + carriedAssignments, x + width - 10, y + 12);
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

    public String getAvatarPath() {
        return avatarPath;
    }

    public String getAvatar() {
        return avatarPath;
    }
}