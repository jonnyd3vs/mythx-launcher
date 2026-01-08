package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;
import mythx.launcher.utility.Utilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jonny
 */
public class LoadingBar extends CreativeComponent {

    public LoadingBar() {
        setBounds(getRectangle());
        load(0);
    }

    public Rectangle getRectangle() {
        return new Rectangle(225, 415, 1, 22);
    }

    @Override
    public void load(int percent) {
        if(percent == 0) {
            setVisible(false);
            repaint();
            return;
        }
        ImageIcon imageIcon = new ImageIcon(getClass().getResource("/progress-bar-loading-full.png"));
        setIcon(imageIcon);

        setSize(Utilities.getPercent(percent, 362), getHeight());

        setVisible(true);

        repaint();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

}
