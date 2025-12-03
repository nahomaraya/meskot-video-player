package com.neu.finalproject.meskot.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginPanel extends JPanel {
    private final VideoPlayerUI parent;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, registerButton, guestButton;
    private JCheckBox showPasswordCheckbox;

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

    private static final String FONT_FAMILY = "Inter";
    private static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.PLAIN, 24);
    private static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    private static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.PLAIN, 14);

    public LoginPanel(VideoPlayerUI parent) {
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
        cardPanel.setBorder(new EmptyBorder(48, 48, 48, 48));
        cardPanel.setPreferredSize(new Dimension(380, 480));

        // Title
        JLabel titleLabel = new JLabel("Welcome back");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

//        JLabel subtitleLabel = new JLabel("Sign in to Meskot Player");
//        subtitleLabel.setFont(FONT_SMALL);
//        subtitleLabel.setForeground(TEXT_SECONDARY);
//        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input fields
        usernameField = new JTextField();
        JPanel usernamePanel = createInputPanel("Username", usernameField);

        passwordField = new JPasswordField();
        passwordField.setEchoChar('•');
        JPanel passwordPanel = createInputPanel("Password", passwordField);

        // Show password
//        showPasswordCheckbox = new JCheckBox("Show password");
//        showPasswordCheckbox.setFont(FONT_SMALL);
//        showPasswordCheckbox.setForeground(TEXT_SECONDARY);
//        showPasswordCheckbox.setBackground(BG_SECONDARY);
//        showPasswordCheckbox.setOpaque(false);
//        showPasswordCheckbox.setFocusPainted(false);
//        showPasswordCheckbox.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        showPasswordCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
//        showPasswordCheckbox.addActionListener(e ->
//                passwordField.setEchoChar(showPasswordCheckbox.isSelected() ? (char) 0 : '•')
//        );

        // Buttons
        loginButton = createPrimaryButton("Sign in");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        guestButton = createSecondaryButton("Continue as guest");
        guestButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Divider
        JPanel dividerPanel = createDivider();
        dividerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Register link
        JPanel registerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        registerPanel.setOpaque(false);

        JLabel noAccountLabel = new JLabel("Don't have an account?");
        noAccountLabel.setFont(FONT_SMALL);
        noAccountLabel.setForeground(TEXT_SECONDARY);

        registerButton = new JButton("Sign up");
        registerButton.setFont(new Font(FONT_FAMILY, Font.PLAIN, 12));
        registerButton.setForeground(ACCENT);
        registerButton.setBackground(null);
        registerButton.setBorder(null);
        registerButton.setContentAreaFilled(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.setFocusPainted(false);
        registerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { registerButton.setForeground(ACCENT_HOVER); }
            public void mouseExited(MouseEvent e) { registerButton.setForeground(ACCENT); }
        });

        registerPanel.add(noAccountLabel);
        registerPanel.add(registerButton);

        // Layout
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(8));
//        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(36));
        cardPanel.add(usernamePanel);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(passwordPanel);
        cardPanel.add(Box.createVerticalStrut(12));
//        cardPanel.add(showPasswordCheckbox);
        cardPanel.add(Box.createVerticalStrut(28));
        cardPanel.add(loginButton);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(dividerPanel);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(guestButton);
        cardPanel.add(Box.createVerticalStrut(28));
        cardPanel.add(registerPanel);

        add(cardPanel);
        setupEventListeners();
    }

    private JPanel createInputPanel(String labelText, JTextField field) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);
        container.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.setMaximumSize(new Dimension(300, 72));

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
        fieldPanel.setPreferredSize(new Dimension(300, 44));
        fieldPanel.setMaximumSize(new Dimension(300, 44));
        fieldPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setBackground(BG_TERTIARY);
        field.setBorder(new EmptyBorder(0, 16, 0, 16));
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
                g2.setColor(getModel().isRollover() ? ACCENT_HOVER : ACCENT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(BG_PRIMARY);
        button.setPreferredSize(new Dimension(300, 44));
        button.setMaximumSize(new Dimension(300, 44));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? BG_TERTIARY : BG_SECONDARY);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                g2.setColor(BORDER_SUBTLE);
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 8, 8));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(FONT_BUTTON);
        button.setForeground(TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(300, 44));
        button.setMaximumSize(new Dimension(300, 44));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createDivider() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(300, 20));

        JPanel leftLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BORDER_SUBTLE);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        leftLine.setOpaque(false);
        leftLine.setPreferredSize(new Dimension(120, 20));

        JPanel rightLine = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(BORDER_SUBTLE);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        rightLine.setOpaque(false);
        rightLine.setPreferredSize(new Dimension(120, 20));

        JLabel orLabel = new JLabel("or");
        orLabel.setFont(FONT_SMALL);
        orLabel.setForeground(TEXT_SECONDARY);

        panel.add(leftLine, BorderLayout.WEST);
        panel.add(orLabel, BorderLayout.CENTER);
        panel.add(rightLine, BorderLayout.EAST);

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
                    if (!get()) showError("Invalid username or password");
                } catch (Exception ex) {
                    showError("Login failed: " + ex.getMessage());
                }
                loginButton.setText("Sign in");
                loginButton.setEnabled(true);
            }
        };
        worker.execute();
    }

    private void continueAsGuest() {
        guestButton.setText("Loading...");
        guestButton.setEnabled(false);
        parent.showPage(VideoPlayerUI.PAGE_SEARCH);
    }

    private void showError(String message) {
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
                g2.setColor(ERROR_RED);
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
        passwordField.setText("");
        showPasswordCheckbox.setSelected(false);
        passwordField.setEchoChar('•');
    }
}