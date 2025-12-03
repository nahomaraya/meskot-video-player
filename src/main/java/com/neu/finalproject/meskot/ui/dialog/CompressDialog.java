package com.neu.finalproject.meskot.ui.dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.function.Consumer;

public class CompressDialog extends JDialog {

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

    private File sourceFile;
    private File outputFile;
    private JLabel sourceLabel;
    private JLabel sourceSizeLabel;
    private JLabel outputLabel;
    private JComboBox<String> resolutionCombo;
    private JComboBox<String> codecCombo;
    private JComboBox<String> qualityCombo;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton compressButton;
    private JButton selectSourceButton;
    private JButton selectOutputButton;
    private JButton cancelButton;

    private Consumer<CompressResult> onCompressStart;

    public CompressDialog(Frame parent) {
        super(parent, "Compress Video", true);
        initUI();
        setSize(520, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JLabel headerLabel = new JLabel("Compress Video");
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

        // Source File
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel srcLabel = new JLabel("Source");
        srcLabel.setForeground(TEXT_PRIMARY);
        srcLabel.setFont(FONT_LABEL);
        contentPanel.add(srcLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        sourceLabel = new JLabel("No file selected");
        sourceLabel.setForeground(TEXT_SECONDARY);
        sourceLabel.setFont(FONT_SMALL);
        contentPanel.add(sourceLabel, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        selectSourceButton = createSecondaryButton("Browse");
        selectSourceButton.setPreferredSize(new Dimension(80, 32));
        selectSourceButton.addActionListener(e -> selectSource());
        contentPanel.add(selectSourceButton, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        sourceSizeLabel = new JLabel("");
        sourceSizeLabel.setForeground(TEXT_SECONDARY);
        sourceSizeLabel.setFont(FONT_SMALL);
        contentPanel.add(sourceSizeLabel, gbc);

        // Output File
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel outLabel = new JLabel("Save to");
        outLabel.setForeground(TEXT_PRIMARY);
        outLabel.setFont(FONT_LABEL);
        contentPanel.add(outLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        outputLabel = new JLabel("No output selected");
        outputLabel.setForeground(TEXT_SECONDARY);
        outputLabel.setFont(FONT_SMALL);
        contentPanel.add(outputLabel, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        selectOutputButton = createSecondaryButton("Save As");
        selectOutputButton.setPreferredSize(new Dimension(80, 32));
        selectOutputButton.addActionListener(e -> selectOutput());
        contentPanel.add(selectOutputButton, gbc);

        // Settings Section
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 12, 8);
        JLabel settingsHeader = new JLabel("Settings");
        settingsHeader.setForeground(TEXT_PRIMARY);
        settingsHeader.setFont(FONT_LABEL);
        contentPanel.add(settingsHeader, gbc);

        gbc.insets = new Insets(6, 8, 6, 8);

        // Resolution
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel resLabel = new JLabel("Resolution");
        resLabel.setForeground(TEXT_SECONDARY);
        resLabel.setFont(FONT_SMALL);
        contentPanel.add(resLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        resolutionCombo = new JComboBox<>(new String[]{
                "Original", "1080p", "720p", "480p", "360p", "240p"
        });
        styleComboBox(resolutionCombo);
        contentPanel.add(resolutionCombo, gbc);

        // Codec
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JLabel codecLabel = new JLabel("Codec");
        codecLabel.setForeground(TEXT_SECONDARY);
        codecLabel.setFont(FONT_SMALL);
        contentPanel.add(codecLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        codecCombo = new JComboBox<>(new String[]{
                "H.265 (HEVC) - Smaller",
                "H.264 (AVC) - Compatible"
        });
        styleComboBox(codecCombo);
        contentPanel.add(codecCombo, gbc);

        // Quality
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        JLabel qualLabel = new JLabel("Quality");
        qualLabel.setForeground(TEXT_SECONDARY);
        qualLabel.setFont(FONT_SMALL);
        contentPanel.add(qualLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        qualityCombo = new JComboBox<>(new String[]{
                "High (CRF 18)",
                "Medium (CRF 23)",
                "Low (CRF 28)",
                "Very Low (CRF 32)"
        });
        qualityCombo.setSelectedIndex(1);
        styleComboBox(qualityCombo);
        contentPanel.add(qualityCombo, gbc);

        // Progress
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 4, 8);
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setFont(FONT_SMALL);
        contentPanel.add(statusLabel, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(4, 8, 8, 8);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setBackground(BG_PRIMARY);
        progressBar.setForeground(ACCENT);
        progressBar.setPreferredSize(new Dimension(420, 6));
        progressBar.setBorderPainted(false);
        contentPanel.add(progressBar, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_PRIMARY);
        buttonPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        cancelButton = createSecondaryButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(90, 38));
        cancelButton.addActionListener(e -> dispose());

        compressButton = createPrimaryButton("Compress");
        compressButton.setPreferredSize(new Dimension(120, 38));
        compressButton.addActionListener(e -> startCompression());

        buttonPanel.add(cancelButton);
        buttonPanel.add(compressButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void selectSource() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Video File");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Video Files", "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm"
        ));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            sourceFile = chooser.getSelectedFile();
            sourceLabel.setText(truncate(sourceFile.getName(), 28));
            double sizeMB = sourceFile.length() / (1024.0 * 1024.0);
            sourceSizeLabel.setText(String.format("%.2f MB", sizeMB));

            String name = sourceFile.getName();
            int dot = name.lastIndexOf('.');
            String base = dot > 0 ? name.substring(0, dot) : name;
            outputFile = new File(sourceFile.getParent(), base + "_compressed.mp4");
            outputLabel.setText(truncate(outputFile.getName(), 28));
        }
    }

    private void selectOutput() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Compressed Video");

        if (sourceFile != null) {
            chooser.setCurrentDirectory(sourceFile.getParentFile());
            String name = sourceFile.getName();
            int dot = name.lastIndexOf('.');
            String base = dot > 0 ? name.substring(0, dot) : name;
            chooser.setSelectedFile(new File(base + "_compressed.mp4"));
        }

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputFile = chooser.getSelectedFile();
            if (!outputFile.getName().toLowerCase().endsWith(".mp4")) {
                outputFile = new File(outputFile.getAbsolutePath() + ".mp4");
            }
            outputLabel.setText(truncate(outputFile.getName(), 28));
        }
    }

    private String truncate(String name, int maxLen) {
        if (name.length() <= maxLen) return name;
        return name.substring(0, maxLen - 3) + "...";
    }

    private void startCompression() {
        if (sourceFile == null || !sourceFile.exists()) {
            showMessage("Please select a source video file.");
            return;
        }
        if (outputFile == null) {
            showMessage("Please select an output location.");
            return;
        }

        if (onCompressStart != null) {
            onCompressStart.accept(new CompressResult(
                    sourceFile, outputFile, getResolution(), getCodec(), getQuality()
            ));
        }
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    public void setOnCompressStart(Consumer<CompressResult> callback) {
        this.onCompressStart = callback;
    }

    public void setProgress(int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(percent);
            statusLabel.setText(status);
            setControlsEnabled(false);
        });
    }

    public void setComplete(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(100);
            progressBar.setForeground(success ? SUCCESS_GREEN : ERROR_RED);
            statusLabel.setText(message);
            compressButton.setEnabled(false);
            cancelButton.setText("Close");
        });
    }

