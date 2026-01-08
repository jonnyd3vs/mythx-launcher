package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;
import mythx.launcher.utility.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jonny
 */
public class AnnouncementOpen extends CreativeComponent {

    String openUrl;

    public AnnouncementOpen(int yPos, String openUrl) {
        setIcon(new ImageIcon(getClass().getResource("/read-post-button.png")));
        setBounds(new Rectangle(757, 145 + yPos, 98, 25));

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                Utilities.openUrl(openUrl);
            }

            public void mouseEntered(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/read-post-button-hover.png")));
            }

            public void mouseExited(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/read-post-button.png")));
            }
        });

    }

    public void setOpenUrl(String openUrl) {
        this.openUrl = openUrl;
    }


}
