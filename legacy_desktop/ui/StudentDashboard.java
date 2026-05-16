package com.placementpro.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import com.placementpro.utils.ThemeConstants;
import java.awt.BorderLayout;

public class StudentDashboard extends JFrame {
    public StudentDashboard() {
        setTitle("PlacementPro | Student");
        setSize(1024, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(ThemeConstants.COLOR_BG);
        
        JLabel lbl = new JLabel("Student Dashboard", JLabel.CENTER);
        lbl.setFont(ThemeConstants.FONT_TITLE);
        lbl.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        add(lbl, BorderLayout.CENTER);
    }
}
