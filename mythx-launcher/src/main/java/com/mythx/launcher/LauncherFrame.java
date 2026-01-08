package com.mythx.launcher;

import com.mythx.launcher.service.ManifestService;
import com.mythx.launcher.web.update.Update;
import com.mythx.launcher.web.update.UpdateGrabber;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URI;
import java.util.ArrayList;

public class LauncherFrame extends JFrame {

    // MythX Purple Color Scheme (matching wrapper)
    private static final Color BG_DARK = new Color(13, 5, 16);           // #0D0510
    private static final Color BG_MEDIUM = new Color(26, 10, 31);        // #1A0A1F
    private static final Color BG_LIGHT = new Color(42, 17, 51);         // #2A1133
    private static final Color ACCENT_PRIMARY = new Color(155, 77, 202); // #9B4DCA
    private static final Color ACCENT_SECONDARY = new Color(100, 29, 155); // #641D9B
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(181, 134, 151); // #B58697
    private static final Color PROGRESS_BG = new Color(61, 26, 74);      // #3D1A4A
    private static final Color BUTTON_HOVER = new Color(180, 100, 220);  // Lighter purple for hover

    public static int mousePositionX;
    public static int mousePositionY;

    private int dragMouseX, dragMouseY;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton playButton;
    private JPanel mainPanel;
    private final ManifestService manifestService = new ManifestService();

    public LauncherFrame() {
    }

    public ArrayList<Component> announcementComponents = new ArrayList<>();

