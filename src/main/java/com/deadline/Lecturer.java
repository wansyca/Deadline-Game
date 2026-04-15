package com.deadline;

import java.awt.*;
import javax.swing.ImageIcon;

public class Lecturer extends GameObject {

    private double speed;
    private double exactX, exactY;
    private Image sprite;
    private double distanceToTarget = 9999;

    private int animTick = 0;

    private enum State {
        WANDER, CHASE
    }

    private State state = State.WANDER;

    // 🔥 VISION DIPERBESAR SUPAYA SELALU NGEJAR
    private double visionRange = 2500;

    public Lecturer(int x, int y, double speed) {
        // 🔥 DOSEN DIGEDEIN
        super(x, y, 130, 140);

        this.speed = speed;
        exactX = x;
        exactY = y;

        sprite = new ImageIcon(getClass().getResource("/assets/Avatar_3_dosen.png")).getImage();
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void updateAI(Player target) {
        double dx = target.getX() - exactX;
        double dy = target.getY() - exactY;

        double distance = Math.sqrt(dx * dx + dy * dy);
        this.distanceToTarget = distance;

        // 🔥 FIX BUG (biar ga NaN)
        if (distance == 0) distance = 0.0001;

        // Selalu chase jika dalam jangkauan map (karena vision range 2500, pasti selalu ngejar)
        if (distance < visionRange)
            state = State.CHASE;
        else
            state = State.WANDER;

        if (state == State.CHASE) {
            double dirX = dx / distance;
            double dirY = dy / distance;

            // 🔥 BOOST KECIL BIAR BISA KABUR
            double boost = 1 + (0.3 * (1 - (distance / visionRange)));

            exactX += dirX * speed * boost;
            exactY += dirY * speed * boost;

        } else {
            // 🔥 GERAK NGELIAR LEBIH HALUS
            exactX += Math.sin(animTick * 0.03) * 1.5;
            exactY += Math.cos(animTick * 0.03) * 1.5;
        }

        animTick++;

        x = (int) exactX;
        y = (int) exactY;
    }

    // 🔥 HITBOX LEBIH KECIL (BIAR FAIR)
    public Rectangle getBounds() {
        return new Rectangle(x + 30, y + 30, width - 60, height - 60);
    }

    public boolean intersects(Player p) {
        return getBounds().intersects(p.getBounds());
    }

    @Override
    public void update() {
        // dikontrol dari GamePanel
    }

    @Override
    public void draw(Graphics2D g) {

        int offsetY = (int) (Math.sin(animTick * 0.3) * 4);

        // 🔥 SHADOW IKUT SIZE
        g.setColor(new Color(0, 0, 0, 80));
        g.fillOval(x + width / 4, y + height - 12, width / 2, 14);

        int shakeX = 0;
        int shakeY = 0;

        // 🔥 MODE NGEJAR + DEKAT = EFEK SEREM
        if (state == State.CHASE && distanceToTarget < 250) {
            shakeX = (int) (Math.random() * 6 - 3);
            shakeY = (int) (Math.random() * 6 - 3);

            g.setColor(new Color(255, 0, 0, 60));
            g.fillOval(x - 20, y - 20, width + 40, height + 40);
        }

        g.drawImage(sprite, x + shakeX, y + offsetY + shakeY, width, height, null);

        // 🔥 DEBUG HITBOX (optional)
        // g.setColor(Color.GREEN);
        // g.draw(getBounds());
    }
}