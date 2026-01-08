package mythx.launcher.components.impl;

import mythx.launcher.components.CreativeComponent;

import java.awt.*;

/**
 * @author Jonny
 */
public class HeaderBlockImage extends CreativeComponent {

    public HeaderBlockImage(int index) {
        setBounds(new Rectangle(28 + (index * 337), 345, 306, 33));
    }


}
