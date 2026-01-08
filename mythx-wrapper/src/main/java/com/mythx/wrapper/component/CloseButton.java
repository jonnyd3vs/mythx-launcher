package com.mythx.wrapper.component;

import com.mythx.wrapper.controller.ErrorController;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jonny
 */
@Slf4j
public class CloseButton extends CreativeComponent {

    public CloseButton() {
        setIcon(new ImageIcon(getClass().getResource(ComponentPath.CLOSE.path)));

        setVisible(false);
        setBounds(getRectangle());

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                log.info("Sends errors...");
                // todo uncomment after developing
                ErrorController.sendError("testUsername"); // sends file with errors to API
                System.exit(0);
            }

            public void mouseEntered(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource(ComponentPath.CLOSE_HOVER.path)));
            }

            public void mouseExited(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource(ComponentPath.CLOSE.path)));
            }
        });
    }

    public Rectangle getRectangle() {
        return new Rectangle(970, 82, 14, 14);
    }

}
