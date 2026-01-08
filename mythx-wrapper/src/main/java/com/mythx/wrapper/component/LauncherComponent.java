package com.mythx.wrapper.component;

/**
 * Components that need to be altered in any sort of way by another component
 * @author Jonny
 */
public enum LauncherComponent {
    CLOSE_BUTTON(new CloseButton()),
    LOADING_BAR(new LoadingBar()),
    LOADING_BAR_BACKGROUND(new LoadingBarBackground()),
    SPLASH_BACKGROUND(new SplashBackground()),
    ERROR_MESSAGE(new TextComp())




    ;

    private CreativeComponent component;

    LauncherComponent(CreativeComponent component) {
        this.component = component;
    }

    public CreativeComponent getComponent() {
        return component;
    }

    public void setComponent(CreativeComponent component) {
        this.component = component;
    }
}
