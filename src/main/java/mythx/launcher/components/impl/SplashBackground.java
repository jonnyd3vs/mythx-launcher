package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jonny
 */
public class SplashBackground extends CreativeComponent {

    public SplashBackground() {
        setIcon(new ImageIcon(getClass().getResource("/splash.png")));

        setBounds(getRectangle());
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
    }

    public Rectangle getRectangle() {
        return new Rectangle(0, 0, 800, 500);
    }
}
