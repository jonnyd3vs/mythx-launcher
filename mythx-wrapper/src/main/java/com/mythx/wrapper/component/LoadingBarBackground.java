package com.mythx.wrapper.component;


import javax.swing.*;
import java.awt.*;

/**
 * @author Jonny
 */
public class LoadingBarBackground extends CreativeComponent {

    public LoadingBarBackground() {
        setIcon(new ImageIcon(getClass().getResource(ComponentPath.PROGRESSBAR.path)));

        setBounds(getRectangle());
        setVisible(false);
    }

    public Rectangle getRectangle() {
        return new Rectangle(220, 520, 725, 50);
    }
}
