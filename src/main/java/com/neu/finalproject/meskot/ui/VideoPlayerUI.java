package com.neu.finalproject.meskot.ui;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;

import javax.swing.*;
import java.awt.*;

public class VideoPlayerUI extends JFrame {

    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;
    private final JTextField urlField;
    private final JButton playButton;

    public VideoPlayerUI() {
        super("Meskot Video Player");

        // Video panel (VLCJ component)
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();

        // URL input
        urlField = new JTextField("http://localhost:8080/api/2/stream");
        playButton = new JButton("Play");

        // Layout setup
        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
        controlPanel.add(urlField, BorderLayout.CENTER);
        controlPanel.add(playButton, BorderLayout.EAST);

        setLayout(new BorderLayout());
        add(controlPanel, BorderLayout.NORTH);
        add(mediaPlayerComponent, BorderLayout.CENTER);

        // Frame properties
        setSize(960, 540);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Button action
        playButton.addActionListener(e -> playVideo());

        // Show window
        setVisible(true);
    }

    private void playVideo() {
        String url = urlField.getText().trim();
        if (!url.isEmpty()) {
            MediaPlayer mediaPlayer = mediaPlayerComponent.mediaPlayer();
            mediaPlayer.media().play(url);
        } else {
            JOptionPane.showMessageDialog(this, "Enter a valid video URL");
        }
    }

//    public static void main(String[] args) {
//        // Initialize VLCJ (optional, but helps detect VLC libs)
//        System.setProperty("VLC_PLUGIN_PATH", "C:\\Program Files\\VideoLAN\\VLC\\plugins");
//        SwingUtilities.invokeLater(VideoPlayerUI::new);
//    }
}
