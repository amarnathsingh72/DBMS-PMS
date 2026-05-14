package com.placementpro;

import com.placementpro.ui.LoginPanel;
import com.placementpro.utils.ThemeConstants;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        // Run UI in the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            JFrame frame = new JFrame("PlacementPro - Login");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(500, 400);
            frame.setLocationRelativeTo(null); // Center on screen
            frame.getContentPane().setBackground(ThemeConstants.COLOR_BG);

            // Add the LoginPanel
            LoginPanel loginPanel = new LoginPanel(frame);
            frame.add(loginPanel);

            frame.setVisible(true);
        });
    }
}
