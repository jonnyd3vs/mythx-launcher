package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;
import mythx.launcher.fonts.LauncherFont;

import java.awt.*;

/**
 * @author Jonny
 */
public class PercentageComplete extends CreativeComponent { ;

    public PercentageComplete() {
        setForeground(new Color(155, 155, 155));
        setFont(LauncherFont.ROBOTO_BOLD_11.getFont());
    }

    @Override
    public int getX() {
        return 470;
    }

    @Override
    public int getY() {
        return 545;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public int getWidth() {
        return getFontMetrics(getFont()).stringWidth(getText());
    }

}
