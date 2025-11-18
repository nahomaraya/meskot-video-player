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
 * Expanded to manage navigation, multi-page logic, and the upload wizard.
 */
public class PlayerPresenter {

    private final VideoPlayerUI view;
    private final MovieApiService apiService;

    // --- STATE FIELDS ADDED TO RESOLVE ERRORS ---
    private List<MovieDto> currentMovieList; // Cache the last list/search results
    private String currentUploadStep = "STEP_1";
    // --------------------------------------------

    public PlayerPresenter(VideoPlayerUI view, MovieApiService apiService) {
        this.view = view;
        this.apiService = apiService;
    }

    // --- Actions Called by View ---

    public void loadInitialMovies() {
        view.showPage(VideoPlayerUI.PAGE_SEARCH); // Start on the search page

        new SwingWorker<List<MovieDto>, Void>() {
            @Override
            protected List<MovieDto> doInBackground() throws Exception {
                return apiService.getMovies();
            }

            @Override
            protected void done() {
                try {
                    // Cache the results and display them on the main list page
                    currentMovieList = get();
                    view.updateMainMovieList(currentMovieList);
                } catch (Exception e) {
                    view.showErrorMessage("Failed to load movies: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void onLoadLibrary() {
        view.showPage(VideoPlayerUI.PAGE_LIBRARY); // Navigate to the Library page

        new SwingWorker<List<MovieDto>, Void>() {
            @Override
            protected List<MovieDto> doInBackground() throws Exception {
                // Fetch ALL movies (no search query)
                return apiService.getMovies();
            }

            @Override
            protected void done() {
                try {
                    // Cache the results and display them on the main list component
                    currentMovieList = get();
                    view.updateMainMovieList(currentMovieList);
                } catch (Exception e) {
                    view.showErrorMessage("Failed to load full library: " + e.getMessage());
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
                    currentMovieList = get(); // Cache the results
                    view.updateMainMovieList(currentMovieList); // Corrected method name
                    view.showPage(VideoPlayerUI.PAGE_LIST); // Show the list page
                } catch (Exception e) {
                    view.showErrorMessage("Failed to search: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void onMovieSelected(MovieDto selectedMovie) {
        if (selectedMovie == null) return;

        // 1. Load the player page
        view.showPage(VideoPlayerUI.PAGE_PLAYER);

        // 2. Populate the side-list with the *current* context
        view.updatePlayerMovieList(currentMovieList);

        // 3. Start playing the selected movie
        String streamUrl = apiService.getBaseUrl() + "/" + selectedMovie.getId() + "/stream";
        System.out.println("▶ Playing: " + selectedMovie.getTitle() + " (" + streamUrl + ")");
        String[] options = {":network-caching=300"};
        view.getMediaPlayer().mediaPlayer().media().play(streamUrl, options);
    }

    public void onNavigate(String pageName) {
        view.showPage(pageName);
        if (VideoPlayerUI.PAGE_UPLOAD.equals(pageName)) {
            // Reset wizard state and data when navigating to the Upload page
            currentUploadStep = "STEP_1";
            view.setUploadWizardPage(currentUploadStep);
            view.setSelectedUploadFile(null);
        }
    }

    public void onPlay() {
        // Operates on the side list on the player page
        MovieDto selected = view.getSelectedMovieFromList();
        if (selected == null) {
            view.showInfoMessage("Please select a movie from the list first.");
            return;
        }
        onMovieSelected(selected);
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
                apiService.downloadMovie(selected.getId(), outputFile,
                        (bytes) -> publish(bytes)
                );
                return null;
            }

            @Override
            protected void process(List<Long> chunks) {
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

    // --- Upload Wizard Logic (Replaces old onUpload() and implements wizard steps) ---

    public void onUploadSelectFile() {
        File file = view.showOpenDialog();
        if (file != null) {
            view.setSelectedUploadFile(file);
            // Auto-advance to next step
            currentUploadStep = "STEP_2";
            view.setUploadWizardPage(currentUploadStep);
        }
    }

    public void onUploadWizardPrevious() {
        if ("STEP_2".equals(currentUploadStep)) {
            currentUploadStep = "STEP_1";
            view.setUploadWizardPage(currentUploadStep);
        }
    }

    public void onUploadWizardNext(String title, String resolution) {
        if ("STEP_1".equals(currentUploadStep)) {
            view.showInfoMessage("Please select a file first.");
        } else if ("STEP_2".equals(currentUploadStep)) {
            // This is the "Upload" button click
            if (title == null || title.trim().isEmpty()) {
                view.showInfoMessage("Please enter a title.");
                return;
            }
            File fileToUpload = view.getSelectedUploadFile();
            if (fileToUpload == null) {
                view.showInfoMessage("An error occurred. Please go back and re-select your file.");
                return;
            }

            // Move to step 3 and start the upload worker
            currentUploadStep = "STEP_3";
            view.setUploadWizardPage(currentUploadStep);
            startUploadWorker(fileToUpload, title, resolution);
        }
    }

    private void startUploadWorker(File fileToUpload, String title, String resolution) {
        view.setUploadProgress("Uploading: 0%", 0, false);

        new SwingWorker<String, Integer>() {
            @Override
            protected String doInBackground() throws Exception {
                JobResponse job = apiService.startUpload(fileToUpload, title, resolution,
                        (percent) -> publish(percent)
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
                    startPollingWorker(jobId);
                } catch (Exception e) {
                    view.showErrorMessage("Upload failed: Failure during upload: " + e.getMessage());
                    onNavigate(VideoPlayerUI.PAGE_SEARCH);
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
                                onNavigate(VideoPlayerUI.PAGE_SEARCH); // Navigate user back to the home page
                                break;
                            case "FAILED":
                                poller.cancel();
                                view.showErrorMessage("Encoding Failed: " + job.getErrorMessage());
                                onNavigate(VideoPlayerUI.PAGE_SEARCH); // Navigate user back
                                break;
                        }
                    });

                } catch (Exception e) {
                    poller.cancel();
                    SwingUtilities.invokeLater(() -> {
                        view.showErrorMessage("Error checking status: " + e.getMessage());
                        onNavigate(VideoPlayerUI.PAGE_SEARCH);
                    });
                }
            }
        }, 0, 2000); // Poll every 2 seconds
    }
}