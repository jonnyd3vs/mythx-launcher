package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jonny
 */
public class AnnouncementBar extends CreativeComponent {

    public AnnouncementBar(int yPos, int index) {

        boolean whichBox = index % 2 == 0;
        setIcon(new ImageIcon(getClass().getResource(whichBox ? "/update-box.png" : "/update-box.png")));
        setBounds(new Rectangle(440, 138 + yPos, 420, 40));
    }


}
