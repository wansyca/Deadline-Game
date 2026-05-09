package com.deadline.game;

public class MapGenerator {
    private int rows, cols;
    private int[][] floor;
    private int[][] objects;
    private int[][] collision;

    public MapGenerator(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.floor = new int[rows][cols];
        this.objects = new int[rows][cols];
        this.collision = new int[rows][cols];
    }

    public void generate() {
        // 1. FILL VOID
        fillArea(0, cols, 0, rows, 3, 99, 1);

        // 2. FLOOR PLAN
        // Horizontal Corridor
        fillArea(2, 57, 15, 21, 0, 0, 0); 
        
        // Lobby area (Open to corridor)
        fillArea(44, 57, 21, 33, 0, 0, 0); 

        // Top Rooms
        fillArea(2, 15, 2, 15, 3, 0, 0); // Classroom 1
        fillArea(16, 29, 2, 15, 3, 0, 0); // Classroom 2
        fillArea(30, 43, 2, 15, 3, 0, 0); // Classroom 3
        fillArea(44, 57, 2, 15, 2, 0, 0); // Lab 1

        // Bottom Rooms
        fillArea(2, 15, 21, 33, 2, 0, 0); // Lab 2
        fillArea(16, 29, 21, 33, 1, 0, 0); // Library
        fillArea(30, 43, 21, 33, 3, 0, 0); // Lecturer

        // 3. WALLS & STRUCTURE
        // Outer boundaries
        drawHWall(1, 57, 1, 1); // Top outer
        drawHWall(1, 57, 33, 18); // Bottom outer
        drawVWall(1, 1, 33, 2); // Left outer
        drawVWall(57, 1, 33, 2); // Right outer

        // Horizontal Room dividers
        drawHWall(1, 57, 14, 1); // Top rooms bottom wall
        drawHWall(1, 43, 20, 1); // Bottom rooms top wall (leaves Lobby open)
        
        // Vertical dividers (Top)
        drawVWall(15, 1, 14, 2);
        drawVWall(29, 1, 14, 2);
        drawVWall(43, 1, 14, 2);
        
        // Vertical dividers (Bottom)
        drawVWall(15, 20, 33, 2);
        drawVWall(29, 20, 33, 2);
        drawVWall(43, 20, 33, 2);

        // 4. DOORS (Centered in each 14-tile room, 2 tiles wide)
        // Top row doors (Corridor at y=14)
        setObject(7, 14, 6, 0); setObject(8, 14, 6, 0);   // CR1
        setObject(21, 14, 6, 0); setObject(22, 14, 6, 0); // CR2
        setObject(35, 14, 6, 0); setObject(36, 14, 6, 0); // CR3
        setObject(50, 14, 7, 0); setObject(51, 14, 7, 0); // Lab1

        // Bottom row doors (Corridor at y=20)
        setObject(7, 20, 7, 0); setObject(8, 20, 7, 0);   // Lab2
        setObject(21, 20, 7, 0); setObject(22, 20, 7, 0); // Library
        setObject(35, 20, 6, 0); setObject(36, 20, 6, 0); // Lecturer
        // Lobby has no door

        // 5. FURNITURE POPULATION
        populateClassroom(2, 2);
        populateClassroom(16, 2);
        populateClassroom(30, 2);
        
        populateLab(44, 2); // Lab 1
        populateLab(2, 21); // Lab 2
        
        populateLibrary(16, 21);
        populateLecturer(30, 21);
        populateLobby(44, 21);
        populateCorridor();

        polishWorld();
    }

    private void fillArea(int x, int xEnd, int y, int yEnd, int floorTile, int objectTile, int coll) {
        for (int r = y; r < yEnd; r++) {
            for (int c = x; c < xEnd; c++) {
                if (r >= 0 && r < rows && c >= 0 && c < cols) {
                    floor[r][c] = floorTile;
                    objects[r][c] = objectTile;
                    collision[r][c] = coll;
                }
            }
        }
    }

    private void drawHWall(int x1, int x2, int y, int type) {
        for (int x = x1; x <= x2; x++) {
            setObject(x, y, type, 1);
        }
    }

    private void drawVWall(int x, int y1, int y2, int type) {
        for (int y = y1; y <= y2; y++) {
            setObject(x, y, type, 1);
        }
    }

    private void setObject(int c, int r, int objId, int coll) {
        if (r >= 0 && r < rows && c >= 0 && c < cols) {
            objects[r][c] = objId;
            collision[r][c] = coll;
        }
    }

    private void populateClassroom(int startX, int startY) {
        setObject(startX + 6, startY, 10, 1); // Whiteboard center
        
        setObject(startX + 6, startY + 2, 17, 1); // Teacher desk
        setObject(startX + 6, startY + 3, 13, 1); // Teacher chair
        
        for (int r = startY + 5; r <= startY + 9; r += 2) {
            for (int c = startX + 2; c <= startX + 10; c += 2) {
                setObject(c, r, 12, 1); // Desk
                setObject(c, r - 1, 13, 1); // Chair (Student chair above desk)
            }
        }
    }

    private void populateLab(int startX, int startY) {
        for (int r = startY + 2; r <= startY + 10; r += 3) { 
            for (int c = startX + 2; c <= startX + 5; c += 3) {
                setObject(c, r, 14, 1); // PC Desk (Built-in chair)
            }
            for (int c = startX + 8; c <= startX + 11; c += 3) {
                setObject(c, r, 14, 1); // PC Desk
            }
        }
    }

    private void populateLibrary(int startX, int startY) {
        for (int r = startY + 1; r <= startY + 5; r++) {
            for (int c = startX + 1; c <= startX + 11; c += 3) {
                setObject(c, r, 15, 1); // Bookshelf
            }
        }
        for (int c = startX + 3; c <= startX + 9; c += 5) {
            setObject(c, startY + 8, 12, 1); // Table top
            setObject(c, startY + 9, 12, 1); // Table bottom
            setObject(c - 1, startY + 8, 13, 1); // Chair
            setObject(c - 1, startY + 9, 13, 1);
            setObject(c + 1, startY + 8, 13, 1);
            setObject(c + 1, startY + 9, 13, 1);
        }
    }

    private void populateLecturer(int startX, int startY) {
        for (int r = startY + 2; r <= startY + 10; r += 4) {
            for (int c = startX + 2; c <= startX + 10; c += 4) {
                setObject(c, r, 17, 1); // Desk (Built-in chair)
            }
        }
    }

    private void populateLobby(int startX, int startY) {
        // Open lobby area
        setObject(startX + 6, startY + 6, 16, 1); // Sofa
        setObject(startX + 7, startY + 6, 16, 1); // Sofa
        setObject(startX + 2, startY + 4, 8, 1); // Plant
        setObject(startX + 11, startY + 4, 8, 1); // Plant
    }

    private void populateCorridor() {
        setObject(10, 15, 9, 1); // Vending machine
        setObject(38, 15, 9, 1); // Vending machine
    }

    private void polishWorld() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (objects[r][c] == 6 || objects[r][c] == 7) {
                    collision[r][c] = 0; // Doors have no collision
                }
            }
        }
    }

    public int[][] getFloor() { return floor; }
    public int[][] getObjects() { return objects; }
    public int[][] getCollision() { return collision; }
}

