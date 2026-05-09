package com.deadline.game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
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
    private static final int SCALE = 4;
    private static final int TILE_SIZE = 16 * SCALE; // 64x64

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
            case 0:
                folder = "dosen_tua";
                break;
            case 1:
                folder = "domu_cowo";
                break;
            case 2:
                folder = "dosen_cewe";
                break;
            default:
                folder = "dosen_tua";
        }

        String basePath = "/assets/dosen/" + folder + "/";
        up1 = loadSafely(basePath + "up/up_1.png");
        up2 = loadSafely(basePath + "up/up_2.png");
        if (up2 == null)
            up2 = up1;

        down1 = loadSafely(basePath + "down/down_1.png");
        down2 = loadSafely(basePath + "down/down_2.png");
        if (down2 == null)
            down2 = down1;

        left1 = loadSafely(basePath + "left/left_1.png");
        left2 = loadSafely(basePath + "left/left_2.png");
        if (left2 == null)
            left2 = left1;

        right1 = loadSafely(basePath + "right/right_1.png");
        right2 = loadSafely(basePath + "right/right_2.png");
        if (right2 == null)
            right2 = right1;

        // Fallback for left facing by flipping right facing
        if (left1 == null && right1 != null) {
            left1 = flipImage(right1);
            left2 = flipImage(right2);
        }
    }

    private BufferedImage loadSafely(String path) {
        try {
            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is != null) {
                return ImageIO.read(is);
            }
        } catch (Exception e) {
        }
        return null;
    }

    private BufferedImage flipImage(BufferedImage src) {
        if (src == null)
            return null;
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

        // Since we need references, we'll keep updateAI for now but call it from
        // update()
        // IF we had a reference. However, GamePanel calls updateAI directly.
        // The real problem is update1() vs update().
    }

    private List<int[]> currentPath;
    private int pathTick = 0;
    private PathFinder pathFinder;

    public void setPathFinder(PathFinder pf) {
        this.pathFinder = pf;
    }

    private int patrolTargetR = -1;
    private int patrolTargetC = -1;
    private int state = 0; // 0 = PATROL, 1 = CHASE
    private int stuckTick = 0;
    private double lastX, lastY;

    public void updateAI(Player player, java.util.List<Lecturer> lecturers, List<Rectangle> obstacles) {
        double dx = player.getX() - exactX;
        double dy = player.getY() - exactY;
        double distToPlayer = Math.sqrt(dx * dx + dy * dy);

        int currentR = (int) (exactY + height / 2) / 64;
        int currentC = (int) (exactX + width / 2) / 64;

        pathTick++;

        // Always chase the player aggressively
        state = 1;

        // Path recalculation frequency
        int recalcInterval = 30; // Recalculate twice a second to avoid jitter
        if (pathTick % recalcInterval == 0 || currentPath == null || currentPath.isEmpty()) {
            int targetR = (int) (player.getY() + 32) / 64;
            int targetC = (int) (player.getX() + 32) / 64;

            // If player tile is blocked, find closest walkable
            if (pathFinder != null && !pathFinder.isWalkable(targetR, targetC)) {
                outer: for (int radius = 1; radius < 4; radius++) {
                    for (int dr = -radius; dr <= radius; dr++) {
                        for (int dc = -radius; dc <= radius; dc++) {
                            if (pathFinder.isWalkable(targetR + dr, targetC + dc)) {
                                targetR += dr;
                                targetC += dc;
                                break outer;
                            }
                        }
                    }
                }
            }

            if (pathFinder != null) {
                currentPath = pathFinder.findPath(currentR, currentC, targetR, targetC);
                // Prevent going back to the center of the current tile causing jitter
                if (currentPath != null && currentPath.size() > 1) {
                    int[] first = currentPath.get(0);
                    if (first[0] == currentR && first[1] == currentC) {
                        currentPath.remove(0);
                    }
                }
            }
        }

        double targetDx = 0;
        double targetDy = 0;

        // 3. MOVEMENT LOGIC
        if (currentPath != null && !currentPath.isEmpty()) {
            int[] nextStep = currentPath.get(0);
            double stepX = nextStep[1] * 64 + 32;
            double stepY = nextStep[0] * 64 + 32;

            // Move towards next waypoint
            double adx = stepX - (exactX + 32);
            double ady = stepY - (exactY + 32);
            double distToStep = Math.sqrt(adx * adx + ady * ady);

            // Smoothly transition to the next step without needing to hit dead center
            if (distToStep < 20) {
                currentPath.remove(0);
                if (!currentPath.isEmpty()) {
                    nextStep = currentPath.get(0);
                    stepX = nextStep[1] * 64 + 32;
                    stepY = nextStep[0] * 64 + 32;
                    adx = stepX - (exactX + 32);
                    ady = stepY - (exactY + 32);
                    distToStep = Math.sqrt(adx * adx + ady * ady);
                }
            }

            if (distToStep > 0) {
                targetDx = adx / distToStep;
                targetDy = ady / distToStep;
            }
            isMoving = true;
        } else {
            // FALLBACK: Move directly towards player if no path found
            double adx = (player.getX() + 32) - (exactX + 32);
            double ady = (player.getY() + 32) - (exactY + 32);
            double dist = Math.sqrt(adx * adx + ady * ady);
            if (dist > 5) {
                targetDx = adx / dist;
                targetDy = ady / dist;
                isMoving = true;
            } else {
                isMoving = false;
            }
        }

        // 4. DYNAMIC DIFFICULTY & AGGRESSION
        // Base speed increased
        double baseSpeed = 5.5;
        int level = GamePanel.currentLevel;
        int books = GamePanel.totalBooksCollected;

        // Scaling with level
        baseSpeed += (level - 1) * 1.5;

        // Scaling with books
        if (books > 5) {
            baseSpeed += (books - 5) * 1.0; // Makin agresif setelah 5 buku
        } else if (books >= 3) {
            baseSpeed += 0.5;
        }

        this.speed = baseSpeed;

        // Soft repulsion from other lecturers
        double repulseX = 0;
        double repulseY = 0;
        if (lecturers != null) {
            for (Lecturer other : lecturers) {
                if (other != this) {
                    double dxL = exactX - other.exactX;
                    double dyL = exactY - other.exactY;
                    double distL = Math.sqrt(dxL * dxL + dyL * dyL);
                    if (distL > 0 && distL < 50) {
                        repulseX += (dxL / distL) * (50 - distL) * 0.15;
                        repulseY += (dyL / distL) * (50 - distL) * 0.15;
                    }
                }
            }
        }

        double nextX = exactX + targetDx * speed + repulseX;
        double nextY = exactY + targetDy * speed + repulseY;

        // Collision check (Dosen cannot walk through walls, desks, etc.)
        // Made AI hitbox slightly smaller so they don't snag on wall corners while
        // pathing
        Rectangle nextBoundsX = new Rectangle((int) nextX + 16, (int) exactY + 24, width - 32, height - 32);
        Rectangle nextBoundsY = new Rectangle((int) exactX + 16, (int) nextY + 24, width - 32, height - 32);

        boolean collisionX = false;
        boolean collisionY = false;

        for (Rectangle r : obstacles) {
            if (nextBoundsX.intersects(r)) collisionX = true;
            if (nextBoundsY.intersects(r)) collisionY = true;
        }

    if(!collisionX) exactX=nextX;
    if(!collisionY) exactY=nextY;

    if(isMoving)

    {
        if (Math.abs(targetDx) >= Math.abs(targetDy)) {
            direction = (targetDx > 0) ? "right" : "left";
        } else {
            //
            direction = (targetDy > 0) ? "down" : "up";
        }

        spriteCounter++;
        if (spriteCounter >= 8) {
            spriteNum = (spriteNum == 1) ? 2 : 1;
            spriteCounter = 0;

        }

    }else
    {
        spriteNum = 1;
        spriteCounter = 0;
    }

    this.x=(int)Math.round(exactX);this.y=(int)Math.round(exactY);
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

        // NORMALIZE SIZE: Balanced height (approx 1.5 tiles)
        int targetHeight = 100;
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
                finalY -= 4; // slight bounce
            }
            g.drawImage(image, drawX, finalY, drawW, drawH, null);
        }
    }

    public Rectangle getBounds() {
        // Full size hitbox so collision happens before visual overlap
        return new Rectangle(x, y, width, height);
    }

    public boolean intersects(Player p) {
        return getBounds().intersects(p.getBounds());
    }
}
