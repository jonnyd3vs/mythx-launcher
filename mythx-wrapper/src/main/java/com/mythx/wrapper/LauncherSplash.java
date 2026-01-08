package com.mythx.wrapper;

import javax.swing.*;
import java.awt.*;

public class LauncherSplash extends JFrame {

    private JProgressBar progressBar;
    private JLabel progressLabel;

    public LauncherSplash() {
    }

    public void start() {
        Dimension size = new Dimension(500, 120);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMaximumSize(size);
        setMinimumSize(size);
        setUndecorated(true);
        setPreferredSize(size);
        setResizable(false);
        getContentPane().setLayout(null);

        // Set the background color to match launcher
        getContentPane().setBackground(new Color(43, 48, 56));

        // Progress label
        progressLabel = new JLabel("Downloading Image Assets...");
        progressLabel.setForeground(Color.WHITE);
        progressLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        progressLabel.setBounds(0, 20, size.width, 30);
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setBounds(50, 60, 400, 30);
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setForeground(new Color(34, 139, 34));  // Forest green
        progressBar.setBackground(new Color(32, 34, 37));

        getContentPane().add(progressLabel);
        getContentPane().add(progressBar);

        // Center on screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        pack();
        setVisible(true);
    }

    public void setProgress(int progress) {
        progressBar.setValue(progress);
    }

    public void setProgressText(String text) {
        progressLabel.setText(text);
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public JLabel getProgressLabel() {
        return progressLabel;
    }
}
