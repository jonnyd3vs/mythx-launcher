package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jonny
 */
public class LoadingBarBackground extends CreativeComponent {

    public LoadingBarBackground() {
        setIcon(new ImageIcon(getClass().getResource("/progress-bar-bg.png")));

        setBounds(getRectangle());
    }

    public Rectangle getRectangle() {
        return new Rectangle(225, 413, 366, 26);
    }
}
