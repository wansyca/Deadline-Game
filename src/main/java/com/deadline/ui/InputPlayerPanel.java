package com.deadline.ui;

import java.awt.Color;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.deadline.audio.SoundManager;
import com.deadline.main.Main;

public class InputPlayerPanel extends JPanel {

    private JTextField nameField;
    private String selectedAvatar = null;

    private ImageButton playBtn;
    private ImageButton backBtn;

    private JLabel title;
    private JLabel nameLabel;
    private JLabel avatarLabel;

    private List<AvatarButton> avatarButtons = new ArrayList<>();
    private Image bgImage, titleAsset;

    private static final int BTN_WIDTH = 170;
    private static final int BTN_HEIGHT = 55;
    private static final int AVATAR_SIZE = 160; // 32 * 5
    private static final int GAP = 30;

    private Font pixelFont;

    public InputPlayerPanel() {
        loadPixelFont();
        setLayout(null);
        setOpaque(false);

        // LOAD ASSET
        bgImage = loadImage("/assets/bg.png");
        titleAsset = loadImage("/assets/player_regis.png");

        title = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (titleAsset != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

                    // Maintain aspect ratio or fill? Usually title assets have specific sizes.
                    // Let's draw it centered.
                    int aw = titleAsset.getWidth(null);
                    int ah = titleAsset.getHeight(null);
                    float scale = Math.min((float) getWidth() / aw, (float) getHeight() / ah);
                    int tw = (int) (aw * scale);
                    int th = (int) (ah * scale);
                    int tx = (getWidth() - tw) / 2;
                    int ty = (getHeight() - th) / 2;

                    g2.drawImage(titleAsset, tx, ty, tw, th, null);
                    g2.dispose();
                }
            }
        };
        title.setFont(new Font("Monospaced", Font.BOLD, 64)); // Larger base size for scaling
        title.setOpaque(false);
        add(title);

        nameLabel = new JLabel("Student Name:") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                String text = getText();

                int scale = 3;
                int bw = getWidth() / scale;
                int bh = getHeight() / scale;
                if (bw <= 0 || bh <= 0)
                    return;

                java.awt.image.BufferedImage buffer = new java.awt.image.BufferedImage(bw, bh,
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
                Graphics2D gBuf = buffer.createGraphics();
                gBuf.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                gBuf.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize2D() / scale));

                // Shadow
                gBuf.setColor(Color.BLACK);
                gBuf.drawString(text, 1, 11);
                // Text
                gBuf.setColor(Color.WHITE);
                gBuf.drawString(text, 0, 10);
                gBuf.dispose();

                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(buffer, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            }
        };
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 30));
        nameLabel.setOpaque(false);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setFont(new Font("Monospaced", Font.BOLD, 30));
        nameField.setBackground(new Color(20, 20, 30));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setOpaque(true); // Keep opaque for background color but sharp border
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2), // Sharp 2px border
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        add(nameField);

        avatarLabel = new JLabel("SELECT CHARACTER:") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                String text = getText();

                int scale = 3;
                int bw = getWidth() / scale;
                int bh = getHeight() / scale;
                if (bw <= 0 || bh <= 0)
                    return;

                java.awt.image.BufferedImage buffer = new java.awt.image.BufferedImage(bw, bh,
                        java.awt.image.BufferedImage.TYPE_INT_ARGB);
                Graphics2D gBuf = buffer.createGraphics();
                gBuf.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                gBuf.setFont(getFont().deriveFont(Font.BOLD, getFont().getSize2D() / scale));

                // Shadow
                gBuf.setColor(Color.BLACK);
                gBuf.drawString(text, 1, 11);
                // Text
                gBuf.setColor(Color.WHITE);
                gBuf.drawString(text, 0, 10);
                gBuf.dispose();

                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2.drawImage(buffer, 0, 0, getWidth(), getHeight(), null);
                g2.dispose();
            }
        };
        avatarLabel.setFont(new Font("Monospaced", Font.BOLD, 30));
        avatarLabel.setOpaque(false);
        add(avatarLabel);

        // AVATAR
        setupAvatarButtons();

        // BUTTON BACK
        backBtn = new ImageButton(
                "/assets/buttons/btn_back_normal.png",
                "/assets/buttons/btn_back_hover.png",
                "/assets/buttons/btn_back_pressed.png",
                BTN_WIDTH, BTN_HEIGHT);
        backBtn.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.switchPage(Main.DASHBOARD);
        });
        add(backBtn);

        // BUTTON NEXT
        playBtn = new ImageButton(
                "/assets/buttons/btn_next_normal.png",
                "/assets/buttons/btn_next_hover.png",
                "/assets/buttons/btn_next_pressed.png",
                BTN_WIDTH, BTN_HEIGHT);
        playBtn.addActionListener(e -> {
            SoundManager.playClickSound();

            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                CustomAlert.showWarning(this, "Empty Input", "Name cannot be empty!");
                return;
            }

            if (selectedAvatar == null) {
                CustomAlert.showWarning(this, "No Avatar Selected", "Please select a character first!");
                return;
            }

            com.deadline.backend.ScoreService scoreService = new com.deadline.backend.ScoreService();
            if (scoreService.isUsernameInLeaderboard(name)) {
                CustomAlert.showError(this, "Username Taken",
                        "Username '" + name + "' already exists.\nPlease choose another name!");
                return;
            }

            Main.goToGameWithLoading(-1, name, selectedAvatar);
        });
        add(playBtn);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });

        SwingUtilities.invokeLater(this::updateLayout);
    }

    private void loadPixelFont() {
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/assets/font/pixel.ttf");
            if (is != null) {
                pixelFont = Font.createFont(Font.TRUETYPE_FONT, is);
                System.out.println("✅ Pixel font loaded successfully!");
            } else {
                System.err.println("❌ Font file not found at /assets/font/pixel.ttf, using Monospaced fallback.");
                pixelFont = new Font("Monospaced", Font.BOLD, 20);
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading font: " + e.getMessage());
            pixelFont = new Font("Monospaced", Font.BOLD, 20);
        }
    }

    private Image loadImage(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void setupAvatarButtons() {
        avatarButtons.add(new AvatarButton("/assets/avatar_cowo.png", "cowo"));
        avatarButtons.add(new AvatarButton("/assets/avatar_cewe.png", "cewe"));

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

        // TITLE - Made larger
        title.setBounds(centerX - 700, 10, 1400, 220);

        int avatarGap = 40;
        int totalAvatarW = (avatarButtons.size() * AVATAR_SIZE) + ((avatarButtons.size() - 1) * avatarGap); // 360px
        int avatarX = centerX - (totalAvatarW / 2);

        // NAME INPUT (Aligned with Avatar Area)
        int nameLabelY = 180;
        nameLabel.setBounds(avatarX, nameLabelY, totalAvatarW, 30);
        nameField.setBounds(avatarX, nameLabelY + 35, totalAvatarW, 50);

        // AVATAR SELECTION
        int avatarLabelY = nameLabelY + 35 + 50 + GAP; // GAP = 30px

        // Align label with boxes width (360px)
        avatarLabel.setBounds(avatarX, avatarLabelY, totalAvatarW, 30);

        // Position boxes 40px below the label Y
        for (int i = 0; i < avatarButtons.size(); i++) {
            avatarButtons.get(i).setBounds(avatarX + (i * (AVATAR_SIZE + avatarGap)), avatarLabelY + 40, AVATAR_SIZE,
                    AVATAR_SIZE);
        }

        // BUTTONS (BACK | NEXT) - Outer edges aligned with character boxes
        int btnY = avatarLabelY + 40 + AVATAR_SIZE + GAP; // GAP = 30px
        int btnGap = 20;
        int totalBtnW = (BTN_WIDTH * 2) + btnGap; // 170 + 20 + 170 = 360px
        int btnStartX = centerX - (totalBtnW / 2);

        backBtn.setBounds(btnStartX, btnY, BTN_WIDTH, BTN_HEIGHT);
        playBtn.setBounds(btnStartX + BTN_WIDTH + btnGap, btnY, BTN_WIDTH, BTN_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        if (bgImage != null) {
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
        }

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.dispose();
    }

    private class AvatarButton extends JPanel {
        private String path;
        private Image img;
        private boolean isSelected = false;
        private boolean isHovered = false;

        public AvatarButton(String imagePath, String folderName) {
            this.path = folderName;

            try {
                java.net.URL url = getClass().getResource(imagePath);
                if (url != null) {
                    img = new ImageIcon(url).getImage();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            setOpaque(false);
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

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

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            if (img != null) {
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            }

            // PIXEL FRAME (Sharp Border)
            if (isSelected) {
                g2.setColor(new Color(255, 0, 0)); // Pure Red
                // Thick 3px border for selection
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                g2.drawRect(2, 2, getWidth() - 5, getHeight() - 5);
            } else if (isHovered) {
                g2.setColor(Color.WHITE);
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            } else {
                g2.setColor(new Color(200, 200, 200));
                g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }

            g2.dispose();
        }
    }
}