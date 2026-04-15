package com.deadline;

import java.awt.Color;
import java.awt.Graphics2D;

public class SubmissionDesk extends GameObject {

    public SubmissionDesk(int x, int y) {
        super(x, y, 120, 60);
    }

    @Override
    public void update() {
        // Desk doesn't move
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(139, 69, 19)); // SaddleBrown color
        g2d.fillRect(x, y, width, height);

        g2d.setColor(Color.WHITE);
        g2d.drawString("SUBMISSION DESK", x + 5, y + height / 2 + 5);

        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
    }
}
