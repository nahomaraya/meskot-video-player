package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.ui.PlayerPresenter;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * The View (V in MVP).
 * This class is now "dumb." It is only responsible for creating, laying out,
 * and displaying components. It delegates all actions to the Presenter.
 */
public class VideoPlayerUI extends JFrame {

    private PlayerPresenter presenter;

    // --- UI Components ---
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JList<MovieDto> movieListUI;
    private final DefaultListModel<MovieDto> movieListModel;
    private final JComboBox<String> movieDropdown;
    private final JTextField searchField;
    private final JSlider volumeSlider;
    private final JButton downloadButton;
    private final JButton uploadButton;
    private final JProgressBar uploadProgressBar;
    JButton playButton = new JButton("▶ Play");
    JButton pauseButton = new JButton("⏸ Pause");
    JButton stopButton = new JButton("⏹ Stop");
    JButton skipForwardButton = new JButton("⏩ +10s");
    JButton skipBackwardButton = new JButton("⏪ -10s");
    JButton searchButton = new JButton("Search");

    public VideoPlayerUI() {
        super("🎬 Meskot Player");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Init Components ---
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        movieListModel = new DefaultListModel<>();
        movieListUI = new JList<>(movieListModel);
        movieListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieListUI.setCellRenderer(new MovieCellRenderer());
        JScrollPane listScrollPane = new JScrollPane(movieListUI);
        listScrollPane.setPreferredSize(new Dimension(220, 600));

        searchField = new JTextField(20);
        downloadButton = new JButton("Download");
        uploadButton = new JButton("Upload");
        uploadProgressBar = new JProgressBar(0, 100);
        movieDropdown = new JComboBox<>();
        volumeSlider = new JSlider(0, 100, 80);

        // --- Layout ---
        // Top Panel (Search, Download, Upload)
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(downloadButton);
        searchPanel.add(uploadButton);

        uploadProgressBar.setStringPainted(true);
        uploadProgressBar.setVisible(false); // Hide until needed

        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(uploadProgressBar, BorderLayout.SOUTH);

        // Control Panel (Playback)
        JPanel controlPanel = new JPanel();
        controlPanel.add(movieDropdown);
        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(skipBackwardButton);
        controlPanel.add(skipForwardButton);
        controlPanel.add(new JLabel("🔊"));
        controlPanel.add(volumeSlider);

        // Player Panel
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
        playerPanel.add(controlPanel, BorderLayout.SOUTH);

        // Split Pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, playerPanel);
        splitPane.setDividerLocation(230);
        splitPane.setResizeWeight(0);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // --- Attach Listeners to Presenter ---
        // This method will be called by Application.java to hook up the actions
    }

    /**
     * Connects all UI component actions to the presenter.
     * This is the core of the MVP pattern.
     */
    public void attachPresenter(PlayerPresenter presenter) {
        this.presenter = presenter;

        // Playback Actions
        playButton.addActionListener(e -> presenter.onPlay());
        pauseButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().pause());
        stopButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().stop());
        skipForwardButton.addActionListener(e -> presenter.onSkip(10));
        skipBackwardButton.addActionListener(e -> presenter.onSkip(-10));
        volumeSlider.addChangeListener(e ->
                mediaPlayerComponent.mediaPlayer().audio().setVolume(volumeSlider.getValue())
        );

        // Movie List Actions
        movieListUI.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    presenter.onPlay();
                }
            }
        });

        // Top Panel Actions
        searchButton.addActionListener(e -> presenter.onSearch());
        downloadButton.addActionListener(e -> presenter.onDownload());
        uploadButton.addActionListener(e -> presenter.onUpload());
    }

    // --- Public Methods for Presenter to Call ---

    public void updateMovieList(List<MovieDto> movies) {
        movieListModel.clear();
        movieDropdown.removeAllItems();
        for (MovieDto m : movies) {
            movieListModel.addElement(m);
            movieDropdown.addItem(m.getTitle());
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

    public void setUploadButtonState(boolean enabled) {
        uploadButton.setEnabled(enabled);
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
        hideTimer.setRepeats(false); // Make it run only once
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
        return movieListUI.getSelectedValue();
    }

    public String getSearchQuery() {
        return searchField.getText().trim();
    }

    public void release() {
        mediaPlayerComponent.release();
    }
}