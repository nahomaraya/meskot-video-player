package com.neu.finalproject.meskot.ui.dialog;

import com.neu.finalproject.meskot.dto.MovieDto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class DownloadDialog extends JDialog {

    private static final Color DARK_BG = new Color(15, 15, 20);
    private static final Color CARD_BG = new Color(25, 25, 35);
    private static final Color ACCENT_PURPLE = new Color(147, 51, 234);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 245);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color INPUT_BG = new Color(30, 30, 40);
    private static final Color BORDER_COLOR = new Color(45, 45, 55);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);

    private final MovieDto movie;
    private File outputFile;
    private JLabel movieTitleLabel;
    private JLabel outputLabel;
    private JComboBox<String> resolutionCombo;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel downloadedLabel;
    private JButton downloadButton;
    private JButton selectOutputButton;
    private JButton cancelButton;

    private Consumer<DownloadResult> onDownloadStart;
    private Runnable onCancel;

    public DownloadDialog(Frame parent, MovieDto movie) {
        super(parent, "Download Video", true);
        this.movie = movie;
        initUI();
        setSize(500, 420);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(DARK_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header
        JLabel headerLabel = new JLabel("Download Video");
        headerLabel.setFont(new Font("Inter", Font.BOLD, 22));
        headerLabel.setForeground(TEXT_PRIMARY);
        mainPanel.add(headerLabel, BorderLayout.NORTH);

        // Content
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

        // Movie Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel titleLabel = new JLabel("Movie:");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 14));
        contentPanel.add(titleLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        movieTitleLabel = new JLabel(movie != null ? movie.getTitle() : "Unknown");
        movieTitleLabel.setForeground(ACCENT_BLUE);
        movieTitleLabel.setFont(new Font("Inter", Font.BOLD, 14));
        contentPanel.add(movieTitleLabel, gbc);

        // Resolution
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel resLabel = new JLabel("Quality:");
        resLabel.setForeground(TEXT_PRIMARY);
        resLabel.setFont(new Font("Inter", Font.BOLD, 14));
        contentPanel.add(resLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        resolutionCombo = new JComboBox<>(new String[]{
                "Original Quality",
                "1080p (Full HD)",
                "720p (HD)",
                "480p (SD)",
                "360p (Low)"
        });
        styleComboBox(resolutionCombo);
        contentPanel.add(resolutionCombo, gbc);

        // Output Location
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel outLabel = new JLabel("Save To:");
        outLabel.setForeground(TEXT_PRIMARY);
        outLabel.setFont(new Font("Inter", Font.BOLD, 14));
        contentPanel.add(outLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        outputLabel = new JLabel("Select location...");
        outputLabel.setForeground(TEXT_SECONDARY);
        outputLabel.setFont(new Font("Inter", Font.PLAIN, 13));
        contentPanel.add(outputLabel, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        selectOutputButton = createStyledButton("Browse...", false);
        selectOutputButton.setPreferredSize(new Dimension(100, 32));
        selectOutputButton.addActionListener(e -> selectOutput());
        contentPanel.add(selectOutputButton, gbc);

        // Estimated Size Info
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        JLabel infoLabel = new JLabel("<html><div style='color:#9CA3AF; font-size:11px;'>" +
                "💡 Lower quality = smaller file size and faster download</div></html>");
        contentPanel.add(infoLabel, gbc);

        // Progress Section
        gbc.gridy = 4;
        gbc.insets = new Insets(20, 8, 5, 8);
        statusLabel = new JLabel("Ready to download");
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        contentPanel.add(statusLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 8, 5, 8);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setBackground(DARK_BG);
        progressBar.setForeground(ACCENT_PURPLE);
        progressBar.setPreferredSize(new Dimension(400, 22));
        contentPanel.add(progressBar, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(5, 8, 8, 8);
        downloadedLabel = new JLabel("");
        downloadedLabel.setForeground(TEXT_SECONDARY);
        downloadedLabel.setFont(new Font("Inter", Font.PLAIN, 11));
        contentPanel.add(downloadedLabel, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(DARK_BG);

        cancelButton = createStyledButton("Cancel", false);
        cancelButton.setPreferredSize(new Dimension(100, 38));
        cancelButton.addActionListener(e -> {
            if (onCancel != null) onCancel.run();
            dispose();
        });

        downloadButton = createStyledButton("Download", true);
        downloadButton.setPreferredSize(new Dimension(140, 38));
        downloadButton.addActionListener(e -> startDownload());

        buttonPanel.add(cancelButton);
        buttonPanel.add(downloadButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // Auto-suggest output location
        suggestOutputLocation();
    }

    private void suggestOutputLocation() {
        String userHome = System.getProperty("user.home");
        File downloads = new File(userHome, "Downloads");
        if (!downloads.exists()) downloads = new File(userHome);

        String title = movie != null ? movie.getTitle() : "video";
        String safeName = title.replaceAll("[^a-zA-Z0-9.-]", "_");
        outputFile = new File(downloads, safeName + ".mp4");
        outputLabel.setText(outputFile.getName());
    }

    private void selectOutput() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Video As");

        String title = movie != null ? movie.getTitle() : "video";
        String resolution = getSelectedResolution();
        String safeName = title.replaceAll("[^a-zA-Z0-9.-]", "_");
        chooser.setSelectedFile(new File(safeName + "-" + resolution + ".mp4"));

        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("MP4 Video", "mp4"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            outputFile = chooser.getSelectedFile();
            if (!outputFile.getName().toLowerCase().endsWith(".mp4")) {
                outputFile = new File(outputFile.getAbsolutePath() + ".mp4");
            }
            outputLabel.setText(outputFile.getName());
        }
    }

    private void startDownload() {
        if (outputFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a save location.",
                    "No Location Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (onDownloadStart != null) {
            DownloadResult result = new DownloadResult(
                    movie, outputFile, getSelectedResolution()
            );
            onDownloadStart.accept(result);
        }
    }

    private String getSelectedResolution() {
        int idx = resolutionCombo.getSelectedIndex();
        return new String[]{"Original", "1080p", "720p", "480p", "360p"}[idx];
    }

    public void setOnDownloadStart(Consumer<DownloadResult> callback) {
        this.onDownloadStart = callback;
    }

    public void setOnCancel(Runnable callback) {
        this.onCancel = callback;
    }

    public void setProgress(long bytesDownloaded, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(true);
            statusLabel.setText(status);
            double mb = bytesDownloaded / (1024.0 * 1024.0);
            downloadedLabel.setText(String.format("Downloaded: %.2f MB", mb));
            setControlsEnabled(false);
        });
    }

    public void setProgressPercent(int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setValue(percent);
            progressBar.setString(percent + "%");
            statusLabel.setText(status);
        });
    }

    public void setComplete(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            progressBar.setForeground(success ? SUCCESS_GREEN : Color.RED);
            statusLabel.setText(message);
            downloadButton.setEnabled(false);
            cancelButton.setText("Close");
        });
    }

    private void setControlsEnabled(boolean enabled) {
        resolutionCombo.setEnabled(enabled);
        selectOutputButton.setEnabled(enabled);
        downloadButton.setEnabled(enabled);
        downloadButton.setText(enabled ? "Download" : "⏳ Downloading...");
    }

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

    public static class DownloadResult {
        public final MovieDto movie;
        public final File outputFile;
        public final String resolution;

        public DownloadResult(MovieDto movie, File outputFile, String resolution) {
            this.movie = movie;
            this.outputFile = outputFile;
            this.resolution = resolution;
        }
    }
}