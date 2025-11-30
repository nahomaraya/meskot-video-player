package com.neu.finalproject.meskot.ui;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final VideoPlayerUI parent;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton, guestButton;

    public LoginPanel(VideoPlayerUI parent) {
        this.parent = parent;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(28, 28, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Netflix Wrapper", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        add(userLabel, gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 2;
        add(passLabel, gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        add(passwordField, gbc);

        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 122, 255));
        loginButton.setForeground(Color.RED);
        loginButton.setFocusPainted(false);
        gbc.gridx = 0;
        add(loginButton, gbc);

        registerButton = new JButton("Register New Account");
        registerButton.setBackground(new Color(52, 199, 89));
        registerButton.setForeground(Color.RED);
        registerButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(registerButton, gbc);

        guestButton = new JButton("Continue as Guest");
        guestButton.setBackground(new Color(142, 142, 147));
        guestButton.setForeground(Color.RED);
        guestButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(guestButton, gbc);

        setupEventListeners();
    }

    private void setupEventListeners() {
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> parent.showRegistrationPanel());
        guestButton.addActionListener(e -> continueAsGuest());
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password");
            return;
        }
        boolean success = parent.performLogin(username, password);
        if (!success) {
            JOptionPane.showMessageDialog(this, "Invalid credentials");
        }
    }

    private void continueAsGuest() {
        boolean success = parent.performLogin("guest", "guest");
        if (!success) {
            JOptionPane.showMessageDialog(this, "Guest access unavailable");
        }
    }
}