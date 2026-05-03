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
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    // MAP GRID SYSTEM
    private static final int TILE_SIZE = 64;
    private static final int MAP_COLS = 80;
    private static final int MAP_ROWS = 60;
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

    private BufferedImage retryImg;
    private BufferedImage menuImg;
    private BufferedImage exitImg;
    private BufferedImage gameOverImg;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        // Load Pixel Assets statically
        PixelAssets.loadAll();
        loadButtonAssets();

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

        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                mapFloor[r][c] = 0; 
            }
        }
        
        for (int r = 0; r < MAP_ROWS; r++) {
            mapObject[r][0] = 1;
            mapObject[r][MAP_COLS - 1] = 1;
        }
        for (int c = 0; c < MAP_COLS; c++) {
            mapObject[0][c] = 1;
            mapObject[MAP_ROWS - 1][c] = 1;
        }

        int centerC = MAP_COLS / 2;
        int centerR = MAP_ROWS / 2;
        int gardenW = 16;
        int gardenH = 12;
        buildGarden(centerC - gardenW/2, centerR - gardenH/2, gardenW, gardenH);

        buildRoom(2, 2, 18, 12, "KELAS");
        buildRoom(22, 2, 18, 12, "KELAS");

        buildRoom(MAP_COLS - 20, 2, 18, 14, "LAB");

        buildRoom(2, MAP_ROWS - 16, 20, 14, "KANTIN");
        buildRoom(24, MAP_ROWS - 12, 10, 10, "TOILET");

        buildRoom(MAP_COLS - 24, MAP_ROWS - 22, 22, 20, "PERPUS");
        buildRoom(MAP_COLS - 20, 18, 18, 12, "DOSEN");
        buildRoom(2, 16, 16, 10, "ADMIN");

        buildRoom(centerC - 10, MAP_ROWS - 14, 20, 12, "LOBBY");

        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                int obj = mapObject[r][c];
                if (obj == 1 || obj == 2 || obj == 3 || obj >= 10) {
                    obstacles.add(new Rectangle(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                }
            }
        }
    }

    private void buildGarden(int startC, int startR, int width, int height) {
        for (int r = startR; r < startR + height; r++) {
            for (int c = startC; c < startC + width; c++) {
                mapFloor[r][c] = (random.nextBoolean()) ? 2 : 3; 
                if (r == startR || r == startR + height - 1 || c == startC || c == startC + width - 1) {
                    if (c != startC + width / 2 && r != startR + height / 2) {
                        mapObject[r][c] = 4; 
                    }
                } else {
                    if (r == startR + height/2 && c == startC + width/2) {
                        mapObject[r][c] = 3; 
                        mapObject[r][c+1] = 3;
                        mapObject[r+1][c] = 3;
                        mapObject[r+1][c+1] = 3;
                    } else if (mapObject[r][c] == 0 && random.nextInt(100) < 15) {
                        mapObject[r][c] = 2; 
                    }
                }
            }
        }
    }

    private void buildRoom(int startC, int startR, int width, int height, String type) {
        int doorC = startC + width / 2;
        int doorR = startR + height - 1; 

        if (type.equals("LOBBY")) {
            doorR = startR; 
        } else if (startR > MAP_ROWS / 2) {
            doorR = startR; 
        }

        for (int r = startR; r < startR + height; r++) {
            for (int c = startC; c < startC + width; c++) {
                if (type.equals("TOILET")) {
                    mapFloor[r][c] = 4;
                } else if (type.equals("LOBBY")) {
                    mapFloor[r][c] = 5;
                } else {
                    mapFloor[r][c] = 1; 
                }

                if (r == startR || r == startR + height - 1 || c == startC || c == startC + width - 1) {
                    if (r == doorR && (c == doorC || c == doorC + 1)) {
                        mapFloor[r][c] = 0; 
                    } else {
                        mapObject[r][c] = 1; 
                    }
                } else {
                    if (type.equals("KELAS")) {
                        if (r >= startR + 2 && r < startR + height - 2 && c >= startC + 2 && c < startC + width - 2) {
                            if (r % 3 == 0 && c % 3 == 0) {
                                mapObject[r][c] = 10; 
                                mapObject[r+1][c] = 11; 
                            }
                        }
                    } else if (type.equals("LAB")) {
                        if (r >= startR + 2 && r < startR + height - 2 && c >= startC + 2 && c < startC + width - 2) {
                            if (r % 3 == 0 && c % 2 == 0) {
                                mapObject[r][c] = 12; 
                            }
                        }
                    } else if (type.equals("PERPUS")) {
                        if (r >= startR + 2 && r < startR + height - 2 && c >= startC + 2 && c < startC + width - 2) {
                            if (c % 4 == 0) {
                                mapObject[r][c] = 13; 
                                mapObject[r][c+1] = 13; 
                            } else if (r % 4 == 0 && c % 4 == 2) {
                                mapObject[r][c] = 10; 
                            }
                        }
                    } else if (type.equals("KANTIN")) {
                        if (r >= startR + 2 && r < startR + height - 2 && c >= startC + 2 && c < startC + width - 2) {
                            if (r % 5 == 0 && c % 5 == 0) {
                                mapObject[r][c] = 14; 
                                mapObject[r][c+1] = 14;
                                mapObject[r+1][c] = 11; 
                                mapObject[r-1][c+1] = 11;
                            }
                        }
                    } else if (type.equals("TOILET")) {
                        if (r == startR + 1 && c >= startC + 1 && c < startC + width - 1) {
                            if (c % 2 == 0) mapObject[r][c] = 15; 
                        }
                        if (r == startR + height - 2 && c >= startC + 1 && c < startC + width - 1) {
                            if (c % 2 == 0 && c != doorC && c != doorC + 1) mapObject[r][c] = 15; 
                        }
                    } else if (type.equals("DOSEN") || type.equals("ADMIN")) {
                        if (r >= startR + 2 && r < startR + height - 2 && c >= startC + 2 && c < startC + width - 2) {
                            if (r % 4 == 0 && c % 4 == 0) {
                                mapObject[r][c] = 10;
                                mapObject[r][c+1] = 12; 
                                mapObject[r+1][c] = 11; 
                            }
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

    private void loadButtonAssets() {
        retryImg = loadAndScale("/assets/buttons/btn_try_normal.png", 170, 55);
        menuImg = loadAndScale("/assets/buttons/btn_menu_normal.png", 170, 55);
        exitImg = loadAndScale("/assets/buttons/btn_exit_normal.png", 100, 40);
        
        // Load Game Over title asset
        gameOverImg = loadOriginalImage("/assets/game_over.png");
    }

    private BufferedImage loadOriginalImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return null;
            ImageIcon icon = new ImageIcon(url);
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(icon.getImage(), 0, 0, null);
            g2.dispose();
            return bi;
        } catch (Exception e) {
            return null;
        }
    }

    private BufferedImage loadAndScale(String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return null;
            ImageIcon icon = new ImageIcon(url);
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(icon.getImage(), 0, 0, w, h, null);
            g2.dispose();
            return bi;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateButtonBounds() {
        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW <= 0)
            panelW = WIDTH;
        if (panelH <= 0)
            panelH = HEIGHT;

        int btnW = 170; // Match InputPlayerPanel
        int btnH = 55;
        int gap = 20;
        int totalW = (btnW * 2) + gap;
        int startX = (panelW - totalW) / 2;
        int btnY = panelH - 160; // Pushed up from previous 100

        btnRetry = new Rectangle(startX, btnY, btnW, btnH);
        btnMenu = new Rectangle(startX + btnW + gap, btnY, btnW, btnH);
        btnExitGame = new Rectangle(panelW - 130, 25, 100, 40);
    }

    private void spawnAssignment() {
        Assignment a;
        boolean safeSpawn;
        do {
            safeSpawn = false;
            int r = random.nextInt(MAP_ROWS);
            int c = random.nextInt(MAP_COLS);

            if ((mapFloor[r][c] == 1 || mapFloor[r][c] == 4 || mapFloor[r][c] == 5) && mapObject[r][c] == 0) {
                a = new Assignment(c * TILE_SIZE + 16, r * TILE_SIZE + 16);
                safeSpawn = true;
            } else {
                a = null; 
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
                
                if (tile == 0) { // Corridor
                    g2.setColor(((r + c) % 2 == 0) ? new Color(200, 200, 200) : new Color(190, 190, 190));
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                } else if (tile == 1) { // Room
                    g2.setColor(((r + c) % 2 == 0) ? new Color(245, 222, 179) : new Color(235, 212, 169)); // Cream
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                } else if (tile == 2) { // Garden Grass 1
                    g2.setColor(new Color(60, 140, 60));
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                } else if (tile == 3) { // Garden Grass 2
                    g2.setColor(new Color(50, 130, 50));
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                } else if (tile == 4) { // Toilet
                    g2.setColor(((r + c) % 2 == 0) ? new Color(220, 240, 255) : new Color(200, 220, 235));
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                } else if (tile == 5) { // Lobby
                    g2.setColor(((r + c) % 2 == 0) ? new Color(250, 250, 250) : new Color(220, 220, 220));
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // 2. Draw Objects
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int obj = mapObject[r][c];
                if (obj == 0) continue;
                
                int px = c * TILE_SIZE;
                int py = r * TILE_SIZE;

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRect(px + 4, py + 8, TILE_SIZE, TILE_SIZE);

                if (obj == 1) { // Wall
                    g2.setColor(new Color(68, 68, 68)); // #444
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE - 4);
                    // Depth effect
                    g2.setColor(new Color(40, 40, 40));
                    g2.fillRect(px, py + TILE_SIZE - 4, TILE_SIZE, 4);
                    g2.setColor(new Color(20, 20, 20));
                    g2.drawRect(px, py, TILE_SIZE, TILE_SIZE);
                } else if (obj == 2) { // Tree
                    // Trunk
                    g2.setColor(new Color(101, 67, 33));
                    g2.fillRect(px + 24, py + 32, 16, 32);
                    // Leaves
                    g2.setColor(new Color(34, 139, 34));
                    g2.fillRect(px + 8, py, 48, 48);
                    g2.setColor(new Color(0, 100, 0));
                    g2.drawRect(px + 8, py, 48, 48);
                } else if (obj == 3) { // Fountain part
                    g2.setColor(new Color(150, 150, 150));
                    g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);
                    g2.setColor(new Color(0, 150, 255));
                    g2.fillOval(px + 8, py + 8, TILE_SIZE - 16, TILE_SIZE - 16);
                } else if (obj == 4) { // Plant Border
                    g2.setColor(new Color(40, 100, 40));
                    g2.fillRect(px, py + 16, TILE_SIZE, TILE_SIZE - 16);
                } else if (obj == 10) { // Desk
                    g2.setColor(new Color(139, 69, 19));
                    g2.fillRect(px + 4, py + 16, TILE_SIZE - 8, TILE_SIZE - 32);
                    g2.setColor(new Color(50, 20, 10));
                    g2.drawRect(px + 4, py + 16, TILE_SIZE - 8, TILE_SIZE - 32);
                } else if (obj == 11) { // Chair
                    g2.setColor(new Color(100, 50, 20));
                    g2.fillRect(px + 16, py + 8, 32, 32);
                } else if (obj == 12) { // PC Monitor
                    g2.setColor(new Color(139, 69, 19));
                    g2.fillRect(px + 4, py + 16, TILE_SIZE - 8, TILE_SIZE - 32);
                    g2.setColor(Color.DARK_GRAY);
                    g2.fillRect(px + 16, py + 8, 32, 16);
                    g2.setColor(Color.CYAN);
                    g2.fillRect(px + 18, py + 10, 28, 12);
                } else if (obj == 13) { // Bookshelf
                    g2.setColor(new Color(80, 40, 10));
                    g2.fillRect(px + 4, py, TILE_SIZE - 8, TILE_SIZE);
                    g2.setColor(Color.RED);
                    g2.fillRect(px + 10, py + 10, 10, 20);
                    g2.setColor(Color.BLUE);
                    g2.fillRect(px + 25, py + 10, 10, 20);
                    g2.setColor(Color.YELLOW);
                    g2.fillRect(px + 40, py + 10, 10, 20);
                    g2.setColor(Color.BLACK);
                    g2.drawRect(px + 4, py, TILE_SIZE - 8, TILE_SIZE);
                } else if (obj == 14) { // Big Table
                    g2.setColor(new Color(200, 180, 150));
                    g2.fillRect(px, py + 8, TILE_SIZE, TILE_SIZE - 16);
                    g2.setColor(new Color(100, 80, 50));
                    g2.drawRect(px, py + 8, TILE_SIZE, TILE_SIZE - 16);
                } else if (obj == 15) { // Toilet Cubicle / Sink
                    g2.setColor(Color.WHITE);
                    g2.fillRect(px + 8, py + 8, TILE_SIZE - 16, TILE_SIZE - 16);
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.drawRect(px + 8, py + 8, TILE_SIZE - 16, TILE_SIZE - 16);
                    g2.setColor(Color.CYAN);
                    g2.fillRect(px + 16, py + 16, TILE_SIZE - 32, TILE_SIZE - 32);
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
            if (exitImg != null) {
                g2.drawImage(exitImg, btnExitGame.x, btnExitGame.y, null);
            } else {
                drawButton(g2, "EXIT", btnExitGame, new Color(180, 40, 40));
            }
        }

        if (isGameOver) {
            g2.setColor(new Color(15, 5, 5, 220));
            g2.fillRect(0, 0, panelW, panelH);

            int currentY = 60;

            // 1. GAME OVER IMAGE (Much Smaller)
            if (gameOverImg != null) {
                float scale = 0.35f; 
                int tw = (int)(gameOverImg.getWidth() * scale);
                int th = (int)(gameOverImg.getHeight() * scale);
                int tx = (panelW - tw) / 2;
                g2.drawImage(gameOverImg, tx, currentY, tw, th, null);
                currentY += th + 15;
            }

            // 2. SUBTEXT
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 22));
            String subText = "Yahh, telat submit tugas";
            g2.drawString(subText, (panelW - g2.getFontMetrics().stringWidth(subText)) / 2, currentY);
            currentY += 50;

            // 3. MINI LEADERBOARD
            if (cachedTopScores != null && !cachedTopScores.isEmpty()) {
                g2.setFont(new Font("Monospaced", Font.BOLD, 20));
                g2.setColor(Color.YELLOW);
                String lbTitle = "TOP 5 SURVIVORS";
                g2.drawString(lbTitle, (panelW - g2.getFontMetrics().stringWidth(lbTitle)) / 2, currentY);
                currentY += 30;

                g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
                g2.setColor(Color.WHITE);
                for (int i = 0; i < cachedTopScores.size(); i++) {
                    Map<String, Object> row = cachedTopScores.get(i);
                    String pName = (String) row.get("player_name");
                    int pScore = ((Number) row.get("score")).intValue();
                    String line = (i + 1) + ". " + String.format("%-15s", pName) + " - " + pScore + " pts";
                    g2.drawString(line, (panelW - g2.getFontMetrics().stringWidth(line)) / 2, currentY + (i * 25));
                }
            }

            // 4. BUTTONS
            if (retryImg != null) {
                g2.drawImage(retryImg, btnRetry.x, btnRetry.y, null);
            }
            if (menuImg != null) {
                g2.drawImage(menuImg, btnMenu.x, btnMenu.y, null);
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