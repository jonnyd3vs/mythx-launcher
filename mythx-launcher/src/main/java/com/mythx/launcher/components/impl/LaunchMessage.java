package com.mythx.launcher.components.impl;

import com.mythx.launcher.components.CreativeComponent;
import com.mythx.launcher.fonts.LauncherFont;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jonny
 */
public class LaunchMessage extends CreativeComponent { ;

    public LaunchMessage() {
        setText("<html>Welcome to <font color =#00FFFF>MythX</font>, click <font color=#00FFFF>'Launch'</font> to play!");
        setForeground(new Color(255, 255, 255));
        setFont(LauncherFont.ROBOTO_BOLD_11.getFont());
        setHorizontalAlignment(SwingConstants.LEFT);
    }

    @Override
    public int getX() {
        return 225;
    }

    @Override
    public int getY() {
        return 445;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public int getWidth() {
        return 250;
    }

}
