package com.mythx.launcher.components.impl;

import com.mythx.launcher.Launch;
import com.mythx.launcher.LauncherSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jonny
 */
public class TopBarDragging extends JLabel {

    JFrame launcherFrame;

    private int mousePositionX;
    private int mousePositionY;

    public TopBarDragging(JFrame launcherFrame) {
        this.launcherFrame = launcherFrame;

        setBounds(getRectangle());

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                dragMouseDragged(evt);
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                dragMousePressed(evt);
            }
        });
    }

    public Rectangle getRectangle() {
        return new Rectangle(0, 0, 900, 101);
    }

    private void dragMouseDragged(MouseEvent evt) {
        int mouseX = evt.getXOnScreen();
        int mouseY = evt.getYOnScreen();

        launcherFrame.setLocation(mouseX - mousePositionX, mouseY - mousePositionY);
    }

    private void dragMousePressed(java.awt.event.MouseEvent evt) {
        mousePositionX = evt.getX();
        mousePositionY = evt.getY();

        if(LauncherSettings.DEVELOPER_MODE) {
            System.out.println("mouseX="+mousePositionX+",mouseY="+mousePositionY);
        }
    }
}
