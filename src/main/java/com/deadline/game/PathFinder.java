package com.deadline.game;

import java.awt.Rectangle;
import java.util.*;

public class PathFinder {
    private int[][] collision;
    private int rows, cols;

    public PathFinder(int[][] collision) {
        this.collision = collision;
        this.rows = collision.length;
        this.cols = collision[0].length;
    }

    private static class Node implements Comparable<Node> {
        int r, c;
        int g, h, f;
        Node parent;

        Node(int r, int c) {
            this.r = r;
            this.c = c;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.f, o.f);
        }
    }

    public List<int[]> findPath(int startR, int startC, int targetR, int targetC) {
        if (targetR < 0 || targetR >= rows || targetC < 0 || targetC >= cols) return null;
        if (collision[targetR][targetC] == 1) return null;

        PriorityQueue<Node> openList = new PriorityQueue<>();
        boolean[][] closedList = new boolean[rows][cols];

        Node startNode = new Node(startR, startC);
        startNode.g = 0;
        startNode.h = Math.abs(targetR - startR) + Math.abs(targetC - startC);
        startNode.f = startNode.g + startNode.h;
        openList.add(startNode);

        while (!openList.isEmpty()) {
            Node current = openList.poll();
            if (current.r == targetR && current.c == targetC) {
                return retracePath(current);
            }

            closedList[current.r][current.c] = true;

            int[] dr = {-1, 1, 0, 0};
            int[] dc = {0, 0, -1, 1};

            for (int i = 0; i < 4; i++) {
                int nr = current.r + dr[i];
                int nc = current.c + dc[i];

                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && collision[nr][nc] == 0 && !closedList[nr][nc]) {
                    Node neighbor = new Node(nr, nc);
                    neighbor.g = current.g + 1;
                    neighbor.h = Math.abs(targetR - nr) + Math.abs(targetC - nc);
                    neighbor.f = neighbor.g + neighbor.h;
                    neighbor.parent = current;

                    // Optimization: Check if neighbor is already in openList with a lower g
                    boolean skip = false;
                    for(Node n : openList) {
                        if(n.r == nr && n.c == nc && n.g <= neighbor.g) {
                            skip = true;
                            break;
                        }
                    }
                    if(!skip) openList.add(neighbor);
                }
            }
        }
        return null;
    }

    private List<int[]> retracePath(Node node) {
        List<int[]> path = new ArrayList<>();
        while (node != null) {
            path.add(0, new int[]{node.r, node.c});
            node = node.parent;
        }
        return path;
    }
}
