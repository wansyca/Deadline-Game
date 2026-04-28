package com.deadline.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.deadline.audio.SoundManager;
import com.deadline.main.Main;

public class InputPlayerPanel extends JPanel {

    private JTextField nameField;
    private String selectedAvatar = null;

    private JButton playBtn;
    private JButton backBtn;

    private JLabel title;
    private JLabel nameLabel;
    private JLabel avatarLabel;

    private List<AvatarButton> avatarButtons = new ArrayList<>();

    public InputPlayerPanel() {
        setLayout(null);
        setBackground(new Color(20, 20, 30));

        // TITLE
        title = new JLabel("PLAYER REGISTRATION", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 36));
        title.setForeground(new Color(255, 50, 50));
        add(title);

        // NAME
        nameLabel = new JLabel("Nama Mahasiswa:");
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setFont(new Font("Monospaced", Font.BOLD, 22));
        nameField.setBackground(new Color(40, 40, 50));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 150, 150), 3),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        add(nameField);

        // AVATAR LABEL
        avatarLabel = new JLabel("PILIH KARAKTER:");
        avatarLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        avatarLabel.setForeground(Color.WHITE);
        add(avatarLabel);

        // AVATAR
        setupAvatarButtons();

        // BUTTON LANJUTKAN
        playBtn = createMainButton("LANJUTKAN", new Color(40, 150, 40));
        playBtn.addActionListener(e -> {
            SoundManager.playClickSound();

            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                CustomAlert.showWarning(this, "Input Kosong", "Nama tidak boleh kosong!");
                return;
            }

            if (selectedAvatar == null) {
                CustomAlert.showWarning(this, "Avatar Belum Pilih", "Pilih karakter terlebih dahulu!");
                return;
            }

            com.deadline.backend.ScoreService scoreService = new com.deadline.backend.ScoreService();
            if (scoreService.isUsernameInLeaderboard(name)) {
                CustomAlert.showError(this, "Username Terpakai",
                        "Username '" + name + "' sudah ada di leaderboard.\nSilakan pilih nama lain!");
                return;
            }

            Main.goToGameWithLoading(-1, name, selectedAvatar);
        });
        add(playBtn);

        // BUTTON BACK
        backBtn = createMainButton("KEMBALI", new Color(150, 40, 40));
        backBtn.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.switchPage(Main.DASHBOARD);
        });
        add(backBtn);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });

        SwingUtilities.invokeLater(this::updateLayout);
    }

    private void setupAvatarButtons() {
        avatarButtons.add(new AvatarButton("/assets/avatar_cowo.png"));
        avatarButtons.add(new AvatarButton("/assets/avatar_cewe.png"));

        for (AvatarButton btn : avatarButtons) {
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    SoundManager.playClickSound();
                    selectedAvatar = btn.path;

                    for (AvatarButton other : avatarButtons) {
                        other.setSelected(other == btn);
                    }
                }
            });
            add(btn);
        }
    }

    private void updateLayout() {
        int centerX = getWidth() / 2;

        title.setBounds(centerX - 250, 50, 500, 50);

        nameLabel.setBounds(centerX - 150, 130, 300, 25);
        nameField.setBounds(centerX - 150, 160, 300, 50);

        avatarLabel.setBounds(centerX - 150, 230, 300, 25);

        // 🔥 BESARIN AVATAR
        int size = 180;
        int gap = 40;

        int totalWidth = (avatarButtons.size() * size) + ((avatarButtons.size() - 1) * gap);
        int startX = centerX - (totalWidth / 2);

        for (int i = 0; i < avatarButtons.size(); i++) {
            avatarButtons.get(i).setBounds(startX + (i * (size + gap)), 260, size, size);
        }

        playBtn.setBounds(centerX - 110, 470, 220, 50);
        backBtn.setBounds(centerX - 110, 540, 220, 50);
    }

    private JButton createMainButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();

                Color current = baseColor;
                if (getModel().isPressed()) {
                    current = baseColor.darker().darker();
                } else if (getModel().isRollover()) {
                    current = baseColor.brighter();
                }

                g2.setColor(current.darker());
                g2.fillRect(4, 4, getWidth() - 4, getHeight() - 4);

                g2.setColor(current);
                g2.fillRect(0, 0, getWidth() - 4, getHeight() - 4);

                g2.setColor(Color.WHITE);
                g2.drawRect(0, 0, getWidth() - 5, getHeight() - 5);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Monospaced", Font.BOLD, 22));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(30, 30, 40));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(new Color(25, 25, 35));
        for (int i = 0; i < getWidth(); i += 32) {
            for (int j = 0; j < getHeight(); j += 32) {
                if ((i + j) % 64 == 0) {
                    g2.fillRect(i, j, 32, 32);
                }
            }
        }
    }

    private class AvatarButton extends JPanel {
        private String path;
        private Image img;
        private boolean isSelected = false;
        private boolean isHovered = false;

        public AvatarButton(String path) {
            this.path = path;

            try {
                java.net.URL url = getClass().getResource(path);

                if (url == null) {
                    System.err.println("❌ Gagal load: " + path);
                } else {
                    img = new ImageIcon(url).getImage();
                    System.out.println("✅ Berhasil load: " + path);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            if (img != null) {
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            }

            if (isSelected) {
                g2.setColor(new Color(0, 255, 100));
                g2.setStroke(new BasicStroke(4));
                g2.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
            } else if (isHovered) {
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
            }

            g2.dispose();
        }
    }
}