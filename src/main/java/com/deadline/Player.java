package com.deadline;

import java.awt.*;
import javax.swing.ImageIcon;

public class Player extends GameObject {

    private Image avatar;
    private String name = "Mahasiswa";

    private double prevExactX, prevExactY;
    private double exactX, exactY;
    private double velX = 0, velY = 0;
    private int dX, dY;

    private int carriedAssignments = 0;
    private final int MAX_CARRY = 3;

    private int collectedBooks = 0;
    private int animTick = 0;
    private boolean facingRight = true;

    public Player(int x, int y) {
        // 🔥 DIGEDEIN + PROPORSIONAL (160x160)
        super(x, y, 160, 160);
        this.exactX = x;
        this.exactY = y;
        this.prevExactX = x;
        this.prevExactY = y;
    }

    public void setAvatar(String path) {
        avatar = new ImageIcon(getClass().getResource(path)).getImage();
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

        if (dx > 0) facingRight = true;
        if (dx < 0) facingRight = false;
    }

    @Override
    public void update() {
        // Kecepatan gerak maksimal
        double speedLimit = 6.5;
        
        // Normalisasi diagonal (agar gerak serong tidak lebih cepat)
        double inputX = dX;
        double inputY = dY;
        if (dX != 0 && dY != 0) {
            double length = Math.sqrt(dX * dX + dY * dY);
            inputX = dX / length;
            inputY = dY / length;
        }

        double targetVelX = inputX * speedLimit;
        double targetVelY = inputY * speedLimit;

        // Interpolasi perpindahan velocity (Lerp) - 0.25 biar snappier
        velX += (targetVelX - velX) * 0.25;
        velY += (targetVelY - velY) * 0.25;

        // Animasi jalan
        if (Math.abs(velX) > 0.5 || Math.abs(velY) > 0.5)
            animTick++;
        else
            animTick = 0;
    }

    // Gerak sumbu X secara terpisah (Sliding Physics)
    public void applyMoveX() {
        prevExactX = exactX;
        exactX += velX;
        x = (int) Math.round(exactX);
    }

    // Gerak sumbu Y secara terpisah (Sliding Physics)
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

    // 🔥 HITBOX (PAD LEBIH BESAR BIAR MUDAH LEWAT CELAH MEJA)
    public Rectangle getBounds() {
        // Kita beri padding 45px kiri-kanan agar hitbox asli hanya ~70px (mirip sebelumnya)
        return new Rectangle(x + 45, y + 45, width - 90, height - 90);
    }

    @Override
    public void draw(Graphics2D g) {
        int offsetY = (int) (Math.sin(animTick * 0.4) * 6);
        int offsetX = (int) (Math.cos(animTick * 0.3) * 4);

        // SHADOW
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x + width / 4, y + height - 15, width / 2, 18);

        // PLAYER IMAGE
        if (avatar != null) {
            if (facingRight)
                g.drawImage(avatar, x + offsetX, y + offsetY, width, height, null);
            else
                g.drawImage(avatar, x + width + offsetX, y + offsetY, -width, height, null);
        }

        // DRAW NAME (Posisi lebih proporsional)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Segoe UI", Font.BOLD, 16));
        int textWidth = g.getFontMetrics().stringWidth(name);
        g.drawString(name, x + (width - textWidth) / 2, y + offsetY - 10);

        // ITEM DI ATAS
        if (carriedAssignments > 0) {
            g.setColor(new Color(255, 215, 0));
            g.fillOval(x + width - 40, y, 32, 32);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Segoe UI", Font.BOLD, 16));
            g.drawString("" + carriedAssignments, x + width - 30, y + 22);
        }

        // 🔥 DEBUG HITBOX (boleh dihapus nanti)
        // g.setColor(Color.RED);
        // g.draw(getBounds());
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
}