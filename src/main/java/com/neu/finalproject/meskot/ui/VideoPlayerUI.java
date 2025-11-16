package com.neu.finalproject.meskot.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.model.Movie;
import lombok.Getter;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
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
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import java.io.File;


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
    private JButton downloadButton = new JButton("Download");
    private JProgressBar uploadProgressBar;



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
            int index = movieDropdown.getSelectedIndex(); // Or get from JList
            if (index >= 0 && movieList != null && index < movieList.size()) {
                // We don't call downloadMovie directly anymore
                // We start the background worker
                startDownloadWorker(movieList.get(index));
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

    private void startDownloadWorker(MovieDto movie) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(movie.getTitle() + ".mp4"));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();

            // Disable the download button while working
            downloadButton.setEnabled(false);
            downloadButton.setText("Downloading...");

            // SwingWorker<Void, Void> means:
            // Void: doInBackground() returns nothing
            // Void: process() is not used
            SwingWorker<Void, Void> worker = new SwingWorker<>() {

                @Override
                protected Void doInBackground() throws Exception {
                    URL url = new URL("http://localhost:8080/api/" + movie.getId() + "/download");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");

                    if (conn.getResponseCode() == 200) {
                        long fileSize = conn.getContentLengthLong();

                        // This creates a popup progress bar!
                        ProgressMonitorInputStream pmis = new ProgressMonitorInputStream(
                                VideoPlayerUI.this,
                                "Downloading " + movie.getTitle(),
                                conn.getInputStream()
                        );

                        // Set progress bar max value
                        if (fileSize != -1) {
                            pmis.getProgressMonitor().setMaximum((int) fileSize);
                        }

                        try (InputStream in = new BufferedInputStream(pmis);
                             FileOutputStream out = new FileOutputStream(outputFile)) {

                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = in.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                        }
                    } else {
                        throw new IOException("Server responded with: " + conn.getResponseMessage());
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        // get() will re-throw any exception from doInBackground()
                        get();
                        JOptionPane.showMessageDialog(VideoPlayerUI.this,
                                "Downloaded successfully!",
                                "Download Complete",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(VideoPlayerUI.this,
                                "Error downloading file: " + e.getMessage(),
                                "Download Failed",
                                JOptionPane.ERROR_MESSAGE);
                    } finally {
                        // Re-enable the button
                        downloadButton.setEnabled(true);
                        downloadButton.setText("Download");
                    }
                }
            };

            // This starts the worker on a new thread
            worker.execute();
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

//    private void startUploadWorker(File fileToUpload, String title) {
//        // 1. Create a SwingWorker to do this on a background thread
//        SwingWorker<String, Integer> worker = new SwingWorker<>() {
//
//            @Override
//            protected String doInBackground() throws Exception {
//                try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//                    HttpPost uploadPost = new HttpPost("http://localhost:8080/api/upload");
//
//                    // We need a custom entity to track progress
//                    // (This is a simplified example; a real one would override writeTo)
//                    HttpEntity multipartEntity = MultipartEntityBuilder.create()
//                            .addBinaryBody("file", fileToUpload, ContentType.DEFAULT_BINARY, fileToUpload.getName())
//                            .addTextBody("title", title)
//                            .addTextBody("resolution", "720p")
//                            .build();
//
//                    // To track progress, you'd wrap 'multipartEntity' in a custom
//                    // class that overrides 'writeTo(OutputStream)' and calls
//                    // publish() with the percentage.
//
//                    // For now, let's just simulate it:
//                    for (int i = 0; i <= 100; i += 10) {
//                        Thread.sleep(200); // Simulate upload chunk
//                        publish(i); // Send progress to the process() method
//                    }
//
//                    // This is where you'd actually execute the request:
//                    // CloseableHttpResponse response = httpClient.execute(uploadPost);
//                    // ... handle response ...
//
//                    return "Upload complete"; // Return a result
//                }
//            }
//
//            @Override
//            protected void process(List<Integer> chunks) {
//                // This runs on the UI thread
//                int latestProgress = chunks.get(chunks.size() - 1);
//                uploadProgressBar.setValue(latestProgress); // Update the JProgressBar
//            }
//
//            @Override
//            protected void done() {
//                try {
//                    String result = get();
//                    JOptionPane.showMessageDialog(null, result);
//                    uploadProgressBar.setValue(100);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    JOptionPane.showMessageDialog(null, "Upload failed");
//                    uploadProgressBar.setValue(0);
//                }
//            }
//        };
//        worker.execute();
//    }
    public void release() {
        mediaPlayerComponent.release();
    }



}
