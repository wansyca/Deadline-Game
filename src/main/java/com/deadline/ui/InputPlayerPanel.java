package com.deadline.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
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
import javax.swing.SwingConstants;
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
    private Image bgImage;

    private static final int BTN_WIDTH = 180;
    private static final int BTN_HEIGHT = 55;
    private static final int AVATAR_SIZE = 150;

    public InputPlayerPanel() {
        setLayout(null);

        // LOAD ASSET
        bgImage = loadImage("/assets/bg.png");

        // TITLE - PLAYER REGISTRATION
        title = new JLabel("PLAYER REGISTRATION", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // 3D Shadow
                g2.setColor(new Color(0, 80, 255));
                g2.drawString(getText(), tx + 4, ty + 4);
                g2.setColor(new Color(255, 0, 0));
                g2.drawString(getText(), tx + 2, ty + 2);
                
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        title.setFont(new Font("Monospaced", Font.BOLD, 42));
        add(title);

        // NAME LABEL
        nameLabel = new JLabel("Student Name:") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g2.setFont(getFont());
                
                g2.setColor(new Color(150, 0, 0)); // Red shadow
                g2.drawString(getText(), 2, 18);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), 0, 16);
                g2.dispose();
            }
        };
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
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

        // CHARACTER LABEL
        avatarLabel = new JLabel("SELECT CHARACTER:") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                g2.setFont(getFont());
                
                g2.setColor(new Color(150, 0, 0));
                g2.drawString(getText(), 2, 18);
                g2.setColor(Color.WHITE);
                g2.drawString(getText(), 0, 16);
                g2.dispose();
            }
        };
        avatarLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        add(avatarLabel);

        // AVATAR
        setupAvatarButtons();

        // BUTTON BACK
        backBtn = new ImageButton(
            "/assets/buttons/btn_back_normal.png",
            "/assets/buttons/btn_back_hover.png",
            "/assets/buttons/btn_back_pressed.png",
            BTN_WIDTH, BTN_HEIGHT
        );
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
            BTN_WIDTH, BTN_HEIGHT
        );
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

        // TITLE
        title.setBounds(centerX - 300, 80, 600, 50); // Moved down from 40 to 80
        
        // NAME INPUT
        int nameLabelY = 180; // Moved down from 130 to 180
        nameLabel.setBounds(centerX - 150, nameLabelY, 300, 25);
        nameField.setBounds(centerX - 150, nameLabelY + 30, 300, 50);

        // AVATAR SELECTION
        int avatarLabelY = nameLabelY + 100;
        avatarLabel.setBounds(centerX - 150, avatarLabelY, 300, 25);

        int avatarGap = 60;
        int totalAvatarW = (avatarButtons.size() * AVATAR_SIZE) + ((avatarButtons.size() - 1) * avatarGap);
        int avatarX = centerX - (totalAvatarW / 2);

        for (int i = 0; i < avatarButtons.size(); i++) {
            avatarButtons.get(i).setBounds(avatarX + (i * (AVATAR_SIZE + avatarGap)), avatarLabelY + 40, AVATAR_SIZE, AVATAR_SIZE);
        }

        // BUTTONS (BACK | NEXT) - 35px gap from avatar area
        int btnY = avatarLabelY + 40 + AVATAR_SIZE + 35; 
        int btnGap = 40;
        int totalBtnW = (BTN_WIDTH * 2) + btnGap;
        int btnStartX = centerX - (totalBtnW / 2);

        backBtn.setBounds(btnStartX, btnY, BTN_WIDTH, BTN_HEIGHT);
        playBtn.setBounds(btnStartX + BTN_WIDTH + btnGap, btnY, BTN_WIDTH, BTN_HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

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

        public AvatarButton(String path) {
            this.path = path;

            try {
                java.net.URL url = getClass().getResource(path);
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

            if (img != null) {
                g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            }

            if (isSelected) {
                g2.setColor(new Color(255, 0, 0));
                g2.setStroke(new BasicStroke(4));
            } else if (isHovered) {
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2));
            } else {
                g2.setColor(new Color(200, 200, 200, 150));
                g2.setStroke(new BasicStroke(1));
            }
            g2.drawRect(2, 2, getWidth() - 4, getHeight() - 4);

            g2.dispose();
        }
    }
}