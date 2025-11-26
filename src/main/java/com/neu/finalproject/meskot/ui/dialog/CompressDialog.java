package com.neu.finalproject.meskot.ui.dialog;



import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

/**
 * Modal dialog for local video compression.
 * Replaces the compression page in the main UI.
 */
public class CompressDialog extends JDialog {

    // --- Color Palette (matching main UI) ---
    private static final Color DARK_BG = new Color(15, 15, 20);
    private static final Color CARD_BG = new Color(25, 25, 35);
    private static final Color ACCENT_PURPLE = new Color(147, 51, 234);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 245);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color INPUT_BG = new Color(30, 30, 40);
    private static final Color BORDER_COLOR = new Color(45, 45, 55);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);

    // --- UI Components ---
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

    // --- Callback ---
    private Consumer<CompressResult> onCompressStart;

    public CompressDialog(Frame parent) {
        super(parent, "Compress Video", true);
        initUI();
        setSize(550, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // === HEADER ===
        JLabel headerLabel = new JLabel("Video Compression");
        headerLabel.setFont(new Font("Inter", Font.BOLD, 24));
        headerLabel.setForeground(TEXT_PRIMARY);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // === CONTENT PANEL ===
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(CARD_BG);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Source File Row ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel srcLabel = new JLabel("Source File:");
        srcLabel.setForeground(TEXT_PRIMARY);
        srcLabel.setFont(new Font("Inter", Font.BOLD, 14));
        contentPanel.add(srcLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        sourceLabel = new JLabel("No file selected");
        sourceLabel.setForeground(TEXT_SECONDARY);
        sourceLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        contentPanel.add(sourceLabel, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        selectSourceButton = createStyledButton("Browse...", false);
        selectSourceButton.setPreferredSize(new Dimension(100, 32));
        selectSourceButton.addActionListener(e -> selectSource());
        contentPanel.add(selectSourceButton, gbc);

        // Source size label
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        sourceSizeLabel = new JLabel("");
        sourceSizeLabel.setForeground(TEXT_SECONDARY);
        sourceSizeLabel.setFont(new Font("Inter", Font.PLAIN, 11));
        contentPanel.add(sourceSizeLabel, gbc);

        // --- Output File Row ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel outLabel = new JLabel("Save To:");
        outLabel.setForeground(TEXT_PRIMARY);
        outLabel.setFont(new Font("Inter", Font.BOLD, 14));
        contentPanel.add(outLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        outputLabel = new JLabel("No output selected");
        outputLabel.setForeground(TEXT_SECONDARY);
        outputLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        contentPanel.add(outputLabel, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        selectOutputButton = createStyledButton("Save As...", false);
        selectOutputButton.setPreferredSize(new Dimension(100, 32));
        selectOutputButton.addActionListener(e -> selectOutput());
        contentPanel.add(selectOutputButton, gbc);

        // === SETTINGS SECTION ===
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 10, 8);
        JLabel settingsHeader = new JLabel("⚙️ Compression Settings");
        settingsHeader.setForeground(TEXT_PRIMARY);
        settingsHeader.setFont(new Font("Inter", Font.BOLD, 16));
        contentPanel.add(settingsHeader, gbc);

        gbc.insets = new Insets(6, 8, 6, 8);

        // Resolution
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        JLabel resLabel = new JLabel("Resolution:");
        resLabel.setForeground(TEXT_PRIMARY);
        resLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        contentPanel.add(resLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        resolutionCombo = new JComboBox<>(new String[]{
                "Original", "1080p", "720p", "480p", "360p", "240p"
        });
        styleComboBox(resolutionCombo);
        contentPanel.add(resolutionCombo, gbc);

        // Codec
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        JLabel codecLabel = new JLabel("Codec:");
        codecLabel.setForeground(TEXT_PRIMARY);
        codecLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        contentPanel.add(codecLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        codecCombo = new JComboBox<>(new String[]{
                "H.265 (HEVC) - Smaller size",
                "H.264 (AVC) - More compatible"
        });
        styleComboBox(codecCombo);
        contentPanel.add(codecCombo, gbc);

        // Quality
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        JLabel qualLabel = new JLabel("Quality:");
        qualLabel.setForeground(TEXT_PRIMARY);
        qualLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        contentPanel.add(qualLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        qualityCombo = new JComboBox<>(new String[]{
                "High (CRF 18) - Larger file",
                "Medium (CRF 23) - Balanced",
                "Low (CRF 28) - Smaller file",
                "Very Low (CRF 32) - Smallest"
        });
        qualityCombo.setSelectedIndex(1); // Default to Medium
        styleComboBox(qualityCombo);
        contentPanel.add(qualityCombo, gbc);

        // === PROGRESS SECTION ===
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 5, 8);
        statusLabel = new JLabel("Ready to compress");
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        contentPanel.add(statusLabel, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(5, 8, 8, 8);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setBackground(DARK_BG);
        progressBar.setForeground(ACCENT_PURPLE);
        progressBar.setPreferredSize(new Dimension(450, 24));
        contentPanel.add(progressBar, gbc);

        // === TIPS SECTION ===
        gbc.gridy = 9;
        gbc.insets = new Insets(15, 8, 5, 8);
//        JLabel tipsLabel = new JLabel("<html><div style='width:400px; color:#9CA3AF; font-size:11px;'>" +
//                "<b>💡 Tips:</b><br>" +
//                "• H.265 produces smaller files but encodes slower<br>" +
//                "• Lower resolution = smaller file size<br>" +
//                "• 720p Medium is a good balance for sharing online" +
//                "</div></html>");
//        contentPanel.add(tipsLabel, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // === BUTTON PANEL ===
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(DARK_BG);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        cancelButton = createStyledButton("Cancel", false);
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> dispose());

        compressButton = createStyledButton("Start Compression", true);
        compressButton.setPreferredSize(new Dimension(180, 40));
        compressButton.addActionListener(e -> startCompression());

        buttonPanel.add(cancelButton);
        buttonPanel.add(compressButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    // === FILE SELECTION METHODS ===

    private void selectSource() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Video File to Compress");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Video Files", "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm"
        ));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            sourceFile = chooser.getSelectedFile();
            sourceLabel.setText(truncateFilename(sourceFile.getName(), 30));

            double sizeMB = sourceFile.length() / (1024.0 * 1024.0);
            sourceSizeLabel.setText(String.format("Size: %.2f MB", sizeMB));

            // Auto-suggest output filename
            String name = sourceFile.getName();
            int dot = name.lastIndexOf('.');
            String base = dot > 0 ? name.substring(0, dot) : name;
            outputFile = new File(sourceFile.getParent(), base + "_compressed.mp4");
            outputLabel.setText(truncateFilename(outputFile.getName(), 30));
        }
    }

    private void selectOutput() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Compressed Video As");

        if (sourceFile != null) {
            chooser.setCurrentDirectory(sourceFile.getParentFile());
            String name = sourceFile.getName();
            int dot = name.lastIndexOf('.');
            String base = dot > 0 ? name.substring(0, dot) : name;
            chooser.setSelectedFile(new File(base + "_compressed.mp4"));
        }

        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MP4 Video", "mp4"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputFile = chooser.getSelectedFile();
            if (!outputFile.getName().toLowerCase().endsWith(".mp4")) {
                outputFile = new File(outputFile.getAbsolutePath() + ".mp4");
            }
            outputLabel.setText(truncateFilename(outputFile.getName(), 30));
        }
    }

    private String truncateFilename(String name, int maxLen) {
        if (name.length() <= maxLen) return name;
        return name.substring(0, maxLen - 3) + "...";
    }

    // === COMPRESSION START ===

    private void startCompression() {
        if (sourceFile == null || !sourceFile.exists()) {
            JOptionPane.showMessageDialog(this,
                    "Please select a valid source video file.",
                    "No Source File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (outputFile == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select an output location.",
                    "No Output Location", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (onCompressStart != null) {
            CompressResult result = new CompressResult(
                    sourceFile,
                    outputFile,
                    getResolution(),
                    getCodec(),
                    getQuality()
            );
            onCompressStart.accept(result);
        }
    }

    // === CALLBACK SETTER ===

    public void setOnCompressStart(Consumer<CompressResult> callback) {
        this.onCompressStart = callback;
    }

    // === PROGRESS UPDATE METHODS ===

    public void setProgress(int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(percent);
            progressBar.setString(percent + "%");
            statusLabel.setText(status);
            setControlsEnabled(false);
        });
    }

    public void setComplete(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(100);
            progressBar.setString("100%");
            progressBar.setForeground(success ? SUCCESS_GREEN : Color.RED);
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
        compressButton.setText(enabled ? "Start Compression" : "⏳ Compressing...");
    }

    // === GETTERS FOR SETTINGS ===

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

    // === STYLING HELPERS ===

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isPrimary) {
                    g2.setPaint(new GradientPaint(0, 0, ACCENT_PURPLE, getWidth(), getHeight(), ACCENT_BLUE));
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

    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(INPUT_BG);
        combo.setForeground(TEXT_PRIMARY);
        combo.setFont(new Font("Inter", Font.PLAIN, 13));
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        combo.setPreferredSize(new Dimension(280, 32));

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

    // === RESULT CLASS ===

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