package com.neu.finalproject.meskot.ui.dialog;

import com.neu.finalproject.meskot.dto.MovieDto;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.function.Consumer;

public class DownloadDialog extends JDialog {

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
        setSize(460, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 16));
        mainPanel.setBackground(BG_PRIMARY);
        mainPanel.setBorder(new EmptyBorder(24, 24, 24, 24));

        // Header
        JLabel headerLabel = new JLabel("Download Video");
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

        // Movie Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        JLabel titleLabel = new JLabel("Movie");
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setFont(FONT_SMALL);
        contentPanel.add(titleLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2; gbc.weightx = 1.0;
        movieTitleLabel = new JLabel(movie != null ? movie.getTitle() : "Unknown");
        movieTitleLabel.setForeground(TEXT_PRIMARY);
        movieTitleLabel.setFont(FONT_LABEL);
        contentPanel.add(movieTitleLabel, gbc);

        // Quality
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel resLabel = new JLabel("Quality");
        resLabel.setForeground(TEXT_SECONDARY);
        resLabel.setFont(FONT_SMALL);
        contentPanel.add(resLabel, gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        resolutionCombo = new JComboBox<>(new String[]{
                "Original", "1080p", "720p", "480p", "360p"
        });
        styleComboBox(resolutionCombo);
        contentPanel.add(resolutionCombo, gbc);

        // Output Location
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        JLabel outLabel = new JLabel("Save to");
        outLabel.setForeground(TEXT_SECONDARY);
        outLabel.setFont(FONT_SMALL);
        contentPanel.add(outLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 1.0;
        outputLabel = new JLabel("Select location...");
        outputLabel.setForeground(TEXT_SECONDARY);
        outputLabel.setFont(FONT_SMALL);
        contentPanel.add(outputLabel, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        selectOutputButton = createSecondaryButton("Browse");
        selectOutputButton.setPreferredSize(new Dimension(80, 32));
        selectOutputButton.addActionListener(e -> selectOutput());
        contentPanel.add(selectOutputButton, gbc);

        // Progress
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 4, 8);
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(TEXT_SECONDARY);
        statusLabel.setFont(FONT_SMALL);
        contentPanel.add(statusLabel, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(4, 8, 4, 8);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(false);
        progressBar.setBackground(BG_PRIMARY);
        progressBar.setForeground(ACCENT);
        progressBar.setPreferredSize(new Dimension(380, 6));
        progressBar.setBorderPainted(false);
        contentPanel.add(progressBar, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(4, 8, 8, 8);
        downloadedLabel = new JLabel("");
        downloadedLabel.setForeground(TEXT_SECONDARY);
        downloadedLabel.setFont(FONT_SMALL);
        contentPanel.add(downloadedLabel, gbc);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BG_PRIMARY);
        buttonPanel.setBorder(new EmptyBorder(16, 0, 0, 0));

        cancelButton = createSecondaryButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(90, 38));
        cancelButton.addActionListener(e -> {
            if (onCancel != null) onCancel.run();
            dispose();
        });

        downloadButton = createPrimaryButton("Download");
        downloadButton.setPreferredSize(new Dimension(110, 38));
        downloadButton.addActionListener(e -> startDownload());

        buttonPanel.add(cancelButton);
        buttonPanel.add(downloadButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
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
        String safeName = title.replaceAll("[^a-zA-Z0-9.-]", "_");
        chooser.setSelectedFile(new File(safeName + ".mp4"));

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
                    "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (onDownloadStart != null) {
            onDownloadStart.accept(new DownloadResult(movie, outputFile, getSelectedResolution()));
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
            downloadedLabel.setText(String.format("%.2f MB downloaded", mb));
            setControlsEnabled(false);
        });
    }

    public void setProgressPercent(int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setValue(percent);
            statusLabel.setText(status);
        });
    }

    public void setComplete(boolean success, String message) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            progressBar.setForeground(success ? SUCCESS_GREEN : ERROR_RED);
            statusLabel.setText(message);
            downloadButton.setEnabled(false);
            cancelButton.setText("Close");
        });
    }

    private void setControlsEnabled(boolean enabled) {
        resolutionCombo.setEnabled(enabled);
        selectOutputButton.setEnabled(enabled);
        downloadButton.setEnabled(enabled);
        downloadButton.setText(enabled ? "Download" : "Downloading...");
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