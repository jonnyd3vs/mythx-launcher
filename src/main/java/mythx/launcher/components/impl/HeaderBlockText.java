package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;
import mythx.launcher.fonts.LauncherFont;

import javax.swing.*;
import java.awt.*;

/**
 * @author Jonny
 */
public class HeaderBlockText extends CreativeComponent {

    public HeaderBlockText(int index, String text) {
        setText("<html><center>"+text+"</center></html>");

        setFont(LauncherFont.POPPINS_REGULAR_14.getFont());

        setHorizontalAlignment(SwingConstants.CENTER);
        setBounds(new Rectangle(28 + (index * 337), 345, 306, 33));
    }


}
