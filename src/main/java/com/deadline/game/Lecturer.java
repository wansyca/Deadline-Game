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
            case 0: folder = "dosen_tua"; break;
            case 1: folder = "domu_cowo"; break;
            case 2: folder = "dosen_cewe"; break;
            default: folder = "dosen_tua";
        }

        try {
            String basePath = "/assets/dosen/" + folder + "/";
            up1 = ImageIO.read(getClass().getResourceAsStream(basePath + "up/up_1.png"));
            up2 = ImageIO.read(getClass().getResourceAsStream(basePath + "up/up_2.png"));
            down1 = ImageIO.read(getClass().getResourceAsStream(basePath + "down/down_1.png"));
            down2 = ImageIO.read(getClass().getResourceAsStream(basePath + "down/down_2.png"));
            left1 = ImageIO.read(getClass().getResourceAsStream(basePath + "left/left_1.png"));
            left2 = ImageIO.read(getClass().getResourceAsStream(basePath + "left/left_2.png"));
            right1 = ImageIO.read(getClass().getResourceAsStream(basePath + "right/right_1.png"));
            right2 = ImageIO.read(getClass().getResourceAsStream(basePath + "right/right_2.png"));
            
            System.out.println("✅ Loaded assets for Lecturer: " + folder);
        } catch (Exception e) {
            System.err.println("❌ Error loading lecturer assets: " + folder);
            // Fallback to flipping if needed
            try {
                if (right1 != null) {
                    left1 = flipImage(right1);
                    left2 = flipImage(right2);
                }
            } catch (Exception e2) {}
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
        
        // Anti-Stuck mechanism
        if (pathTick % 30 == 0) {
            if (Math.abs(exactX - lastX) < 2 && Math.abs(exactY - lastY) < 2) {
                stuckTick++;
            } else {
                stuckTick = 0;
            }
            lastX = exactX;
            lastY = exactY;
        }

        // STATE LOGIC
        if (distToPlayer < 400) { // Chase when player is nearby
            state = 1; 
        } else if (distToPlayer > 600) { // Stop chasing when far away
            state = 0; 
        }

        // PATH GENERATION
        if (pathTick % 15 == 0 || currentPath == null || currentPath.isEmpty() || stuckTick > 2) {
            if (pathFinder != null) {
                if (state == 1) { // CHASE PLAYER
                    int targetR = (int) (player.getY() + player.getHeight() / 2) / 64;
                    int targetC = (int) (player.getX() + player.getWidth() / 2) / 64;
                    currentPath = pathFinder.findPath(currentR, currentC, targetR, targetC);
                } else { // PATROL CORRIDOR NATURALLY
                    // If no target, or reached target, or stuck
                    if (patrolTargetR == -1 || (currentR == patrolTargetR && currentC == patrolTargetC) || stuckTick > 2) {
                        // Pick random valid tile on the map to patrol to
                        int mapRows = 75;
                        int mapCols = 75;
                        for (int attempts = 0; attempts < 50; attempts++) {
                            int tr = (int)(Math.random() * mapRows);
                            int tc = (int)(Math.random() * mapCols);
                            if (pathFinder.isWalkable(tr, tc)) {
                                patrolTargetR = tr;
                                patrolTargetC = tc;
                                break;
                            }
                        }
                    }
                    if (patrolTargetR != -1) {
                        currentPath = pathFinder.findPath(currentR, currentC, patrolTargetR, patrolTargetC);
                    }
                }
                stuckTick = 0;
            }
        }

        double targetDx = 0;
        double targetDy = 0;

        if (currentPath != null && !currentPath.isEmpty()) {
            int[] nextStep = currentPath.get(0);
            double stepX = nextStep[1] * 64 + 32;
            double stepY = nextStep[0] * 64 + 32;
            
            // If close to waypoint, remove it and proceed to next
            if (Math.abs(exactX + width/2 - stepX) < 15 && Math.abs(exactY + height/2 - stepY) < 15) {
                currentPath.remove(0);
                if (!currentPath.isEmpty()) {
                    nextStep = currentPath.get(0);
                    stepX = nextStep[1] * 64 + 32;
                    stepY = nextStep[0] * 64 + 32;
                }
            }
            
            if (!currentPath.isEmpty()) {
                double angle = Math.atan2(stepY - (exactY + height/2), stepX - (exactX + width/2));
                targetDx = Math.cos(angle);
                targetDy = Math.sin(angle);
                isMoving = true;
            } else {
                isMoving = false;
            }
        } else {
            isMoving = false;
            patrolTargetR = -1; // Force new target next tick if path failed
        }

        speed = (state == 1) ? 4.5 : 2.0;

        double nextX = exactX + targetDx * speed;
        double nextY = exactY + targetDy * speed;

        // Collision check (Dosen cannot walk through walls, desks, etc.)
        Rectangle nextBoundsX = new Rectangle((int)nextX + 10, (int)exactY + 20, width - 20, height - 24);
        Rectangle nextBoundsY = new Rectangle((int)exactX + 10, (int)nextY + 20, width - 20, height - 24);
        
        boolean collisionX = false;
        boolean collisionY = false;
        for (Rectangle r : obstacles) {
            if (nextBoundsX.intersects(r)) collisionX = true;
            if (nextBoundsY.intersects(r)) collisionY = true;
        }

        if (!collisionX) exactX = nextX;
        if (!collisionY) exactY = nextY;

        if (isMoving) {
            if (Math.abs(targetDx) >= Math.abs(targetDy)) {
                direction = (targetDx > 0) ? "right" : "left";
            } else {
                direction = (targetDy > 0) ? "down" : "up";
            }

            spriteCounter++;
            if (spriteCounter >= 8) {
                spriteNum = (spriteNum == 1) ? 2 : 1;
                spriteCounter = 0;
            }
        } else {
            spriteNum = 1;
            spriteCounter = 0;
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

        // NORMALIZE SIZE: Scale everything to 64px height regardless of PNG resolution
        int targetHeight = 64;
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
        return new Rectangle(x + 15, y + 30, width - 30, height - 34);
    }

    public boolean intersects(Player p) {
        return getBounds().intersects(p.getBounds());
    }
}
