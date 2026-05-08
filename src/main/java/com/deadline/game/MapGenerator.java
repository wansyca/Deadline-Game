package com.deadline.game;

import java.util.Random;

public class MapGenerator {
    private int rows, cols;
    private int[][] floor;
    private int[][] objects;
    private int[][] collision;
    private Random random = new Random();

    public MapGenerator(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.floor = new int[rows][cols];
        this.objects = new int[rows][cols];
        this.collision = new int[rows][cols];
    }

    public void generate() {
        // 1. Initial Fill: Solid Void / Wall Center (for thick walls feel)
        fillArea(0, cols, 0, rows, 3, 3, 1);

        // Grid Lines
        int X0 = 4, X1 = 26, X_TOP_SPLIT = 38, X_BOT_SPLIT = 46, X2 = 54, X3 = 76;
        int Y0 = 4, Y1 = 32, Y2 = 56, Y3 = 76;

        // 2. Rooms
        // LOBBY (Center)
        createRoom(X1, Y1, X2, Y2, 2, "LOBBY");

        // KELAS (Top Left)
        createRoom(X0, Y0, X_TOP_SPLIT, Y1, 0, "CLASSROOM");
        setObject(32, Y1, 6, 0); // Door to Lobby (Bottom Wall)

        // PERPUSTAKAAN (Top Right)
        createRoom(X_TOP_SPLIT, Y0, X3, Y1, 2, "LIBRARY");
        setObject(46, Y1, 6, 0); // Door to Lobby (Bottom Wall)

        // LAB (Mid Left)
        createRoom(X0, Y1, X1, Y2, 1, "LAB");
        setObject(X1, 44, 6, 0); // Door to Lobby (Right Wall)

        // DOSEN (Mid Right)
        createRoom(X2, Y1, X3, Y2, 0, "DOSEN");
        setObject(X2, 44, 6, 0); // Door to Lobby (Left Wall)

        // KELAS KECIL (Bottom Left)
        createRoom(X0, Y2, X_BOT_SPLIT, Y3, 0, "CLASSROOM_SMALL");
        setObject(34, Y2, 6, 0); // Door to Lobby (Top Wall)

        // TOILET (Bottom Right)
        createRoom(X_BOT_SPLIT, Y2, X3, Y3, 1, "TOILET");
        setObject(50, Y2, 6, 0); // Door to Lobby (Top Wall)

        // 3. Polish and World Details
        polishWorld();
    }

