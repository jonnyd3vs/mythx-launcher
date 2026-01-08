package com.mythx.launcher.web.server;

import java.awt.*;
import java.util.ArrayList;

public class Server {

    private final ArrayList<Component> components = new ArrayList<>();

    private final String serverName;
    private final String background;
    private final String playersOnline;
    private final String discordLink;
    private final String clientLink;
    private final String playColor;
    private final String playColorHover;
    private final String description;
    private int newLauncher;
    private int serverStatus;

    public Server(String serverName, String background, String playersOnline, String discordLink, String clientLink, String playColor, String playColorHover, String description, int newLauncher, int serverStatus) {
        this.serverName = serverName;
        this.background = background;
        this.playersOnline = playersOnline;
        this.discordLink = discordLink;
        this.clientLink = clientLink;
        this.playColor = playColor;
        this.playColorHover = playColorHover;
        this.description = description;
        this.newLauncher = newLauncher;
        this.serverStatus = serverStatus;
    }

    public String getServerName() {
        return serverName;
    }

    public String getBackground() {
        return background;
    }

    public String getPlayersOnline() {
        return playersOnline;
    }

    public String getDiscordLink() {
        return discordLink;
    }

    public String getClientLink() {
        return clientLink;
    }

    public String getPlayColor() {
        return playColor;
    }

    public String getPlayColorHover() {
        return playColorHover;
    }

    public int getServerStatus() {
        return serverStatus;
    }

    public ArrayList<Component> getComponents() {
        return components;
    }

    public int getNewLauncher() {
        return newLauncher;
    }

    public String getDescription() {
        return description;
    }
}
