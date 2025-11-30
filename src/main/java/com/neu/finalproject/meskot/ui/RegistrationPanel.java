package com.neu.finalproject.meskot.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class RegistrationPanel extends JPanel {
    private final VideoPlayerUI parent;
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton, backToLoginButton;
    private JCheckBox showPasswordCheckbox;
    private JCheckBox termsCheckbox;

    // Theme colors matching VideoPlayerUI
    private static final Color DARK_BG = new Color(15, 15, 20);
    private static final Color CARD_BG = new Color(25, 25, 35);
    private static final Color ACCENT_PURPLE = new Color(147, 51, 234);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 245);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color INPUT_BG = new Color(30, 30, 40);
    private static final Color BORDER_COLOR = new Color(45, 45, 55);
    private static final Color ERROR_RED = new Color(239, 68, 68);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);

    public RegistrationPanel(VideoPlayerUI parent) {
        this.parent = parent;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(DARK_BG);

        // Main card container
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 24, 24));
                g2.setColor(BORDER_COLOR);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 24, 24));
                g2.dispose();
            }
        };
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(35, 50, 35, 50));
        cardPanel.setPreferredSize(new Dimension(450, 650));

        // Logo/Icon
        JPanel logoPanel = createLogoPanel();
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Join Meskot Player today");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        usernameField = new JTextField();
        JPanel usernamePanel = createInputPanel("Username", "👤", usernameField);

        // Email field
        emailField = new JTextField();
        JPanel emailPanel = createInputPanel("Email", "📧", emailField);

        // Password field
        passwordField = new JPasswordField();
        passwordField.setEchoChar('•');
        JPanel passwordPanel = createInputPanel("Password", "🔒", passwordField);

        // Confirm Password field
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setEchoChar('•');
        JPanel confirmPasswordPanel = createInputPanel("Confirm Password", "🔒", confirmPasswordField);

        // Show password checkbox
        showPasswordCheckbox = new JCheckBox("Show passwords");
        showPasswordCheckbox.setFont(new Font("Inter", Font.PLAIN, 12));
        showPasswordCheckbox.setForeground(TEXT_SECONDARY);
        showPasswordCheckbox.setBackground(CARD_BG);
        showPasswordCheckbox.setOpaque(false);
        showPasswordCheckbox.setFocusPainted(false);
        showPasswordCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordCheckbox.addActionListener(e -> {
            char echoChar = showPasswordCheckbox.isSelected() ? (char) 0 : '•';
            passwordField.setEchoChar(echoChar);
            confirmPasswordField.setEchoChar(echoChar);
        });

        // Terms checkbox
        termsCheckbox = new JCheckBox("<html>I agree to the <font color='#9333EA'>Terms of Service</font> and <font color='#9333EA'>Privacy Policy</font></html>");
        termsCheckbox.setFont(new Font("Inter", Font.PLAIN, 12));
        termsCheckbox.setForeground(TEXT_SECONDARY);
        termsCheckbox.setBackground(CARD_BG);
        termsCheckbox.setOpaque(false);
        termsCheckbox.setFocusPainted(false);
        termsCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        termsCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Register button
        registerButton = createPrimaryButton("Create Account");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Back to login link
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        loginPanel.setOpaque(false);

        JLabel hasAccountLabel = new JLabel("Already have an account?");
        hasAccountLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        hasAccountLabel.setForeground(TEXT_SECONDARY);

        backToLoginButton = new JButton("Sign in");
        backToLoginButton.setFont(new Font("Inter", Font.BOLD, 13));
        backToLoginButton.setForeground(ACCENT_PURPLE);
        backToLoginButton.setBackground(null);
        backToLoginButton.setBorder(null);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backToLoginButton.setForeground(ACCENT_BLUE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                backToLoginButton.setForeground(ACCENT_PURPLE);
            }
        });

        loginPanel.add(hasAccountLabel);
        loginPanel.add(backToLoginButton);

        // Add components to card
        cardPanel.add(logoPanel);
        cardPanel.add(Box.createVerticalStrut(15));
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(25));
        cardPanel.add(usernamePanel);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(emailPanel);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(passwordPanel);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(confirmPasswordPanel);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(showPasswordCheckbox);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(termsCheckbox);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(registerButton);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(loginPanel);

        add(cardPanel);

        setupEventListeners();
    }

    private JPanel createLogoPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient circle background
                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_BLUE,
                        getWidth(), getHeight(), ACCENT_PURPLE
                );
                g2.setPaint(gradient);
                g2.fill(new Ellipse2D.Double(10, 0, 60, 60));

                // Plus icon for "create"
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawLine(40, 20, 40, 40);
                g2.drawLine(30, 30, 50, 30);

                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(80, 60));
        panel.setMaximumSize(new Dimension(80, 60));
        panel.setOpaque(false);
        return panel;
    }

    private JPanel createInputPanel(String labelText, String icon, JTextField field) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.setMaximumSize(new Dimension(350, 75));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Inter", Font.PLAIN, 13));
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fieldPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER_COLOR);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        fieldPanel.setLayout(new BorderLayout());
        fieldPanel.setOpaque(false);
        fieldPanel.setPreferredSize(new Dimension(350, 44));
        fieldPanel.setMaximumSize(new Dimension(350, 44));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        iconLabel.setBorder(new EmptyBorder(0, 12, 0, 8));

        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBackground(INPUT_BG);
        field.setBorder(new EmptyBorder(0, 0, 0, 12));
        field.setOpaque(false);

        // Add focus effect
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                fieldPanel.setBorder(BorderFactory.createLineBorder(ACCENT_PURPLE, 2));
                fieldPanel.repaint();
            }
            @Override
            public void focusLost(FocusEvent e) {
                fieldPanel.setBorder(null);
                fieldPanel.repaint();
            }
        });

        fieldPanel.add(iconLabel, BorderLayout.WEST);
        fieldPanel.add(field, BorderLayout.CENTER);

        container.add(label);
        container.add(Box.createVerticalStrut(6));
        container.add(fieldPanel);

        return container;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, ACCENT_BLUE,
                        getWidth(), 0, ACCENT_PURPLE
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();

                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Inter", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(350, 48));
        button.setMaximumSize(new Dimension(350, 48));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setFont(new Font("Inter", Font.BOLD, 16));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setFont(new Font("Inter", Font.BOLD, 15));
            }
        });

        return button;
    }

    private void setupEventListeners() {
        registerButton.addActionListener(e -> attemptRegistration());
        backToLoginButton.addActionListener(e -> parent.showLoginPanel());

        // Enter key navigation
        usernameField.addActionListener(e -> emailField.requestFocus());
        emailField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> confirmPasswordField.requestFocus());
        confirmPasswordField.addActionListener(e -> attemptRegistration());
    }

    private void attemptRegistration() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        // Validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Please enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        if (!termsCheckbox.isSelected()) {
            showError("Please agree to the Terms of Service");
            return;
        }

        // Show loading state
        registerButton.setText("Creating account...");
        registerButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return parent.performRegistration(username, email, password);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        showSuccess("Account created successfully!");
                        clearFields();
                        // Delay then show login
                        Timer timer = new Timer(1500, evt -> parent.showLoginPanel());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showError("Registration failed. Username may already exist.");
                    }
                } catch (Exception ex) {
                    showError("Registration failed: " + ex.getMessage());
                }
                registerButton.setText("Create Account");
                registerButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        showDialog(message, ERROR_RED, "⚠️");
    }

    private void showSuccess(String message) {
        showDialog(message, SUCCESS_GREEN, "✓");
    }

    private void showDialog(String message, Color accentColor, String icon) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setForeground(accentColor);
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>");
        messageLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Inter", Font.BOLD, 13));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(accentColor);
        okButton.setPreferredSize(new Dimension(80, 32));
        okButton.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> dialog.dispose());

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(okButton);

        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    public void clearFields() {
        usernameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        showPasswordCheckbox.setSelected(false);
        termsCheckbox.setSelected(false);
        passwordField.setEchoChar('•');
        confirmPasswordField.setEchoChar('•');
    }
}