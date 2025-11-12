package com.neu.finalproject.meskot;

import com.neu.finalproject.meskot.ui.VideoPlayerUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;


@SpringBootApplication
public class MeskotApplication {
	public static void main(String[] args) {

        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(MeskotApplication.class, args);
        System.setProperty("VLC_PLUGIN_PATH", "C:\\Program Files\\VideoLAN\\VLC\\plugins");
        SwingUtilities.invokeLater(VideoPlayerUI::new);
	}

}