    private void setControlsEnabled(boolean enabled) {
        selectSourceButton.setEnabled(enabled);
        selectOutputButton.setEnabled(enabled);
        resolutionCombo.setEnabled(enabled);
        codecCombo.setEnabled(enabled);
        qualityCombo.setEnabled(enabled);
        compressButton.setEnabled(enabled);
        compressButton.setText(enabled ? "Compress" : "Compressing...");
    }

    public String getResolution() {
        String sel = (String) resolutionCombo.getSelectedItem();
        return "Original".equals(sel) ? null : sel;
    }

    public String getCodec() {
        String sel = (String) codecCombo.getSelectedItem();
        return sel.contains("H.265") ? "h265" : "h264";
    }

    public int getQuality() {
        int idx = qualityCombo.getSelectedIndex();
        return new int[]{18, 23, 28, 32}[idx];
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

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(BG_TERTIARY);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(FONT_SMALL);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_SUBTLE));
        combo.setPreferredSize(new Dimension(240, 32));
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

    public static class CompressResult {
        public final File source;
        public final File output;
        public final String resolution;
        public final String codec;
        public final int quality;

        public CompressResult(File source, File output, String resolution, String codec, int quality) {
            this.source = source;
            this.output = output;
            this.resolution = resolution;
            this.codec = codec;
            this.quality = quality;
        }
    }
}