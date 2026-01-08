package com.mythx.launcher.components.impl;

import com.mythx.launcher.components.CreativeComponent;

import java.awt.*;

/**
 * @author Jonny
 */
public class Background extends CreativeComponent {

    public Background() {
        setBounds(getRectangle());
    }

    public Rectangle getRectangle() {
        return new Rectangle(0, 0, 900, 508);
    }
}
