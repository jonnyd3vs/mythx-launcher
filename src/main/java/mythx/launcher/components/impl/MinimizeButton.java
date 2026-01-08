package mythx.launcher.components.impl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Jonny
 */
public class MinimizeButton extends JLabel {

    JFrame launcherFrame;

    public MinimizeButton(JFrame launcherFrame) {
        this.launcherFrame = launcherFrame;

        setIcon(new ImageIcon(getClass().getResource("/minimize.png")));

        setBounds(getRectangle());

        addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent evt) {
                launcherFrame.setState(JFrame.ICONIFIED); //minimized
            }

            public void mouseEntered(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/minimize_hover.png")));
            }

            public void mouseExited(MouseEvent evt) {
                setIcon(new ImageIcon(getClass().getResource("/minimize.png")));
            }
        });
    }

    public Rectangle getRectangle() {
        return new Rectangle(858, 11, 15, 4);
    }

}
