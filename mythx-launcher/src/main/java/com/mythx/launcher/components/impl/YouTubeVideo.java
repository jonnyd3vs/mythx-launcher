package com.mythx.launcher.components.impl;

import com.mythx.launcher.Launch;
import com.mythx.launcher.components.CreativeComponent;
import com.mythx.launcher.utility.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;

/**
 * @author Jonny
 */
public class YouTubeVideo extends CreativeComponent {
    private final static Logger LOGGER = LoggerFactory.getLogger(YouTubeVideo.class);

    private String videoId;

    public YouTubeVideo() {

        // create a line border with the specified color and width
        Border border = BorderFactory.createLineBorder(Color.WHITE, 0);

        // set the border of this component
        setBorder(border);

        setBounds(getRectangle());

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                Utilities.openUrl("https://www.youtube.com/watch?v="+ videoId);
            }
        });

    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
        try {

            URL url = new URL("https://i.ytimg.com/vi/"+ videoId +"/hqdefault.jpg");
            Image image = ImageIO.read(url);

            if (image != null) {
                Image scaledImage = image.getScaledInstance(335, 190, Image.SCALE_DEFAULT);
                setIcon(new ImageIcon(scaledImage));
            } else {
                LOGGER.warn("Failed to load image from URL.");
            }
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
        }
    }

    public Rectangle getRectangle() {
        return new Rectangle(60, 155, 335, 190);
    }

    public void launch() {
        if(Launch.getDownload() == null) {
            // if(UpdateCheck.updateExists() != 0) {
            //Launch.resetDownload(ServerGrabber.SERVERS.get(0).getClientLink(), ServerGrabber.SERVERS.get(0).getServerName()).start();
            /*} else {
                Utilities.launchClient();
            }*/
        }
    }
}
