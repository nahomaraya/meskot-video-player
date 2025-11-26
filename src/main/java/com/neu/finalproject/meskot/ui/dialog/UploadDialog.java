package com.neu.finalproject.meskot.ui.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class UploadDialog extends JDialog {

    private static final Color DARK_BG = new Color(15, 15, 20);
    private static final Color CARD_BG = new Color(25, 25, 35);
    private static final Color ACCENT_PURPLE = new Color(147, 51, 234);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 245);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color INPUT_BG = new Color(30, 30, 40);
    private static final Color BORDER_COLOR = new Color(45, 45, 55);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);

    private File selectedFile;
    private JLabel fileLabel;
    private JLabel fileSizeLabel;
    private JTextField titleField;
    private JComboBox<String> resolutionCombo;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton uploadButton;
    private JButton selectFileButton;
    private JButton cancelButton;

    private boolean confirmed = false;
    private Consumer<UploadResult> onUploadStart;

    public UploadDialog(Frame parent) {
        super(parent, "📤 Upload Video", true);
        initUI();
        setSize(500, 450);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("Upload a Video");
        headerLabel.setFont(new Font("Inter", Font.BOLD, 22));
        headerLabel.setForeground(TEXT_PRIMARY);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CARD_BG);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // File Selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel selectLabel = new JLabel("Video File:");
        selectLabel.setForeground(TEXT_PRIMARY);
        selectLabel.setFont(new Font("Inter", Font.BOLD, 14));
        contentPanel.add(selectLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(TEXT_SECONDARY);
        fileLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        contentPanel.add(fileLabel, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        selectFileButton = createStyledButton("Browse...", false);
        selectFileButton.setPreferredSize(new Dimension(100, 32));
        selectFileButton.addActionListener(e -> selectFile());
        contentPanel.add(selectFileButton, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        fileSizeLabel = new JLabel("");
        fileSizeLabel.setForeground(TEXT_SECONDARY);
        fileSizeLabel.setFont(new Font("Inter", Font.PLAIN, 11));
        contentPanel.add(fileSizeLabel, gbc);

        // Title
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));
        contentPanel.add(titleLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        titleField = new JTextField(25);
        styleTextField(titleField);
        contentPanel.add(titleField, gbc);

        // Resolution
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel resLabel = new JLabel("Resolution:");
        resLabel.setForeground(TEXT_PRIMARY);
        resLabel.setFont(new Font("Inter", Font.BOLD, 14));
        contentPanel.add(resLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        resolutionCombo = new JComboBox<>(new String[]{"1080p", "720p", "480p", "360p"});
        resolutionCombo.setSelectedIndex(1);
        styleComboBox(resolutionCombo);
        contentPanel.add(resolutionCombo, gbc);

        // Progress Section
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 5, 8);
        statusLabel = new JLabel("Ready to upload");
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        contentPanel.add(statusLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 8, 8, 8);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBackground(DARK_BG);
        progressBar.setForeground(ACCENT_PURPLE);
        progressBar.setPreferredSize(new Dimension(400, 20));
        progressBar.setVisible(false);
        contentPanel.add(progressBar, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(DARK_BG);

        cancelButton = createStyledButton("Cancel", false);
        cancelButton.setPreferredSize(new Dimension(100, 38));
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        uploadButton = createStyledButton("Upload", true);
        uploadButton.setPreferredSize(new Dimension(120, 38));
        uploadButton.addActionListener(e -> startUpload());

        buttonPanel.add(cancelButton);
        buttonPanel.add(uploadButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void selectFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Video File");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Video Files", "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm"
        ));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedFile = chooser.getSelectedFile();
            fileLabel.setText(selectedFile.getName());
            double sizeMB = selectedFile.length() / (1024.0 * 1024.0);
            fileSizeLabel.setText(String.format("Size: %.2f MB", sizeMB));

            // Auto-fill title from filename
            String name = selectedFile.getName();
            int dot = name.lastIndexOf('.');
            titleField.setText(dot > 0 ? name.substring(0, dot) : name);
        }
    }

    private void startUpload() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a video file.",
                    "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title.",
                    "Title Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        confirmed = true;
        if (onUploadStart != null) {
            UploadResult result = new UploadResult(
                    selectedFile,
                    titleField.getText().trim(),
                    (String) resolutionCombo.getSelectedItem()
            );
            onUploadStart.accept(result);
        }
    }

    public void setOnUploadStart(Consumer<UploadResult> callback) {
        this.onUploadStart = callback;
    }

    public void setProgress(int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(true);
            progressBar.setValue(percent);
            progressBar.setString(percent + "%");
            statusLabel.setText(status);
            setControlsEnabled(false);
        });
    }

    public void setIndeterminate(String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            statusLabel.setText(status);
        });
    }

    public void setComplete(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            progressBar.setForeground(success ? SUCCESS_GREEN : Color.RED);
            statusLabel.setText(message);

            Timer closeTimer = new Timer(2000, e -> dispose());
            closeTimer.setRepeats(false);
            closeTimer.start();
        });
    }

    private void setControlsEnabled(boolean enabled) {
        selectFileButton.setEnabled(enabled);
        titleField.setEnabled(enabled);
        resolutionCombo.setEnabled(enabled);
        uploadButton.setEnabled(enabled);
    }

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isPrimary) {
                    GradientPaint gradient = new GradientPaint(0, 0, ACCENT_PURPLE, getWidth(), getHeight(), ACCENT_BLUE);
                    g2.setPaint(gradient);
                } else {
                    g2.setColor(CARD_BG);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (!isPrimary) {
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Inter", Font.BOLD, 13));
        button.setForeground(TEXT_PRIMARY);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(INPUT_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(8, 10, 8, 10)
        ));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(INPUT_BG);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(new Font("Inter", Font.PLAIN, 14));
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT_PURPLE : INPUT_BG);
                setForeground(TEXT_PRIMARY);
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return this;
            }
        });
    }

    // Result class
    public static class UploadResult {
        public final File file;
        public final String title;
        public final String resolution;

        public UploadResult(File file, String title, String resolution) {
            this.file = file;
            this.title = title;
            this.resolution = resolution;
        }
    }
}