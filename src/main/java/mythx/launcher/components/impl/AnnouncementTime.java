package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;
import mythx.launcher.fonts.LauncherFont;

import java.awt.*;

/**
 * @author Jonny
 */
public class AnnouncementTime extends CreativeComponent {

    int yPos = 0;

    public AnnouncementTime(int yPos) {
        this.yPos = yPos;

        setForeground(new Color(39, 188, 187));
        setFont(LauncherFont.ROBOTO_BOLD_11.getFont());
    }

    public void setText(String text) {
        super.setText(text);

        if(text == null || text.isEmpty())
            return;

        setBounds(new Rectangle( 450, 154 + yPos, 190, 25));
    }


}
