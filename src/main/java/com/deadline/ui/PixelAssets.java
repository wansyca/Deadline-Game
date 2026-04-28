package com.deadline.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class PixelAssets {

        private static final Map<Character, Color> PALETTE = new HashMap<>();

        static {
                // Transparent
                PALETTE.put(' ', new Color(0, 0, 0, 0));
                // Skin tones
                PALETTE.put('S', new Color(255, 205, 148)); // Skin
                PALETTE.put('s', new Color(234, 168, 114)); // Skin dark
                // Hair
                PALETTE.put('H', new Color(60, 40, 20)); // Dark Hair
                PALETTE.put('h', new Color(150, 80, 20)); // Brown Hair
                PALETTE.put('W', new Color(200, 200, 200)); // White Hair (Tua)
                // Clothes
                PALETTE.put('C', new Color(40, 100, 200)); // Blue Shirt (Player)
                PALETTE.put('c', new Color(20, 60, 150)); // Dark Blue Shirt
                PALETTE.put('D', new Color(180, 40, 40)); // Red Shirt (Dosen)
                PALETTE.put('d', new Color(120, 20, 20)); // Dark Red
                PALETTE.put('P', new Color(50, 50, 50)); // Pants Dark
                PALETTE.put('p', new Color(30, 30, 30)); // Pants Darker
                // Shoes
                PALETTE.put('B', new Color(20, 20, 20)); // Black Shoes
                // Nature (Grass/Trees)
                PALETTE.put('G', new Color(85, 170, 85)); // Grass Light
                PALETTE.put('g', new Color(60, 140, 60)); // Grass Dark
                PALETTE.put('T', new Color(40, 100, 40)); // Tree Green
                PALETTE.put('t', new Color(20, 80, 20)); // Tree Dark Green
                PALETTE.put('R', new Color(100, 70, 40)); // Trunk
                PALETTE.put('F', new Color(240, 100, 100)); // Flower
                // Roads & Buildings
                PALETTE.put('A', new Color(100, 100, 100)); // Asphalt
                PALETTE.put('a', new Color(80, 80, 80)); // Asphalt dark
                PALETTE.put('V', new Color(160, 160, 160)); // Paving
                PALETTE.put('v', new Color(140, 140, 140)); // Paving dark
                PALETTE.put('W', new Color(180, 180, 190)); // Wall
                PALETTE.put('w', new Color(140, 140, 150)); // Wall dark
                // Items
                PALETTE.put('K', new Color(44, 62, 80)); // Book Cover
                PALETTE.put('k', new Color(236, 240, 241)); // Book Pages
                PALETTE.put('E', new Color(241, 196, 15)); // Book Gold
                // Furniture
                PALETTE.put('M', new Color(139, 69, 19)); // Wood Desk
                PALETTE.put('m', new Color(101, 50, 14)); // Wood Desk Dark
                // New Palette for Realism
                PALETTE.put('U', new Color(255, 255, 255)); // Pure White (Lines)
                PALETTE.put('Q', new Color(0, 120, 255)); // Water Blue
                PALETTE.put('q', new Color(0, 80, 200)); // Water Blue Dark
        }

        public static BufferedImage generate(String[] layout, int scale) {
                int w = layout[0].length();
                int h = layout.length;
                BufferedImage img = new BufferedImage(w * scale, h * scale, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();

                for (int y = 0; y < h; y++) {
                        String row = layout[y];
                        for (int x = 0; x < w; x++) {
                                char c = row.charAt(x);
                                Color col = PALETTE.getOrDefault(c, new Color(0, 0, 0, 0));
                                g2.setColor(col);
                                g2.fillRect(x * scale, y * scale, scale, scale);
                        }
                }
                g2.dispose();
                return img;
        }

        // ============================================
        // PLAYER ANIMATIONS (16x16)
        // ============================================
        public static final String[] PLAYER_IDLE = {
                        "      HHHH      ",
                        "     HSSSSHH    ",
                        "     HSSSSHH    ",
                        "      SSSS      ",
                        "      CCCC      ",
                        "     CCCCCC     ",
                        "    SCCCCCC S   ",
                        "    S C  C  S   ",
                        "      P  P      ",
                        "      P  P      ",
                        "     PP  PP     ",
                        "     PP  PP     ",
                        "     BB  BB     ",
                        "                ",
                        "                ",
                        "                "
        };

        public static final String[] PLAYER_WALK_1 = {
                        "      HHHH      ",
                        "     HSSSSHH    ",
                        "     HSSSSHH    ",
                        "      SSSS      ",
                        "      CCCC      ",
                        "     CCCCCC     ",
                        "    SCCCCCC S   ",
                        "    S C  C      ",
                        "      P  P      ",
                        "      PP P      ",
                        "      P PP      ",
                        "      P  BB     ",
                        "     BB         ",
                        "                ",
                        "                ",
                        "                "
        };

        public static final String[] PLAYER_WALK_2 = {
                        "      HHHH      ",
                        "     HSSSSHH    ",
                        "     HSSSSHH    ",
                        "      SSSS      ",
                        "      CCCC      ",
                        "     CCCCCC     ",
                        "    SCCCCCC S   ",
                        "      C  C  S   ",
                        "      P  P      ",
                        "      P PP      ",
                        "     PP P       ",
                        "    BB  P       ",
                        "         BB     ",
                        "                ",
                        "                ",
                        "                "
        };

        // ============================================
        // LECTURER (16x16) - DOSEN UMUM
        // ============================================
        public static final String[] LECTURER_IDLE = {
                        "      WWWW      ",
                        "     WSSSSW     ",
                        "     WSSSSW     ",
                        "      SSSS      ",
                        "      DDDD      ",
                        "     DDDDDD     ",
                        "    SDDDDDD S   ",
                        "    S D  D  S   ",
                        "      P  P      ",
                        "      P  P      ",
                        "     PP  PP     ",
                        "     PP  PP     ",
                        "     BB  BB     ",
                        "                ",
                        "                ",
                        "                "
        };
        public static final String[] LECTURER_WALK_1 = {
                        "      WWWW      ",
                        "     WSSSSW     ",
                        "     WSSSSW     ",
                        "      SSSS      ",
                        "      DDDD      ",
                        "     DDDDDD     ",
                        "    SDDDDDD S   ",
                        "    S D  D      ",
                        "      P  P      ",
                        "      PP P      ",
                        "      P PP      ",
                        "      P  BB     ",
                        "     BB         ",
                        "                ",
                        "                ",
                        "                "
        };
        public static final String[] LECTURER_WALK_2 = {
                        "      WWWW      ",
                        "     WSSSSW     ",
                        "     WSSSSW     ",
                        "      SSSS      ",
                        "      DDDD      ",
                        "     DDDDDD     ",
                        "    SDDDDDD S   ",
                        "      D  D  S   ",
                        "      P  P      ",
                        "      P PP      ",
                        "     PP P       ",
                        "    BB  P       ",
                        "         BB     ",
                        "                ",
                        "                ",
                        "                "
        };

        // ============================================
        // MAP TILES (16x16) - WILL BE SCALED TO 64x64 IN GAME
        // ============================================
        public static final String[] TILE_GRASS = {
                        "GgGGgGGGgGGgGGGG",
                        "GGGGGGGGGGGGGGGG",
                        "GGgGGgGGGGgGGGGg",
                        "GGGGGGGGGGGGGGGG",
                        "gGGGGgGGgGGgGGgG",
                        "GGGGGGGGGGGGGGGG",
                        "GGgGGGGgGGGGgGGG",
                        "GGGGGGGGGGGGGGGG",
                        "GGGGgGGGGgGGGGgG",
                        "GgGGGGGGGGGGgGGG",
                        "GGGGGGgGGGGGGGGG",
                        "GGgGGGGGGgGGGGgG",
                        "GGGGGGGGGGGGGGGG",
                        "gGGgGGGGgGGGGgGG",
                        "GGGGGGGGGGGGGGGG",
                        "GGGGgGGgGGgGGgGG"
        };

        public static final String[] TILE_GRASS_FLOWER = {
                        "GgGGgGGGgGGgGGGG",
                        "GGGFGGGGGGGGGGGG",
                        "GGgGGgGGGGgGGGGg",
                        "GGGGGGGGGGGGGFGG",
                        "gGGGGgGGgGGgGGgG",
                        "GGGGGGGGGGGGGGGG",
                        "GGgGGGGgGGGGgGGG",
                        "GGGGGGGFGGGGGGGG",
                        "GGGGgGGGGgGGGGgG",
                        "GgGGGGGGGGGGgGGG",
                        "GGGGGGgGGGGGGGGG",
                        "GGgGGGGGGgGGGGgG",
                        "GGGGGGGFGGGGGGGG",
                        "gGGgGGGGgGGGGgGG",
                        "GGGFGGGGGGGGGGGG",
                        "GGGGgGGgGGgGGgGG"
        };

        public static final String[] TILE_ROAD = {
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA"
        };

        public static final String[] TILE_ROAD_MARK = {
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAEEEEEAaAaAa",
                        "aAaAaEEEEEaAaAaA",
                        "AaAaAEEEEEAaAaAa",
                        "aAaAaEEEEEaAaAaA",
                        "AaAaAEEEEEAaAaAa",
                        "aAaAaEEEEEaAaAaA",
                        "AaAaAEEEEEAaAaAa",
                        "aAaAaEEEEEaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA",
                        "AaAaAaAaAaAaAaAa",
                        "aAaAaAaAaAaAaAaA"
        };

        public static final String[] TILE_PAVING = {
                        "VvVvVvVvVvVvVvVv",
                        "vVvVvVvVvVvVvVvV",
                        "VvVvVvVvVvVvVvVv",
                        "vVvVvVvVvVvVvVvV",
                        "VvVvVvVvVvVvVvVv",
                        "vVvVvVvVvVvVvVvV",
                        "VvVvVvVvVvVvVvVv",
                        "vVvVvVvVvVvVvVvV",
                        "VvVvVvVvVvVvVvVv",
                        "vVvVvVvVvVvVvVvV",
                        "VvVvVvVvVvVvVvVv",
                        "vVvVvVvVvVvVvVvV",
                        "VvVvVvVvVvVvVvVv",
                        "vVvVvVvVvVvVvVvV",
                        "VvVvVvVvVvVvVvVv",
                        "vVvVvVvVvVvVvVvV"
        };

        public static final String[] TILE_FLOOR = {
                        "WwWwWwWwWwWwWwWw",
                        "wWwWwWwWwWwWwWwW",
                        "WwWwWwWwWwWwWwWw",
                        "wWwWwWwWwWwWwWwW",
                        "WwWwWwWwWwWwWwWw",
                        "wWwWwWwWwWwWwWwW",
                        "WwWwWwWwWwWwWwWw",
                        "wWwWwWwWwWwWwWwW",
                        "WwWwWwWwWwWwWwWw",
                        "wWwWwWwWwWwWwWwW",
                        "WwWwWwWwWwWwWwWw",
                        "wWwWwWwWwWwWwWwW",
                        "WwWwWwWwWwWwWwWw",
                        "wWwWwWwWwWwWwWwW",
                        "WwWwWwWwWwWwWwWw",
                        "wWwWwWwWwWwWwWwW"
        };

        public static final String[] TILE_WHITE_LINE = {
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU",
                        "UUUUUUUUUUUUUUUU"
        };

        public static final String[] FOUNTAIN = {
                        "      QQQQ      ",
                        "    QQqqqqQQ    ",
                        "   QqqqqqqqqQ   ",
                        "  QqqqqQQqqqqQ  ",
                        "  QqqqQQQQqqqQ  ",
                        " QqqqQQQQQQqqqQ ",
                        " QqqQQQQQQQQqqQ ",
                        " QqqQQQQQQQQqqQ ",
                        " QqqqQQQQQQqqqQ ",
                        "  QqqqQQQQqqqQ  ",
                        "  QqqqqQQqqqqQ  ",
                        "   QqqqqqqqqQ   ",
                        "    QQqqqqQQ    ",
                        "      QQQQ      ",
                        "                ",
                        "                "
        };

        public static final String[] TREE = {
                        "      TTTT      ",
                        "    TTtTTtTT    ",
                        "   TTtTTtTTtT   ",
                        "  TTTTtTTtTTTT  ",
                        "  TtTTtTTtTTtT  ",
                        " TTTTtTTtTTTtTT ",
                        " TtTTtTTtTTtTTt ",
                        "  TTTTtTTtTTTT  ",
                        "  TtTTtTTtTTtT  ",
                        "    TTtTTtTT    ",
                        "      RRRR      ",
                        "      RRRR      ",
                        "      RRRR      ",
                        "     RRRRRR     ",
                        "                ",
                        "                "
        };

        public static final String[] DESK = {
                        "                ",
                        "                ",
                        "   MMMMMMMMMM   ",
                        "  MmmmmmmmmmMM  ",
                        "  MMMMMMMMMMMM  ",
                        "  MmmmmmmmmmMM  ",
                        "  MMMMMMMMMMMM  ",
                        "   M        M   ",
                        "   M        M   ",
                        "   m        m   ",
                        "   m        m   ",
                        "                ",
                        "                ",
                        "                ",
                        "                ",
                        "                "
        };

        public static final String[] BOOK = {
                        "                ",
                        "                ",
                        "                ",
                        "      KKKKK     ",
                        "     KKKKKk     ",
                        "     KEEKEk     ",
                        "     KKKKKk     ",
                        "     KKKKKk     ",
                        "     KEEKEk     ",
                        "     KKKKKk     ",
                        "      kkkkk     ",
                        "                ",
                        "                ",
                        "                ",
                        "                ",
                        "                "
        };

        /**
         * Loads player sprites from PNG files.
         * Maps to directions: front, back, left, right + walking animations.
         */
        public static void loadPlayerSprites(String avatarPath) {
                try {
                        int size = 64;

                        imgPlayerDepan = loadAndScale(avatarPath, size, size);

                        if (imgPlayerDepan == null) {
                                imgPlayerDepan = loadAndScale("/assets/bawah.png", size, size);
                        }

                        imgPlayerBelakang = loadAndScale("/assets/belakang.png", size, size);
                        imgPlayerKiri = loadAndScale("/assets/kiri.png", size, size);
                        imgPlayerKanan = loadAndScale("/assets/kanan.png", size, size);

                        imgPlayerJalanKiri = loadAndScale("/assets/jln_kiri.png", size, size);
                        imgPlayerJalanKanan = loadAndScale("/assets/jln_kanan.png", size, size);

                        System.out.println("Player sprites loaded: " + avatarPath + " (Size: " + size + ")");
                } catch (Exception e) {
                        System.err.println("Error loading player sprites: " + e.getMessage());
                }
        }

        private static BufferedImage loadAndScale(String path, int targetW, int targetH) {
                try {
                        java.net.URL url = PixelAssets.class.getResource(path);
                        if (url == null)
                                return null;
                        ImageIcon icon = new ImageIcon(url);
                        Image img = icon.getImage();
                        BufferedImage bi = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g2 = bi.createGraphics();
                        g2.drawImage(img, 0, 0, targetW, targetH, null);
                        g2.dispose();
                        return bi;
                } catch (Exception e) {
                        return null;
                }
        }

        // Pre-generated loaded images
        public static BufferedImage imgPlayerDepan;
        public static BufferedImage imgPlayerBelakang;
        public static BufferedImage imgPlayerKiri;
        public static BufferedImage imgPlayerKanan;
        public static BufferedImage imgPlayerJalanKiri;
        public static BufferedImage imgPlayerJalanKanan;

        public static BufferedImage imgLecturerIdle;
        public static BufferedImage[] imgLecturerWalk;

        public static BufferedImage imgGrass;
        public static BufferedImage imgGrassFlower;
        public static BufferedImage imgRoad;
        public static BufferedImage imgRoadMark;
        public static BufferedImage imgPaving;
        public static BufferedImage imgFloor;

        public static BufferedImage imgTree;
        public static BufferedImage imgDesk;
        public static BufferedImage imgBook;
        public static BufferedImage imgWhiteLine;
        public static BufferedImage imgFountain;

        public static void loadAll() {
                int charScale = 4; // Scale 16x16 to 64x64 visually per character

                imgLecturerIdle = generate(LECTURER_IDLE, charScale);
                imgLecturerWalk = new BufferedImage[] { generate(LECTURER_WALK_1, charScale),
                                generate(LECTURER_IDLE, charScale), generate(LECTURER_WALK_2, charScale),
                                generate(LECTURER_IDLE, charScale) };

                imgGrass = generate(TILE_GRASS, charScale);
                imgGrassFlower = generate(TILE_GRASS_FLOWER, charScale);
                imgRoad = generate(TILE_ROAD, charScale);
                imgRoadMark = generate(TILE_ROAD_MARK, charScale);
                imgPaving = generate(TILE_PAVING, charScale);
                imgFloor = generate(TILE_FLOOR, charScale);

                imgTree = generate(TREE, charScale);
                imgDesk = generate(DESK, charScale);
                imgBook = generate(BOOK, charScale);
                imgWhiteLine = generate(TILE_WHITE_LINE, charScale);
                imgFountain = generate(FOUNTAIN, charScale);
        }
}