    public void start() {
        Dimension size = new Dimension(800, 500);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setResizable(false);
        setBackground(new Color(0, 0, 0, 0));

        // Main panel with rounded corners
        mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(BG_DARK);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));

                // Inner border
                g2.setColor(BG_LIGHT);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 18, 18));

                // Top accent line
                g2.setColor(ACCENT_PRIMARY);
                g2.fillRoundRect(20, 8, getWidth() - 40, 3, 2, 2);

                g2.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setOpaque(false);

        // Top bar with title and window controls
        JPanel topBar = createTopBar();
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Center content area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(10, 30, 20, 30));

        // Left side - Announcements
        JPanel announcementsPanel = createAnnouncementsPanel();
        centerPanel.add(announcementsPanel, BorderLayout.CENTER);

        // Right side - Play section
        JPanel playPanel = createPlayPanel();
        centerPanel.add(playPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom navigation
        JPanel navPanel = createNavigationPanel();
        mainPanel.add(navPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Make window draggable
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragMouseX = e.getX();
                dragMouseY = e.getY();
                mousePositionX = e.getX();
                mousePositionY = e.getY();
                System.out.println("mouseX=" + mousePositionX + ",mouseY=" + mousePositionY);
            }
        });
        mainPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - dragMouseX, e.getYOnScreen() - dragMouseY);
            }
        });

        // Beta mode shortcut
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_B) {
                    LauncherSettings.BETA_MODE = true;
                    System.out.println("Beta mode activated.");
                    statusLabel.setText("Beta mode activated");
                }
            }
        });

        setFocusable(true);

        // Set rounded shape
        setShape(new RoundRectangle2D.Double(0, 0, size.width, size.height, 20, 20));

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(20, 30, 10, 30));

        // Title
        JLabel titleLabel = new JLabel("MythX");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(ACCENT_PRIMARY);
        topBar.add(titleLabel, BorderLayout.WEST);

        // Window controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JLabel minimizeBtn = new JLabel("—");
        minimizeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        minimizeBtn.setForeground(TEXT_SECONDARY);
        minimizeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        minimizeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setState(Frame.ICONIFIED);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                minimizeBtn.setForeground(TEXT_PRIMARY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                minimizeBtn.setForeground(TEXT_SECONDARY);
            }
        });
        controls.add(minimizeBtn);

        JLabel closeBtn = new JLabel("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        closeBtn.setForeground(TEXT_SECONDARY);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                closeBtn.setForeground(Color.RED);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeBtn.setForeground(TEXT_SECONDARY);
            }
        });
        controls.add(closeBtn);

        topBar.add(controls, BorderLayout.EAST);

        return topBar;
    }

    private JPanel createAnnouncementsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(0, 0, 0, 20));

        // Section header
        JLabel header = new JLabel("LATEST UPDATES");
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(ACCENT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(Box.createVerticalStrut(15));

        // Announcements container with scroll
        JPanel announcementsContainer = new JPanel();
        announcementsContainer.setLayout(new BoxLayout(announcementsContainer, BoxLayout.Y_AXIS));
        announcementsContainer.setOpaque(false);

        if (UpdateGrabber.updateRequest != null && UpdateGrabber.updateRequest.getData() != null) {
            int count = 0;
            for (Update update : UpdateGrabber.updateRequest.getData()) {
                if (count >= 5) break; // Limit to 5 announcements
                JPanel updatePanel = createUpdateItem(update);
                announcementsContainer.add(updatePanel);
                announcementsContainer.add(Box.createVerticalStrut(10));
                count++;
            }
        } else {
            JLabel noUpdates = new JLabel("No updates available");
            noUpdates.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            noUpdates.setForeground(TEXT_SECONDARY);
            announcementsContainer.add(noUpdates);
        }

        JScrollPane scrollPane = new JScrollPane(announcementsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(400, 250));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Style scrollbar
        scrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(8, 0));

        panel.add(scrollPane);

        return panel;
    }

    private JPanel createUpdateItem(Update update) {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_MEDIUM);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        panel.setLayout(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(12, 15, 12, 15));
        panel.setMaximumSize(new Dimension(400, 60));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel title = new JLabel(update.getTitle());
        title.setFont(new Font("Segoe UI", Font.BOLD, 13));
        title.setForeground(TEXT_PRIMARY);
        panel.add(title, BorderLayout.CENTER);

        JLabel date = new JLabel(update.getDate());
        date.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        date.setForeground(TEXT_SECONDARY);
        panel.add(date, BorderLayout.EAST);

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl("https://mythxrsps.com/updates/" + update.getId());
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_PRIMARY, 1),
                    new EmptyBorder(11, 14, 11, 14)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBorder(new EmptyBorder(12, 15, 12, 15));
            }
        });

        return panel;
    }

    private JPanel createPlayPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(280, 300));
        panel.setBorder(new EmptyBorder(0, 20, 0, 0));

        // Logo/Title area
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_MEDIUM);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Border
                g2.setColor(BG_LIGHT);
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(30, 20, 30, 20));
        logoPanel.setMaximumSize(new Dimension(280, 150));
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel serverName = new JLabel("MythX");
        serverName.setFont(new Font("Segoe UI", Font.BOLD, 36));
        serverName.setForeground(ACCENT_PRIMARY);
        serverName.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(serverName);

        JLabel tagline = new JLabel("RSPS");
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tagline.setForeground(TEXT_SECONDARY);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoPanel.add(tagline);

        panel.add(logoPanel);
        panel.add(Box.createVerticalStrut(20));

        // Status label
        statusLabel = new JLabel("Ready to play!");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(statusLabel);
        panel.add(Box.createVerticalStrut(8));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setForeground(ACCENT_PRIMARY);
        progressBar.setBackground(PROGRESS_BG);
        progressBar.setPreferredSize(new Dimension(240, 8));
        progressBar.setMaximumSize(new Dimension(280, 8));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setVisible(false);

        // Custom progress bar UI
        progressBar.setUI(new javax.swing.plaf.basic.BasicProgressBarUI() {
            @Override
            protected void paintDeterminate(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = c.getWidth();
                int height = c.getHeight();

                // Background
                g2.setColor(PROGRESS_BG);
                g2.fillRoundRect(0, 0, width, height, height, height);

                // Progress
                int progressWidth = (int) (width * ((double) progressBar.getValue() / 100));
                if (progressWidth > 0) {
                    GradientPaint gradient = new GradientPaint(0, 0, ACCENT_PRIMARY, progressWidth, 0, ACCENT_SECONDARY);
                    g2.setPaint(gradient);
                    g2.fillRoundRect(0, 0, progressWidth, height, height, height);
                }

                g2.dispose();
            }
        });

        panel.add(progressBar);
        panel.add(Box.createVerticalStrut(15));

        // Play button
        playButton = new JButton("PLAY NOW") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2.setColor(ACCENT_SECONDARY);
                } else if (getModel().isRollover()) {
                    g2.setColor(BUTTON_HOVER);
                } else {
                    GradientPaint gradient = new GradientPaint(0, 0, ACCENT_PRIMARY, 0, getHeight(), ACCENT_SECONDARY);
                    g2.setPaint(gradient);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2.setColor(TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);

                g2.dispose();
            }
        };
        playButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        playButton.setForeground(TEXT_PRIMARY);
        playButton.setContentAreaFilled(false);
        playButton.setBorderPainted(false);
        playButton.setFocusPainted(false);
        playButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        playButton.setPreferredSize(new Dimension(240, 50));
        playButton.setMaximumSize(new Dimension(280, 50));
        playButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        playButton.addActionListener(e -> launchGame());

        panel.add(playButton);

        return panel;
    }

    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 30, 25, 30));

        String[][] navItems = new String[][] {
            {"Website", "https://mythxrsps.com/"},
            {"Vote", "https://mythxrsps.com/vote"},
            {"Store", "https://mythxrsps.com/store"},
            {"Updates", "https://mythxrsps.com/updates"},
            {"Discord", "https://mythxrsps.com/discord"},
        };

        for (String[] nav : navItems) {
            JLabel navButton = createNavButton(nav[0], nav[1]);
            panel.add(navButton);
        }

        return panel;
    }

    private JLabel createNavButton(String text, String url) {
        JLabel button = new JLabel(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(TEXT_SECONDARY);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(8, 15, 8, 15));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUrl(url);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(ACCENT_PRIMARY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_SECONDARY);
            }
        });

        return button;
    }

    private void launchGame() {
        if (Launch.getDownload() != null) {
            return; // Already downloading
        }

        playButton.setEnabled(false);
        playButton.setText("LAUNCHING...");
        progressBar.setVisible(true);
        progressBar.setValue(0);
        statusLabel.setText("Connecting...");

        new Thread(() -> {
            String downloadUrl;
            String filename;

            if (manifestService.fetchManifest()) {
                downloadUrl = manifestService.getClientUrl();
                filename = manifestService.getClientFilename();
                System.out.println("Manifest version: " + manifestService.getVersion());
                System.out.println("Downloading from: " + downloadUrl);
            } else {
                System.out.println("Manifest fetch failed, using fallback URL");
                downloadUrl = LauncherSettings.getClientDownloadUrl();
                filename = "mythx.jar";
            }

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Downloading client...");
            });

            Launch.resetDownload(downloadUrl, filename != null ? filename : "mythx.jar").start();
        }).start();
    }

    // Public methods for updating progress from Download class
    public void setProgress(int percent) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(true);
            progressBar.setValue(percent);
            if (percent >= 100) {
                statusLabel.setText("Launching game...");
                playButton.setText("LAUNCHING...");
            }
        });
    }

    public void setStatusText(String text) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(text);
        });
    }

    public void resetPlayButton() {
        SwingUtilities.invokeLater(() -> {
            playButton.setEnabled(true);
            playButton.setText("PLAY NOW");
            progressBar.setVisible(false);
            progressBar.setValue(0);
            statusLabel.setText("Ready to play!");
        });
    }

    private void openUrl(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.out.println("Failed to open URL: " + url);
            e.printStackTrace();
        }
    }

    // Custom scrollbar UI
    private class CustomScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            this.thumbColor = BG_LIGHT;
            this.trackColor = BG_DARK;
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
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            return button;
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(BG_LIGHT);
            g2.fillRoundRect(thumbBounds.x + 1, thumbBounds.y, thumbBounds.width - 2, thumbBounds.height, 6, 6);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            // Don't paint track for cleaner look
        }
    }
}