    private void fillArea(int x, int xEnd, int y, int yEnd, int floorTile, int objectTile, int coll) {
        for (int r = y; r < yEnd && r < rows; r++) {
            for (int c = x; c < xEnd && c < cols; c++) {
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    floor[r][c] = floorTile;
                    objects[r][c] = objectTile;
                    collision[r][c] = coll;
                }
            }
        }
    }

    private void createRoom(int startX, int startY, int endX, int endY, int floorTile, String type) {
        // Interior floor
        fillArea(startX + 1, endX, startY + 1, endY, floorTile, 0, 0);

        // Walls
        for (int c = startX + 1; c < endX; c++) {
            setObject(c, startY, 1, 1); // Top Wall
            setObject(c, endY, 18, 1); // Bottom Wall
        }
        for (int r = startY + 1; r < endY; r++) {
            setObject(startX, r, 2, 1); // Left Wall
            setObject(endX, r, 2, 1); // Right Wall
        }

        // Corners
        setObject(startX, startY, 4, 1); // Top-Left
        setObject(endX, startY, 5, 1); // Top-Right
        setObject(startX, endY, 2, 1); // Bottom-Left (Side Wall works best for bottom corners)
        setObject(endX, endY, 2, 1); // Bottom-Right

        populateFurniture(startX + 1, startY + 1, endX - startX - 1, endY - startY - 1, type);
    }

    private void populateFurniture(int x, int y, int w, int h, String type) {
        if (type.equals("CLASSROOM") || type.equals("CLASSROOM_SMALL")) {
            // Whiteboard at front
            setObject(x + w / 2 - 1, y + 1, 10, 1);
            setObject(x + w / 2, y + 1, 10, 1);
            
            // Teacher Desk
            setObject(x + w / 2, y + 3, 17, 1);
            
            // Dense Student Desks Grid
            for (int r = y + 6; r < y + h - 3; r += 3) {
                for (int c = x + 3; c < x + w - 3; c += 3) {
                    setObject(c, r, 12, 1); // Meja
                    setObject(c, r + 1, 13, 1); // Kursi
                }
            }
            
            // Decor
            setObject(x + 1, y + 1, 8, 1); // Plant
            setObject(x + w - 2, y + 1, 11, 0); // Lamp
            
        } else if (type.equals("LIBRARY")) {
            // Dense Bookshelves Arrays
            for (int c = x + 3; c < x + w - 3; c += 5) {
                for (int r = y + 2; r < y + h - 8; r += 2) {
                    setObject(c, r, 15, 1);
                    setObject(c + 1, r, 15, 1);
                }
            }
            // Reading area at bottom
            for (int c = x + 4; c < x + w - 4; c += 6) {
                setObject(c, y + h - 5, 12, 1); // Table
                setObject(c, y + h - 4, 13, 1); // Chair
                setObject(c, y + h - 6, 13, 1); // Chair (facing down)
            }
            // Cozy corners
            setObject(x + 1, y + 1, 11, 0);
            setObject(x + w - 2, y + 1, 11, 0);
            setObject(x + 2, y + h - 3, 16, 1); // Sofa

        } else if (type.equals("LAB")) {
            // PC Rows
            for (int r = y + 3; r < y + h - 3; r += 4) {
                for (int c = x + 2; c < x + w - 2; c += 3) {
                    setObject(c, r, 14, 1); // PC Desk
                    setObject(c, r + 1, 13, 1); // Chair
                }
            }
            setObject(x + w - 2, y + 1, 9, 1); // Vending

        } else if (type.equals("TOILET")) {
            // Sinks
            for (int c = x + 2; c < x + w - 2; c += 3) {
                setObject(c, y + 1, 14, 1); // Sink
            }
            // Dividers / Stalls
            for (int r = y + 5; r < y + h - 2; r += 3) {
                setObject(x + 3, r, 2, 1); // Wall side as stall divider
                setObject(x + 4, r, 13, 1); // Toilet bowl
            }

        } else if (type.equals("DOSEN")) {
            // Professional Office Desks
            for (int c = x + 3; c < x + w - 4; c += 6) {
                for (int r = y + 3; r < y + h - 4; r += 5) {
                    setObject(c, r, 17, 1); // Teacher desk
                    setObject(c, r + 1, 13, 1); // Chair
                    setObject(c + 2, r, 8, 1); // Plant
                }
            }
            // Break area
            setObject(x + w - 2, y + 2, 9, 1); // Vending
            setObject(x + w - 3, y + 2, 15, 1); // Bookshelf
            setObject(x + 2, y + 2, 16, 1); // Sofa

        } else if (type.equals("LOBBY")) {
            // Massive Central Lobby Hub
            // Reception Area
            setObject(x + w / 2 - 2, y + h / 2, 17, 1); 
            setObject(x + w / 2 - 1, y + h / 2, 17, 1); 
            setObject(x + w / 2, y + h / 2, 17, 1); 
            setObject(x + w / 2 + 1, y + h / 2, 17, 1);
            setObject(x + w / 2, y + h / 2 + 1, 13, 1); // Receptionist Chair
            
            // Waiting Lounges
            setObject(x + 3, y + 3, 16, 1); // Sofa Left
            setObject(x + 3, y + 5, 16, 1);
            setObject(x + 5, y + 4, 11, 0); // Lamp
            
            setObject(x + w - 4, y + 3, 16, 1); // Sofa Right
            setObject(x + w - 4, y + 5, 16, 1);
            setObject(x + w - 6, y + 4, 11, 0); // Lamp

            // Vending & Info Board
            setObject(x + w - 3, y + h - 3, 9, 1);
            setObject(x + w - 4, y + h - 3, 9, 1);
            setObject(x + 4, y + h - 3, 10, 1); // Board
        }
    }

    private void polishWorld() {
        // Ensure doors are passable and no collision issues
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (objects[r][c] == 6 || objects[r][c] == 7) {
                    collision[r][c] = 0;
                }
            }
        }
    }

    private void setObject(int c, int r, int objId, int coll) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            objects[r][c] = objId;
            collision[r][c] = coll;
        }
    }

    public int[][] getFloor() { return floor; }
    public int[][] getObjects() { return objects; }
    public int[][] getCollision() { return collision; }
}
