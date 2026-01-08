package mythx.launcher;

import mythx.launcher.components.LauncherComponent;
import mythx.launcher.components.impl.*;
import mythx.launcher.components.impl.nav.NavButton;
import mythx.launcher.components.impl.nav.NavSocial;
import mythx.launcher.web.update.Update;
import mythx.launcher.web.update.UpdateGrabber;
import mythx.launcher.web.video.VideoGrabber;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Optional;

public class LauncherFrame extends JFrame {

    public static int mousePositionX;
    public static int mousePositionY;

    public LauncherFrame() {

    }

    public ArrayList<Component> announcementComponents = new ArrayList<>();

    public void start() {

        Dimension size = new Dimension(900, 508);

        setMaximumSize(size);
        setMinimumSize(size);

        setUndecorated(true);

        setBackground(new Color(1.0f, 1.0f, 1.0f, 0.5f));

        setPreferredSize(size);

        setResizable(false);
        getContentPane().setLayout(null);

        setIconImage(new ImageIcon(this.getClass().getResource(
                "/nav/nav-logo.png")).getImage());

        /**
         * All custom components for the launcher
         */
        getContentPane().add(new MinimizeButton(this));
        getContentPane().add(new CloseButton());

        /**
         * Default components
         */


        int i = 0;
        if (UpdateGrabber.updateRequest != null) {
            for (Update update : UpdateGrabber.updateRequest.getData()) {
                AnnouncementOpen announcementOpen = new AnnouncementOpen(40 * i, "https://mythxrsps.com/updates/" + update.getId());
                announcementComponents.add(announcementOpen);
                getContentPane().add(announcementOpen);

                AnnouncementTime announcementTime = new AnnouncementTime(40 * i);
                announcementTime.setText(update.getDate());
                announcementComponents.add(announcementTime);
                getContentPane().add(announcementTime);

                AnnouncementTitle announcementTitle = new AnnouncementTitle(40 * i);
                announcementTitle.setText(update.getTitle());
                announcementComponents.add(announcementTitle);
                getContentPane().add(announcementTitle);

                AnnouncementBar announcementBar = new AnnouncementBar(40 * i, i);
                announcementComponents.add(announcementBar);
                getContentPane().add(announcementBar);
                i++;
            }
    }

          getContentPane().add(LauncherComponent.PERCENTAGE_COMPLETE.getComponent());
          getContentPane().add(LauncherComponent.LAUNCH_MESSAGE.getComponent());

           getContentPane().add(LauncherComponent.LOADING_BAR.getComponent());

        getContentPane().add(new LoadingBarBackground());

        getContentPane().add(LauncherComponent.PLAY_BUTTON.getComponent());

   //     if(VideoGrabber.VIDEOS.containsKey(mainServer.getServerName().toLowerCase())) {
            //LauncherComponent.VIDEO_IMAGE.getComponent().setVideoId(VideoGrabber.VIDEOS.get(mainServer.getServerName().toLowerCase()).getVideoId());


        Optional.ofNullable(VideoGrabber.getVideoId()) // if the link is active, it will be possible to go to the video
                .ifPresent(videoId -> {
            LauncherComponent.VIDEO_IMAGE.getComponent().setVideoId(videoId);
            getContentPane().add(LauncherComponent.VIDEO_IMAGE.getComponent());
        });


        String[][] navTabs = new String[][] {
                {"Website", "https://mythxrsps.com/"},
                {"Vote", "https://mythxrsps.com/vote"},
                {"Store", "https://mythxrsps.com/store"},
                {"Updates", "https://mythxrsps.com/updates"},
                {"Support", "https://mythxrsps.com/support"},
        };

        int xPadding = 0;

        for(String[] nav : navTabs) {
            NavButton navButton = new NavButton(90 + xPadding, nav[1], nav[0].toLowerCase());

            getContentPane().add(navButton);
            xPadding += navButton.getIcon().getIconWidth() + 8;
        }

        String[][] navSocial = new String[][] {
                {"Discord", "https://mythxrsps.com/discord"},
                {"YouTube", "https://www.youtube.com/@MythXRSPS"},
        };

         xPadding = 0;

        for(String[] nav : navSocial) {
            NavSocial navButton = new NavSocial(750 + xPadding, nav[1], nav[0].toLowerCase());

            getContentPane().add(navButton);
            xPadding += navButton.getIcon().getIconWidth() + 8;
        }

        LauncherComponent.BACKGROUND.getComponent().setIcon(new ImageIcon(this.getClass().getResource(
                "/background.png")));
        getContentPane().add(LauncherComponent.BACKGROUND.getComponent());

        getContentPane().add(new TopBarDragging(this));

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        pack();

        setVisible(true);

        addMouseWheelListener(e -> {

        });

        addKeyListener(new KeyListener() {
                           @Override
                           public void keyTyped(KeyEvent e) {

                           }

                           @Override
                           public void keyPressed(KeyEvent e) {

                               if (e.isControlDown() && e.getKeyChar() != 'a' && e.getKeyCode() == 66) {
                                   LauncherSettings.BETA_MODE = true;
                                   System.out.println("Beta mode activated.");
                               }
                           }

                           @Override
                           public void keyReleased(KeyEvent e) {

                           }
                       });


                addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent evt) {
                        mousePositionX = evt.getX();
                        mousePositionY = evt.getY();


                        // if(LauncherSettings.DEVELOPER_MODE) {
                        System.out.println("mouseX=" + mousePositionX + ",mouseY=" + mousePositionY);
                        //  }
                    }

                });

    }




}
