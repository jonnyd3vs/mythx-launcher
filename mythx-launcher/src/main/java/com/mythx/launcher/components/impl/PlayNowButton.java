package com.mythx.launcher.components.impl;

import com.mythx.launcher.Launch;
import com.mythx.launcher.LauncherSettings;
import com.mythx.launcher.components.CreativeComponent;
import com.mythx.launcher.service.ManifestService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jonny
 */
public class PlayNowButton extends CreativeComponent {
    private String serverName;
    private final ManifestService manifestService = new ManifestService();

    public PlayNowButton() {
        serverName = "MythX";

        setIcon(new ImageIcon(getClass().getResource("/Play-ACTIVE.png")));
        setBounds(getRectangle());

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                // Fetch download URL from manifest.json
                if (manifestService.fetchManifest()) {
                    String downloadUrl = manifestService.getClientUrl();
                    String filename = manifestService.getClientFilename();
                    System.out.println("Manifest version: " + manifestService.getVersion());
                    System.out.println("Downloading from: " + downloadUrl);
                    launch(downloadUrl, filename != null ? filename : serverName.toLowerCase() + ".jar");
                } else {
                    // Fallback to default URL if manifest fetch fails
                    System.out.println("Manifest fetch failed, using fallback URL");
                    launch(LauncherSettings.getClientDownloadUrl(), serverName.toLowerCase() + ".jar");
                }
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

    public void launch(String downloadUrl, String filename) {
        if(Launch.getDownload() == null) {
            System.out.println("Starting download: " + downloadUrl);
            Launch.resetDownload(downloadUrl, filename).start();
        }
    }


    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
}
