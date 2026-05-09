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
        // Cross Corridor & Lobby (floor=0)
        fillArea(18, 24, 2, 33, 0, 0, 0); // Vertical corridor
        fillArea(2, 40, 14, 19, 0, 0, 0); // Horizontal corridor

        // 4 Symmetrical Rooms
        fillArea(2, 17, 2, 13, 3, 0, 0);  // Classroom (Top-Left)
        fillArea(25, 40, 2, 13, 2, 0, 0); // Library (Top-Right)
        fillArea(2, 17, 19, 33, 1, 0, 0); // Computer Lab (Bottom-Left)
        fillArea(25, 40, 19, 33, 3, 0, 0); // Lecturer (Bottom-Right)

        // 3. WALLS & STRUCTURE
        // Outer boundaries
        drawHWall(1, 40, 1, 1); // Top outer wall
        drawHWall(1, 40, 33, 18); // Bottom outer wall
        drawVWall(1, 1, 33, 2); // Left outer wall
        drawVWall(40, 1, 33, 2); // Right outer wall

        // Inner boundaries for cross corridor
        // Top-Left inner
        drawVWall(17, 1, 13, 2); 
        drawHWall(1, 17, 13, 1);
        // Top-Right inner
        drawVWall(24, 1, 13, 2);
        drawHWall(24, 40, 13, 1);
        // Bottom-Left inner
        drawVWall(17, 19, 33, 2);
        drawHWall(1, 17, 19, 1);
        // Bottom-Right inner
        drawVWall(24, 19, 33, 2);
        drawHWall(24, 40, 19, 1);

        // Corners
        setObject(1, 1, 4, 1); // Top-left outer
        setObject(40, 1, 5, 1); // Top-right outer
        setObject(1, 33, 2, 1); // Bottom-left outer
        setObject(40, 33, 2, 1); // Bottom-right outer

        setObject(17, 13, 5, 1); // Top-Left room, bottom-right corner
        setObject(24, 13, 4, 1); // Top-Right room, bottom-left corner
        setObject(17, 19, 5, 1); // Bottom-Left room, top-right corner
        setObject(24, 19, 4, 1); // Bottom-Right room, top-left corner

        // 4. DOORS
        setObject(17, 7, 6, 0); // Classroom Door
        setObject(24, 7, 7, 0); // Library Door
        setObject(17, 26, 7, 0); // Lab Door
        setObject(24, 26, 6, 0); // Lecturer Door

        // 5. FURNITURE POPULATION
        populateClassroom();
        populateLab();
        populateLibrary();
        populateLecturer();
        populateLobby();
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

    private void populateClassroom() {
        setObject(9, 2, 10, 1); // Whiteboard center
        
        setObject(9, 4, 17, 1); // Teacher desk
        setObject(9, 3, 13, 1); // Teacher chair (imgKursi)
        
        for (int r = 7; r <= 11; r += 2) {
            for (int c = 4; c <= 14; c += 2) {
                setObject(c, r, 12, 1); // Desk
                setObject(c, r - 1, 13, 1); // Chair (Student chair)
            }
        }
        setObject(2, 2, 11, 0); // Lamp
        setObject(16, 2, 11, 0); // Lamp
    }

    private void populateLab() {
        for (int r = 22; r <= 30; r += 3) { 
            for (int c = 4; c <= 7; c++) {
                setObject(c, r, 14, 1); // PC Desk (Chair built-in)
            }
            for (int c = 11; c <= 14; c++) {
                setObject(c, r, 14, 1); // PC Desk (Chair built-in)
            }
        }
        setObject(2, 20, 11, 0);
        setObject(16, 20, 11, 0);
    }

    private void populateLibrary() {
        for (int r = 3; r <= 7; r++) {
            for (int c = 26; c <= 38; c += 3) {
                setObject(c, r, 15, 1);     // Left shelf
            }
        }
        for (int c = 28; c <= 36; c += 5) {
            setObject(c, 10, 12, 1); // Table top
            setObject(c, 11, 12, 1); // Table bottom
            setObject(c - 1, 10, 13, 1); // Chair
            setObject(c - 1, 11, 13, 1);
            setObject(c + 1, 10, 13, 1);
            setObject(c + 1, 11, 13, 1);
        }
        setObject(25, 2, 11, 0);
        setObject(39, 2, 11, 0);
    }

    private void populateLecturer() {
        for (int r = 22; r <= 30; r += 4) {
            for (int c = 26; c <= 36; c += 4) {
                setObject(c, r, 17, 1); // Desk (Chair built-in)
            }
        }
        setObject(25, 20, 11, 0);
        setObject(39, 20, 11, 0);
    }

    private void populateLobby() {
        // Center intersection: x=18 to 23, y=14 to 18
        setObject(20, 16, 16, 1); // Sofa
        setObject(21, 16, 16, 1); // Sofa
        setObject(18, 14, 8, 1); // Plant
        setObject(23, 14, 8, 1); // Plant
    }

    private void populateCorridor() {
        setObject(20, 2, 9, 1); // Vending machine top
        setObject(20, 32, 9, 1); // Vending machine bottom
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

