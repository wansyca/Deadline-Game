package com.deadline.game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.deadline.audio.SoundManager;
import com.deadline.main.Main;
import com.deadline.model.Assignment;
import com.deadline.service.LeaderboardManager;
import com.deadline.ui.CustomAlert;
import com.deadline.ui.PixelAssets;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // MAP GRID SYSTEM
    private static final int TILE_SIZE = 64;
    private static final int MAP_COLS = 120;
    private static final int MAP_ROWS = 90;
    private static final int WORLD_WIDTH = MAP_COLS * TILE_SIZE;
    private static final int WORLD_HEIGHT = MAP_ROWS * TILE_SIZE;

    private static final int FPS = 60;

    private int camX = 0;
    private int camY = 0;

    private Timer timer;
    private Player player;
    private List<Map<String, Object>> cachedTopScores = new ArrayList<>();
    private List<Lecturer> lecturers;
    private List<Assignment> assignments;
    private List<Rectangle> obstacles;

    private int[][] mapFloor;
    private int[][] mapObject;

    private Random random = new Random();
    private boolean isGameOver = false;
    private boolean scoreSaved = false;
    private boolean soundPlayed = false;
    private int leaderboardScrollY = 0;

    private int survivalTime = 0;
    private int ticks = 0;
    private int collectedBooks = 0;
    private int currentLevel = 1;
    private int targetBooks = 10;
    private int levelTime = 60;
    private int timeLeft = 60;

    private int currentPlayerId = -1;

    private boolean up, down, left, right;

    private Rectangle btnMenu;
    private Rectangle btnRetry;
    private Rectangle btnExitGame;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        // Load Pixel Assets statically
        PixelAssets.loadAll();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (!isGameOver && btnExitGame != null && btnExitGame.contains(p)) {
                    SoundManager.playClickSound();
                    int result = CustomAlert.showConfirm(
                            GamePanel.this,
                            "EXIT GAME",
                            "Yakin mau keluar?\nScore kamu tidak akan masuk leaderboard.",
                            new String[] { "Keluar", "Batal" });

                    if (result == 0) {
                        if (timer != null)
                            timer.stop();
                        Main.switchPage(Main.DASHBOARD);
                    }
                }

                if (!isGameOver) {
                    for (int i = 0; i < assignments.size(); i++) {
                        Assignment a = assignments.get(i);
                        Rectangle clickArea = new Rectangle(a.getX() - camX - 10, a.getY() - camY - 10,
                                a.getWidth() + 20, a.getHeight() + 20);
                        if (clickArea.contains(p)) {
                            collectBook(i);
                            break;
                        }
                    }
                }

                if (isGameOver) {
                    if (btnRetry != null && btnRetry.contains(p)) {
                        SoundManager.playClickSound();
                        initGame();
                    } else if (btnMenu != null && btnMenu.contains(p)) {
                        SoundManager.playClickSound();
                        Main.switchPage(Main.DASHBOARD);
                    }
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                if ((!isGameOver && btnExitGame != null && btnExitGame.contains(p)) ||
                        (isGameOver && ((btnRetry != null && btnRetry.contains(p))
                                || (btnMenu != null && btnMenu.contains(p))))) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });

        addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (isGameOver && cachedTopScores != null) {
                    leaderboardScrollY += e.getWheelRotation() * 25;
                    if (leaderboardScrollY < 0)
                        leaderboardScrollY = 0;

                    int maxScroll = (cachedTopScores.size() * 35) - 140;
                    if (maxScroll < 0)
                        maxScroll = 0;
                    if (leaderboardScrollY > maxScroll)
                        leaderboardScrollY = maxScroll;

                    repaint();
                }
            }
        });

        initGame();

        timer = new Timer(1000 / FPS, this);
        timer.start();
    }

    public void resetGame(int playerId, String playerName, String avatarPath) {
        this.currentPlayerId = playerId;
        up = false;
        down = false;
        left = false;
        right = false;

        // Dynamic player sprite loading
        PixelAssets.loadPlayerSprites(avatarPath);

        player = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        player.setName(playerName);
        player.setAvatar(avatarPath);

        initGame();
    }

    private void levelUp() {
        currentLevel++;
        targetBooks += 5;
        levelTime += 30;
        timeLeft = levelTime;
        collectedBooks = 0;
        survivalTime += 10;

        for (int i = 0; i < 3; i++) {
            spawnLecturer();
        }
        for (int i = 0; i < 20; i++) {
            spawnAssignment();
        }
    }

    private void initGame() {
        isGameOver = false;
        scoreSaved = false;
        soundPlayed = false;
        leaderboardScrollY = 0;
        survivalTime = 0;
        ticks = 0;
        collectedBooks = 0;
        currentLevel = 1;
        targetBooks = 10;
        levelTime = 60;
        timeLeft = levelTime;

        if (player == null) {
            player = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        }

        lecturers = new ArrayList<>();
        assignments = new ArrayList<>();

        generateMap();

        boolean safe = false;
        int spawnX = WORLD_WIDTH / 2;
        int spawnY = WORLD_HEIGHT / 2;

        while (!safe) {
            player.setX(spawnX);
            player.setY(spawnY);
            safe = true;
            for (Rectangle r : obstacles) {
                if (player.getBounds().intersects(r)) {
                    safe = false;
                    spawnX = random.nextInt(WORLD_WIDTH - 500) + 250;
                    spawnY = random.nextInt(WORLD_HEIGHT - 500) + 250;
                    break;
                }
            }
        }
        player.resetCarriedAssignments();

        for (int i = 0; i < 6; i++) {
            spawnLecturer();
        }

        for (int i = 0; i < 50; i++) {
            spawnAssignment();
        }
    }

    private void generateMap() {
        mapFloor = new int[MAP_ROWS][MAP_COLS];
        mapObject = new int[MAP_ROWS][MAP_COLS];
        obstacles = new ArrayList<>();

        // 1. Fill base grass
        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                mapFloor[r][c] = random.nextInt(10) > 8 ? 1 : 0; // Grass or Grass Flower
            }
        }

        // 2. Main Roads (Cross)
        int roadH = MAP_ROWS / 2;
        int roadV = MAP_COLS / 2;
        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = roadV - 3; c <= roadV + 3; c++) {
                mapFloor[r][c] = (c == roadV) ? 4 : 3; // Asphalt + Mark
            }
        }
        for (int c = 0; c < MAP_COLS; c++) {
            for (int r = roadH - 3; r <= roadH + 3; r++) {
                mapFloor[r][c] = (r == roadH) ? 4 : 3;
            }
        }

        // 3. Paving Sidewalks
        for (int r = 0; r < MAP_ROWS; r++) {
            mapFloor[r][roadV - 4] = 2;
            mapFloor[r][roadV + 4] = 2;
        }
        for (int c = 0; c < MAP_COLS; c++) {
            mapFloor[roadH - 4][c] = 2;
            mapFloor[roadH + 4][c] = 2;
        }

        // 4. PLAZA (Center Intersection)
        for (int r = roadH - 6; r <= roadH + 6; r++) {
            for (int c = roadV - 6; c <= roadV + 6; c++) {
                mapFloor[r][c] = 2; // Paving
            }
        }
        mapObject[roadH][roadV] = 4; // Fountain

        // 5. ZONES OVERHAUL

        // QUADRANT 1: TOP-LEFT (CAMPUS PARK)
        // Varied grass already filled. Just add trees in clusters.
        for (int i = 0; i < 30; i++) {
            int r = random.nextInt(roadH - 10) + 2;
            int c = random.nextInt(roadV - 10) + 2;
            mapObject[r][c] = 2; // Tree
        }

        // QUADRANT 2: TOP-RIGHT (LIBRARY COMPLEX)
        buildLogicalBuilding(roadV + 10, 5, 40, 30, "LIBRARY");

        // QUADRANT 3: BOTTOM-LEFT (FACULTY OF SCIENCE)
        buildLogicalBuilding(5, roadH + 10, 45, 30, "FACULTY");

        // QUADRANT 4: BOTTOM-RIGHT (PARKING & SPORTS)
        // Parking Lot (Right next to road)
        for (int r = roadH + 8; r < roadH + 25; r++) {
            for (int c = roadV + 8; c < roadV + 20; c++) {
                mapFloor[r][c] = 3; // Asphalt
                if ((c - roadV) % 3 == 0) {
                    mapObject[r][c] = 5; // Parking Line (represented as object for now)
                }
            }
        }
        // Sports Field (Futsal/Basket)
        for (int r = roadH + 8; r < roadH + 30; r++) {
            for (int c = roadV + 30; c < roadV + 50; c++) {
                mapFloor[r][c] = 0; // Green floor (Grass tile 0 is green)
                // Boundaries
                if (r == roadH + 8 || r == roadH + 29 || c == roadV + 30 || c == roadV + 49 || c == roadV + 40) {
                    mapObject[r][c] = 5; // White Line
                }
            }
        }

        // 5. Border Walls
        for (int r = 0; r < MAP_ROWS; r++) {
            mapObject[r][0] = 2;
            mapObject[r][MAP_COLS - 1] = 2; // Trees on border
        }
        for (int c = 0; c < MAP_COLS; c++) {
            mapObject[0][c] = 2;
            mapObject[MAP_ROWS - 1][c] = 2;
        }

        // 6. Random Trees in grass
        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                if (mapFloor[r][c] <= 1 && mapObject[r][c] == 0 && random.nextInt(10) > 7) {
                    mapObject[r][c] = 2;
                }
            }
        }

        // Build Collision Data
        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                int obj = mapObject[r][c];
                if (obj > 0) {
                    obstacles.add(new Rectangle(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                }
            }
        }
    }

    private void buildLogicalBuilding(int startC, int startR, int width, int height, String type) {
        int corridorR = startR + height / 2;

        for (int r = startR; r < startR + height; r++) {
            for (int c = startC; c < startC + width; c++) {
                mapFloor[r][c] = 5; // Floor

                // Outer Walls
                if (r == startR || r == startR + height - 1 || c == startC || c == startC + width - 1) {
                    // Main Entrance Gap
                    if (!(r == startR + height - 1 && c > startC + width / 2 - 2 && c < startC + width / 2 + 2)) {
                        mapObject[r][c] = 1; // Wall
                    }
                } else if (r == corridorR) {
                    // Corridor is empty
                } else {
                    // Room Walls (Logical Partitions)
                    if (c % 10 == 0 && c != startC && c != startC + width - 1) {
                        // Door gap in partitions
                        if (r != corridorR - 1 && r != corridorR + 1) {
                            mapObject[r][c] = 1;
                        }
                    }

                    // Furniture based on type
                    if (type.equals("LIBRARY")) {
                        if (c % 2 == 0 && r != corridorR) {
                            mapObject[r][c] = 3; // Bookshelf (Desk tile)
                        }
                    } else {
                        if (r % 3 == 0 && c % 3 == 0 && random.nextBoolean()) {
                            mapObject[r][c] = 3; // Desk
                        }
                    }
                }
            }
        }
    }

    private void spawnLecturer() {
        double baseSpeed = 1.5 + (collectedBooks * 0.2);
        baseSpeed = Math.min(baseSpeed, 5.0);
        int type = random.nextInt(3);
        double finalSpeed = baseSpeed + (type == 1 ? 0.5 : (type == 0 ? -0.5 : 0));

        lecturers.add(new Lecturer(
                random.nextInt(WORLD_WIDTH),
                random.nextInt(WORLD_HEIGHT),
                finalSpeed,
                type));
    }

    private void loadLeaderboardFromDB() {
        new Thread(() -> {
            com.deadline.backend.ScoreService ss = new com.deadline.backend.ScoreService();
            List<Map<String, Object>> scores = ss.getAllScores();
            SwingUtilities.invokeLater(() -> {
                // Ambil 5 teratas saja untuk mini leaderboard di game over
                cachedTopScores = scores.size() > 5 ? scores.subList(0, 5) : scores;
                repaint();
            });
        }).start();
    }

    private void saveFinalScore() {
        if (player != null && player.getName() != null && !player.getName().isEmpty()) {
            int scoreToSave = survivalTime + (collectedBooks * 10);
            System.out.println("Saving final score for: " + player.getName() + " Score: " + scoreToSave);
            LeaderboardManager.saveScore(player.getName(), scoreToSave, survivalTime, player.getAvatar());
        }
    }

    private void updateButtonBounds() {
        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW <= 0)
            panelW = WIDTH;
        if (panelH <= 0)
            panelH = HEIGHT;

        int btnY = panelH / 2 + 200;
        btnRetry = new Rectangle(panelW / 2 - 200, btnY, 180, 50);
        btnMenu = new Rectangle(panelW / 2 + 20, btnY, 180, 50);
        btnExitGame = new Rectangle(panelW - 130, 25, 100, 40);
    }

    private void spawnAssignment() {
        Assignment a;
        boolean safeSpawn;
        do {
            safeSpawn = false;
            int r = random.nextInt(MAP_ROWS);
            int c = random.nextInt(MAP_COLS);

            // Only spawn on logical "reward" zones: Floor (5) or Sports Field/Park (Grass
            // 0/1)
            // AND not on obstacles
            if ((mapFloor[r][c] == 5 || mapFloor[r][c] <= 1) && mapObject[r][c] == 0) {
                a = new Assignment(c * TILE_SIZE + 16, r * TILE_SIZE + 16);
                safeSpawn = true;
            } else {
                a = null; // Dummy to keep loop going
            }
        } while (!safeSpawn || a == null);
        assignments.add(a);
    }

    public void playGameOverSound() {
        SoundManager.playGameOverSound();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        if (isGameOver) {
            if (!soundPlayed) {
                playGameOverSound();
                soundPlayed = true;
            }
            return;
        }

        ticks++;
        if (ticks % FPS == 0) {
            survivalTime++;
            timeLeft--;

            if (timeLeft <= 0) {
                isGameOver = true;
                if (!scoreSaved) {
                    scoreSaved = true;
                    new Thread(() -> {
                        saveFinalScore();
                        loadLeaderboardFromDB();
                    }).start();
                }
            }
        }

        int spawnDelay = Math.max(20, FPS * (2 - collectedBooks / 10));
        int maxLecturers = 6 + (collectedBooks);

        if (ticks % spawnDelay == 0 && lecturers.size() < maxLecturers) {
            spawnLecturer();
        }

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
        player.update();

        player.applyMoveX();
        if (player.getX() < 0 || player.getX() > WORLD_WIDTH - player.getWidth())
            player.rollbackX();
        for (Rectangle rect : obstacles) {
            if (player.getBounds().intersects(rect)) {
                player.rollbackX();
                break;
            }
        }

        player.applyMoveY();
        if (player.getY() < 0 || player.getY() > WORLD_HEIGHT - player.getHeight())
            player.rollbackY();
        for (Rectangle rect : obstacles) {
            if (player.getBounds().intersects(rect)) {
                player.rollbackY();
                break;
            }
        }

        for (Lecturer l : lecturers) {
            l.updateAI(player, lecturers);
            l.update();
            if (l.intersects(player)) {
                isGameOver = true;
                if (!scoreSaved) {
                    scoreSaved = true;
                    new Thread(() -> {
                        saveFinalScore();
                        loadLeaderboardFromDB();
                    }).start();
                }
            }
        }

        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            a.update();
            if (player.intersects(a)) {
                collectBook(i);
                i--;
            }
        }

        camX = player.getX() - getWidth() / 2;
        camY = player.getY() - getHeight() / 2;

        camX = Math.max(0, Math.min(camX, WORLD_WIDTH - getWidth()));
        camY = Math.max(0, Math.min(camY, WORLD_HEIGHT - getHeight()));
    }

    private void collectBook(int index) {
        if (index < 0 || index >= assignments.size())
            return;

        SoundManager.playBookSound();
        collectedBooks++;
        assignments.remove(index);

        if (collectedBooks >= targetBooks) {
            levelUp();
        }

        if (currentPlayerId != -1) {
            new com.deadline.backend.SubmissionService().submitTask(currentPlayerId,
                    "Collected Assignment " + collectedBooks, "SUCCESS");
        }

        for (Lecturer l : lecturers) {
            double baseSpeed = 1.5 + (collectedBooks * 0.2);
            l.setSpeed(Math.min(baseSpeed, 5.0));
        }

        spawnAssignment();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // PIXEL ART RENDERING: Nearest Neighbor & No AA
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        g2.translate(-camX, -camY);

        // Draw Map visible area
        int startCol = Math.max(0, camX / TILE_SIZE);
        int startRow = Math.max(0, camY / TILE_SIZE);
        int endCol = Math.min(MAP_COLS, (camX + getWidth()) / TILE_SIZE + 2);
        int endRow = Math.min(MAP_ROWS, (camY + getHeight()) / TILE_SIZE + 2);

        // 1. Draw Floor
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int tile = mapFloor[r][c];
                int px = c * TILE_SIZE;
                int py = r * TILE_SIZE;
                switch (tile) {
                    case 0:
                        g2.drawImage(PixelAssets.imgGrass, px, py, null);
                        break;
                    case 1:
                        g2.drawImage(PixelAssets.imgGrassFlower, px, py, null);
                        break;
                    case 2:
                        g2.drawImage(PixelAssets.imgPaving, px, py, null);
                        break;
                    case 3:
                        g2.drawImage(PixelAssets.imgRoad, px, py, null);
                        break;
                    case 4:
                        g2.drawImage(PixelAssets.imgRoadMark, px, py, null);
                        break;
                    case 5:
                        g2.drawImage(PixelAssets.imgFloor, px, py, null);
                        break;
                }
            }
        }

        // 2. Draw Objects
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int obj = mapObject[r][c];
                int px = c * TILE_SIZE;
                int py = r * TILE_SIZE;
                switch (obj) {
                    case 1: // Wall
                        g2.setColor(new Color(140, 140, 150));
                        g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                        g2.setColor(new Color(100, 100, 110));
                        g2.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                        break;
                    case 2:
                        g2.drawImage(PixelAssets.imgTree, px, py, null);
                        break;
                    case 3:
                        g2.drawImage(PixelAssets.imgDesk, px, py, null);
                        break;
                    case 4:
                        g2.drawImage(PixelAssets.imgFountain, px, py, null);
                        break;
                    case 5:
                        g2.drawImage(PixelAssets.imgWhiteLine, px, py, null);
                        break;
                }
            }
        }

        // 3. Draw Entities
        for (Assignment a : assignments)
            a.draw(g2);
        for (Lecturer l : lecturers)
            l.draw(g2);
        player.draw(g2);

        g2.translate(camX, camY);

        drawUI(g2);
    }

    private void drawButton(Graphics2D g2, String text, Rectangle rect, Color bgColor) {
        g2.setColor(bgColor.darker());
        g2.fillRect(rect.x + 4, rect.y + 4, rect.width, rect.height);

        g2.setColor(bgColor);
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);

        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.setColor(Color.WHITE);
        g2.drawString(text, rect.x + (rect.width - tw) / 2, rect.y + 32);
    }

    private void drawUI(Graphics2D g2) {
        int panelW = getWidth();
        int panelH = getHeight();
        updateButtonBounds();

        // Pixel UI HUD
        int hudW = 350;
        int hudH = 100;
        int hudX = 25;
        int hudY = 25;

        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(hudX + 5, hudY + 5, hudW, hudH);

        g2.setColor(new Color(30, 30, 40));
        g2.fillRect(hudX, hudY, hudW, hudH);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(hudX, hudY, hudW, hudH);

        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(new Color(180, 200, 255));
        g2.drawString("SURVIVOR: " + player.getName().toUpperCase(), hudX + 20, hudY + 25);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("TARGET", hudX + 20, hudY + 58);

        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(collectedBooks + "/" + targetBooks, hudX + 150, hudY + 59);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("TIME", hudX + 20, hudY + 85);

        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.setColor(new Color(0, 255, 200));
        g2.drawString(timeLeft + "s", hudX + 150, hudY + 86);

        int badgeW = 100;
        int badgeH = 50;
        int badgeX = hudX + hudW - badgeW - 15;
        int badgeY = hudY + (hudH - badgeH) / 2 + 5;

        g2.setColor(new Color(50, 50, 60));
        g2.fillRect(badgeX, badgeY, badgeW, badgeH);
        g2.setColor(Color.WHITE);
        g2.drawRect(badgeX, badgeY, badgeW, badgeH);

        g2.setFont(new Font("Monospaced", Font.BOLD, 12));
        g2.drawString("LEVEL", badgeX + (badgeW - g2.getFontMetrics().stringWidth("LEVEL")) / 2, badgeY + 18);

        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        String lvlStr = String.valueOf(currentLevel);
        g2.drawString(lvlStr, badgeX + (badgeW - g2.getFontMetrics().stringWidth(lvlStr)) / 2, badgeY + 42);

        if (!isGameOver) {
            drawButton(g2, "EXIT", btnExitGame, new Color(180, 40, 40));
        }

        if (isGameOver) {
            g2.setColor(new Color(15, 5, 5, 200));
            g2.fillRect(0, 0, panelW, panelH);

            g2.setFont(new Font("Monospaced", Font.BOLD, 80));
            String overText = "GAME OVER";
            int textWidth = g2.getFontMetrics().stringWidth(overText);

            g2.setColor(Color.BLACK);
            g2.drawString(overText, (panelW - textWidth) / 2 + 5, panelH / 2 - 125);
            g2.setColor(new Color(255, 50, 50));
            g2.drawString(overText, (panelW - textWidth) / 2, panelH / 2 - 130);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 24));
            String subText = "Yahh, telat submit tugas";
            g2.drawString(subText, (panelW - g2.getFontMetrics().stringWidth(subText)) / 2, panelH / 2 - 80);

            drawButton(g2, "RETRY", btnRetry, new Color(40, 150, 40));
            drawButton(g2, "MENU", btnMenu, new Color(40, 40, 150));

            if (cachedTopScores != null && !cachedTopScores.isEmpty()) {
                int boardY = panelH / 2 - 10;
                g2.setFont(new Font("Monospaced", Font.BOLD, 20));
                g2.setColor(Color.YELLOW);
                String lbTitle = "TOP SURVIVORS";
                g2.drawString(lbTitle, (panelW - g2.getFontMetrics().stringWidth(lbTitle)) / 2, boardY);

                int startY = boardY + 30;
                g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
                g2.setColor(Color.WHITE);
                for (int i = 0; i < cachedTopScores.size(); i++) {
                    Map<String, Object> row = cachedTopScores.get(i);
                    String pName = (String) row.get("player_name");
                    int pScore = ((Number) row.get("score")).intValue();
                    String line = (i + 1) + ". " + pName + " - " + pScore + " pts";
                    g2.drawString(line, (panelW - g2.getFontMetrics().stringWidth(line)) / 2, startY + (i * 25));
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
            up = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            down = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            left = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            right = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP)
            up = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN)
            down = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT)
            left = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT)
            right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}