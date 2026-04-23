package com.deadline;

import javax.swing.*;
import java.awt.*;

public class LoadingPage extends JPanel {

    private int progress = 0;

    public LoadingPage() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 10, 10));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel title = new JLabel("23:59 — SUBMIT OR DIE", SwingConstants.CENTER);
        title.setForeground(new Color(255, 50, 50));
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        gbc.gridy = 0;
        centerPanel.add(title, gbc);

        JLabel percent = new JLabel("Loading 0%", SwingConstants.CENTER);
        percent.setForeground(Color.LIGHT_GRAY);
        percent.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        gbc.gridy = 1;
        centerPanel.add(percent, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // 🔥 progress bar custom
        Timer timer = new Timer(30, e -> {
            if (progress < 100) {
                progress++;
                percent.setText("Menyiapkan berkas deadline... " + progress + "%");
                repaint();
            } else {
                ((Timer) e.getSource()).stop();
            }
        });

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent evt) {
                progress = 0;
                timer.restart();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int barWidth = 400;
        int x = (w - barWidth) / 2;
        int y = getHeight() / 2 + 80;

        // background bar
        g2.setColor(new Color(50, 50, 50));
        g2.fillRoundRect(x, y, barWidth, 12, 10, 10);

        // progress merah
        g2.setColor(new Color(220, 0, 0));
        g2.fillRoundRect(x, y, (int) (barWidth * (progress / 100.0)), 12, 10, 10);

        // Glow effect
        g2.setColor(new Color(255, 0, 0, 50));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(x - 2, y - 2, barWidth + 4, 16, 12, 12);
    }
}