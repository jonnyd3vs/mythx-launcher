package com.mythx.wrapper.component;

public enum ComponentPath {
    FAVICON("/images/favicon.png"),
    SPLASH("/images/splash.png"),
    PROGRESSBAR("/images/progress_bar.png"),
    PROGRESSBAR_FULL("/images/progress_bar_full.png"),
    CLOSE("/images/close.png"),
    CLOSE_HOVER("/images/close_hover.png"),


    ;

    public String path;

    ComponentPath(String path) {
        this.path = path;
    }
}
