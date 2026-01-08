package com.mythx.wrapper.component;

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
        return new Rectangle(220, 520, 1, 50);
    }

    @Override
    public void load(int percent) {
        if(percent == 0) {
            setVisible(false);
            repaint();
            return;
        }
        ImageIcon imageIcon = new ImageIcon(getClass().getResource(ComponentPath.PROGRESSBAR_FULL.path));
        setIcon(imageIcon);

        setSize(getPercent(percent, 540), getHeight());

        setVisible(true);

        repaint();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }

    public static int getPercent(int current, int pixels) {
        return (int) ((pixels) * .01 * current);
    }

}
