package com.mementis.mementis;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class HelloWorldSwing {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Hello World Swing");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.add(new JLabel("Hello, World!"));
            frame.pack();
            frame.setLocationRelativeTo(null); // center the window
            frame.setVisible(true);
        });
    }
}
