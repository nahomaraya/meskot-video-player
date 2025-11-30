package com.neu.finalproject.meskot.ui;

import javax.swing.*;
import java.awt.*;

public class RegistrationPanel extends JPanel {
    private final VideoPlayerUI parent;
    private JTextField usernameField, emailField;
    private JPasswordField passwordField, confirmPasswordField;
    private JButton registerButton, backButton;

    public RegistrationPanel(VideoPlayerUI parent) {
        this.parent = parent;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(28, 28, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titleLabel, gbc);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        add(userLabel, gbc);
        usernameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        add(usernameField, gbc);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 2;
        add(emailLabel, gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 2;
        add(emailField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 3;
        add(passLabel, gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 3;
        add(passwordField, gbc);

        JLabel confirmPassLabel = new JLabel("Confirm Password:");
        confirmPassLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 4;
        add(confirmPassLabel, gbc);
        confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 4;
        add(confirmPasswordField, gbc);

        registerButton = new JButton("Register");
        registerButton.setBackground(new Color(52, 199, 89));
        registerButton.setForeground(Color.GREEN);
        registerButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(registerButton, gbc);

        backButton = new JButton("Back to Login");
        backButton.setBackground(new Color(142, 142, 147));
        backButton.setForeground(Color.RED);
        backButton.setFocusPainted(false);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        add(backButton, gbc);

        setupEventListeners();
    }

    private void setupEventListeners() {
        registerButton.addActionListener(e -> attemptRegistration());
        backButton.addActionListener(e -> parent.showLoginPanel());
    }

    private void attemptRegistration() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return;
        }
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }
        boolean success = parent.performRegistration(username, email, password);
        if (success) {
            JOptionPane.showMessageDialog(this, "Registration successful. Please log in.");
            parent.showLoginPanel();
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Try a different username/email.");
        }
    }
}