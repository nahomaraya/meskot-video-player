package com.neu.finalproject.meskot;

import com.neu.finalproject.meskot.ui.MovieApiService;
import com.neu.finalproject.meskot.ui.PlayerPresenter;
import com.neu.finalproject.meskot.ui.VideoPlayerUI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;


@SpringBootApplication
public class MeskotApplication {
	public static void main(String[] args) {

        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(MeskotApplication.class, args);
        //set the vlc path f
        SwingUtilities.invokeLater(() -> {

            // 1. Create the service (data/network layer)
            MovieApiService apiService = new MovieApiService();

            // 2. Create the view (UI layer)
            VideoPlayerUI view = new VideoPlayerUI();

            // 3. Create the presenter (logic layer) and link them
            PlayerPresenter presenter = new PlayerPresenter(view, apiService);

            // 4. Attach the presenter to the view (this hooks up all the buttons)
            view.attachPresenter(presenter);

            // 5. Show the UI
            view.setVisible(true);

            // 6. Tell the presenter to load the initial data
            new Thread(() -> {
                presenter.loadInitialMovies();
            }).start();
        });
	}

}
