package com.neu.finalproject.meskot.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.model.Movie;
import lombok.Getter;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.stream.Collectors;
import java.util.List;
public class VideoPlayerUI extends JFrame {
    // Simple inner model class for deserialization
//    @Getter
//    static class MovieItem {
//        private Long id;
//        private String title;
//    }

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JComboBox<String> movieDropdown;
    ;
    private List<MovieDto> movieList;
    private final JTextField searchField;
    private final JSlider volumeSlider;
    private final DefaultListModel<MovieDto> movieListModel;
    private JList<MovieDto> movieListUI;



    public VideoPlayerUI() {
        super("🎬 Meskot Player");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- Media Player ---
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        // --- Movie List ---
        movieListModel = new DefaultListModel<>();
        movieListUI = new JList<>(movieListModel);
        movieListUI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        movieListUI.setCellRenderer(new MovieCellRenderer());
        JScrollPane listScrollPane = new JScrollPane(movieListUI);
        listScrollPane.setPreferredSize(new Dimension(220, 600));

        // Double-click to play
        movieListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    playSelectedMovie();
                }
            }
        });

        // --- Top Panel (Search + Download) ---
        JPanel topPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        JButton downloadButton = new JButton("Download");

        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(downloadButton);

        // --- Control Panel ---
        JPanel controlPanel = new JPanel();
        movieDropdown = new JComboBox<>();
        JButton playButton = new JButton("▶ Play");
        JButton pauseButton = new JButton("⏸ Pause");
        JButton stopButton = new JButton("⏹ Stop");
        JButton skipForwardButton = new JButton("⏩ +10s");
        JButton skipBackwardButton = new JButton("⏪ -10s");
        volumeSlider = new JSlider(0, 100, 80);

        controlPanel.add(movieDropdown);
        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);
        controlPanel.add(skipBackwardButton);
        controlPanel.add(skipForwardButton);
        controlPanel.add(new JLabel("🔊"));
        controlPanel.add(volumeSlider);

        // --- Player Panel (Media Player + Controls) ---
        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.add(mediaPlayerComponent, BorderLayout.CENTER);
        playerPanel.add(controlPanel, BorderLayout.SOUTH);

        // --- Split Pane (Movie List | Player) ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, playerPanel);
        splitPane.setDividerLocation(230);
        splitPane.setResizeWeight(0);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // --- Actions ---
        playButton.addActionListener(e -> playSelectedMovie());
        pauseButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().pause());
        stopButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().stop());
        skipForwardButton.addActionListener(e -> skip(10));
        skipBackwardButton.addActionListener(e -> skip(-10));
        volumeSlider.addChangeListener(e ->
                mediaPlayerComponent.mediaPlayer().audio().setVolume(volumeSlider.getValue())
        );

        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) searchMovies(query);
            else searchMovies(null);
        });

        downloadButton.addActionListener(e -> {
            int index = movieDropdown.getSelectedIndex();
            if (index >= 0 && movieList != null && index < movieList.size()) {
                downloadMovie(movieList.get(index).getId());
            }
        });

        // Load movies from backend
        loadMovies();

        setVisible(true);
    }

    private void searchMovies(String query) {
        try {
            String apiUrl = "http://localhost:8080/api" + (query != null ? "/search?query=" + URLEncoder.encode(query, "UTF-8") : "");
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String json = reader.lines().collect(Collectors.joining());
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                movieList = mapper.readValue(json, new TypeReference<List<MovieDto>>() {});
                movieDropdown.removeAllItems();
                movieList.forEach(m -> movieDropdown.addItem(m.getTitle()));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load movies: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void downloadMovie(Long movieId) {
        try {
            URL url = new URL("http://localhost:8080/api/" + movieId + "/download");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                String disposition = conn.getHeaderField("Content-Disposition");
                String fileName = "downloaded_video.mp4";
                if (disposition != null && disposition.contains("filename=")) {
                    fileName = disposition.split("filename=")[1].replace("\"", "");
                }

                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(fileName));
                if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                    try (InputStream in = conn.getInputStream();
                         FileOutputStream out = new FileOutputStream(chooser.getSelectedFile())) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Downloaded successfully!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to download file: " + conn.getResponseMessage());
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error downloading file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onPlay(ActionEvent event) {
        int index = movieDropdown.getSelectedIndex();
        if (index < 0 || movieList == null) return;

        MovieDto selected = movieList.get(index);
        String streamUrl = "http://localhost:8080/api/" + selected.getId() + "/stream";
        mediaPlayerComponent.mediaPlayer().media().play(streamUrl);
    }

    private void skip(int seconds) {
        long currentTime = mediaPlayerComponent.mediaPlayer().status().time();
        long newTime = currentTime + (seconds * 1000L);
        if (newTime < 0) newTime = 0;
        mediaPlayerComponent.mediaPlayer().controls().setTime(newTime);
    }


    private void loadMovies() {
        try {
            URL url = new URL("http://localhost:8080/api/movies");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String json = reader.lines().collect(Collectors.joining());
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.disable(com.fasterxml.jackson.databind.MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES);
                movieList = mapper.readValue(json, new TypeReference<List<MovieDto>>() {});;
                movieListModel.clear();
                for (MovieDto m : movieList) {
                    movieListModel.addElement(m);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load movies: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void playSelectedMovie() {
        int index = movieListUI.getSelectedIndex();
        if (index < 0 || movieList == null || movieList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a movie first.");
            return;
        }

        MovieDto selected = movieList.get(index);
        String streamUrl = "http://localhost:8080/api/" + selected.getId() + "/stream";
        System.out.println("▶ Playing: " + selected.getTitle() + " (" + streamUrl + ")");
        String[] options = {":network-caching=300"};
        mediaPlayerComponent.mediaPlayer().media().play(streamUrl, options);
    }

    public void release() {
        mediaPlayerComponent.release();
    }



}
