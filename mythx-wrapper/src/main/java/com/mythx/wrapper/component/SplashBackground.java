package com.mythx.wrapper.component;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jonny
 */
public class SplashBackground extends CreativeComponent {

    public SplashBackground() {
        setIcon(new ImageIcon(getClass().getResource(ComponentPath.SPLASH.path)));

        setBounds(getRectangle());
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);
        setVisible(false);

    }

    public Rectangle getRectangle() {
        return new Rectangle(0, 0, 1170, 660);
    }
}
