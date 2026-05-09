package com.deadline.game;

import java.util.Random;

/**
 * MapGenerator - Final Professional Version.
 * Hand-crafted, ultra-realistic university indoor layout.
 * Features a central corridor, organized rooms, and perfect wall connectivity.
 */
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
        // 1. FILL VOID (Dark background, collision=1, object=99 to prevent book spawns)
        fillArea(0, cols, 0, rows, 3, 99, 1);

        // 2. FLOOR PLAN
        // Corridor (floor=0)
        fillArea(35, 45, 5, 66, 0, 0, 0); 
        // Lobby (floor=0)
        fillArea(20, 60, 65, 75, 0, 0, 0); 

        // Left Wing Rooms
        fillArea(6, 35, 6, 25, 3, 0, 0);  // Classroom (floor_dark)
        fillArea(6, 35, 26, 45, 2, 0, 0); // Library (floor_library)
        fillArea(6, 35, 46, 65, 3, 0, 0); // Lecturer (floor_dark)

        // Right Wing Rooms
        fillArea(45, 74, 6, 35, 1, 0, 0); // Computer Lab (floor_lab)
        fillArea(45, 74, 36, 65, 1, 0, 0); // Restroom (floor_lab)

        // 3. WALLS & STRUCTURE (Mathematically Perfect, Zero Overlap)
        // Outer boundaries
        drawHWall(6, 73, 5, 1); // Top outer wall
        drawHWall(21, 58, 75, 18); // Bottom outer wall (Lobby)
        drawVWall(5, 6, 64, 2); // Far Left outer wall
        drawVWall(74, 6, 64, 2); // Far Right outer wall
        drawVWall(20, 66, 74, 2); // Lobby Left
        drawVWall(59, 66, 74, 2); // Lobby Right
        
        // Connect Lobby bottom to wings
        drawHWall(6, 19, 65, 18); // Left wing bottom outer wall
        drawHWall(60, 73, 65, 18); // Right wing bottom outer wall

        // Central Corridor Walls
        drawVWall(35, 6, 64, 2); // Left corridor wall
        drawVWall(44, 6, 64, 2); // Right corridor wall

        // Room horizontal dividers
        drawHWall(6, 34, 25, 1); // Class - Library divider
        drawHWall(6, 34, 45, 1); // Library - Lecturer divider
        drawHWall(45, 73, 35, 1); // Lab - Restroom divider
        
        // Lobby top wall sections (leaves corridor entrance open)
        drawHWall(21, 34, 65, 1);
        drawHWall(45, 58, 65, 1);

        // Corners for seamless connections (No Overlaps)
        setObject(5, 5, 4, 1); // Top-left building
        setObject(74, 5, 5, 1); // Top-right building
        setObject(20, 65, 4, 1); // Lobby top-left
        setObject(59, 65, 5, 1); // Lobby top-right
        setObject(35, 65, 5, 1); // Corridor-Lobby left inner
        setObject(44, 65, 4, 1); // Corridor-Lobby right inner

        // Bottom outer corners
        setObject(5, 65, 2, 1); // Left wing bottom-left (side wall seamlessly ends it)
        setObject(74, 65, 2, 1); // Right wing bottom-right
        setObject(20, 75, 2, 1); // Lobby bottom-left
        setObject(59, 75, 2, 1); // Lobby bottom-right

        // 4. DOORS (wall - wall - DOOR - wall - wall format)
        // Left doors (placed exactly on the left corridor wall)
        setObject(35, 15, 6, 0); // Classroom Door
        setObject(35, 35, 7, 0); // Library Door
        setObject(35, 55, 6, 0); // Lecturer Door

        // Right doors (placed exactly on the right corridor wall)
        setObject(44, 20, 7, 0); // Lab Door
        setObject(44, 50, 6, 0); // Restroom Door

        // 5. FURNITURE POPULATION
        populateClassroom();
        populateLibrary();
        populateLecturer();
        populateLab();
        populateRestroom();
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
        // Area: x = 6 to 34, y = 6 to 24
        // Front area
        setObject(20, 6, 10, 1); // Whiteboard center
        setObject(21, 6, 10, 1); // Whiteboard extension
        
        setObject(20, 8, 17, 1); // Teacher desk
        setObject(20, 9, 13, 1); // Teacher chair
        
        // Student desks (Dense grid, 1 tile gap horizontally, 2 tiles walking space vertically)
        for (int r = 13; r <= 22; r += 3) {
            for (int c = 10; c <= 30; c += 2) {
                setObject(c, r, 12, 1); // Desk
                setObject(c, r + 1, 13, 1); // Chair
            }
        }
        setObject(7, 7, 11, 0); // Lamp
        setObject(33, 7, 11, 0); // Lamp
    }

    private void populateLibrary() {
        // Area: x = 6 to 34, y = 26 to 44
        // Dense double-sided bookshelf aisles
        for (int r = 28; r <= 38; r++) {
            for (int c = 8; c <= 30; c += 4) {
                setObject(c, r, 15, 1);     // Left shelf of the block
                setObject(c + 1, r, 15, 1); // Right shelf of the block
            }
        }
        
        // Proper reading desks (1x2 table surrounded by 4 chairs)
        for (int c = 10; c <= 30; c += 5) {
            setObject(c, 41, 12, 1); // Table top
            setObject(c, 42, 12, 1); // Table bottom
            setObject(c - 1, 41, 13, 1); // Chair left-top
            setObject(c - 1, 42, 13, 1); // Chair left-bottom
            setObject(c + 1, 41, 13, 1); // Chair right-top
            setObject(c + 1, 42, 13, 1); // Chair right-bottom
        }
        
        setObject(7, 27, 11, 0);
        setObject(33, 27, 11, 0);
    }

    private void populateLecturer() {
        // Area: x = 6 to 34, y = 46 to 64
        // Dense office cubicle layout
        for (int r = 50; r <= 60; r += 4) {
            for (int c = 8; c <= 32; c += 4) {
                setObject(c, r, 17, 1); // Desk
                setObject(c, r + 1, 13, 1); // Chair
            }
        }
        
        setObject(7, 47, 11, 0);
        setObject(33, 47, 11, 0);
    }

    private void populateLab() {
        // Area: x = 45 to 73, y = 6 to 34
        // Strict requirement: Only use meja_lab (14) with built-in chair. Continuous rows.
        for (int r = 10; r <= 30; r += 3) {
            // Left computer block
            for (int c = 48; c <= 55; c++) {
                setObject(c, r, 14, 1);
            }
            // Center aisle is x=56, 57, 58
            // Right computer block
            for (int c = 59; c <= 66; c++) {
                setObject(c, r, 14, 1);
            }
        }
        setObject(46, 7, 11, 0);
        setObject(72, 7, 11, 0);
    }

    private void populateRestroom() {
        // Area: x = 45 to 73, y = 36 to 64
        // Sinks array along the top wall
        for (int c = 48; c <= 60; c += 2) {
            setObject(c, 37, 12, 1); // Sink/Counter
        }
        setObject(46, 37, 11, 0);
    }

    private void populateLobby() {
        // Area: x = 20 to 59, y = 65 to 74
        // Massive continuous Reception Desk
        drawHWall(25, 32, 68, 17); 
        setObject(27, 69, 13, 1); // Receptionist 1
        setObject(30, 69, 13, 1); // Receptionist 2
        
        // Information Area
        setObject(22, 67, 10, 1); // Board
        setObject(24, 67, 8, 1); // Plant
        setObject(34, 67, 8, 1); // Plant
        
        // Waiting Lounge (Parallel sofas)
        for (int c = 48; c <= 54; c += 2) {
            setObject(c, 68, 16, 1); // Top row
            setObject(c, 71, 16, 1); // Bottom row
        }

        // Vending Machines
        setObject(22, 73, 9, 1);
        setObject(23, 73, 9, 1);
        
        setObject(21, 74, 11, 0);
        setObject(58, 74, 11, 0);
    }

    private void populateCorridor() {
        // Add subtle details to the long corridor
        setObject(36, 10, 9, 1); // Vending
        setObject(43, 18, 8, 1); // Plant
        setObject(36, 25, 10, 1); // Info board
        setObject(43, 40, 9, 1); // Vending
        setObject(36, 60, 8, 1); // Plant
    }

    private void polishWorld() {
        // Ensure doors have zero collision
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (objects[r][c] == 6 || objects[r][c] == 7) {
                    collision[r][c] = 0;
                }
            }
        }
    }

    public int[][] getFloor() { return floor; }
    public int[][] getObjects() { return objects; }
    public int[][] getCollision() { return collision; }
}

