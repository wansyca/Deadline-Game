package com.deadline;

import java.awt.*;
import javax.swing.ImageIcon;

public class Player extends GameObject {

    private Image avatar;
    private String name = "Mahasiswa";

    private int prevX, prevY;
    private double exactX, exactY;
    private double velX = 0, velY = 0;
    private int dX, dY;

    private int carriedAssignments = 0;
    private final int MAX_CARRY = 3;

    private int animTick = 0;
    private boolean facingRight = true;

    public Player(int x, int y) {
        // 🔥 DIGEDEIN + PROPORSIONAL
        super(x, y, 110, 110);
        this.exactX = x;
        this.exactY = y;
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
        prevX = x;
        prevY = y;

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

        // Interpolasi perpindahan velocity agar mulus (Lerp)
        velX += (targetVelX - velX) * 0.2;
        velY += (targetVelY - velY) * 0.2;

        exactX += velX;
        exactY += velY;

        x = (int) Math.round(exactX);
        y = (int) Math.round(exactY);

        // Animasi jalan
        if (Math.abs(velX) > 0.5 || Math.abs(velY) > 0.5)
            animTick++;
        else
            animTick = 0;
    }

    public void rollback() {
        exactX = prevX;
        exactY = prevY;
        x = prevX;
        y = prevY;
    }

    // 🔥 HITBOX (PENTING BUAT COLLISION)
    public Rectangle getBounds() {
        return new Rectangle(x + 20, y + 20, width - 40, height - 40);
    }

    @Override
    public void draw(Graphics2D g) {
        int offsetY = (int) (Math.sin(animTick * 0.4) * 5);
        int offsetX = (int) (Math.cos(animTick * 0.3) * 3);

        // SHADOW
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(x + width / 4, y + height - 10, width / 2, 12);

        // PLAYER IMAGE
        if (avatar != null) {
            if (facingRight)
                g.drawImage(avatar, x + offsetX, y + offsetY, width, height, null);
            else
                g.drawImage(avatar, x + width, y + offsetY, -width, height, null);
        }

        // DRAW NAME
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        int textWidth = g.getFontMetrics().stringWidth(name);
        g.drawString(name, x + (width - textWidth) / 2, y + offsetY - 5);

        // ITEM DI ATAS
        if (carriedAssignments > 0) {
            g.setColor(Color.YELLOW);
            g.fillOval(x + width - 30, y - 20, 28, 28);

            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("" + carriedAssignments, x + width - 20, y);
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
}