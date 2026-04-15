package com.deadline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;

@SuppressWarnings("unused")
public class InputPlayerPanel extends JPanel {

    private JTextField nameField;
    private String selectedAvatar = "assets/Avatar_1_cowo.png";

    private JButton maleBtn;
    private JButton femaleBtn;

    private Image bg;

    public InputPlayerPanel() {
        setLayout(null);

        bg = new ImageIcon(getClass().getResource("/assets/bg.png")).getImage();

        int centerX = 800 / 2;

        // TITLE
        JLabel title = new JLabel("PLAYER REGISTRATION", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(255, 50, 50));
        title.setBounds(centerX - 250, 40, 500, 50);
        add(title);

        // NAME
        JLabel nameLabel = new JLabel("Nama Mahasiswa:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setBounds(centerX - 150, 120, 300, 25);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(centerX - 150, 150, 300, 40);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameField.setBackground(new Color(30, 30, 30));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 0, 0), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        add(nameField);

        // AVATAR LABEL
        JLabel avatarLabel = new JLabel("Pilih Avatar:");
        avatarLabel.setForeground(Color.WHITE);
        avatarLabel.setBounds(centerX - 150, 210, 300, 25);
        add(avatarLabel);

        // AVATAR
        maleBtn = createAvatarCard("/assets/Avatar_1_cowo.png");
        maleBtn.setBounds(centerX - 180, 250, 150, 170);
        add(maleBtn);

        femaleBtn = createAvatarCard("/assets/Avatar_2_cewe.png");
        femaleBtn.setBounds(centerX + 30, 250, 150, 170);
        add(femaleBtn);

        selectCard(maleBtn, "/assets/Avatar_1_cowo.png");

        // BUTTON LANJUTKAN
        JButton playBtn = createMainButton("LANJUTKAN");
        playBtn.setBounds(centerX - 110, 460, 220, 50);
        playBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty())
                name = "Mahasiswa";

            Main.goToGameWithLoading(name, selectedAvatar);
        });
        add(playBtn);

        // BUTTON BACK
        JButton backBtn = createMainButton("KEMBALI");
        backBtn.setBounds(centerX - 110, 530, 220, 50);
        backBtn.addActionListener(e -> Main.switchPage(Main.DASHBOARD));
        add(backBtn);
    }

    private JButton createMainButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2.setColor(new Color(100, 0, 0));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(200, 0, 0));
                } else {
                    g2.setColor(new Color(150, 0, 0));
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createAvatarCard(String path) {
        ImageIcon icon;
        try {
            java.net.URL imgUrl = getClass().getResource(path);
            if (imgUrl == null)
                throw new Exception("Resource not found: " + path);
            icon = new ImageIcon(imgUrl);
        } catch (Exception e) {
            return new JButton("Error Loading");
        }

        if (icon.getIconWidth() == -1) {
            return new JButton("Error");
        }

        Image img = icon.getImage();
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        int size = Math.min(w, h);
        int x = (w - size) / 2;
        int y = (h - size) / 2;

        Image cropped = Toolkit.getDefaultToolkit().createImage(
                new FilteredImageSource(img.getSource(),
                        new CropImageFilter(x, y, size, size)));

        Image scaled = cropped.getScaledInstance(110, 110, Image.SCALE_SMOOTH);

        JLabel imageLabel = new JLabel(new ImageIcon(scaled));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel statusLabel = new JLabel("Click to use", SwingConstants.CENTER);
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(50, 60, 60));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        panel.add(imageLabel, BorderLayout.CENTER);
        panel.add(statusLabel, BorderLayout.SOUTH);

        JButton btn = new JButton();
        btn.setLayout(new BorderLayout());
        btn.add(panel);
        btn.setFocusPainted(false);
        btn.setBorder(null);

        btn.addActionListener(e -> selectCard(btn, path));

        return btn;
    }

    private void selectCard(JButton btn, String path) {
        selectedAvatar = path;

        resetCard(maleBtn);
        resetCard(femaleBtn);

        btn.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));

        if (btn.getComponentCount() > 0 && btn.getComponent(0) instanceof JPanel) {
            JPanel panel = (JPanel) btn.getComponent(0);
            if (panel.getComponentCount() > 1 && panel.getComponent(1) instanceof JLabel) {
                JLabel status = (JLabel) panel.getComponent(1);
                status.setText("Using");
                status.setForeground(Color.WHITE);
            }
        }
    }

    private void resetCard(JButton btn) {
        if (btn == null)
            return;
        btn.setBorder(null);

        if (btn.getComponentCount() > 0 && btn.getComponent(0) instanceof JPanel) {
            JPanel panel = (JPanel) btn.getComponent(0);
            panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

            if (panel.getComponentCount() > 1 && panel.getComponent(1) instanceof JLabel) {
                JLabel status = (JLabel) panel.getComponent(1);
                status.setText("Click to use");
                status.setForeground(Color.LIGHT_GRAY);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (bg != null) {
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
        }

        g.setColor(new Color(0, 0, 0, 130));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}