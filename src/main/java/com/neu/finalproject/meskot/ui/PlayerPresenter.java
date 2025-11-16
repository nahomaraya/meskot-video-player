package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.ui.dto.JobResponse;
import com.neu.finalproject.meskot.ui.dto.UploadJob;
import com.neu.finalproject.meskot.ui.MovieApiService;
import com.neu.finalproject.meskot.ui.VideoPlayerUI;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The Presenter (P in MVP).
 * This class is the "brain" of the UI. It handles all logic,
 * runs background tasks, and tells the View what to show.
 */
public class PlayerPresenter {

    private final VideoPlayerUI view;
    private final MovieApiService apiService;

    public PlayerPresenter(VideoPlayerUI view, MovieApiService apiService) {
        this.view = view;
        this.apiService = apiService;
    }

    // --- Actions Called by View ---

    public void loadInitialMovies() {
        new SwingWorker<List<MovieDto>, Void>() {
            @Override
            protected List<MovieDto> doInBackground() throws Exception {
                return apiService.getMovies();
            }

            @Override
            protected void done() {
                try {
                    view.updateMovieList(get());
                } catch (Exception e) {
                    view.showErrorMessage("Failed to load movies: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void onSearch() {
        String query = view.getSearchQuery();
        new SwingWorker<List<MovieDto>, Void>() {
            @Override
            protected List<MovieDto> doInBackground() throws Exception {
                if (query.isEmpty()) {
                    return apiService.getMovies();
                } else {
                    return apiService.searchMovies(query);
                }
            }

            @Override
            protected void done() {
                try {
                    view.updateMovieList(get());
                } catch (Exception e) {
                    view.showErrorMessage("Failed to search: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void onPlay() {
        MovieDto selected = view.getSelectedMovieFromList();
        if (selected == null) {
            view.showInfoMessage("Please select a movie first.");
            return;
        }
        // Building the URL is logic, so it stays in the Presenter
        String streamUrl = apiService.getBaseUrl() + "/" + selected.getId() + "/stream";
        System.out.println("▶ Playing: " + selected.getTitle() + " (" + streamUrl + ")");
        String[] options = {":network-caching=300"};
        view.getMediaPlayer().mediaPlayer().media().play(streamUrl, options);
    }

    public void onSkip(int seconds) {
        long currentTime = view.getMediaPlayer().mediaPlayer().status().time();
        long newTime = currentTime + (seconds * 1000L);
        if (newTime < 0) newTime = 0;
        view.getMediaPlayer().mediaPlayer().controls().setTime(newTime);
    }

    public void onDownload() {
        MovieDto selected = view.getSelectedMovieFromList();
        if (selected == null) {
            view.showInfoMessage("Please select a movie to download.");
            return;
        }

        File outputFile = view.showSaveDialog(selected.getTitle() + ".mp4");
        if (outputFile == null) return; // User cancelled

        view.setDownloadButtonState(false, "Downloading...");

        new SwingWorker<Void, Long>() {
            @Override
            protected Void doInBackground() throws Exception {
                // The API service handles the download and uses the consumer to publish progress
                apiService.downloadMovie(selected.getId(), outputFile,
                        (bytes) -> publish(bytes) // Publish progress
                );
                return null;
            }

            @Override
            protected void process(List<Long> chunks) {
                // Note: This gives bytes, not percent.
                // For a real app, you'd get total size first to make a percentage.
                long bytes = chunks.get(chunks.size() - 1);
                view.setDownloadButtonState(false, String.format("Downloading... (%.2f MB)", bytes / (1024.0 * 1024.0)));
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions
                    view.showInfoMessage("Downloaded successfully!");
                } catch (Exception e) {
                    view.showErrorMessage("Download failed: " + e.getMessage());
                } finally {
                    view.setDownloadButtonState(true, "Download");
                }
            }
        }.execute();
    }

    public void onUpload() {
        File fileToUpload = view.showOpenDialog();
        if (fileToUpload == null) return; // User cancelled

        String title = view.showInputDialog("Enter a title for the movie:", "Movie Title");
        if (title == null || title.trim().isEmpty()) return; // User cancelled

        view.setUploadButtonState(false);
        view.setDownloadButtonState(false, "Download");
        view.setUploadProgress("Uploading: 0%", 0, false);

        new SwingWorker<String, Integer>() {
            @Override
            protected String doInBackground() throws Exception {
                // Call the API, which will publish progress via the callback
                JobResponse job = apiService.startUpload(fileToUpload, title, "720p",
                        (percent) -> publish(percent) // Publish progress
                );
                return job.getJobId();
            }

            @Override
            protected void process(List<Integer> chunks) {
                int percent = chunks.get(chunks.size() - 1);
                view.setUploadProgress("Uploading: " + percent + "%", percent, false);
            }

            @Override
            protected void done() {
                try {
                    String jobId = get();
                    view.setUploadProgress("Upload Complete. Encoding...", 0, true);
                    startPollingWorker(jobId); // Start polling for encoding status
                } catch (Exception e) {
                    view.showErrorMessage("Upload failed: " + e.getMessage());
                    resetUploadUI();
                }
            }
        }.execute();
    }

    private void startPollingWorker(String jobId) {
        Timer poller = new Timer(true); // Use daemon thread
        poller.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    UploadJob job = apiService.getUploadStatus(jobId);

                    SwingUtilities.invokeLater(() -> {
                        switch (job.getStatus()) {
                            case "PENDING":
                                view.setUploadProgress("Encoding: Pending...", 0, true);
                                break;
                            case "ENCODING":
                                view.setUploadProgress("Encoding: " + job.getProgress() + "%", job.getProgress(), false);
                                break;
                            case "COMPLETED":
                                poller.cancel();
                                view.hideUploadProgressAfterDelay("Encoding Complete!");
                                resetUploadUI();
                                loadInitialMovies(); // Refresh list
                                break;
                            case "FAILED":
                                poller.cancel();
                                view.showErrorMessage("Encoding Failed: " + job.getErrorMessage());
                                resetUploadUI();
                                break;
                        }
                    });

                } catch (Exception e) {
                    poller.cancel();
                    SwingUtilities.invokeLater(() -> {
                        view.showErrorMessage("Error checking status: " + e.getMessage());
                        resetUploadUI();
                    });
                }
            }
        }, 0, 2000); // Poll every 2 seconds
    }

    private void resetUploadUI() {
        view.setUploadButtonState(true);
        view.setDownloadButtonState(true, "Download");
    }
}