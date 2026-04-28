package com.deadline.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import com.deadline.backend.SubmissionService;
import com.deadline.game.GameObject;

public class SubmissionDesk extends GameObject {

    private SubmissionService submissionService;

    public SubmissionDesk(int x, int y) {
        super(x, y, 200, 80);
        this.submissionService = new SubmissionService();
    }

    public void processSubmission(int playerId, String title, String status) {
        if (playerId != -1) {
            submissionService.submitTask(playerId, title, status);
        }
    }

    @Override
    public void update() {
        // Desk doesn't move
    }

    @Override
    public void draw(Graphics2D g2) {
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 150));
        int textW = 110;
        int textX = x + (width - textW) / 2;
        int textY = y + height / 2 - 10;
        g2.fillRoundRect(textX, textY - 14, textW, 20, 10, 10);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String text = "SUBMIT HERE";
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, x + (width - tw) / 2, textY);
    }
}
