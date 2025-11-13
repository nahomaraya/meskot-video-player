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
    private final JButton playButton;
    private final JButton pauseButton;
    private final JButton stopButton;
    private List<MovieDto> movieList;
    private JTextField searchField;
    private JButton searchButton;
    private JButton downloadButton;


    public VideoPlayerUI() {
        super("Meskot Video Player");



        // URL input
//        urlField = new JTextField("http://localhost:8080/api/2/stream");
//        playButton = new JButton("Play");

        // Layout setup
//        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
//        controlPanel.add(urlField, BorderLayout.CENTER);
//        controlPanel.add(playButton, BorderLayout.EAST);
//
//        setLayout(new BorderLayout());
//        add(controlPanel, BorderLayout.NORTH);
//        add(mediaPlayerComponent, BorderLayout.CENTER);

        // Frame properties
        setTitle("🎬 Meskot Player");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        add(mediaPlayerComponent, BorderLayout.CENTER);


        JPanel topPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        downloadButton = new JButton("Download");

        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(searchButton);
        topPanel.add(downloadButton);

        add(topPanel, BorderLayout.NORTH);

//        // Button action
//        playButton.addActionListener(e -> playVideo());

        // Controls
        JPanel controlPanel = new JPanel();
        movieDropdown = new JComboBox<>();
        playButton = new JButton("▶ Play");
        pauseButton = new JButton("⏸ Pause");
        stopButton = new JButton("⏹ Stop");

        controlPanel.add(movieDropdown);
        controlPanel.add(playButton);
        controlPanel.add(pauseButton);
        controlPanel.add(stopButton);

        add(controlPanel, BorderLayout.SOUTH);

        // Actions
        playButton.addActionListener(this::onPlay);
        pauseButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().pause());
        stopButton.addActionListener(e -> mediaPlayerComponent.mediaPlayer().controls().stop());
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim();
            if (!query.isEmpty()) {
                loadMoviesFromApi(query);
            } else {
                loadMoviesFromApi(null);
            }
        });

        downloadButton.addActionListener(e -> {
            int index = movieDropdown.getSelectedIndex();
            if (index >= 0 && index < movieList.size()) {
                MovieDto selected = movieList.get(index);
                downloadMovie(selected.getId());
            }
        });

        // Show window
        setVisible(true);
    }

    private void loadMoviesFromApi(String query) {
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



}
