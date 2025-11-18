package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.ui.PlayerPresenter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.List;

/**
 * The View (V in MVP) - Grokpedia-inspired design
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

    // --- Grokpedia Color Palette ---
    private static final Color DARK_BG = new Color(15, 15, 20);
    private static final Color CARD_BG = new Color(25, 25, 35);
    private static final Color ACCENT_PURPLE = new Color(147, 51, 234);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color TEXT_PRIMARY = new Color(240, 240, 245);
    private static final Color TEXT_SECONDARY = new Color(156, 163, 175);
    private static final Color INPUT_BG = new Color(30, 30, 40);
    private static final Color BORDER_COLOR = new Color(45, 45, 55);

    // --- Common UI Components (used by Presenter) ---
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JList<MovieDto> playerMovieListUI;
    private final DefaultListModel<MovieDto> playerMovieListModel;
    private final JList<MovieDto> mainMovieListUI;
    private final DefaultListModel<MovieDto> mainMovieListModel;

    private final JTextField mainSearchField;
    private final JSlider volumeSlider;
    private final JButton downloadButton;
    private final JButton uploadButton;
    private final JProgressBar uploadProgressBar;

    // --- Upload Wizard Components ---
    private final CardLayout uploadCardLayout;
    private final JPanel uploadWizardPanel;
    private JButton uploadNextButton;
    private JButton uploadPrevButton;
    private final JLabel uploadStepLabel;
    private File selectedUploadFile;

    // --- Constructor ---
    public VideoPlayerUI() {
        super("🎬 Meskot Player");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

        // --- Build Pages ---
        mainPanel.add(createSearchPage(), PAGE_SEARCH);
        mainPanel.add(createMovieListPage(), PAGE_LIST);
        mainPanel.add(createPlayerPage(), PAGE_PLAYER);
        mainPanel.add(createUploadPage(), PAGE_UPLOAD);

        add(mainPanel);
    }

    // --- Page Factory Methods ---

    private JPanel createSearchPage() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(DARK_BG);
        GridBagConstraints gbc = new GridBagConstraints();

        // --- Hero Title with Gradient Effect (simulated with shadow) ---
        JLabel title = new JLabel("<html><div style='text-align: center;'>" +
                "<span style='font-size: 72px; font-weight: 900; letter-spacing: -2px;'>Meskot</span><br>" +
                "<span style='font-size: 36px; font-weight: 300; color: #9CA3AF;'>Cinema</span>" +
                "</div></html>");
        title.setFont(new Font("SF Pro Display", Font.BOLD, 72));
        title.setForeground(TEXT_PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        // --- Subtitle ---
        JLabel subtitle = new JLabel("Discover and stream your favorite movies");
        subtitle.setFont(new Font("Inter", Font.PLAIN, 18));
        subtitle.setForeground(TEXT_SECONDARY);
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);

        // --- Custom Search Field with Grokpedia styling ---
        RoundedSearchField searchField = new RoundedSearchField();
        searchField.setPreferredSize(new Dimension(700, 60));
        searchField.setFont(new Font("Inter", Font.PLAIN, 18));
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(TEXT_PRIMARY);
        searchField.setBackground(INPUT_BG);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Replace the old mainSearchField with this custom one
        mainSearchField.setPreferredSize(new Dimension(700, 60));
        mainSearchField.setFont(new Font("Inter", Font.PLAIN, 18));
        mainSearchField.setForeground(TEXT_PRIMARY);
        mainSearchField.setCaretColor(TEXT_PRIMARY);
        mainSearchField.setBackground(INPUT_BG);
        mainSearchField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 15),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // --- Action Buttons with Gradient Style ---
        JButton searchButton = createGrokButton("Search Movies", true);
        JButton uploadNavButton = createGrokButton("Upload Movie", false);

        // --- Layout Assembly ---
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 20, 10, 20);
        gbc.gridy = 0;
        page.add(title, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 30, 20);
        page.add(subtitle, gbc);

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
        page.add(uploadNavButton, gbc);

        // --- Footer Info ---
        JLabel footerLabel = new JLabel("Source code");
        footerLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        footerLabel.setForeground(TEXT_SECONDARY);
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(40, 20, 20, 20);
        gbc.anchor = GridBagConstraints.CENTER;
        page.add(footerLabel, gbc);

        // Attach listeners
        searchButton.addActionListener(e -> presenter.onSearch());
        mainSearchField.addActionListener(e -> presenter.onSearch());
        uploadNavButton.addActionListener(e -> presenter.onNavigate(PAGE_UPLOAD));

        return page;
    }

    // --- Helper: Create Grokpedia-style button ---
    private JButton createGrokButton(String text, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isPrimary) {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, ACCENT_PURPLE,
                            getWidth(), getHeight(), ACCENT_BLUE
                    );
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

    // --- Helper: Rounded Border ---
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
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }

    // --- Rounded Search Field ---
    private class RoundedSearchField extends JTextField {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            g2.dispose();
            super.paintComponent(g);
        }
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

        // Attach listeners
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

    private JPanel createPlayerPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(DARK_BG);

        JScrollPane listScrollPane = new JScrollPane(playerMovieListUI);
        listScrollPane.setPreferredSize(new Dimension(300, 600));
        playerMovieListUI.setBackground(CARD_BG);
        playerMovieListUI.setForeground(TEXT_PRIMARY);

        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(CARD_BG);
        JButton playButton = new JButton("▶ Play");
        JButton pauseButton = new JButton("⏸ Pause");
        JButton stopButton = new JButton("⏹ Stop");
        JButton skipForwardButton = new JButton("⏩ +10s");
        JButton skipBackwardButton = new JButton("⏪ -10s");

        for (JButton btn : new JButton[]{playButton, pauseButton, stopButton, skipForwardButton, skipBackwardButton}) {
            btn.setBackground(CARD_BG);
            btn.setForeground(TEXT_PRIMARY);
            btn.setFocusPainted(false);
        }

        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(skipBackwardButton);
        controlPanel.add(skipForwardButton);
        JLabel volumeLabel = new JLabel("🔊");
        volumeLabel.setForeground(TEXT_PRIMARY);
        controlPanel.add(volumeLabel);
        controlPanel.add(volumeSlider);

        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
        playerPanel.add(controlPanel, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, playerPanel);
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

    public void attachPresenter(PlayerPresenter presenter) {
        this.presenter = presenter;
    }

    public void showPage(String pageName) {
        mainCardLayout.show(mainPanel, pageName);
    }

    public void updateMainMovieList(List<MovieDto> movies) {
        mainMovieListModel.clear();
        for (MovieDto m : movies) {
            mainMovieListModel.addElement(m);
        }
    }

    public void updatePlayerMovieList(List<MovieDto> movies) {
        playerMovieListModel.clear();
        for (MovieDto m : movies) {
            playerMovieListModel.addElement(m);
        }
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

    public void setSelectedUploadFile(File file) {
        this.selectedUploadFile = file;
    }

    public File getSelectedUploadFile() {
        return this.selectedUploadFile;
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
        javax.swing.Timer hideTimer = new javax.swing.Timer(5000, e -> {
            uploadProgressBar.setVisible(false);
        });
        hideTimer.setRepeats(false);
        hideTimer.start();
    }

    public File showSaveDialog(String suggestedName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(suggestedName));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }

    public File showOpenDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a video to upload");
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public String showInputDialog(String message, String title) {
        return JOptionPane.showInputDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
    }

    public EmbeddedMediaPlayerComponent getMediaPlayer() {
        return mediaPlayerComponent;
    }

    public MovieDto getSelectedMovieFromList() {
        return playerMovieListUI.getSelectedValue();
    }

    public String getSearchQuery() {
        return mainSearchField.getText().trim();
    }

    public void release() {
        mediaPlayerComponent.release();
    }
}