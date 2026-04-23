package com.deadline;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.Image;
import java.util.List;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SurvivorRankingUI extends JPanel {
    private JPanel contentPanel;

    public SurvivorRankingUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(10, 10, 15));

        add(createHeader(), BorderLayout.NORTH);

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        add(contentPanel, BorderLayout.CENTER);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData();
            }
        });

        // Initial data load
        refreshData();
    }

    private void refreshData() {
        if (contentPanel == null)
            return;
        contentPanel.removeAll();
        List<LeaderboardManager.PlayerScore> scores = LeaderboardManager.loadScores(100);

        // Podiums
        contentPanel.add(createPodiumSection(scores), BorderLayout.NORTH);

        // List Section
        contentPanel.add(createListSection(scores), BorderLayout.CENTER);

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // ===== HEADER =====
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        header.setBackground(new Color(20, 20, 40));

        JButton backBtn = new JButton("← BACK TO MENU");
        backBtn.setForeground(new Color(200, 220, 255));
        backBtn.setBackground(new Color(40, 40, 60));
        backBtn.setFocusPainted(false);
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        backBtn.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.switchPage(Main.DASHBOARD);
        });

        JLabel title = new JLabel("HALL OF SURVIVORS");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setHorizontalAlignment(SwingConstants.CENTER);


        // PANEL KHUSUS BIAR BENER-BENER CENTER
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(title);


        header.add(backBtn, BorderLayout.WEST);
        header.add(centerPanel, BorderLayout.CENTER);
        header.add(Box.createHorizontalStrut(150), BorderLayout.EAST);

        

        return header;
    }

    // ===== PODIUM =====
    private JPanel createPodiumSection(List<LeaderboardManager.PlayerScore> scores) {
        JPanel panel = new JPanel(new GridLayout(1, 3, 30, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 20, 80));

        LeaderboardManager.PlayerScore p1 = scores.size() > 0 ? scores.get(0) : null;
        LeaderboardManager.PlayerScore p2 = scores.size() > 1 ? scores.get(1) : null;
        LeaderboardManager.PlayerScore p3 = scores.size() > 2 ? scores.get(2) : null;

        // Order: 2nd, 1st, 3rd
        panel.add(createPodiumCard("2ND", p2 != null ? p2.name : "---", p2 != null ? String.valueOf(p2.score) : "0",
                140, new Color(192, 192, 192)));
        panel.add(createPodiumCard("1ST", p1 != null ? p1.name : "---", p1 != null ? String.valueOf(p1.score) : "0",
                190, new Color(255, 215, 0)));
        panel.add(createPodiumCard("3RD", p3 != null ? p3.name : "---", p3 != null ? String.valueOf(p3.score) : "0",
                110, new Color(205, 127, 50)));

        return panel;
    }

    private JPanel createPodiumCard(String rank, String name, String score, int height, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        JLabel nameLbl = new JLabel(name.toUpperCase());
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLbl = new JLabel(score + " POINTS");
        scoreLbl.setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
        scoreLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoreLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel box = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Shadow
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 5, 20, 20);

                // Body
                GradientPaint gp = new GradientPaint(0, 0, new Color(40, 40, 50), 0, getHeight(),
                        new Color(20, 20, 30));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Rank Highlight
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                g2.fillRoundRect(0, 0, getWidth(), 40, 20, 20);
                g2.fillRect(0, 20, getWidth(), 20);

                // Glow Border
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 20, 20);

                g2.dispose();
            }
        };
        box.setPreferredSize(new Dimension(100, height));
        box.setMaximumSize(new Dimension(120, height));
        box.setOpaque(false);

        JLabel rankLbl = new JLabel(rank);
        rankLbl.setForeground(Color.WHITE);
        rankLbl.setFont(new Font("Impact", Font.PLAIN, 36));
        box.add(rankLbl);

        card.add(nameLbl);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(scoreLbl);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(box);

        return card;
    }

    // ===== LIST =====
    private JComponent createListSection(List<LeaderboardManager.PlayerScore> scores) {
        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(BorderFactory.createEmptyBorder(20, 100, 40, 100));

        if (scores.size() <= 3) {
            JLabel empty = new JLabel("No other survivors recorded yet...");
            empty.setForeground(new Color(100, 100, 120));
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 16));
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            list.add(Box.createVerticalGlue());
            list.add(empty);
            list.add(Box.createVerticalGlue());
        } else {
            for (int i = 3; i < scores.size(); i++) {
                LeaderboardManager.PlayerScore ps = scores.get(i);
                list.add(createRow(
                    String.valueOf(i + 1),
                    ps.name,
                    String.valueOf(ps.score),
                    ps.avatarPath
                ));
                list.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JScrollPane scroll = new JScrollPane(list);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));
        scroll.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(60, 60, 80);
                this.trackColor = new Color(20, 20, 30);
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
        return scroll;
    }

    private JPanel createRow(String rank, String name, String score, String avatarPath) {

        

    JPanel row = new JPanel(new BorderLayout()) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 10));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.setColor(new Color(255, 255, 255, 20));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
        }
    };

    row.setOpaque(false);
    row.setPreferredSize(new Dimension(0, 55));
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
    row.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

    // 🔥 AVATAR
    JLabel avatarLabel = new JLabel();
    avatarLabel.setPreferredSize(new Dimension(40, 40));

    try {
        if (avatarPath != null) {
            ImageIcon icon = new ImageIcon(getClass().getResource(avatarPath));
            Image img = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            avatarLabel.setIcon(new ImageIcon(img));
        }
    } catch (Exception e) {
        avatarLabel.setText("?");
        avatarLabel.setForeground(Color.WHITE);
    }

    // 🔥 TEXT
    JLabel nameLabel = new JLabel("#" + rank + "  " + name.toUpperCase());
    nameLabel.setForeground(new Color(220, 220, 240));
    nameLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));

    JLabel scoreLabel = new JLabel(score + " PTS");
    scoreLabel.setForeground(new Color(150, 180, 255));
    scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

    // 🔥 LEFT CONTAINER (AVATAR + NAME)
    JPanel leftPanel = new JPanel();
    leftPanel.setOpaque(false);
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));

    leftPanel.add(avatarLabel);
    leftPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    leftPanel.add(nameLabel);

    row.add(leftPanel, BorderLayout.WEST);
    row.add(scoreLabel, BorderLayout.EAST);

    return row;
}
}
