package mythx.launcher.components.impl;

import mythx.launcher.Launch;
import mythx.launcher.LauncherSettings;
import mythx.launcher.components.CreativeComponent;
import mythx.launcher.web.controller.GeneralProjectController;
import mythx.launcher.web.service.GeneralProjectService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jonny
 */
public class PlayNowButton extends CreativeComponent {
    private String serverName;
    private final GeneralProjectService generalProjectService = new GeneralProjectService(new GeneralProjectController());

    public PlayNowButton() {
        serverName = "MythX";

        setIcon(new ImageIcon(getClass().getResource("/Play-ACTIVE.png")));
        setBounds(getRectangle());

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                String downloadUrl = generalProjectService.getDownLoadUrl();
                launch("https://storage.googleapis.com/legionent/mythx_client.jar", serverName.toLowerCase());
            }

            public void mouseEntered(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/Play-ACTIVE-HOVER.png")));
            }


            public void mouseExited(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/Play-ACTIVE.png")));
            }
        });
    }

    public Rectangle getRectangle() {
        return new Rectangle(615, 403, 248, 64);
    }

    public void launch(String downloadUrl, String serverName) {
        if(Launch.getDownload() == null) {
            // if(UpdateCheck.updateExists() != 0) {

            if(LauncherSettings.BETA_MODE) {
                downloadUrl = downloadUrl.replaceAll("legionent", "rsps-beta");

                System.out.println("Download url : "+downloadUrl);
            }

            Launch.resetDownload(downloadUrl, serverName+".dat").start();
            /*} else {
                Utilities.launchClient();
            }*/
        }
    }


    public void setServerName(String serverName) {
        this.serverName = serverName;
    } // right now does not work correctly in PlayNowButton will execute reset value
}
