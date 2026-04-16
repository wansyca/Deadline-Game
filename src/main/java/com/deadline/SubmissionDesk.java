package com.deadline;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import com.deadline.backend.SubmissionService;

public class SubmissionDesk extends GameObject {

    private SubmissionService submissionService;

    public SubmissionDesk(int x, int y) {
        super(x, y, 300, 100);
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
        // Anti-aliasing
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Bayangan bawah
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillRoundRect(x + 5, y + 5, width, height, 10, 10);

        // 2. Body Meja (Coklat Tua)
        g2.setColor(new Color(62, 39, 35));
        g2.fillRoundRect(x, y, width, height, 8, 8);

        // 3. Permukaan Atas (Warna Kayu Polished)
        g2.setColor(new Color(93, 64, 55));
        g2.fillRoundRect(x, y, width, height - 10, 8, 8);

        // 4. Label "SUBMIT HERE"
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        String text = "SUBMIT HERE";
        int tw = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, x + (width - tw) / 2, y + height - 25);

        // Highlight tepi
        g2.setColor(new Color(255, 255, 255, 30));
        g2.drawRoundRect(x, y, width, height - 10, 8, 8);
    }
}
