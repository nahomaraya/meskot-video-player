package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;
import lombok.Getter;
import lombok.Setter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * The View (V in MVP) - Grokpedia-inspired design
 * Updated with global progress bar and data source selector
 */
public class VideoPlayerUI extends JFrame {

    private PlayerPresenter presenter;
    private final CardLayout mainCardLayout;
    private final JPanel mainPanel;

    // --- Page Names (Constants) ---
    public static final String PAGE_SEARCH = "SEARCH";
    public static final String PAGE_LIST = "LIST";
    public static final String PAGE_PLAYER = "PLAYER";
    public static final String PAGE_UPLOAD = "UPLOAD";
    public static final String PAGE_LIBRARY = "LIBRARY";

    // --- Data Source Constants ---
    public static final String SOURCE_LOCAL = "Local Storage";
    public static final String SOURCE_SUPABASE = "Supabase";
    public static final String SOURCE_INTERNET_ARCHIVE = "Internet Archive";

    // --- Grokpedia Color Palette ---
    private static final Color DARK_BG = new Color(15, 15, 20);
    private static final Color CARD_BG = new Color(25, 25, 35);
    private static final Color ACCENT_PURPLE = new Color(147, 51, 234);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 245);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color INPUT_BG = new Color(30, 30, 40);
    private static final Color BORDER_COLOR = new Color(45, 45, 55);
    private static final Color SUCCESS_GREEN = new Color(34, 197, 94);
    private static final Color WARNING_ORANGE = new Color(249, 115, 22);

    // --- Common UI Components ---
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JList<MovieDto> playerMovieListUI;
    private final DefaultListModel<MovieDto> playerMovieListModel;
    private final JList<MovieDto> mainMovieListUI;
    private final DefaultListModel<MovieDto> mainMovieListModel;
    private final JList<MovieDto> libraryMovieListUI;
    private final JTextField mainSearchField;
    JLabel movieTitleLabel = new JLabel("Select a movie");
    private final JSlider volumeSlider;
    private final JButton downloadButton;
    private final JButton uploadButton;
    private final JProgressBar uploadProgressBar;

    // --- NEW: Global Status Bar Components ---
    private final JPanel globalStatusBar;
    private final JComboBox<String> dataSourceCombo;
    private final JProgressBar globalProgressBar;
    private final JLabel progressLabel;
    private final JLabel progressStatusLabel;
    private final JPanel progressPanel;

    // --- Upload Wizard Components ---
    private final CardLayout uploadCardLayout;
    private final JPanel uploadWizardPanel;
    private JButton uploadNextButton;
    private JButton uploadPrevButton;
    private final JLabel uploadStepLabel;
    @Getter @Setter
    private File selectedUploadFile;

    // --- Constructor ---
    public VideoPlayerUI() {
        super("🎬 Meskot Player");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // --- Initialize Global Status Bar Components FIRST ---
        dataSourceCombo = new JComboBox<>(new String[]{
                SOURCE_LOCAL, SOURCE_SUPABASE, SOURCE_INTERNET_ARCHIVE
        });
        globalProgressBar = new JProgressBar(0, 100);
        progressLabel = new JLabel("");
        progressStatusLabel = new JLabel("");
        progressPanel = new JPanel(new BorderLayout(5, 0));
        globalStatusBar = createGlobalStatusBar();

        // --- Menu Bar ---
        setJMenuBar(createMenuBar());

        mainCardLayout = new CardLayout();
        mainPanel = new JPanel(mainCardLayout);

        // --- Init shared components ---
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        playerMovieListModel = new DefaultListModel<>();
        playerMovieListUI = new JList<>(playerMovieListModel);
        playerMovieListUI.setCellRenderer(new MovieCellRenderer());

        mainMovieListModel = new DefaultListModel<>();
        mainMovieListUI = new JList<>(mainMovieListModel);
        mainMovieListUI.setCellRenderer(new MovieCellRenderer());
        mainMovieListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mainMovieListUI.setLayoutOrientation(JList.VERTICAL);

        libraryMovieListUI = new JList<>(mainMovieListModel);
        libraryMovieListUI.setCellRenderer(new MovieCellRenderer());
        libraryMovieListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        libraryMovieListUI.setLayoutOrientation(JList.VERTICAL);

        mainSearchField = new JTextField(40);
        volumeSlider = new JSlider(0, 100, 80);
        downloadButton = new JButton("Download");
        uploadButton = new JButton("Upload");
        uploadProgressBar = new JProgressBar(0, 100);
        uploadCardLayout = new CardLayout();
        uploadWizardPanel = new JPanel(uploadCardLayout);
        uploadNextButton = new JButton("Next >");
        uploadPrevButton = new JButton("< Previous");
        uploadStepLabel = new JLabel("Step 1: Select File");

        // --- Main Layout with Status Bar ---
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.add(globalStatusBar, BorderLayout.NORTH);
        contentWrapper.add(mainPanel, BorderLayout.CENTER);

        // --- Build Pages ---
        mainPanel.add(createSearchPage(), PAGE_SEARCH);
        mainPanel.add(createMovieListPage(), PAGE_LIST);
        mainPanel.add(createPlayerPage(), PAGE_PLAYER);
        mainPanel.add(createUploadPage(), PAGE_UPLOAD);
        mainPanel.add(createLibraryPage(), PAGE_LIBRARY);

        add(contentWrapper);
    }

    // --- NEW: Global Status Bar ---
    private JPanel createGlobalStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(CARD_BG);
        statusBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                new EmptyBorder(8, 15, 8, 15)
        ));

        // LEFT: Data Source Selector
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(CARD_BG);

        JLabel sourceLabel = new JLabel("📁 Data Source:");
        sourceLabel.setForeground(TEXT_SECONDARY);
        sourceLabel.setFont(new Font("Inter", Font.PLAIN, 13));

        dataSourceCombo.setBackground(INPUT_BG);
        dataSourceCombo.setForeground(TEXT_PRIMARY);
        dataSourceCombo.setFont(new Font("Inter", Font.PLAIN, 13));
        dataSourceCombo.setPreferredSize(new Dimension(160, 28));
        dataSourceCombo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        // Custom renderer for dark theme
        dataSourceCombo.setRenderer(new DefaultListCellRenderer() {
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

        // Add listener for data source changes
        dataSourceCombo.addActionListener(e -> {
            if (presenter != null) {
                String selected = (String) dataSourceCombo.getSelectedItem();
                presenter.onDataSourceChanged(selected);
            }
        });

        leftPanel.add(sourceLabel);
        leftPanel.add(dataSourceCombo);

        // RIGHT: Progress Bar Panel
        progressPanel.setBackground(CARD_BG);
        progressPanel.setPreferredSize(new Dimension(350, 35));
        progressPanel.setVisible(false); // Hidden by default

        JPanel progressInfoPanel = new JPanel(new BorderLayout());
        progressInfoPanel.setBackground(CARD_BG);

        progressLabel.setForeground(TEXT_PRIMARY);
        progressLabel.setFont(new Font("Inter", Font.BOLD, 12));

        progressStatusLabel.setForeground(TEXT_SECONDARY);
        progressStatusLabel.setFont(new Font("Inter", Font.PLAIN, 11));

        progressInfoPanel.add(progressLabel, BorderLayout.WEST);
        progressInfoPanel.add(progressStatusLabel, BorderLayout.EAST);

        globalProgressBar.setPreferredSize(new Dimension(300, 8));
        globalProgressBar.setBackground(DARK_BG);
        globalProgressBar.setForeground(ACCENT_PURPLE);
        globalProgressBar.setBorderPainted(false);
        globalProgressBar.setStringPainted(false);

        // Cancel button for long operations
        JButton cancelButton = new JButton("✕");
        cancelButton.setFont(new Font("Inter", Font.BOLD, 12));
        cancelButton.setForeground(TEXT_SECONDARY);
        cancelButton.setBackground(CARD_BG);
        cancelButton.setBorder(new EmptyBorder(2, 8, 2, 8));
        cancelButton.setFocusPainted(false);
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> {
            if (presenter != null) {
                presenter.onCancelOperation();
            }
        });

        JPanel progressBarWrapper = new JPanel(new BorderLayout(5, 0));
        progressBarWrapper.setBackground(CARD_BG);
        progressBarWrapper.add(globalProgressBar, BorderLayout.CENTER);
        progressBarWrapper.add(cancelButton, BorderLayout.EAST);

        progressPanel.add(progressInfoPanel, BorderLayout.NORTH);
        progressPanel.add(progressBarWrapper, BorderLayout.SOUTH);

        statusBar.add(leftPanel, BorderLayout.WEST);
        statusBar.add(progressPanel, BorderLayout.EAST);

        return statusBar;
    }

    // --- Public Methods for Global Progress Bar ---

    /**
     * Show progress for upload/download operations
     * @param type "UPLOAD" or "DOWNLOAD"
     * @param filename The file being processed
     * @param percent Progress percentage (0-100)
     * @param status Additional status text
     */
    public void showGlobalProgress(String type, String filename, int percent, String status) {
        SwingUtilities.invokeLater(() -> {
            progressPanel.setVisible(true);

            String icon = "UPLOAD".equals(type) ? "⬆️" : "⬇️";
            String truncatedName = filename.length() > 25
                    ? filename.substring(0, 22) + "..."
                    : filename;

            progressLabel.setText(icon + " " + truncatedName);
            progressStatusLabel.setText(status);

            if (percent < 0) {
                globalProgressBar.setIndeterminate(true);
            } else {
                globalProgressBar.setIndeterminate(false);
                globalProgressBar.setValue(percent);
            }

            // Color based on type
            globalProgressBar.setForeground("UPLOAD".equals(type) ? ACCENT_BLUE : ACCENT_PURPLE);
        });
    }

    /**
     * Show completion state for progress bar
     */
    public void showGlobalProgressComplete(String message, boolean success) {
        SwingUtilities.invokeLater(() -> {
            progressLabel.setText(success ? "✓ " + message : "✗ " + message);
            progressStatusLabel.setText("");
            globalProgressBar.setIndeterminate(false);
            globalProgressBar.setValue(100);
            globalProgressBar.setForeground(success ? SUCCESS_GREEN : WARNING_ORANGE);

            // Auto-hide after 3 seconds
            Timer hideTimer = new Timer(3000, e -> hideGlobalProgress());
            hideTimer.setRepeats(false);
            hideTimer.start();
        });
    }

    /**
     * Hide the global progress bar
     */
    public void hideGlobalProgress() {
        SwingUtilities.invokeLater(() -> {
            progressPanel.setVisible(false);
            globalProgressBar.setValue(0);
            globalProgressBar.setIndeterminate(false);
            progressLabel.setText("");
            progressStatusLabel.setText("");
        });
    }

    /**
     * Get the currently selected data source
     */
    public String getSelectedDataSource() {
        return (String) dataSourceCombo.getSelectedItem();
    }

    /**
     * Set the data source programmatically
     */
    public void setSelectedDataSource(String source) {
        dataSourceCombo.setSelectedItem(source);
    }

    // --- Menu Bar ---
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(DARK_BG);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));

        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(TEXT_PRIMARY);
        fileMenu.setFont(new Font("Inter", Font.PLAIN, 14));

        JMenuItem openItem = new JMenuItem("Open Video File...");
        styleMenuItem(openItem);
        openItem.addActionListener(e -> {
            if (presenter != null) presenter.onOpenLocalVideo();
        });

        JMenuItem exitItem = new JMenuItem("Exit");
        styleMenuItem(exitItem);
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu navMenu = new JMenu("Navigate");
        navMenu.setForeground(TEXT_PRIMARY);
        navMenu.setFont(new Font("Inter", Font.PLAIN, 14));

        JMenuItem searchItem = new JMenuItem("Search Home");
        styleMenuItem(searchItem);
        searchItem.addActionListener(e -> {
            if (presenter != null) presenter.onNavigate(PAGE_SEARCH);
        });

        JMenuItem libraryItem = new JMenuItem("Full Library");
        styleMenuItem(libraryItem);
        libraryItem.addActionListener(e -> {
            if (presenter != null) presenter.onLoadLibrary();
        });

        JMenuItem uploadItem = new JMenuItem("Upload Wizard");
        styleMenuItem(uploadItem);
        uploadItem.addActionListener(e -> {
            if (presenter != null) presenter.onNavigate(PAGE_UPLOAD);
        });

        navMenu.add(searchItem);
        navMenu.add(libraryItem);
        navMenu.add(uploadItem);

        menuBar.add(fileMenu);
        menuBar.add(navMenu);

        return menuBar;
    }

    private void styleMenuItem(JMenuItem item) {
        item.setBackground(CARD_BG);
        item.setForeground(TEXT_PRIMARY);
        item.setFont(new Font("Inter", Font.PLAIN, 14));
        item.setBorder(new EmptyBorder(5, 10, 5, 10));
        item.setOpaque(true);
    }

    // --- Page Factory Methods ---
    // (Keep all existing page creation methods: createSearchPage, createLibraryPage,
    //  createPlayerPage, createMovieListPage, createUploadPage)
    // ... [TRUNCATED FOR BREVITY - Keep your existing implementations]

    private JPanel createSearchPage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(DARK_BG);
        GridBagConstraints gbc = new GridBagConstraints();

        JLabel title = new JLabel("<html><div style='text-align: center;'>" +
                "<span style='font-size: 72px; font-weight: 900;'>Meskot Video Player</span><br>" +
                "</div></html>");
        title.setFont(new Font("SF Pro Display", Font.BOLD, 72));
        title.setForeground(TEXT_PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        mainSearchField.setPreferredSize(new Dimension(700, 60));
        mainSearchField.setFont(new Font("Inter", Font.PLAIN, 18));
        mainSearchField.setForeground(TEXT_PRIMARY);
        mainSearchField.setCaretColor(TEXT_PRIMARY);
        mainSearchField.setBackground(INPUT_BG);
        mainSearchField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 15),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        JButton searchButton = createGrokButton("Search Movies", true);
        JButton uploadNavButton = createGrokButton("Upload Movie", false);
        JButton libraryNavButton = createGrokButton("View Full Library", false);
        libraryNavButton.setPreferredSize(new Dimension(200, 50));

        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 10, 20);
        gbc.gridy = 0;
        page.add(title, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 20, 20, 5);
        page.add(mainSearchField, gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.insets = new Insets(20, 5, 20, 20);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setBackground(DARK_BG);
        buttonPanel.add(searchButton);
        page.add(buttonPanel, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 20, 10, 20);

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actionButtonsPanel.setBackground(DARK_BG);
        actionButtonsPanel.add(libraryNavButton);
        actionButtonsPanel.add(uploadNavButton);
        page.add(actionButtonsPanel, gbc);

        JLabel footerLabel = new JLabel("Source code");
        footerLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        footerLabel.setForeground(TEXT_SECONDARY);
        gbc.gridy = 4;
        gbc.insets = new Insets(40, 20, 20, 20);
        page.add(footerLabel, gbc);

        searchButton.addActionListener(e -> presenter.onSearch());
        mainSearchField.addActionListener(e -> presenter.onSearch());
        uploadNavButton.addActionListener(e -> presenter.onNavigate(PAGE_UPLOAD));
        libraryNavButton.addActionListener(e -> presenter.onLoadLibrary());

        return page;
    }

    private JPanel createLibraryPage() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBorder(new EmptyBorder(10, 10, 10, 10));
        page.setBackground(DARK_BG);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        topPanel.setBackground(DARK_BG);

        JLabel libraryLabel = new JLabel("🎬 Full Movie Library");
        libraryLabel.setForeground(TEXT_PRIMARY);
        libraryLabel.setFont(new Font("Inter", Font.BOLD, 24));
        topPanel.add(libraryLabel);

        JButton homeButton = createGrokButton("Home", false);
        homeButton.setPreferredSize(new Dimension(120, 40));
        topPanel.add(homeButton);

        libraryMovieListUI.setBackground(CARD_BG);
        libraryMovieListUI.setForeground(TEXT_PRIMARY);

        JScrollPane scrollPane = new JScrollPane(libraryMovieListUI);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        page.add(topPanel, BorderLayout.NORTH);
        page.add(scrollPane, BorderLayout.CENTER);

        homeButton.addActionListener(e -> presenter.onNavigate(PAGE_SEARCH));
        libraryMovieListUI.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    presenter.onMovieSelected(libraryMovieListUI.getSelectedValue());
                }
            }
        });

        return page;
    }

    private JButton createGrokButton(String text, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isPrimary) {
                    GradientPaint gradient = new GradientPaint(0, 0, ACCENT_PURPLE,
                            getWidth(), getHeight(), ACCENT_BLUE);
                    g2.setPaint(gradient);
                } else {
                    g2.setColor(CARD_BG);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (!isPrimary) {
                    g2.setColor(BORDER_COLOR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(new Font("Inter", Font.BOLD, 16));
        button.setForeground(TEXT_PRIMARY);
        button.setPreferredSize(new Dimension(200, 50));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private final Color color;
        private final int radius;
        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
        @Override
        public Insets getBorderInsets(Component c) { return new Insets(2, 2, 2, 2); }
    }

    public void updateNowPlayingLabel(String title) {
        movieTitleLabel.setText(title);
    }

// ... Continue in next artifact due to length
// === Continue from VideoPlayerUI.java Part 1 ===

    private JPanel createPlayerPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(DARK_BG);

        JScrollPane listScrollPane = new JScrollPane(playerMovieListUI);
        listScrollPane.setPreferredSize(new Dimension(300, 600));
        playerMovieListUI.setBackground(CARD_BG);
        playerMovieListUI.setForeground(TEXT_PRIMARY);

        JPanel nowPlayingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        nowPlayingPanel.setBackground(CARD_BG);
        nowPlayingPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel nowPlayingLabel = new JLabel("Now Playing: ");
        nowPlayingLabel.setForeground(TEXT_SECONDARY);
        nowPlayingLabel.setFont(new Font("Inter", Font.BOLD, 14));

        movieTitleLabel.setText("Select a movie");
        movieTitleLabel.setForeground(TEXT_PRIMARY);
        movieTitleLabel.setFont(new Font("Inter", Font.PLAIN, 14));

        nowPlayingPanel.add(nowPlayingLabel);
        nowPlayingPanel.add(movieTitleLabel);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(CARD_BG);
        controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel timePanel = new JPanel(new BorderLayout());
        timePanel.setBackground(CARD_BG);

        JLabel currentTimeLabel = new JLabel("00:00");
        currentTimeLabel.setForeground(TEXT_PRIMARY);
        currentTimeLabel.setFont(new Font("Inter", Font.PLAIN, 12));

        JLabel durationLabel = new JLabel("00:00");
        durationLabel.setForeground(TEXT_PRIMARY);
        durationLabel.setFont(new Font("Inter", Font.PLAIN, 12));

        timePanel.add(currentTimeLabel, BorderLayout.WEST);
        timePanel.add(durationLabel, BorderLayout.EAST);

        JSlider seekSlider = new JSlider(0, 100, 0);
        seekSlider.setBackground(CARD_BG);
        seekSlider.setForeground(ACCENT_PURPLE);
        seekSlider.setPaintTicks(false);
        seekSlider.setPaintLabels(false);

        Timer seekTimer = new Timer(500, e -> {
            if (mediaPlayerComponent.mediaPlayer().status().isPlaying()) {
                long current = presenter.getCurrentTime();
                long duration = presenter.getDuration();
                if (duration > 0) {
                    int position = (int) ((current * 100) / duration);
                    seekSlider.setValue(position);
                    currentTimeLabel.setText(presenter.formatTime(current));
                    durationLabel.setText(presenter.formatTime(duration));
                }
            }
        });
        seekTimer.start();

        seekSlider.addChangeListener(e -> {
            if (seekSlider.getValueIsAdjusting()) {
                long duration = presenter.getDuration();
                if (duration > 0) {
                    long seekTime = (seekSlider.getValue() * duration) / 100;
                    presenter.onSeek(seekTime);
                }
            }
        });

        JPanel playbackPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        playbackPanel.setBackground(CARD_BG);

        JButton playButton = new JButton("▶");
        JButton pauseButton = new JButton("⏸");
        JButton stopButton = new JButton("⏹");
        JButton skipBackwardButton = new JButton("⏪ -10s");
        JButton skipForwardButton = new JButton("⏩ +10s");

        for (JButton btn : new JButton[]{playButton, pauseButton, stopButton,
                skipForwardButton, skipBackwardButton}) {
            btn.setBackground(CARD_BG);
            btn.setForeground(TEXT_PRIMARY);
            btn.setFocusPainted(false);
            btn.setPreferredSize(new Dimension(80, 35));
        }

        playButton.setPreferredSize(new Dimension(60, 35));
        pauseButton.setPreferredSize(new Dimension(60, 35));
        stopButton.setPreferredSize(new Dimension(60, 35));

        playbackPanel.add(skipBackwardButton);
        playbackPanel.add(playButton);
        playbackPanel.add(pauseButton);
        playbackPanel.add(stopButton);
        playbackPanel.add(skipForwardButton);

        JPanel volumeDownloadPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        volumeDownloadPanel.setBackground(CARD_BG);

        JLabel volumeLabel = new JLabel("🔊");
        volumeLabel.setForeground(TEXT_PRIMARY);
        volumeSlider.setBackground(CARD_BG);
        volumeSlider.setPreferredSize(new Dimension(120, 25));

        downloadButton.setText("📥 Download");
        downloadButton.setFont(new Font("Inter", Font.BOLD, 14));
        downloadButton.setForeground(TEXT_PRIMARY);
        downloadButton.setBackground(CARD_BG);
        downloadButton.setPreferredSize(new Dimension(140, 35));
        downloadButton.setFocusPainted(false);
        downloadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        volumeDownloadPanel.add(volumeLabel);
        volumeDownloadPanel.add(volumeSlider);
        volumeDownloadPanel.add(downloadButton);

        controlPanel.add(timePanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(seekSlider);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(playbackPanel);
        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(volumeDownloadPanel);

        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.add(nowPlayingPanel, BorderLayout.NORTH);
        playerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
        playerPanel.add(controlPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                listScrollPane, playerPanel);
        splitPane.setDividerLocation(310);
        splitPane.setResizeWeight(0);

        page.add(splitPane, BorderLayout.CENTER);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setBackground(CARD_BG);
        JButton homeButton = createGrokButton("Home", false);
        JButton backToListButton = createGrokButton("Back to List", false);
        homeButton.setPreferredSize(new Dimension(120, 40));
        backToListButton.setPreferredSize(new Dimension(150, 40));
        topBar.add(homeButton);
        topBar.add(backToListButton);
        page.add(topBar, BorderLayout.NORTH);

        homeButton.addActionListener(e -> presenter.onNavigate(PAGE_SEARCH));
        backToListButton.addActionListener(e -> presenter.onNavigate(PAGE_LIST));
        playButton.addActionListener(e -> presenter.onPlay());
        pauseButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().pause());
        stopButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().stop());
        skipForwardButton.addActionListener(e -> presenter.onSkip(10));
        skipBackwardButton.addActionListener(e -> presenter.onSkip(-10));
        downloadButton.addActionListener(e -> presenter.onDownloadWithResolution());

        volumeSlider.addChangeListener(e ->
                mediaPlayerComponent.mediaPlayer().audio().setVolume(volumeSlider.getValue())
        );

        playerMovieListUI.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    presenter.onPlay();
                }
            }
        });

        return page;
    }

    private JPanel createMovieListPage() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBorder(new EmptyBorder(10, 10, 10, 10));
        page.setBackground(DARK_BG);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(DARK_BG);
        JLabel resultsLabel = new JLabel("Search Results:");
        resultsLabel.setForeground(TEXT_PRIMARY);
        resultsLabel.setFont(new Font("Inter", Font.BOLD, 18));
        topPanel.add(resultsLabel);

        JButton homeButton = createGrokButton("Home", false);
        homeButton.setPreferredSize(new Dimension(120, 40));
        topPanel.add(homeButton);

        mainMovieListUI.setBackground(CARD_BG);
        mainMovieListUI.setForeground(TEXT_PRIMARY);
        JScrollPane scrollPane = new JScrollPane(mainMovieListUI);
        scrollPane.getViewport().setBackground(CARD_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        page.add(topPanel, BorderLayout.NORTH);
        page.add(scrollPane, BorderLayout.CENTER);

        homeButton.addActionListener(e -> presenter.onNavigate(PAGE_SEARCH));
        mainMovieListUI.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    presenter.onMovieSelected(mainMovieListUI.getSelectedValue());
                }
            }
        });

        return page;
    }

    private JPanel createUploadPage() {
        JPanel page = new JPanel(new BorderLayout(10, 10));
        page.setBorder(new EmptyBorder(20, 20, 20, 20));
        page.setBackground(DARK_BG);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_BG);
        uploadStepLabel.setForeground(TEXT_PRIMARY);
        uploadStepLabel.setFont(new Font("Inter", Font.BOLD, 20));
        headerPanel.add(uploadStepLabel, BorderLayout.CENTER);

        JButton homeButton = createGrokButton("Home", false);
        homeButton.setPreferredSize(new Dimension(120, 40));
        headerPanel.add(homeButton, BorderLayout.WEST);
        page.add(headerPanel, BorderLayout.NORTH);

        JPanel step1 = new JPanel(new GridBagLayout());
        step1.setBackground(DARK_BG);
        JButton selectFileButton = createGrokButton("Select Video File", true);
        selectFileButton.setPreferredSize(new Dimension(250, 60));
        step1.add(selectFileButton);

        JPanel step2 = new JPanel(new GridBagLayout());
        step2.setBackground(DARK_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        step2.add(titleLabel, gbc);
        gbc.gridx = 1;
        JTextField titleField = new JTextField(30);
        titleField.setBackground(INPUT_BG);
        titleField.setForeground(TEXT_PRIMARY);
        titleField.setCaretColor(TEXT_PRIMARY);
        titleField.setFont(new Font("Inter", Font.PLAIN, 14));
        step2.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        JLabel resLabel = new JLabel("Resolution:");
        resLabel.setForeground(TEXT_PRIMARY);
        resLabel.setFont(new Font("Inter", Font.PLAIN, 16));
        step2.add(resLabel, gbc);
        gbc.gridx = 1;
        JComboBox<String> resCombo = new JComboBox<>(new String[]{"720p", "480p", "1080p"});
        resCombo.setBackground(INPUT_BG);
        resCombo.setForeground(TEXT_PRIMARY);
        step2.add(resCombo, gbc);

        JPanel step3 = new JPanel(new BorderLayout());
        step3.setBackground(DARK_BG);
        step3.add(uploadProgressBar, BorderLayout.CENTER);

        uploadWizardPanel.add(step1, "STEP_1");
        uploadWizardPanel.add(step2, "STEP_2");
        uploadWizardPanel.add(step3, "STEP_3");

        page.add(uploadWizardPanel, BorderLayout.CENTER);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setBackground(DARK_BG);
        uploadPrevButton = createGrokButton("< Previous", false);
        uploadNextButton = createGrokButton("Next >", true);
        uploadPrevButton.setPreferredSize(new Dimension(140, 45));
        uploadNextButton.setPreferredSize(new Dimension(140, 45));
        navPanel.add(uploadPrevButton);
        navPanel.add(uploadNextButton);
        page.add(navPanel, BorderLayout.SOUTH);

        homeButton.addActionListener(e -> presenter.onNavigate(PAGE_SEARCH));
        selectFileButton.addActionListener(e -> presenter.onUploadSelectFile());
        uploadNextButton.addActionListener(e ->
                presenter.onUploadWizardNext(titleField.getText(), (String)resCombo.getSelectedItem())
        );
        uploadPrevButton.addActionListener(e -> presenter.onUploadWizardPrevious());

        return page;
    }

    // --- Public Methods for Presenter ---
    public void attachPresenter(PlayerPresenter presenter) { this.presenter = presenter; }
    public void showPage(String pageName) { mainCardLayout.show(mainPanel, pageName); }

    public void updateMainMovieList(List<MovieDto> movies) {
        mainMovieListModel.clear();
        for (MovieDto m : movies) mainMovieListModel.addElement(m);
    }

    public void updatePlayerMovieList(List<MovieDto> movies) {
        playerMovieListModel.clear();
        if (movies != null) for (MovieDto m : movies) playerMovieListModel.addElement(m);
    }

    public void setUploadWizardPage(String step) {
        uploadCardLayout.show(uploadWizardPanel, step);
        if ("STEP_1".equals(step)) {
            uploadStepLabel.setText("Step 1: Select File");
            uploadPrevButton.setEnabled(false);
            uploadNextButton.setEnabled(true);
            uploadNextButton.setText("Next >");
        } else if ("STEP_2".equals(step)) {
            uploadStepLabel.setText("Step 2: Enter Details");
            uploadPrevButton.setEnabled(true);
            uploadNextButton.setEnabled(true);
            uploadNextButton.setText("Upload");
        } else if ("STEP_3".equals(step)) {
            uploadStepLabel.setText("Step 3: Uploading...");
            uploadPrevButton.setEnabled(false);
            uploadNextButton.setEnabled(false);
        }
    }

    public void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    public void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    public void setDownloadButtonState(boolean enabled, String text) {
        downloadButton.setEnabled(enabled);
        downloadButton.setText(text);
    }
    public void setUploadProgress(String text, int value, boolean indeterminate) {
        uploadProgressBar.setVisible(true);
        uploadProgressBar.setIndeterminate(indeterminate);
        uploadProgressBar.setString(text);
        uploadProgressBar.setValue(value);
    }
    public void hideUploadProgressAfterDelay(String message) {
        uploadProgressBar.setString(message);
        uploadProgressBar.setIndeterminate(false);
        Timer hideTimer = new Timer(5000, e -> uploadProgressBar.setVisible(false));
        hideTimer.setRepeats(false);
        hideTimer.start();
    }
    public File showSaveDialog(String suggestedName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(suggestedName));
        return chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION
                ? chooser.getSelectedFile() : null;
    }
    public File showOpenDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a video to upload");
        return fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION
                ? fileChooser.getSelectedFile() : null;
    }
    public File showLocalVideoDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open Video File for Playback");
        return fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION
                ? fileChooser.getSelectedFile() : null;
    }
    public String showInputDialog(String message, String title) {
        return JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
    }
    public EmbeddedMediaPlayerComponent getMediaPlayer() { return mediaPlayerComponent; }
    public MovieDto getSelectedMovieFromList() { return playerMovieListUI.getSelectedValue(); }
    public String getSearchQuery() { return mainSearchField.getText().trim(); }
    public void release() { mediaPlayerComponent.release(); }
}