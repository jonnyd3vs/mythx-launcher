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
public class OpenYoutubeVideo extends CreativeComponent {

    public String videoId;

    public void setVideoLink(String videoId) {
        this.videoId = videoId;
    }

    public OpenYoutubeVideo() {
       /* setIcon(new ImageIcon(getClass().getResource("/images/open-on-youtube-btn.png")));

        setBounds(getRectangle());

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                Utilities.openUrl("https://www.youtube.com/watch?v="+ videoId);
            }

            public void mouseEntered(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/images/open-on-youtube-btn-hover.png")));
            }

            public void mouseExited(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/images/open-on-youtube-btn.png")));
            }
        });*/
    }

    public Rectangle getRectangle() {
        return new Rectangle(28, 550, 306, 32);
    }

}
