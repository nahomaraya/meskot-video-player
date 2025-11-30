package com.neu.finalproject.meskot.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel extends JPanel {
    private final VideoPlayerUI parent;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton, guestButton;
    private JCheckBox showPasswordCheckbox;

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

    public LoginPanel(VideoPlayerUI parent) {
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
        cardPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        cardPanel.setPreferredSize(new Dimension(420, 520));

        // Logo/Icon
        JPanel logoPanel = createLogoPanel();
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title
        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Sign in to continue to Meskot Player");
        subtitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Username field
        usernameField = new JTextField();
        JPanel usernamePanel = createInputPanel("Username", "👤", usernameField);

        // Password field
        passwordField = new JPasswordField();
        passwordField.setEchoChar('•');
        JPanel passwordPanel = createInputPanel("Password", "🔒", passwordField);

        // Show password checkbox
        showPasswordCheckbox = new JCheckBox("Show password");
        showPasswordCheckbox.setFont(new Font("Inter", Font.PLAIN, 12));
        showPasswordCheckbox.setForeground(TEXT_SECONDARY);
        showPasswordCheckbox.setBackground(CARD_BG);
        showPasswordCheckbox.setOpaque(false);
        showPasswordCheckbox.setFocusPainted(false);
        showPasswordCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordCheckbox.addActionListener(e -> {
            if (showPasswordCheckbox.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });

        // Login button
        loginButton = createPrimaryButton("Sign In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JPanel dividerPanel = createDivider();
        dividerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Guest button
        guestButton = createSecondaryButton("Continue as Guest");
        guestButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Register link
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        registerPanel.setOpaque(false);

        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        noAccountLabel.setForeground(TEXT_SECONDARY);

        registerButton = new JButton("Sign up");
        registerButton.setFont(new Font("Inter", Font.BOLD, 13));
        registerButton.setForeground(ACCENT_PURPLE);
        registerButton.setBackground(null);
        registerButton.setBorder(null);
        registerButton.setContentAreaFilled(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setFocusPainted(false);
        registerButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                registerButton.setForeground(ACCENT_BLUE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                registerButton.setForeground(ACCENT_PURPLE);
            }
        });

        registerPanel.add(noAccountLabel);
        registerPanel.add(registerButton);

        // Add components to card
        cardPanel.add(logoPanel);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(30));
        cardPanel.add(usernamePanel);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(passwordPanel);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(showPasswordCheckbox);
        cardPanel.add(Box.createVerticalStrut(24));
        cardPanel.add(loginButton);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(dividerPanel);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(guestButton);
        cardPanel.add(Box.createVerticalStrut(24));
        cardPanel.add(registerPanel);

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
                        0, 0, ACCENT_PURPLE,
                        getWidth(), getHeight(), ACCENT_BLUE
                );
                g2.setPaint(gradient);
                g2.fill(new Ellipse2D.Double(10, 0, 60, 60));

                // Play icon
                g2.setColor(Color.WHITE);
                int[] xPoints = {32, 32, 52};
                int[] yPoints = {18, 42, 30};
                g2.fillPolygon(xPoints, yPoints, 3);

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
        container.setMaximumSize(new Dimension(320, 80));

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
        fieldPanel.setPreferredSize(new Dimension(320, 48));
        fieldPanel.setMaximumSize(new Dimension(320, 48));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Icon
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconLabel.setBorder(new EmptyBorder(0, 15, 0, 10));

        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBackground(INPUT_BG);
        field.setBorder(new EmptyBorder(0, 0, 0, 15));
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
        container.add(Box.createVerticalStrut(8));
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
                        0, 0, ACCENT_PURPLE,
                        getWidth(), 0, ACCENT_BLUE
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();

                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Inter", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(320, 48));
        button.setMaximumSize(new Dimension(320, 48));
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

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(BORDER_COLOR);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Inter", Font.PLAIN, 14));
        button.setForeground(TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(320, 44));
        button.setMaximumSize(new Dimension(320, 44));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(ACCENT_PURPLE);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(TEXT_PRIMARY);
            }
        });

        return button;
    }

    private JPanel createDivider() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(320, 20));

        JPanel leftLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BORDER_COLOR);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        leftLine.setOpaque(false);

        JPanel rightLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BORDER_COLOR);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        rightLine.setOpaque(false);

        JLabel orLabel = new JLabel("or");
        orLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        orLabel.setForeground(TEXT_SECONDARY);

        panel.add(leftLine, BorderLayout.WEST);
        panel.add(orLabel, BorderLayout.CENTER);
        panel.add(rightLine, BorderLayout.EAST);

        // Make lines expand
        leftLine.setPreferredSize(new Dimension(130, 20));
        rightLine.setPreferredSize(new Dimension(130, 20));

        return panel;
    }

    private void setupEventListeners() {
        loginButton.addActionListener(e -> attemptLogin());
        registerButton.addActionListener(e -> parent.showRegistrationPanel());
        guestButton.addActionListener(e -> continueAsGuest());
        passwordField.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Show loading state
        loginButton.setText("Signing in...");
        loginButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return parent.performLogin(username, password);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (!success) {
                        showError("Invalid username or password");
                    }
                } catch (Exception ex) {
                    showError("Login failed: " + ex.getMessage());
                }
                loginButton.setText("Sign In");
                loginButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void continueAsGuest() {
        guestButton.setText("Loading...");
        guestButton.setEnabled(false);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return parent.performLogin("guest", "guest");
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (!success) {
                        showError("Guest access is currently unavailable");
                    }
                } catch (Exception ex) {
                    showError("Connection failed");
                }
                guestButton.setText("Continue as Guest");
                guestButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        // Create a styled error dialog
        JDialog errorDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        errorDialog.setUndecorated(true);
        errorDialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(ERROR_RED);
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 16, 16));
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        panel.setOpaque(false);

        JLabel iconLabel = new JLabel("⚠️");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel messageLabel = new JLabel("<html><center>" + message + "</center></html>");
        messageLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("OK");
        okButton.setFont(new Font("Inter", Font.BOLD, 13));
        okButton.setForeground(Color.WHITE);
        okButton.setBackground(ERROR_RED);
        okButton.setPreferredSize(new Dimension(80, 32));
        okButton.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> errorDialog.dispose());

        panel.add(iconLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(15));
        panel.add(okButton);

        errorDialog.add(panel);
        errorDialog.pack();
        errorDialog.setLocationRelativeTo(this);
        errorDialog.setVisible(true);
    }

    // Method to clear fields when panel is shown
    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        showPasswordCheckbox.setSelected(false);
        passwordField.setEchoChar('•');
    }
}