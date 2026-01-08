package com.mythx.wrapper.component;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;


public class TextComp extends CreativeComponent {
    public TextComp() {

        setText("<html><span style='color:white;'>Something went wrong. Please try again later or contact </span><a href=''>support</a></html>");
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // Change cursor to hand pointer
        setBounds(820/2, 520, 700, 14);
        setVisible(false);
        // Add a mouse listener to handle the click event
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    // todo change link
                    // Open the link in the default browser
                    Desktop.getDesktop().browse(new URI("https://mythxrsps.com/contact-us"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


    }
}
