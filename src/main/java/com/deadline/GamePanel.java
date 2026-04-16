package com.deadline;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.BasicStroke;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.RadialGradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.Timer;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // =========================
    // SCREEN
    // =========================
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // =========================
    // WORLD (MAP SUPER LUAS - KAMPUS GEDUNG A)
    // =========================
    private static final int WORLD_WIDTH = 8000;
    private static final int WORLD_HEIGHT = 6000;

    private static final int FPS = 60;

    // =========================
    // CAMERA
    // =========================
    private int camX = 0;
    private int camY = 0;

    // =========================
    // GAME OBJECT
    // =========================
    private Timer timer;
    private Player player;
    private List<SubmissionDesk> submissionDesks;
    private List<Lecturer> lecturers;
    private List<Assignment> assignments;
    private List<Rectangle> obstacles;

    private Random random = new Random();
    private boolean isGameOver = false;

    // 🔥 SURVIVAL MODE
    private int survivalTime = 0;
    private int ticks = 0;
    private int collectedBooks = 0;
    private int totalScore = 0;
    private long gameStartTime;
    
    // BACKEND INTEGRATION
    private int currentPlayerId = -1;

    // Movement
    private boolean up, down, left, right;
    
    // UI Buttons bounds
    private Rectangle btnMenu;
    private Rectangle btnRetry;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (isGameOver) {
                    if (btnRetry != null && btnRetry.contains(p)) {
                        saveFinalScore();
                        initGame();
                    } else if (btnMenu != null && btnMenu.contains(p)) {
                        saveFinalScore();
                        Main.switchPage(Main.DASHBOARD);
                    }
                }
            }
        });

        initGame();

        timer = new Timer(1000 / FPS, this);
        timer.start();

        System.out.println("GAME PANEL 8000x6000 SURVIVAL VERSION KELOAD ✅");
    }

    public void resetGame(int playerId, String playerName, String avatarPath) {
        this.currentPlayerId = playerId;
        // Reset state
        up = false; down = false; left = false; right = false;

        // Reset entities
        player = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        player.setName(playerName);
        player.setAvatar(avatarPath);

        initGame();
    }

    // =========================
    // INIT GAME
    // =========================
    private void initGame() {
        isGameOver = false;
        survivalTime = 0;
        ticks = 0;
        collectedBooks = 0;
        totalScore = 0;
        gameStartTime = System.currentTimeMillis();

        if (player == null) {
            player = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
            player.setAvatar("/assets/Avatar_1_cowo.png");
        }

        lecturers = new ArrayList<>();
        assignments = new ArrayList<>();
        submissionDesks = new ArrayList<>();

        initObstacles();

        // 🔥 SAFETY SPAWN: Cari lokasi kosong untuk player agar tidak nyangkut saat mulai
        boolean safe = false;
        int attempts = 0;
        int spawnX = WORLD_WIDTH / 2;
        int spawnY = WORLD_HEIGHT / 2;
        
        while (!safe && attempts < 100) {
            player.setX(spawnX);
            player.setY(spawnY);
            safe = true;
            for (Rectangle r : obstacles) {
                if (player.getBounds().intersects(r)) {
                    safe = false;
                    spawnX = random.nextInt(WORLD_WIDTH - 500) + 250;
                    spawnY = random.nextInt(WORLD_HEIGHT - 500) + 250;
                    attempts++;
                    break;
                }
            }
        }
        player.resetCarriedAssignments();

        // Spawn lecturers (Awal game di map luas: 4 dosen)
        for (int i = 0; i < 4; i++) {
            spawnLecturer();
        }

        // Spawn assignments (Seebarkan buku lebih banyak di map luas)
        for (int i = 0; i < 50; i++) {
            spawnAssignment();
        }
    }

    private void spawnLecturer() {
        // Kecepatan meningkat seiring buku
        double speed = 1.5 + (collectedBooks * 0.3);
        speed = Math.min(speed, 6.0); // Batas maksimum speed 6.0

        lecturers.add(new Lecturer(
                random.nextInt(WORLD_WIDTH),
                random.nextInt(WORLD_HEIGHT),
                speed));
    }

    private void initObstacles() {
        obstacles = new ArrayList<>();
        submissionDesks = new ArrayList<>();
        int wallThin = 60;

        // 1. BOUNDARY WALLS (Tembok Luar Kampus)
        obstacles.add(new Rectangle(0, 0, WORLD_WIDTH, wallThin));
        obstacles.add(new Rectangle(0, WORLD_HEIGHT - wallThin, WORLD_WIDTH, wallThin));
        obstacles.add(new Rectangle(0, 0, wallThin, WORLD_HEIGHT));
        obstacles.add(new Rectangle(WORLD_WIDTH - wallThin, 0, wallThin, WORLD_HEIGHT));

        // 2. CENTRAL CORRIDOR (Lorong Utama)
        // Horizontal Corridor in the middle
        int corridorY = WORLD_HEIGHT / 2 - 250;
        int corridorH = 500;
        // Tembok lorong atas & bawah dengan celah pintu
        for (int x = 0; x < WORLD_WIDTH; x += 1000) {
            // Celah pintu tiap 1000px
            obstacles.add(new Rectangle(x, corridorY, 800, wallThin));
            obstacles.add(new Rectangle(x, corridorY + corridorH, 800, wallThin));
        }

        // 3. CLASSROOMS (Ruangan-Ruangan)
        // Kita bagi map jadi 8 ruangan besar (4 atas, 4 bawah)
        int roomW = WORLD_WIDTH / 4;
        int roomH = (WORLD_HEIGHT - corridorH) / 2;

        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 4; col++) {
                int startX = col * roomW;
                int startY = (row == 0) ? 0 : corridorY + corridorH;

                // Tembok pemisah antar kelas (vertical)
                if (col > 0) {
                    obstacles.add(new Rectangle(startX, startY, wallThin, roomH));
                }

                // ISI KELAS (Meja & Kursi)
                generateRoomDecor(startX + 150, startY + 150, roomW - 300, roomH - 350);
                
                // SUBMISSION DESK (Satu di tiap ruangan atau tiap 2 ruangan)
                if ((row + col) % 2 == 0) {
                    int deskX = startX + roomW / 2 - 150;
                    int deskY = startY + (row == 0 ? 100 : roomH - 200);
                    Rectangle sdRect = new Rectangle(deskX, deskY, 300, 100);
                    obstacles.add(sdRect);
                    submissionDesks.add(new SubmissionDesk(deskX, deskY));
                }
            }
        }

        // 4. FURNITURE TAMBAHAN DI LORONG (Lemari & Bangku)
        for (int x = 500; x < WORLD_WIDTH; x += 1500) {
            // Lemari di lorong
            obstacles.add(new Rectangle(x, corridorY + 100, 150, 80));
            // Bangku di sisi lain
            obstacles.add(new Rectangle(x + 400, corridorY + corridorH - 180, 200, 60));
        }
    }

    private void generateRoomDecor(int x, int y, int w, int h) {
        int mejaWidth = 120;
        int mejaHeight = 80;
        int spacingX = 350;
        int spacingY = 250;

        // Pola Meja Mahasiswa
        for (int curY = y + 100; curY < y + h - 100; curY += spacingY) {
            for (int curX = x; curX < x + w - 100; curX += spacingX) {
                obstacles.add(new Rectangle(curX, curY, mejaWidth, mejaHeight));
            }
        }

        // Meja Dosen / Podium di depan kelas
        obstacles.add(new Rectangle(x + w / 2 - 100, y, 200, 80));
        
        // Lemari di pojok kelas
        obstacles.add(new Rectangle(x, y, 80, 150));
        obstacles.add(new Rectangle(x + w - 80, y + h - 150, 80, 150));
    }

    private void spawnAssignment() {
        Assignment a;
        boolean overlap;
        do {
            overlap = false;
            a = new Assignment(
                    random.nextInt(WORLD_WIDTH - 100) + 50,
                    random.nextInt(WORLD_HEIGHT - 100) + 50);
            Rectangle areaCek = new Rectangle(a.getX() - 15, a.getY() - 15, a.getWidth() + 30, a.getHeight() + 30);
            for (Rectangle r : obstacles) {
                if (areaCek.intersects(r)) {
                    overlap = true;
                    break;
                }
            }
        } while (overlap);
        assignments.add(a);
    }

    // =========================
    // GAME LOOP
    // =========================
    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        if (isGameOver) {
            return;
        }

        // ⏱️ 1. WAKTU MAJU (SURVIVAL TIME)
        ticks++;
        if (ticks % FPS == 0) {
            survivalTime++;
            totalScore = survivalTime + (collectedBooks * 10);
        }

        // 👨‍🏫 2. SPAWN DOSEN BERTAHAP (MAKIN SULIT)
        int spawnDelay = Math.max(30, FPS * (3 - collectedBooks / 5));
        int maxLecturers = 2 + (collectedBooks / 2);

        if (ticks % spawnDelay == 0 && lecturers.size() < maxLecturers) {
            spawnLecturer();
        }

        // Movement
        int dx = 0, dy = 0;
        if (up)
            dy--;
        if (down)
            dy++;
        if (left)
            dx--;
        if (right)
            dx++;

        player.setDirection(dx, dy);
        player.update(); // Hitung velocity

        // --- STEP movement X (Sliding Collision) ---
        player.applyMoveX();
        // Batas map X
        if (player.getX() < 0 || player.getX() > WORLD_WIDTH - player.getWidth()) {
            player.rollbackX();
        }
        // Collide obstacles X
        for (Rectangle rect : obstacles) {
            if (player.getBounds().intersects(rect)) {
                player.rollbackX();
                break;
            }
        }

        // --- STEP movement Y (Sliding Collision) ---
        player.applyMoveY();
        // Batas map Y
        if (player.getY() < 0 || player.getY() > WORLD_HEIGHT - player.getHeight()) {
            player.rollbackY();
        }
        // Collide obstacles Y
        for (Rectangle rect : obstacles) {
            if (player.getBounds().intersects(rect)) {
                player.rollbackY();
                break;
            }
        }

        // Dosen AI
        for (Lecturer l : lecturers) {
            l.updateAI(player);

            if (l.intersects(player)) {
                System.out.println("KETANGKAP DOSEN 💀");
                isGameOver = true;
            }
        }

        // Ambil buku (Bonus Score)
        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            if (player.intersects(a)) {
                collectedBooks++;
                totalScore = survivalTime + (collectedBooks * 10);
                assignments.remove(i);
                
                // BACKEND: Record submission via SubmissionDesk if needed, 
                // but since GamePanel gives score instantly, we just record task submission.
                if (currentPlayerId != -1 && !submissionDesks.isEmpty()) {
                    submissionDesks.get(0).processSubmission(currentPlayerId, "Collected Assignment " + collectedBooks, "SUCCESS");
                }
                
                // Update semua kecepatan dosen saat ini
                double newSpeed = Math.min(1.5 + (collectedBooks * 0.3), 6.0);
                for (Lecturer l : lecturers) {
                    l.setSpeed(newSpeed);
                }

                // Spawn buku baru supaya di map tetap ada buku untuk diambil
                spawnAssignment();
                i--;
            }
        }

        // Kamera follow player
        camX = player.getX() - WIDTH / 2;
        camY = player.getY() - HEIGHT / 2;

        camX = Math.max(0, Math.min(camX, WORLD_WIDTH - WIDTH));
        camY = Math.max(0, Math.min(camY, WORLD_HEIGHT - HEIGHT));
    }

    // =========================
    // RENDER
    // =========================
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Camera
        g2.translate(-camX, -camY);

        // Background - Lantai Kelas (Bersih & Elegan)
        g2.setColor(new Color(230, 230, 235)); 
        g2.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        
        // Garis ubin lantai yang tipis dan halus
        g2.setColor(new Color(0, 0, 0, 15)); 
        for (int x = 0; x < WORLD_WIDTH; x += 100) {
            g2.drawLine(x, 0, x, WORLD_HEIGHT);
        }
        for (int y = 0; y < WORLD_HEIGHT; y += 100) {
            g2.drawLine(0, y, WORLD_WIDTH, y);
        }

        // Obstacles (Meja & Tembok)
        for (Rectangle rect : obstacles) {
            if (rect.width == WORLD_WIDTH || rect.height == WORLD_HEIGHT) {
                // Tembok Luar
                g2.setColor(new Color(44, 62, 80)); 
                g2.fill(rect);
                g2.setColor(new Color(52, 73, 94));
                g2.draw(rect);
            } else if (rect.width == 300) {
                // Meja Dosen sudah digambar oleh submissionDesks.draw()
            } else {
                // MEJA MAHASISWA (GAMBAR PROSEDURAL - BERSIH & NO JARING)
                // 1. Kaki Meja
                g2.setColor(new Color(80, 50, 40));
                g2.fillRect(rect.x + 5, rect.y + 5, 8, rect.height - 10);
                g2.fillRect(rect.x + rect.width - 13, rect.y + 5, 8, rect.height - 10);
                
                // 2. Permukaan Meja
                g2.setColor(new Color(121, 85, 72));
                g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height - 10, 8, 8);
                
                // 3. Highlight/Shading
                g2.setColor(new Color(255, 255, 255, 30));
                g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height - 10, 8, 8);
            }
        }

        for (SubmissionDesk d : submissionDesks) {
            d.draw(g2);
        }

        for (Assignment a : assignments)
            a.draw(g2);
        for (Lecturer l : lecturers)
            l.draw(g2);
        player.draw(g2);

        // Reset camera
        g2.translate(camX, camY);

        drawUI(g2);
    }

    private void updateButtonBounds() {
        int panelW = getWidth();
        int panelH = getHeight();
        int btnY = panelH / 2 + 60;
        btnRetry = new Rectangle(panelW / 2 - 210, btnY, 200, 50);
        btnMenu = new Rectangle(panelW / 2 + 10, btnY, 200, 50);
    }

    private void drawButton(Graphics2D g2, String text, Rectangle rect, Color bgColor) {
        // Shadow/Glow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(rect.x + 3, rect.y + 3, rect.width, rect.height, 15, 15);

        // Gradient Background
        GradientPaint gp = new GradientPaint(rect.x, rect.y, bgColor, rect.x, rect.y + rect.height, bgColor.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        
        // Border
        g2.setColor(new Color(255, 255, 255, 100));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);
        
        // Text
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.setColor(Color.WHITE);
        g2.drawString(text, rect.x + (rect.width - tw) / 2, rect.y + 32);
    }

    private void drawUI(Graphics2D g2) {
        int panelW = getWidth();
        int panelH = getHeight();
        
        // --- 💎 MODERN GLASS HUD ---
        int hudW = 350;
        int hudH = 100;
        int hudX = 25;
        int hudY = 25;

        // Outer Glow/Shadow
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect(hudX - 2, hudY - 2, hudW + 4, hudH + 4, 20, 20);

        // Glass Body
        g2.setColor(new Color(20, 20, 25, 200));
        g2.fillRoundRect(hudX, hudY, hudW, hudH, 20, 20);
        
        // Top Highlight
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRoundRect(hudX, hudY, hudW, 35, 20, 20);
        g2.fillRect(hudX, hudY + 20, hudW, 15); // Flatten bottom of top round

        // Border
        g2.setColor(new Color(255, 255, 255, 60));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(hudX, hudY, hudW, hudH, 20, 20);

        // Styling teks
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Player Name (Header)
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(new Color(180, 200, 255));
        g2.drawString("📍 SURVIVOR: " + player.getName().toUpperCase(), hudX + 20, hudY + 25);

        // Stats
        g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("⏱️ SURVIVED", hudX + 20, hudY + 58);
        
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.setColor(new Color(0, 255, 200));
        g2.drawString(survivalTime + "s", hudX + 150, hudY + 59);

        g2.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("📚 BOOKS", hudX + 20, hudY + 85);
        
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(String.valueOf(collectedBooks), hudX + 150, hudY + 86);

        // Score Badge
        int badgeW = 100;
        int badgeH = 50;
        int badgeX = hudX + hudW - badgeW - 15;
        int badgeY = hudY + (hudH - badgeH) / 2 + 5;
        
        g2.setColor(new Color(255, 255, 255, 20));
        g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 10, 10);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawRoundRect(badgeX, badgeY, badgeW, badgeH, 10, 10);
        
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        g2.setColor(new Color(200, 200, 200));
        g2.drawString("SCORE", badgeX + (badgeW - g2.getFontMetrics().stringWidth("SCORE"))/2, badgeY + 18);
        
        g2.setFont(new Font("Impact", Font.PLAIN, 24));
        g2.setColor(Color.WHITE);
        String scoreStr = String.valueOf(totalScore);
        g2.drawString(scoreStr, badgeX + (badgeW - g2.getFontMetrics().stringWidth(scoreStr))/2, badgeY + 42);

        // --- 💀 GAME OVER SCREEN ---
        if (isGameOver) {
            updateButtonBounds();
            
            // Background Blur-ish Overlay
            g2.setColor(new Color(15, 5, 5, 220));
            g2.fillRect(0, 0, panelW, panelH);

            // Red Vignette
            RadialGradientPaint rgp = new RadialGradientPaint(
                new Point(panelW / 2, panelH / 2),
                panelW,
                new float[]{0.0f, 1.0f},
                new Color[]{new Color(100, 0, 0, 0), new Color(50, 0, 0, 150)}
            );
            g2.setPaint(rgp);
            g2.fillRect(0, 0, panelW, panelH);

            // Text Shadow
            g2.setFont(new Font("Impact", Font.PLAIN, 100));
            String overText = "GAME OVER";
            int textWidth = g2.getFontMetrics().stringWidth(overText);
            g2.setColor(new Color(0, 0, 0, 150));
            g2.drawString(overText, (panelW - textWidth) / 2 + 5, panelH / 2 - 15);
            
            g2.setColor(new Color(255, 50, 50));
            g2.drawString(overText, (panelW - textWidth) / 2, panelH / 2 - 20);
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI Light", Font.PLAIN, 28));
            String subText = "Yahh, telat submit tugas";
            int subWidth = g2.getFontMetrics().stringWidth(subText);
            g2.drawString(subText, (panelW - subWidth) / 2, panelH / 2 + 30);
            
            g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
            String scorFinal = "Poin Akhir: " + totalScore;
            int sfWidth = g2.getFontMetrics().stringWidth(scorFinal);
            g2.drawString(scorFinal, (panelW - sfWidth) / 2, panelH / 2 + 70);
            
            int btnY_real = panelH / 2 + 110;
            btnRetry.y = btnY_real;
            btnMenu.y = btnY_real;
            
            drawButton(g2, "COBA LAGI", btnRetry, new Color(180, 30, 30));
            drawButton(g2, "KE MENU", btnMenu, new Color(40, 40, 45));
        }
    }

    // =========================
    // INPUT
    // =========================
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                up = true;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                down = true;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                left = true;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                right = true;
                break;
            case KeyEvent.VK_ENTER:
                // No more levels
                break;
            case KeyEvent.VK_B:
                if (isGameOver) {
                    saveFinalScore();
                    Main.switchPage(Main.DASHBOARD);
                }
                break;
            case KeyEvent.VK_R:
                if (isGameOver) {
                    saveFinalScore();
                    initGame();
                }
                break;
        }
    }

    private void saveFinalScore() {
        int timePlayed = (int) ((System.currentTimeMillis() - gameStartTime) / 1000);
        
        // BACKEND: Save to database
        if (currentPlayerId != -1) {
            com.deadline.backend.ScoreService scoreService = new com.deadline.backend.ScoreService();
            scoreService.saveScore(currentPlayerId, totalScore, timePlayed);
        }
        
        // Use older LeaderboardManager for fallback/txt compatibility if we don't rewrite it.
        LeaderboardManager.saveScore(player.getName(), totalScore, timePlayed);
        
        // Reset time start for restart
        gameStartTime = System.currentTimeMillis();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
            case KeyEvent.VK_UP:
                up = false;
                break;
            case KeyEvent.VK_S:
            case KeyEvent.VK_DOWN:
                down = false;
                break;
            case KeyEvent.VK_A:
            case KeyEvent.VK_LEFT:
                left = false;
                break;
            case KeyEvent.VK_D:
            case KeyEvent.VK_RIGHT:
                right = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}