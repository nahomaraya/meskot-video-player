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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

        loadMoviesFromApi();


        // Show window
        setVisible(true);
    }

    private void loadMoviesFromApi() {
        try {
            URL url = new URL("http://localhost:8080/api/movies");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String json = reader.lines().collect(Collectors.joining());
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

                movieList = mapper.readValue(json, new TypeReference<List<MovieDto>>() {});
                movieDropdown.removeAllItems();
                movieList.forEach(m -> movieDropdown.addItem(m.getTitle()));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load movies: " + e.getMessage());
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
