package com.placementpro.ui;

import com.placementpro.dao.AuthDAO;
import com.placementpro.utils.Session;
import com.placementpro.utils.ThemeConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPanel extends JPanel {
    private JFrame parentFrame;
    private JLabel errorLabel;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private String currentRole = "STUDENT";
    private JPanel tabHeader;

    public LoginPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(cardLayout);
        
        setLayout(new BorderLayout());
        setBackground(ThemeConstants.COLOR_BG);
        setBorder(new EmptyBorder(30, 40, 30, 40));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ThemeConstants.COLOR_BG);
        
        JLabel logoLabel = new JLabel("🎓", JLabel.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 40));
        
        JLabel titleLabel = new JLabel("PlacementPro", JLabel.CENTER);
        titleLabel.setFont(ThemeConstants.FONT_TITLE);
        titleLabel.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        
        headerPanel.add(logoLabel, BorderLayout.NORTH);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Custom Tab Header
        tabHeader = new JPanel(new GridLayout(1, 3, 10, 0));
        tabHeader.setBackground(ThemeConstants.COLOR_BG);
        tabHeader.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        tabHeader.add(createTabButton("Student", "STUDENT"));
        tabHeader.add(createTabButton("Faculty", "FACULTY"));
        tabHeader.add(createTabButton("Company", "COMPANY"));
        
        // Form Cards
        cardPanel.setBackground(ThemeConstants.COLOR_SURFACE);
        cardPanel.add(createLoginForm("USN", "STUDENT"), "STUDENT");
        cardPanel.add(createLoginForm("Faculty ID", "FACULTY"), "FACULTY");
        cardPanel.add(createLoginForm("Company ID", "COMPANY"), "COMPANY");
        
        // Container for Tabs + Forms
        JPanel contentContainer = new JPanel(new BorderLayout());
        contentContainer.setBackground(ThemeConstants.COLOR_BG);
        contentContainer.add(tabHeader, BorderLayout.NORTH);
        contentContainer.add(cardPanel, BorderLayout.CENTER);
        
        add(contentContainer, BorderLayout.CENTER);

        // Error Label
        errorLabel = new JLabel(" ", JLabel.CENTER);
        errorLabel.setForeground(ThemeConstants.STATUS_REJECTED);
        errorLabel.setFont(ThemeConstants.FONT_SMALL);
        add(errorLabel, BorderLayout.SOUTH);
        
        updateTabStyles();
    }

    private JButton createTabButton(String text, String role) {
        JButton btn = new JButton(text);
        btn.setFont(ThemeConstants.FONT_HEADING);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            currentRole = role;
            cardLayout.show(cardPanel, role);
            updateTabStyles();
        });
        
        return btn;
    }

    private void updateTabStyles() {
        for (Component c : tabHeader.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                if (b.getText().toUpperCase().startsWith(currentRole.substring(0,3))) {
                    b.setForeground(ThemeConstants.COLOR_ACCENT);
                    b.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, ThemeConstants.COLOR_ACCENT));
                } else {
                    b.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
                    b.setBorder(null);
                }
            }
        }
    }

    private JPanel createLoginForm(String idLabelText, String role) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(ThemeConstants.COLOR_SURFACE);
        panel.setBorder(BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER, 1));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        JLabel idLabel = new JLabel(idLabelText);
        idLabel.setFont(ThemeConstants.FONT_SMALL);
        idLabel.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
        gbc.gridy = 0;
        panel.add(idLabel, gbc);

        JTextField idField = new JTextField();
        styleTextField(idField);
        gbc.gridy = 1;
        panel.add(idField, gbc);

        JLabel pwdLabel = new JLabel("Password");
        pwdLabel.setFont(ThemeConstants.FONT_SMALL);
        pwdLabel.setForeground(ThemeConstants.COLOR_TEXT_SECONDARY);
        gbc.gridy = 2;
        panel.add(pwdLabel, gbc);

        JPasswordField pwdField = new JPasswordField();
        styleTextField(pwdField);
        gbc.gridy = 3;
        panel.add(pwdField, gbc);

        JButton loginBtn = new JButton("Sign In");
        loginBtn.setBackground(ThemeConstants.COLOR_ACCENT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(ThemeConstants.FONT_BUTTON);
        loginBtn.setPreferredSize(new Dimension(0, 40));
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 20, 10, 20);
        panel.add(loginBtn, gbc);

        loginBtn.addActionListener(e -> {
            String id = idField.getText().trim();
            String pwd = new String(pwdField.getPassword());
            handleLogin(id, pwd, role);
        });

        return panel;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(ThemeConstants.COLOR_BG);
        field.setForeground(ThemeConstants.COLOR_TEXT_PRIMARY);
        field.setCaretColor(Color.WHITE);
        field.setFont(ThemeConstants.FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeConstants.COLOR_BORDER, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        field.setPreferredSize(new Dimension(250, 35));
    }

    private void handleLogin(String id, String pwd, String role) {
        errorLabel.setText("Authenticating...");
        
        // Run in background to not freeze UI
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                if (role.equals("STUDENT")) return AuthDAO.validateStudent(id, pwd);
                if (role.equals("FACULTY")) return AuthDAO.validateFaculty(id, pwd);
                if (role.equals("COMPANY")) {
                    try {
                        return AuthDAO.validateCompany(Integer.parseInt(id), pwd);
                    } catch (Exception e) { return false; }
                }
                return false;
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        Session.login(id, role);
                        parentFrame.dispose();
                        if (role.equals("STUDENT")) new StudentDashboard().setVisible(true);
                        else if (role.equals("FACULTY")) new FacultyDashboard().setVisible(true);
                        else new CompanyDashboard().setVisible(true);
                    } else {
                        errorLabel.setText("Invalid " + role.toLowerCase() + " credentials.");
                    }
                } catch (Exception e) {
                    errorLabel.setText("System error: " + e.getMessage());
                }
            }
        }.execute();
    }
}
