package com.deadline.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class PixelAssets {

    // TILES - FLOOR
    public static BufferedImage imgFloorWhite;
    public static BufferedImage imgFloorDark;
    public static BufferedImage imgFloorLab;
    public static BufferedImage imgFloorLibrary;

    // TILES - WALLS
    public static BufferedImage imgWallTop;
    public static BufferedImage imgWallBottom;
    public static BufferedImage imgWallSide;
    public static BufferedImage imgWallCenter;
    public static BufferedImage imgCornerLeft;
    public static BufferedImage imgCornerRight;

    // TILES - DOORS
    public static BufferedImage imgDoorClass;
    public static BufferedImage imgDoorLabLibrary;

    // TILES - DECORATIONS
    public static BufferedImage imgPlant;
    public static BufferedImage imgVending;
    public static BufferedImage imgBoard;
    public static BufferedImage imgLamp;

    // FURNITURE
    public static BufferedImage imgMeja;
    public static BufferedImage imgKursi;
    public static BufferedImage imgMejaLab;
    public static BufferedImage imgRakBuku;
    public static BufferedImage imgBangkuLobby;
    public static BufferedImage imgMejaDosen;

    // ITEMS
    public static BufferedImage imgBook;

    // ENTITIES
    public static BufferedImage imgLecturerIdle;

    public static void loadAll() {
        // Load Tiles
        imgFloorWhite = load("/assets/tiles/floor/floor_white.png");
        imgFloorDark = load("/assets/tiles/floor/floor_dark.png");
        imgFloorLab = load("/assets/tiles/floor/floor_lab.png");
        imgFloorLibrary = load("/assets/tiles/floor/floor_library.png");

        imgWallTop = load("/assets/tiles/walls/wal_top.png");
        imgWallBottom = load("/assets/tiles/walls/wal_top.png");
        imgWallSide = load("/assets/tiles/walls/wall_side.png");
        imgWallCenter = load("/assets/tiles/walls/wall_center.png");
        imgCornerLeft = load("/assets/tiles/walls/corner_left.png");
        imgCornerRight = load("/assets/tiles/walls/wall_right.png"); // Fallback for corner_right

        imgDoorClass = load("/assets/tiles/doors/door.png");
        imgDoorLabLibrary = load("/assets/tiles/doors/door_lab&library.png");

        imgPlant = load("/assets/tiles/decorations/plant.png");
        imgVending = load("/assets/tiles/decorations/vending.png");
        imgBoard = load("/assets/tiles/decorations/board.png");
        imgLamp = load("/assets/tiles/decorations/lamp.png");

        // Load Furniture
        imgMeja = load("/assets/furniture/classroom/meja.png");
        imgKursi = load("/assets/furniture/classroom/kursi.png");
        imgMejaLab = load("/assets/furniture/lab/meja_lab.png");
        imgRakBuku = load("/assets/furniture/library/rak_buku.png");
        imgBangkuLobby = load("/assets/furniture/lobby/bangku_lobi.png");
        imgMejaDosen = load("/assets/furniture/office/meja_dosen.png");

        // Load Items
        imgBook = load("/assets/items/books/book.png");

        // Entities
        imgLecturerIdle = load("/assets/dosen/dosen_tua/down_1.png");
    }

    private static BufferedImage load(String path) {
        try {
            java.net.URL url = PixelAssets.class.getResource(path);
            if (url == null) {
                System.err.println("❌ Could not find asset: " + path);
                return createPlaceholder();
            }
            return ImageIO.read(url);
        } catch (Exception e) {
            System.err.println("❌ Error loading asset: " + path);
            return createPlaceholder();
        }
    }

    private static BufferedImage createPlaceholder() {
        BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setColor(java.awt.Color.MAGENTA);
        g2.fillRect(0, 0, 16, 16);
        g2.dispose();
        return bi;
    }

    public static BufferedImage loadAndScale(String path, int targetW, int targetH) {
        try {
            java.net.URL url = PixelAssets.class.getResource(path);
            if (url == null) return createPlaceholder();
            ImageIcon icon = new ImageIcon(url);
            BufferedImage bi = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = bi.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.drawImage(icon.getImage(), 0, 0, targetW, targetH, null);
            g2.dispose();
            return bi;
        } catch (Exception e) {
            return createPlaceholder();
        }
    }
}
