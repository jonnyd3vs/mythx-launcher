package com.mythx.launcher.components.impl;

import com.mythx.launcher.web.error.ErrorController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jonny
 */
public class CloseButton extends JLabel {

    public CloseButton() {
        setIcon(new ImageIcon(getClass().getResource("/close.png")));

        setBounds(getRectangle());

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                System.out.println("Sends errors...");
                ErrorController.sendError("testUsername"); // sends file with errors to API
                System.exit(0);
            }

            public void mouseEntered(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/close_hover.png")));
            }

            public void mouseExited(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/close.png")));
            }
        });
    }

    public Rectangle getRectangle() {
        return new Rectangle(880, 7, 12, 13);
    }

}
