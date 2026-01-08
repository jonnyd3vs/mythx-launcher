package mythx.launcher;

import mythx.launcher.components.impl.*;

import javax.swing.*;
import java.awt.*;

public class LauncherSplash extends JFrame {

    public LauncherSplash() {

    }

    public void start() {

        Dimension size = new Dimension(800, 500);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setMaximumSize(size);
        setMinimumSize(size);

        setUndecorated(true);

        setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));

        setPreferredSize(size);

        setResizable(false);
        getContentPane().setLayout(null);

        setIconImage(new ImageIcon(this.getClass().getResource(
                "/nav/nav-logo.png")).getImage());

        getContentPane().add(new SplashBackground());

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        pack();

        setVisible(true);



    }




}
