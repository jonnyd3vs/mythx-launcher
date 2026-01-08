package mythx.launcher.fonts;

import mythx.launcher.LauncherSettings;

import java.awt.*;
import java.io.IOException;

public enum LauncherFont {

    ROBOTO_BOLD_15("roboto_regular.ttf", 15f, Font.TRUETYPE_FONT),
    ROBOTO_REGULAR_13("roboto_regular.ttf", 13f, Font.TRUETYPE_FONT),
    ROBOTO_REGULAR_12("roboto_regular.ttf", 12f, Font.TRUETYPE_FONT),
    ROBOTO_BOLD_13("roboto_bold.ttf", 13f, Font.TRUETYPE_FONT),
    ROBOTO_BOLD_11("roboto_regular.ttf", 11f, Font.TRUETYPE_FONT),
    ROBOTO_BOLD_12("roboto_regular.ttf", 12f, Font.TRUETYPE_FONT),
    ROBOTO_BOLD_10("roboto_regular.ttf", 10f, Font.TRUETYPE_FONT),
    POPPINS_BOLD_20("poppins_bold.ttf", 20f, Font.TRUETYPE_FONT),
    POPPINS_REGULAR_14("poppins_bold.ttf", 14f, Font.TRUETYPE_FONT),

    ;

    LauncherFont(String fileName, float fontSize, int fontStyle) {
        this.fileName = fileName;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
    }

    private java.awt.Font font;
    private String fileName;
    private float fontSize;
    private int fontStyle;

    public static void loadFonts() {
        for(LauncherFont launcherFont : LauncherFont.values()) {
            try {
                launcherFont.setFont(Font.createFont(launcherFont.getFontStyle(), LauncherFont.class.getResourceAsStream(LauncherSettings.FONTS_DIRECTORY + launcherFont.getFileName())).deriveFont(launcherFont.getFontSize()));
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(launcherFont.getFont());
            } catch (IOException | FontFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public java.awt.Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        this.font = font;
    }

    public String getFileName() {
        return fileName;
    }

    public float getFontSize() {
        return fontSize;
    }

    public int getFontStyle() {
        return fontStyle;
    }
}
