package com.deadline;

import java.awt.*;

public class Lecturer extends GameObject {

    private double speed;
    private double exactX, exactY;
    private Image image;
    private int animTick = 0;
    private boolean facingRight = true;

    public Lecturer(int x, int y, double speed, Image image) {
        // 🔥 UKURAN SAMA DENGAN PLAYER (160x160)
        super(x, y, 160, 160);
        this.speed = speed;
        this.exactX = x;
        this.exactY = y;
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return this.speed;
    }

    public void updateAI(Player player, java.util.List<Lecturer> lecturers) {
        // 6. GERAK DOSEN KE PLAYER
        double dx = player.getX() - exactX;
        double dy = player.getY() - exactY;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 0) {
            dx /= dist;
            dy /= dist;

            exactX += dx * speed;
            exactY += dy * speed;
            
            // Animasi jalan dan hadap kiri/kanan
            animTick++;
            if (dx > 0) facingRight = true;
            else if (dx < 0) facingRight = false;
        } else {
            animTick = 0;
        }

        // 7. TAMBAH RANDOM GERAK (BIAR GA KAKU)
        exactX += (Math.random() - 0.5) * 0.5;
        exactY += (Math.random() - 0.5) * 0.5;

        // 5. ANTI NUMPUK (WAJIB) - Jarak diperbesar karena ukuran dosen lebih besar
        for (Lecturer other : lecturers) {
            if (other != this) {
                double diffX = this.exactX - other.exactX;
                double diffY = this.exactY - other.exactY;
                double distance = Math.sqrt(diffX * diffX + diffY * diffY);

                if (distance < 100) {
                    exactX += diffX * 0.05;
                    exactY += diffY * 0.05;
                }
            }
        }

        this.x = (int) exactX;
        this.y = (int) exactY;
    }

    @Override
    public void update() {
        // Dikontrol dari GamePanel lewat updateAI
    }

    @Override
    public void draw(Graphics2D g) {
        // Efek ngambang/bobbing
        int offsetY = (int) (Math.sin(animTick * 0.4) * 6);

        // 9. RENDER BAYANGAN (Sama seperti player)
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(x + width / 4, y + height - 15, width / 2, 18);

        // 8. RENDER DOSEN
        if (image != null) {
            if (facingRight) {
                g.drawImage(image, x, y + offsetY, width, height, null);
            } else {
                g.drawImage(image, x + width, y + offsetY, -width, height, null);
            }
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y + offsetY, width, height);
        }
    }

    public Rectangle getBounds() {
        // Hitbox pas di badan (Disamakan dengan offset player agar konsisten)
        return new Rectangle(x + 45, y + 45, width - 90, height - 90);
    }

    public boolean intersects(Player p) {
        return getBounds().intersects(p.getBounds());
    }
}