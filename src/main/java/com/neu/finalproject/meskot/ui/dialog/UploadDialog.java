package com.neu.finalproject.meskot.ui.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.function.Consumer;

public class UploadDialog extends JDialog {

    // Two-color scheme: Dark grays + Orange accent
    private static final Color BG_PRIMARY = new Color(33, 33, 33);
    private static final Color BG_SECONDARY = new Color(48, 48, 48);
    private static final Color BG_TERTIARY = new Color(64, 64, 64);
    private static final Color BORDER_SUBTLE = new Color(75, 75, 75);
    private static final Color TEXT_PRIMARY = new Color(236, 236, 236);
    private static final Color TEXT_SECONDARY = new Color(156, 156, 156);
    private static final Color ACCENT = new Color(255, 136, 0);
    private static final Color ACCENT_HOVER = new Color(255, 160, 50);
    private static final Color SUCCESS_GREEN = new Color(80, 180, 80);
    private static final Color ERROR_RED = new Color(220, 80, 80);

    private static final String FONT_FAMILY = "Inter";
    private static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.PLAIN, 20);
    private static final Font FONT_BODY = new Font(FONT_FAMILY, Font.PLAIN, 14);
    private static final Font FONT_SMALL = new Font(FONT_FAMILY, Font.PLAIN, 12);
    private static final Font FONT_LABEL = new Font(FONT_FAMILY, Font.PLAIN, 13);
    private static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.PLAIN, 13);

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
        super(parent, "Upload Video", true);
        initUI();
        setSize(460, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JLabel headerLabel = new JLabel("Upload Video");
        headerLabel.setFont(FONT_TITLE);
        headerLabel.setForeground(TEXT_PRIMARY);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BG_SECONDARY);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // File Selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel selectLabel = new JLabel("File");
        selectLabel.setForeground(TEXT_SECONDARY);
        selectLabel.setFont(FONT_SMALL);
        contentPanel.add(selectLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(TEXT_SECONDARY);
        fileLabel.setFont(FONT_SMALL);
        contentPanel.add(fileLabel, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        selectFileButton = createSecondaryButton("Browse");
        selectFileButton.setPreferredSize(new Dimension(80, 32));
        selectFileButton.addActionListener(e -> selectFile());
        contentPanel.add(selectFileButton, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        fileSizeLabel = new JLabel("");
        fileSizeLabel.setForeground(TEXT_SECONDARY);
        fileSizeLabel.setFont(FONT_SMALL);
        contentPanel.add(fileSizeLabel, gbc);

        // Title
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel titleLabel = new JLabel("Title");
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setFont(FONT_SMALL);
        contentPanel.add(titleLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        titleField = new JTextField(20);
        styleTextField(titleField);
        contentPanel.add(titleField, gbc);

        // Resolution
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel resLabel = new JLabel("Resolution");
        resLabel.setForeground(TEXT_SECONDARY);
        resLabel.setFont(FONT_SMALL);
        contentPanel.add(resLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        resolutionCombo = new JComboBox<>(new String[]{"1080p", "720p", "480p", "360p"});
        resolutionCombo.setSelectedIndex(1);
        styleComboBox(resolutionCombo);
        contentPanel.add(resolutionCombo, gbc);

        // Progress
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 4, 8);
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setFont(FONT_SMALL);
        contentPanel.add(statusLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(4, 8, 8, 8);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setBackground(BG_PRIMARY);
        progressBar.setForeground(ACCENT);
        progressBar.setPreferredSize(new Dimension(380, 6));
        progressBar.setBorderPainted(false);
        progressBar.setVisible(false);
        contentPanel.add(progressBar, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_PRIMARY);
        buttonPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        cancelButton = createSecondaryButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(90, 38));
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        uploadButton = createPrimaryButton("Upload");
        uploadButton.setPreferredSize(new Dimension(100, 38));
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
            fileLabel.setText(truncate(selectedFile.getName(), 25));
            double sizeMB = selectedFile.length() / (1024.0 * 1024.0);
            fileSizeLabel.setText(String.format("%.2f MB", sizeMB));

            String name = selectedFile.getName();
            int dot = name.lastIndexOf('.');
            titleField.setText(dot > 0 ? name.substring(0, dot) : name);
        }
    }

    private String truncate(String name, int maxLen) {
        if (name.length() <= maxLen) return name;
        return name.substring(0, maxLen - 3) + "...";
    }

    private void startUpload() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a video file.",
                    "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (titleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a title.",
                    "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        confirmed = true;
        if (onUploadStart != null) {
            onUploadStart.accept(new UploadResult(
                    selectedFile,
                    titleField.getText().trim(),
                    (String) resolutionCombo.getSelectedItem()
            ));
        }
    }

    public void setOnUploadStart(Consumer<UploadResult> callback) {
        this.onUploadStart = callback;
    }

    public void setProgress(int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(true);
            progressBar.setValue(percent);
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
            progressBar.setForeground(success ? SUCCESS_GREEN : ERROR_RED);
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
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(BG_TERTIARY);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(FONT_BODY);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_SUBTLE),
                new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(BG_TERTIARY);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_SMALL);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        combo.setPreferredSize(new Dimension(200, 32));
        combo.setFocusable(false);

        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT : BG_TERTIARY);
                setForeground(TEXT_PRIMARY);
                setBorder(new EmptyBorder(6, 12, 6, 12));
                setFont(FONT_SMALL);
                return this;
            }
        });
    }

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