package com.mythx.launcher.jdk;

import javax.swing.*;
import java.awt.*;

/**
 * Progress window for JDK download
 * Shows download status, progress bar, and estimated time remaining
 */
public class JdkDownloadWindow extends JFrame {
    private final JLabel statusLabel;
    private final JProgressBar progressBar;
    private final JLabel progressLabel;
    private final JLabel speedLabel;

    private long totalBytes = 0;
    private long startTime = 0;

    public JdkDownloadWindow() {
        super("Downloading Java 11");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(400, 150);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(new Color(45, 45, 48));

        // Status label
        statusLabel = new JLabel("Preparing download...");
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        progressBar.setForeground(new Color(0, 150, 136));
        progressBar.setBackground(new Color(60, 60, 65));

        // Progress label (percentage and bytes)
        progressLabel = new JLabel("0%");
        progressLabel.setForeground(new Color(200, 200, 200));
        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Speed label
        speedLabel = new JLabel("");
        speedLabel.setForeground(new Color(150, 150, 150));
        speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        // Add components with spacing
        mainPanel.add(statusLabel);
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(progressBar);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(progressLabel);
        mainPanel.add(Box.createVerticalStrut(3));
        mainPanel.add(speedLabel);

        add(mainPanel);
        getContentPane().setBackground(new Color(45, 45, 48));
    }

    /**
     * Set the status message
     */
    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(status));
    }

    /**
     * Set the total bytes for download
     */
    public void setTotalBytes(long total) {
        this.totalBytes = total;
        this.startTime = System.currentTimeMillis();
    }

    /**
     * Update the bytes downloaded
     */
    public void setBytesDownloaded(long downloaded) {
        SwingUtilities.invokeLater(() -> {
            if (totalBytes > 0) {
                int percent = (int) ((downloaded * 100) / totalBytes);
                progressBar.setValue(percent);

                String downloadedStr = formatBytes(downloaded);
                String totalStr = formatBytes(totalBytes);
                progressLabel.setText(String.format("%d%% - %s / %s", percent, downloadedStr, totalStr));

                // Calculate speed and ETA
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed > 0 && downloaded > 0) {
                    long bytesPerSec = (downloaded * 1000) / elapsed;
                    long remaining = totalBytes - downloaded;
                    long etaSeconds = bytesPerSec > 0 ? remaining / bytesPerSec : 0;

                    speedLabel.setText(String.format("%s/s - %s remaining",
                            formatBytes(bytesPerSec),
                            formatTime(etaSeconds)));
                }
            } else {
                progressLabel.setText(formatBytes(downloaded) + " downloaded");
            }
        });
    }

    /**
     * Set error message
     */
    public void setError(String error) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(error);
            statusLabel.setForeground(new Color(255, 100, 100));
        });
    }

    /**
     * Format bytes to human readable string
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * Format seconds to human readable time
     */
    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return String.format("%dm %ds", seconds / 60, seconds % 60);
        } else {
            return String.format("%dh %dm", seconds / 3600, (seconds % 3600) / 60);
        }
    }
}
