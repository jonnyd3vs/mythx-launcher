package mythx.launcher.components.impl.nav;

import mythx.launcher.components.CreativeComponent;
import mythx.launcher.utility.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jonny
 */
public class NavSocial extends CreativeComponent {

    public NavSocial(int xPos, String url, String name) {

        setIcon(null);

        setIcon(new ImageIcon(getClass().getResource("/nav/nav-"+name+".png")));

        setBounds(new Rectangle(xPos, 17, this.getIcon().getIconWidth(), 32));

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                Utilities.openUrl(url);
            }

            public void mouseEntered(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/nav/nav-"+name+"-hover.png")));
            }

            public void mouseExited(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/nav/nav-"+name+".png")));
            }
        });

    }
}
