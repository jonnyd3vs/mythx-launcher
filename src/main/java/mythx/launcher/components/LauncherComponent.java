package mythx.launcher.components;

import mythx.launcher.components.impl.*;

/**
 * Components that need to be altered in any sort of way by another component
 * @author Jonny
 */
public enum LauncherComponent {

    BACKGROUND(new Background()),

    PLAY_BUTTON(new PlayNowButton()),

    VIDEO_BLOCK_HEADER_IMG(new HeaderBlockImage(0)),
    ANNOUNCMENTS_BLOCK_HEADER_IMG(new HeaderBlockImage(1)),
    VIDEO_BLOCK_HEADER_TEXT(new HeaderBlockText(0, "FEATURED VIDEO")),
    ANNOUNCMENTS_BLOCK_HEADER_TEXT(new HeaderBlockText(1, "ANNOUNCEMENTS")),

    OPEN_YOUTUBE_VIDEO(new OpenYoutubeVideo()),

    VIDEO_IMAGE(new YouTubeVideo()),

    LOADING_BAR(new LoadingBar()),

    LAUNCH_MESSAGE(new LaunchMessage()),
    PERCENTAGE_COMPLETE(new PercentageComplete()),


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
