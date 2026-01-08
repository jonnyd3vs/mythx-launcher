package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;
import mythx.launcher.fonts.LauncherFont;

import java.awt.*;

/**
 * @author Jonny
 */
public class AnnouncementTitle extends CreativeComponent {

    int yPos = 0;

    public AnnouncementTitle(int yPos) {
        this.yPos = yPos;

        setForeground(new Color(233, 233, 233));
        setFont(LauncherFont.ROBOTO_REGULAR_12.getFont());
    }

    public void setText(String text) {
        super.setText(text);

        if(text == null || text.isEmpty())
            return;

        setBounds(new Rectangle( 450, 138 + yPos, 300, 25));
    }


}
