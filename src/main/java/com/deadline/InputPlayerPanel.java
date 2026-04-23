package com.deadline;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import com.deadline.backend.PlayerService;

@SuppressWarnings("unused")
public class InputPlayerPanel extends JPanel {

    private JTextField nameField;
    private String selectedAvatar = "/assets/avatar_1_cowo.png";

    private JButton maleBtn;
    private JButton femaleBtn;

    private JButton playBtn;
    private JButton backBtn;

    private JLabel title;
    private JLabel nameLabel;
    private JLabel avatarLabel;

    private Image bg;

    public InputPlayerPanel() {
        setLayout(null);

        bg = new ImageIcon(getClass().getResource("/assets/bg.png")).getImage();

        // TITLE
        title = new JLabel("PLAYER REGISTRATION", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(255, 50, 50));
        add(title);

        // NAME
        nameLabel = new JLabel("Nama Mahasiswa:");
        nameLabel.setForeground(Color.WHITE);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameField.setBackground(new Color(30, 30, 30));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 0, 0), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        add(nameField);

        // AVATAR LABEL
        avatarLabel = new JLabel("Pilih Avatar:");
        avatarLabel.setForeground(Color.WHITE);
        add(avatarLabel);

        // AVATAR
        maleBtn = createAvatarCard("/assets/avatar_1_cowo.png");
        add(maleBtn);

        femaleBtn = createAvatarCard("/assets/avatar_2_cewe.png");
        add(femaleBtn);

        selectCard(maleBtn, "/assets/avatar_1_cowo.png");

        // BUTTON LANJUTKAN
        playBtn = createMainButton("LANJUTKAN");
        playBtn.addActionListener(e -> {
            SoundManager.playClickSound();
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                CustomAlert.showWarning(this, "Input Kosong", "Nama tidak boleh kosong!");
                return;
            }

            // Save player to database
            PlayerService playerService = new PlayerService();
            
            // 🔥 VALIDASI USERNAME DUPLIKAT
            if (playerService.isUsernameExists(name)) {
                CustomAlert.showError(this, "Username Terpakai", 
                    "Username '" + name + "' sudah digunakan.\nSilakan pilih nama lain!");
                return;
            }

            int playerId = playerService.createPlayer(name, selectedAvatar);
            
            if (playerId == -1) {
                CustomAlert.showError(this, "Database Error", "Gagal membuat/mengambil data player!");
                return;
            }

            Main.goToGameWithLoading(playerId, name, selectedAvatar);
});
        add(playBtn);

        // BUTTON BACK
        backBtn = createMainButton("KEMBALI");
        backBtn.addActionListener(e -> {
            SoundManager.playClickSound();
            Main.switchPage(Main.DASHBOARD);
        });
        add(backBtn);

        // 🔥 AUTO CENTER SAAT RESIZE
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateLayout();
            }
        });

        // 🔥 PANGGIL AWAL
        SwingUtilities.invokeLater(this::updateLayout);
    }

    // =========================
    // 🔥 METHOD CENTER LAYOUT
    // =========================
    private void updateLayout() {
        int centerX = getWidth() / 2;

        title.setBounds(centerX - 250, 40, 500, 50);

        nameLabel.setBounds(centerX - 150, 120, 300, 25);
        nameField.setBounds(centerX - 150, 150, 300, 40);

        avatarLabel.setBounds(centerX - 150, 210, 300, 25);

        maleBtn.setBounds(centerX - 180, 250, 150, 170);
        femaleBtn.setBounds(centerX + 30, 250, 150, 170);

        playBtn.setBounds(centerX - 110, 460, 220, 50);
        backBtn.setBounds(centerX - 110, 530, 220, 50);
    }

    // =========================
    // BUTTON STYLE
    // =========================
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

    // =========================
    // AVATAR CARD
    // =========================
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

        btn.addActionListener(e -> {
            SoundManager.playClickSound();
            selectCard(btn, path);
        });

        return btn;
    }

    private void selectCard(JButton btn, String path) {
        selectedAvatar = path;

        resetCard(maleBtn);
        resetCard(femaleBtn);

        btn.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));

        JPanel panel = (JPanel) btn.getComponent(0);
        JLabel status = (JLabel) panel.getComponent(1);
        status.setText("Using");
        status.setForeground(Color.WHITE);
    }

    private void resetCard(JButton btn) {
        if (btn == null) return;

        btn.setBorder(null);

        JPanel panel = (JPanel) btn.getComponent(0);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));

        JLabel status = (JLabel) panel.getComponent(1);
        status.setText("Click to use");
        status.setForeground(Color.LIGHT_GRAY);
    }

    // =========================
    // BACKGROUND
    // =========================
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