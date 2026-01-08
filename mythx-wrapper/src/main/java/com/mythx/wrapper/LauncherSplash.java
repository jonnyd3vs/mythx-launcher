package com.mythx.wrapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;

public class LauncherSplash extends JFrame {

    // MythX Purple Color Scheme
    private static final Color BG_DARK = new Color(13, 5, 16);           // #0D0510
    private static final Color BG_MEDIUM = new Color(26, 10, 31);        // #1A0A1F
    private static final Color BG_LIGHT = new Color(42, 17, 51);         // #2A1133
    private static final Color ACCENT_PRIMARY = new Color(155, 77, 202); // #9B4DCA
    private static final Color ACCENT_SECONDARY = new Color(100, 29, 155); // #641D9B
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255);
    private static final Color TEXT_SECONDARY = new Color(181, 134, 151); // #B58697
    private static final Color PROGRESS_BG = new Color(61, 26, 74);      // #3D1A4A

    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel titleLabel;
    private JPanel mainPanel;

    private int mouseX, mouseY;

    public LauncherSplash() {
    }

    public void start() {
        Dimension size = new Dimension(450, 180);

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
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Title
        titleLabel = new JLabel("MythX");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(ACCENT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Status label
        statusLabel = new JLabel("Initializing...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(false);
        progressBar.setBorderPainted(false);
        progressBar.setForeground(ACCENT_PRIMARY);
        progressBar.setBackground(PROGRESS_BG);
        progressBar.setPreferredSize(new Dimension(380, 12));
        progressBar.setMaximumSize(new Dimension(380, 12));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

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

        // Close button panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topPanel.setOpaque(false);
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        JLabel closeBtn = new JLabel("✕");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
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
        topPanel.add(closeBtn);

        // Add components
        mainPanel.add(topPanel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(progressBar);

        setContentPane(mainPanel);

        // Make window draggable
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
        mainPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                setLocation(e.getXOnScreen() - mouseX, e.getYOnScreen() - mouseY);
            }
        });

        // Set rounded shape
        setShape(new RoundRectangle2D.Double(0, 0, size.width, size.height, 20, 20));

        // Center on screen
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setProgress(int progress) {
        if (progressBar != null) {
            progressBar.setValue(progress);
        }
    }

    public void setProgressText(String text) {
        if (statusLabel != null) {
            statusLabel.setText(text);
        }
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getProgressLabel() {
        return statusLabel;
    }
}
