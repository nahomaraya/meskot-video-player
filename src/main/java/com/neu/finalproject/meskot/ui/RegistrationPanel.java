package com.neu.finalproject.meskot.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
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

    // Two-color scheme matching VideoPlayerUI
    private static final Color BG_PRIMARY = new Color(33, 33, 33);
    private static final Color BG_SECONDARY = new Color(48, 48, 48);
    private static final Color BG_TERTIARY = new Color(64, 64, 64);
    private static final Color BORDER_SUBTLE = new Color(75, 75, 75);
    private static final Color TEXT_PRIMARY = new Color(236, 236, 236);
    private static final Color TEXT_SECONDARY = new Color(156, 156, 156);
    private static final Color ACCENT = new Color(255, 136, 0);
    private static final Color ACCENT_HOVER = new Color(255, 160, 50);
    private static final Color ERROR_RED = new Color(220, 80, 80);
    private static final Color SUCCESS_GREEN = new Color(80, 180, 80);

    private static final String FONT_FAMILY = "Inter";
    private static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.PLAIN, 24);
    private static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    private static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.PLAIN, 14);

    public RegistrationPanel(VideoPlayerUI parent) {
        this.parent = parent;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        setBackground(BG_PRIMARY);

        // Main card
        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.setColor(BORDER_SUBTLE);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 16, 16));
                g2.dispose();
            }
        };
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(40, 48, 40, 48));
        cardPanel.setPreferredSize(new Dimension(400, 600));

        // Title
        JLabel titleLabel = new JLabel("Create account");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Join Meskot Player");
        subtitleLabel.setFont(FONT_SMALL);
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input fields
        usernameField = new JTextField();
        JPanel usernamePanel = createInputPanel("Username", usernameField);

        emailField = new JTextField();
        JPanel emailPanel = createInputPanel("Email", emailField);

        passwordField = new JPasswordField();
        passwordField.setEchoChar('•');
        JPanel passwordPanel = createInputPanel("Password", passwordField);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setEchoChar('•');
        JPanel confirmPanel = createInputPanel("Confirm password", confirmPasswordField);

        // Show password
        showPasswordCheckbox = new JCheckBox("Show passwords");
        showPasswordCheckbox.setFont(FONT_SMALL);
        showPasswordCheckbox.setForeground(TEXT_SECONDARY);
        showPasswordCheckbox.setBackground(BG_SECONDARY);
        showPasswordCheckbox.setOpaque(false);
        showPasswordCheckbox.setFocusPainted(false);
        showPasswordCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        showPasswordCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordCheckbox.addActionListener(e -> {
            char echo = showPasswordCheckbox.isSelected() ? (char) 0 : '•';
            passwordField.setEchoChar(echo);
            confirmPasswordField.setEchoChar(echo);
        });

        // Terms
        termsCheckbox = new JCheckBox("I agree to the Terms of Service");
        termsCheckbox.setFont(FONT_SMALL);
        termsCheckbox.setForeground(TEXT_SECONDARY);
        termsCheckbox.setBackground(BG_SECONDARY);
        termsCheckbox.setOpaque(false);
        termsCheckbox.setFocusPainted(false);
        termsCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        termsCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Register button
        registerButton = createPrimaryButton("Create account");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Back to login
        JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        loginPanel.setOpaque(false);

        JLabel hasAccountLabel = new JLabel("Already have an account?");
        hasAccountLabel.setFont(FONT_SMALL);
        hasAccountLabel.setForeground(TEXT_SECONDARY);

        backToLoginButton = new JButton("Sign in");
        backToLoginButton.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
        backToLoginButton.setForeground(ACCENT);
        backToLoginButton.setBackground(null);
        backToLoginButton.setBorder(null);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backToLoginButton.setFocusPainted(false);
        backToLoginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { backToLoginButton.setForeground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e) { backToLoginButton.setForeground(ACCENT); }
        });

        loginPanel.add(hasAccountLabel);
        loginPanel.add(backToLoginButton);

        // Layout
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(28));
        cardPanel.add(usernamePanel);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(emailPanel);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(passwordPanel);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(confirmPanel);
        cardPanel.add(Box.createVerticalStrut(12));
        cardPanel.add(showPasswordCheckbox);
        cardPanel.add(Box.createVerticalStrut(8));
        cardPanel.add(termsCheckbox);
        cardPanel.add(Box.createVerticalStrut(24));
        cardPanel.add(registerButton);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(loginPanel);

        add(cardPanel);
        setupEventListeners();
    }

    private JPanel createInputPanel(String labelText, JTextField field) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.setMaximumSize(new Dimension(320, 68));

        JLabel label = new JLabel(labelText);
        label.setFont(FONT_SMALL);
        label.setForeground(TEXT_PRIMARY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel fieldPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_TERTIARY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(BORDER_SUBTLE);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
            }
        };
        fieldPanel.setLayout(new BorderLayout());
        fieldPanel.setOpaque(false);
        fieldPanel.setPreferredSize(new Dimension(320, 42));
        fieldPanel.setMaximumSize(new Dimension(320, 42));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBackground(BG_TERTIARY);
        field.setBorder(new EmptyBorder(0, 14, 0, 14));
        field.setOpaque(false);

        field.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                fieldPanel.setBorder(BorderFactory.createLineBorder(ACCENT, 1));
                fieldPanel.repaint();
            }
            public void focusLost(FocusEvent e) {
                fieldPanel.setBorder(null);
                fieldPanel.repaint();
            }
        });

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
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(BG_PRIMARY);
        button.setPreferredSize(new Dimension(320, 44));
        button.setMaximumSize(new Dimension(320, 44));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void setupEventListeners() {
        registerButton.addActionListener(e -> attemptRegistration());
        backToLoginButton.addActionListener(e -> parent.showLoginPanel());
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

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showDialog("Please fill in all fields", ERROR_RED);
            return;
        }

        if (username.length() < 3) {
            showDialog("Username must be at least 3 characters", ERROR_RED);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showDialog("Please enter a valid email address", ERROR_RED);
            return;
        }

        if (password.length() < 6) {
            showDialog("Password must be at least 6 characters", ERROR_RED);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showDialog("Passwords do not match", ERROR_RED);
            return;
        }

        if (!termsCheckbox.isSelected()) {
            showDialog("Please agree to the Terms of Service", ERROR_RED);
            return;
        }

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
                    if (get()) {
                        showDialog("Account created successfully", SUCCESS_GREEN);
                        clearFields();
                        Timer timer = new Timer(1500, evt -> parent.showLoginPanel());
                        timer.setRepeats(false);
                        timer.start();
                    } else {
                        showDialog("Registration failed. Username may exist.", ERROR_RED);
                    }
                } catch (Exception ex) {
                    showDialog("Registration failed: " + ex.getMessage(), ERROR_RED);
                }
                registerButton.setText("Create account");
                registerButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void showDialog(String message, Color accentColor) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), true);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(accentColor);
                g2.setStroke(new BasicStroke(1));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                g2.dispose();
            }
        };
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(24, 32, 24, 32));
        panel.setOpaque(false);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(FONT_BODY);
        messageLabel.setForeground(TEXT_PRIMARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton okButton = new JButton("OK");
        okButton.setFont(FONT_BUTTON);
        okButton.setForeground(TEXT_PRIMARY);
        okButton.setBackground(BG_TERTIARY);
        okButton.setPreferredSize(new Dimension(80, 32));
        okButton.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        okButton.setFocusPainted(false);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        okButton.addActionListener(e -> dialog.dispose());

        panel.add(messageLabel);
        panel.add(Box.createVerticalStrut(20));
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