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
    private static final int MAP_ROWS = 80;
    private static final int WORLD_WIDTH = MAP_COLS * TILE_SIZE;
    private static final int WORLD_HEIGHT = MAP_ROWS * TILE_SIZE;

    private Font pixelFont;
    private final int FPS = 60;

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
    private double currentZoom = 2.0;
    private double targetZoom = 2.0;
    private double actualCamX = 0;
    private double actualCamY = 0;

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
        loadPixelFont();
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

    private void spawnLecturer() {
        double baseSpeed = 2.5 + (currentLevel * 0.5);
        int type = random.nextInt(3);
        int sx = 0, sy = 0;
        boolean safe = false;
        int attempts = 0;

        while (!safe && attempts < 1000) {
            attempts++;
            int r = random.nextInt(MAP_ROWS);
            int c = random.nextInt(MAP_COLS);
            
            // Far spawn check
            double dx = (c * TILE_SIZE) - player.getX();
            double dy = (r * TILE_SIZE) - player.getY();
            double dist = Math.sqrt(dx*dx + dy*dy);
            
            if (dist > 600 && mapFloor[r][c] == 0 && mapObject[r][c] == 0) {
                sx = c * TILE_SIZE;
                sy = r * TILE_SIZE;
                safe = true;
            }
        }

        if (safe) {
            Lecturer l = new Lecturer(sx, sy, baseSpeed, type);
            l.setPathFinder(pathFinder);
            lecturers.add(l);
        }
    }

    private void levelUp() {
        currentLevel++;
        generateMap(); 
        SoundManager.playBookSound();
        
        // Bonus challenge on level up
        for (int i = 0; i < 2; i++) spawnLecturer();
        
        System.out.println("🚀 Level Up! Now Level: " + currentLevel);
    }

    private void initGame() {
        isGameOver = false;
        scoreSaved = false;
        soundPlayed = false;
        leaderboardScrollY = 0;
        survivalTime = 0;
        ticks = 0;
        collectedBooks = 0;
        targetBooks = 10;
        
        generateMap();

        if (player == null) {
            player = new Player(WORLD_WIDTH / 2, WORLD_HEIGHT / 2);
        }

        // Safe spawn for player (Center of Lobby)
        player.setX(WORLD_WIDTH / 2);
        player.setY(WORLD_HEIGHT / 2);
        player.resetCarriedAssignments();

        actualCamX = player.getX() - (WIDTH / currentZoom) / 2;
        actualCamY = player.getY() - (HEIGHT / currentZoom) / 2;

        lecturers = new ArrayList<>();
        assignments = new ArrayList<>();

        // Start with 1 lecturer
        spawnLecturer();
        
        // Spawn initial assignments
        for (int i = 0; i < 20; i++) spawnAssignment();
    }

    private PathFinder pathFinder;

    private void generateMap() {
        obstacles = new ArrayList<>();
        MapGenerator mg = new MapGenerator(MAP_ROWS, MAP_COLS);
        mg.generate();
        
        mapFloor = mg.getFloor();
        mapObject = mg.getObjects();
        int[][] collisionData = mg.getCollision();
        pathFinder = new PathFinder(collisionData);

        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                if (collisionData[r][c] == 1) {
                    obstacles.add(new Rectangle(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE));
                }
            }
        }
    }

    private void loadLeaderboardFromDB() {
        new Thread(() -> {
            com.deadline.backend.ScoreService ss = new com.deadline.backend.ScoreService();
            List<Map<String, Object>> scores = ss.getAllScores();
            SwingUtilities.invokeLater(() -> {
                cachedTopScores = scores.size() > 5 ? scores.subList(0, 5) : scores;
                repaint();
            });
        }).start();
    }

    private void saveFinalScore() {
        if (player != null && player.getName() != null && !player.getName().isEmpty()) {
            int scoreToSave = survivalTime + (collectedBooks * 10);
            LeaderboardManager.saveScore(player.getName(), scoreToSave, survivalTime, player.getAvatar());
        }
    }

    private void loadButtonAssets() {
        retryImg = loadAndScale("/assets/ui/buttons/btn_try_normal.png", 170, 55);
        menuImg = loadAndScale("/assets/ui/buttons/btn_menu_normal.png", 170, 55);
        exitImg = loadAndScale("/assets/ui/buttons/btn_exit_normal.png", 100, 40);
        gameOverImg = loadOriginalImage("/assets/ui/panels/game_over.png");
    }

    private BufferedImage loadOriginalImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url == null) return null;
            ImageIcon icon = new ImageIcon(url);
            BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.drawImage(icon.getImage(), 0, 0, null);
            g2.dispose();
            return bi;
        } catch (Exception e) { return null; }
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
        } catch (Exception e) { return null; }
    }

    private void updateButtonBounds() {
        int panelW = getWidth();
        int panelH = getHeight();
        if (panelW <= 0) panelW = WIDTH;
        if (panelH <= 0) panelH = HEIGHT;

        int btnW = 170, btnH = 55, gap = 20;
        int totalW = (btnW * 2) + gap;
        int startX = (panelW - totalW) / 2;
        int btnY = panelH - 160;

        btnRetry = new Rectangle(startX, btnY, btnW, btnH);
        btnMenu = new Rectangle(startX + btnW + gap, btnY, btnW, btnH);
        btnExitGame = new Rectangle(panelW - 130, 25, 100, 40);
    }

    private void spawnAssignment() {
        Assignment a = null;
        boolean safeSpawn = false;
        int attempts = 0;
        
        while (!safeSpawn && attempts < 500) {
            attempts++;
            int r = random.nextInt(MAP_ROWS);
            int c = random.nextInt(MAP_COLS);
            
            // Spawn on empty floor
            if (mapFloor[r][c] != 3 && mapObject[r][c] == 0) {
                a = new Assignment(c * TILE_SIZE, r * TILE_SIZE);
                safeSpawn = true;
            }
        }
        
        if (a != null) assignments.add(a);
    }

    public void playGameOverSound() { SoundManager.playGameOverSound(); }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateGame();
        repaint();
    }

    private void updateGame() {
        if (isGameOver) {
            if (!soundPlayed) { playGameOverSound(); soundPlayed = true; }
            return;
        }

        ticks++;
        if (ticks % FPS == 0) survivalTime++;

        int dx = 0, dy = 0;
        if (up) dy--; if (down) dy++; if (left) dx--; if (right) dx++;

        player.setDirection(dx, dy);
        player.update();

        player.applyMoveX();
        if (player.getX() < 0 || player.getX() > WORLD_WIDTH - player.getWidth()) player.rollbackX();
        for (Rectangle rect : obstacles) if (player.getBounds().intersects(rect)) { player.rollbackX(); break; }

        player.applyMoveY();
        if (player.getY() < 0 || player.getY() > WORLD_HEIGHT - player.getHeight()) player.rollbackY();
        for (Rectangle rect : obstacles) if (player.getBounds().intersects(rect)) { player.rollbackY(); break; }

        for (Lecturer l : lecturers) {
            l.updateAI(player, lecturers, obstacles);
            l.update();
            if (l.intersects(player)) {
                isGameOver = true;
                if (!scoreSaved) {
                    scoreSaved = true;
                    new Thread(() -> { saveFinalScore(); loadLeaderboardFromDB(); }).start();
                }
            }
        }

        for (int i = 0; i < assignments.size(); i++) {
            Assignment a = assignments.get(i);
            a.update();
            if (player.intersects(a)) { collectBook(i); i--; }
        }

        // --- CAMERA SYSTEM ---
        targetZoom = 2.0;
        currentZoom += (targetZoom - currentZoom) * 0.1;

        int viewW = (int) (getWidth() / currentZoom);
        int viewH = (int) (getHeight() / currentZoom);
        
        int targetCamX = player.getX() - viewW / 2;
        int targetCamY = player.getY() - viewH / 2;
        
        // Clamp camera
        targetCamX = Math.max(0, Math.min(targetCamX, WORLD_WIDTH - viewW));
        targetCamY = Math.max(0, Math.min(targetCamY, WORLD_HEIGHT - viewH));

        // Smooth camera movement
        actualCamX += (targetCamX - actualCamX) * 0.1;
        actualCamY += (targetCamY - actualCamY) * 0.1;
        
        camX = (int) actualCamX;
        camY = (int) actualCamY;
    }

    private void collectBook(int index) {
        if (index < 0 || index >= assignments.size()) return;
        SoundManager.playBookSound();
        collectedBooks++;
        assignments.remove(index);

        if (collectedBooks >= targetBooks) levelUp();

        // 1 BOOK = 1 NEW DOSEN
        spawnLecturer();
        spawnAssignment();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        
        g2.scale(currentZoom, currentZoom);
        g2.translate(-camX, -camY);

        int viewW = (int) (getWidth() / currentZoom);
        int viewH = (int) (getHeight() / currentZoom);

        int startCol = Math.max(0, camX / TILE_SIZE);
        int startRow = Math.max(0, camY / TILE_SIZE);
        int endCol = Math.min(MAP_COLS, (camX + viewW) / TILE_SIZE + 2);
        int endRow = Math.min(MAP_ROWS, (camY + viewH) / TILE_SIZE + 2);

        // Draw Floor
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int tile = mapFloor[r][c];
                BufferedImage img = null;
                switch(tile) {
                    case 0: img = PixelAssets.imgFloorWhite; break;
                    case 1: img = PixelAssets.imgFloorLab; break;
                    case 2: img = PixelAssets.imgFloorLibrary; break;
                    case 3: img = PixelAssets.imgFloorDark; break;
                }
                if (img != null) g2.drawImage(img, c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
            }
        }

        // Draw Objects
        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                int obj = mapObject[r][c];
                if (obj == 0) continue;
                BufferedImage img = null;
                switch(obj) {
                    case 1: img = PixelAssets.imgWallTop; break;
                    case 2: img = PixelAssets.imgWallSide; break;
                    case 3: img = PixelAssets.imgWallCenter; break;
                    case 4: img = PixelAssets.imgCornerLeft; break;
                    case 5: img = PixelAssets.imgCornerRight; break;
                    case 6: img = PixelAssets.imgDoorClass; break;
                    case 7: img = PixelAssets.imgDoorLabLibrary; break;
                    case 8: img = PixelAssets.imgPlant; break;
                    case 9: img = PixelAssets.imgVending; break;
                    case 10: img = PixelAssets.imgBoard; break;
                    case 11: img = PixelAssets.imgLamp; break;
                    case 12: img = PixelAssets.imgMeja; break;
                    case 13: img = PixelAssets.imgKursi; break;
                    case 14: img = PixelAssets.imgMejaLab; break;
                    case 15: img = PixelAssets.imgRakBuku; break;
                    case 16: img = PixelAssets.imgBangkuLobby; break;
                    case 17: img = PixelAssets.imgMejaDosen; break;
                    case 18: img = PixelAssets.imgWallBottom; break;
                }
                if (img != null) {
                    g2.drawImage(img, c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
                }
            }
        }

        for (Assignment a : assignments) a.draw(g2);
        for (Lecturer l : lecturers) { l.setPathFinder(pathFinder); l.draw(g2); }
        player.draw(g2);

        g2.translate(camX, camY);
        applyLighting(g2);
        g2.scale(1.0/currentZoom, 1.0/currentZoom);
        drawUI(g2);
    }

    private void applyLighting(Graphics2D g2) {
        int w = getWidth();
        int h = getHeight();
        
        BufferedImage overlay = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gL = overlay.createGraphics();
        
        // 1. CINEMATIC AMBIENCE (Deep Dark Blue, Less Opaque)
        gL.setColor(new Color(2, 2, 10, 150)); 
        gL.fillRect(0, 0, w, h);
        
        // 2. PLAYER SPOTLIGHT (Tense but clear)
        gL.setComposite(java.awt.AlphaComposite.DstOut);
        int px = (int)((player.getX() - camX + player.getWidth()/2) * currentZoom);
        int py = (int)((player.getY() - camY + player.getHeight()/2) * currentZoom);
        
        drawRadialGradient(gL, px, py, (int)(300 * currentZoom), new Color(255, 255, 255, 255));
        drawRadialGradient(gL, px, py, (int)(600 * currentZoom), new Color(255, 255, 220, 120));
        
        // 3. WARM LAMP GLOWS
        int startCol = Math.max(0, camX / TILE_SIZE);
        int startRow = Math.max(0, camY / TILE_SIZE);
        int endCol = Math.min(MAP_COLS, (camX + (int)(w/currentZoom)) / TILE_SIZE + 2);
        int endRow = Math.min(MAP_ROWS, (camY + (int)(h/currentZoom)) / TILE_SIZE + 2);

        for (int r = startRow; r < endRow; r++) {
            for (int c = startCol; c < endCol; c++) {
                if (mapObject[r][c] == 11) { // LAMP
                    int lx = (int)((c * TILE_SIZE - camX + TILE_SIZE / 2) * currentZoom);
                    int ly = (int)((r * TILE_SIZE - camY + TILE_SIZE / 2) * currentZoom);
                    drawRadialGradient(gL, lx, ly, (int)(160 * currentZoom), new Color(255, 180, 80, 255));
                }
            }
        }

        gL.dispose();
        g2.drawImage(overlay, 0, 0, null);
        
        // 4. VIGNETTE
        float[] dist = {0.3f, 1.0f};
        Color[] colors = {new Color(0,0,0,0), new Color(0,0,0,120)};
        java.awt.RadialGradientPaint rgp = new java.awt.RadialGradientPaint(
            new Point(w/2, h/2), (float)w * 0.7f, dist, colors);
        g2.setPaint(rgp);
        g2.fillRect(0, 0, w, h);
    }

    private void drawHUD(Graphics2D g) {
        int w = getWidth();
        int h = getHeight();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Professional Dark Translucent Style
        Color boxColor = new Color(0, 0, 0, 190);
        Color borderColor = new Color(255, 255, 255, 100);
        g.setFont(new Font("Arial", Font.BOLD, 14));

        // 1. TOP-LEFT: STATUS BOX
        g.setColor(boxColor);
        g.fillRoundRect(20, 20, 200, 110, 10, 10);
        g.setColor(borderColor);
        g.drawRoundRect(20, 20, 200, 110, 10, 10);
        
        g.setColor(Color.WHITE);
        g.drawString("PLAYER", 40, 45);
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.drawString(player.getName().toLowerCase(), 40, 65);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("LEVEL: " + currentLevel, 40, 85);
        g.setColor(new Color(255, 215, 0));
        g.drawString("BUKU: " + collectedBooks + " / " + targetBooks, 40, 105);

        // 2. TOP-CENTER: TIMER BOX
        g.setColor(boxColor);
        g.fillRoundRect(w/2 - 60, 20, 120, 50, 10, 10);
        g.setColor(borderColor);
        g.drawRoundRect(w/2 - 60, 20, 120, 50, 10, 10);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String timeStr = String.format("%02d:%02d", timeLeft/60, timeLeft%60);
        g.drawString(timeStr, w/2 - 25, 53);

        // 3. BOTTOM-LEFT: TUJUAN BOX
        g.setColor(boxColor);
        g.fillRoundRect(20, h - 130, 220, 110, 10, 10);
        g.setColor(borderColor);
        g.drawRoundRect(20, h - 130, 220, 110, 10, 10);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.drawString("TUJUAN", 40, h - 105);
        g.setFont(new Font("Arial", Font.PLAIN, 11));
        g.drawString("• Kumpulkan semua buku", 40, h - 85);
        g.drawString("• Hindari dosen", 40, h - 70);
        g.drawString("• Naik level sebanyak mungkin", 40, h - 55);
        g.drawString("• Dapatkan skor tertinggi!", 40, h - 40);

        // 4. BOTTOM-RIGHT: PROGRESS BOX
        g.setColor(boxColor);
        g.fillRoundRect(w - 240, h - 100, 220, 80, 10, 10);
        g.setColor(borderColor);
        g.drawRoundRect(w - 240, h - 100, 220, 80, 10, 10);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("PROGRESS", w - 220, h - 75);
        g.drawString("BUKU: " + collectedBooks + " / " + targetBooks, w - 220, h - 60);
        
        // Bar
        g.setColor(new Color(40, 40, 40));
        g.fillRect(w - 220, h - 50, 180, 8);
        g.setColor(new Color(0, 255, 100));
        int progW = (int)(180 * ((double)collectedBooks / targetBooks));
        g.fillRect(w - 220, h - 50, progW, 8);
        
        g.setColor(new Color(255, 100, 100));
        g.drawString("DOSEN: " + lecturers.size(), w - 220, h - 30);
    }

    private void drawRadialGradient(Graphics2D g2, int x, int y, int radius, Color color) {
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {color, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)};
        java.awt.RadialGradientPaint rgp = new java.awt.RadialGradientPaint(
            new Point(x, y), radius, dist, colors);
        g2.setPaint(rgp);
        g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
    }

    private void drawButton(Graphics2D g2, String text, Rectangle rect, Color bgColor) {
        g2.setColor(bgColor.darker());
        g2.fillRect(rect.x + 4, rect.y + 4, rect.width, rect.height);

        g2.setColor(bgColor);
        g2.fillRect(rect.x, rect.y, rect.width, rect.height);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(rect.x, rect.y, rect.width, rect.height);

        if (pixelFont != null) {
            g2.setFont(pixelFont.deriveFont(Font.BOLD, 18));
        } else {
            g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        }
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.setColor(Color.WHITE);
        g2.drawString(text, rect.x + (rect.width - tw) / 2, rect.y + 32);
    }

    private void loadPixelFont() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/assets/ui/fonts/pixel.ttf");
            if (is != null) {
                pixelFont = Font.createFont(Font.TRUETYPE_FONT, is);
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading pixel font in GamePanel");
        }
    }

    private void drawUI(Graphics2D g2) {
        int panelW = getWidth();
        int panelH = getHeight();
        updateButtonBounds();

        drawHUD(g2); // Professional HUD

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
            if (pixelFont != null) {
                g2.setFont(pixelFont.deriveFont(Font.PLAIN, 22));
            } else {
                g2.setFont(new Font("Monospaced", Font.PLAIN, 22));
            }
            String subText = "Yahh, telat submit tugas";
            g2.drawString(subText, (panelW - g2.getFontMetrics().stringWidth(subText)) / 2, currentY);
            currentY += 50;

            // 3. MINI LEADERBOARD
            if (cachedTopScores != null && !cachedTopScores.isEmpty()) {
                if (pixelFont != null) {
                    g2.setFont(pixelFont.deriveFont(Font.BOLD, 20));
                } else {
                    g2.setFont(new Font("Monospaced", Font.BOLD, 20));
                }
                g2.setColor(Color.YELLOW);
                String lbTitle = "TOP 5 SURVIVORS";
                g2.drawString(lbTitle, (panelW - g2.getFontMetrics().stringWidth(lbTitle)) / 2, currentY);
                currentY += 30;

                if (pixelFont != null) {
                    g2.setFont(pixelFont.deriveFont(Font.PLAIN, 16));
                } else {
                    g2.setFont(new Font("Monospaced", Font.PLAIN, 16));
                }
                g2.setColor(Color.WHITE);
                for (int i = 0; i < cachedTopScores.size(); i++) {
                    Map<String, Object> row = cachedTopScores.get(i);
                    String pName = (String) row.get("player_name");
                    int pScore = ((Number) row.get("score")).intValue();
                    String line = String.format("%d. %-15s %10d", i + 1, pName, pScore);
                    g2.drawString(line, (panelW - g2.getFontMetrics().stringWidth(line)) / 2, currentY);
                    currentY += 25;
                }
                currentY += 20;
            }

            // 4. BUTTONS (RETRY | MENU)
            if (btnRetry != null && btnMenu != null) {
                if (retryImg != null) {
                    g2.drawImage(retryImg, btnRetry.x, btnRetry.y, null);
                } else {
                    drawButton(g2, "RETRY", btnRetry, new Color(40, 160, 40));
                }

                if (menuImg != null) {
                    g2.drawImage(menuImg, btnMenu.x, btnMenu.y, null);
                } else {
                    drawButton(g2, "MENU", btnMenu, new Color(180, 100, 40));
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