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

        // Set the background color - dark purple theme
        getContentPane().setBackground(new Color(10, 10, 20));

        // Progress label
        progressLabel = new JLabel("Checking for updates...");
        progressLabel.setForeground(new Color(200, 180, 255));  // Light purple text
        progressLabel.setFont(new Font("Verdana", Font.BOLD, 18));
        progressLabel.setBounds(0, 20, size.width, 30);
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Progress bar - purple theme
        progressBar = new JProgressBar();
        progressBar.setBounds(50, 60, 400, 30);
        progressBar.setBorderPainted(false);
        progressBar.setStringPainted(false);
        progressBar.setForeground(new Color(139, 92, 246));  // Bright purple #8B5CF6
        progressBar.setBackground(new Color(26, 26, 46));    // Dark purple background

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
