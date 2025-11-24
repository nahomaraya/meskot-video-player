package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.ui.dto.JobResponse;
import com.neu.finalproject.meskot.ui.dto.UploadJob;
import com.neu.finalproject.meskot.ui.MovieApiService;
import com.neu.finalproject.meskot.ui.VideoPlayerUI;

import javax.swing.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
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

    private MovieDto currentlyPlayingMovie;

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

        // Store the currently playing movie
        currentlyPlayingMovie = selectedMovie;
        view.updateNowPlayingLabel(selectedMovie.getTitle());
        view.showPage(VideoPlayerUI.PAGE_PLAYER);
        view.updatePlayerMovieList(currentMovieList);

        String streamUrl = apiService.getBaseUrl() + "/" + selectedMovie.getId() + "/stream";
        System.out.println("▶ Playing: " + selectedMovie.getTitle() + " (" + streamUrl + ")");
        String[] options = {":network-caching=300"};
        view.getMediaPlayer().mediaPlayer().media().play(streamUrl, options);
    }

    public void onPlay() {
        // Check if we have a currently playing movie first
        if (currentlyPlayingMovie != null) {
            // Resume/replay current movie
            view.getMediaPlayer().mediaPlayer().controls().play();
            return;
        }

        // Otherwise, try to get selection from list
        MovieDto selected = view.getSelectedMovieFromList();
        if (selected == null) {
            view.showInfoMessage("Please select a movie from the list first.");
            return;
        }
        onMovieSelected(selected);
    }

    public void onOpenLocalVideo() {
        File file = view.showLocalVideoDialog();

        if (file != null) {
            view.showPage(VideoPlayerUI.PAGE_PLAYER);
            MovieDto localMovie = new MovieDto();
            localMovie.setId(-1L);
            localMovie.setTitle(file.getName() + " (Local File)");
            localMovie.setUploadedDate(LocalDateTime.now());

            // Store as currently playing
            currentlyPlayingMovie = localMovie;

            view.updatePlayerMovieList(Collections.singletonList(localMovie));
            String mediaPath = file.getAbsolutePath();
            System.out.println("Playing Local File: " + mediaPath);

            view.getMediaPlayer().mediaPlayer().media().play(mediaPath);
        }
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


    /**
     * Seek to a specific time in the video
     */
    public void onSeek(long timeInMillis) {
        view.getMediaPlayer().mediaPlayer().controls().setTime(timeInMillis);
    }

    /**
     * Get current playback time
     */
    public long getCurrentTime() {
        return view.getMediaPlayer().mediaPlayer().status().time();
    }

    /**
     * Get total video duration
     */
    public long getDuration() {
        return view.getMediaPlayer().mediaPlayer().status().length();
    }

    /**
     * Format time in mm:ss format
     */
    public String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Download with resolution selection
     */
    public void onDownloadWithResolution() {
        // Use currently playing movie if available
        MovieDto selected = currentlyPlayingMovie;

        // If no movie is playing, try to get selection from list
        if (selected == null) {
            selected = view.getSelectedMovieFromList();
        }

        if (selected == null) {
            view.showInfoMessage("Please select a movie to download.");
            return;
        }

        // Show resolution selection dialog
        String[] resolutions = {"Original", "1080p", "720p", "480p", "360p"};
        String selectedResolution = (String) JOptionPane.showInputDialog(
                null,
                "Select download resolution:",
                "Download Options",
                JOptionPane.QUESTION_MESSAGE,
                null,
                resolutions,
                resolutions[0]
        );

        if (selectedResolution == null) return;

        File outputFile = view.showSaveDialog(
                selected.getTitle() + "-" + selectedResolution + ".mp4"
        );
        if (outputFile == null) return;

        view.setDownloadButtonState(false, "Starting download...");

        if ("Original".equals(selectedResolution) || isInternetArchiveMovie(selected)) {
            startDirectDownload(selected, outputFile);
        } else {
            startConversionDownload(selected, selectedResolution, outputFile);
        }
    }

    private boolean isInternetArchiveMovie(MovieDto movie) {
        // Check if it's an IA movie - you may need to add a field to MovieDto
        return movie.getSourceType() != null &&
                movie.getSourceType().equals("INTERNET_ARCHIVE");
    }

    private void startDirectDownload(MovieDto movie, File outputFile) {
        new SwingWorker<Void, Long>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiService.downloadMovie(movie.getId(), outputFile,
                        (bytes) -> publish(bytes)
                );
                return null;
            }

            @Override
            protected void process(List<Long> chunks) {
                long bytes = chunks.get(chunks.size() - 1);
                view.setDownloadButtonState(false,
                        String.format("Downloading... (%.2f MB)", bytes / (1024.0 * 1024.0)));
            }

            @Override
            protected void done() {
                try {
                    get();
                    view.showInfoMessage("Downloaded successfully!");
                } catch (Exception e) {
                    view.showErrorMessage("Download failed: " + e.getMessage());
                } finally {
                    view.setDownloadButtonState(true, "Download");
                }
            }
        }.execute();
    }

    private void startConversionDownload(MovieDto movie, String resolution, File outputFile) {
        new SwingWorker<String, Integer>() {
            @Override
            protected String doInBackground() throws Exception {
                // Start conversion job
                return apiService.startConversion(movie.getId(), resolution);
            }

            @Override
            protected void done() {
                try {
                    String jobId = get();
                    view.setDownloadButtonState(false, "Converting...");
                    pollConversionAndDownload(jobId, outputFile);
                } catch (Exception e) {
                    view.showErrorMessage("Failed to start conversion: " + e.getMessage());
                    view.setDownloadButtonState(true, "Download");
                }
            }
        }.execute();
    }

    private void pollConversionAndDownload(String jobId, File outputFile) {
        Timer poller = new Timer(true);

        poller.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    UploadJob job = apiService.getDownloadStatus(jobId);

                    SwingUtilities.invokeLater(() -> {
                        switch (job.getStatus()) {
                            case "ENCODING":
                                view.setDownloadButtonState(false,
                                        "Converting: " + job.getProgress() + "%");
                                break;

                            case "COMPLETED":
                                poller.cancel();
                                view.setDownloadButtonState(false, "Downloading...");
                                downloadConvertedFile(jobId, outputFile);
                                break;

                            case "FAILED":
                                poller.cancel();
                                view.showErrorMessage("Conversion failed: " +
                                        job.getErrorMessage());
                                view.setDownloadButtonState(true, "Download");
                                break;
                        }
                    });

                } catch (Exception e) {
                    poller.cancel();
                    SwingUtilities.invokeLater(() -> {
                        view.showErrorMessage("Error: " + e.getMessage());
                        view.setDownloadButtonState(true, "Download");
                    });
                }
            }
        }, 0, 2000);
    }

    private void downloadConvertedFile(String jobId, File outputFile) {
        new SwingWorker<Void, Long>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiService.downloadConvertedFile(jobId, outputFile,
                        (bytes) -> publish((long) bytes)
                );
                return null;
            }

            @Override
            protected void process(List<Long> chunks) {
                long bytes = chunks.get(chunks.size() - 1);
                view.setDownloadButtonState(false,
                        String.format("Downloading... (%.2f MB)", bytes / (1024.0 * 1024.0)));
            }

            @Override
            protected void done() {
                try {
                    get();
                    view.showInfoMessage("Downloaded successfully!");
                } catch (Exception e) {
                    view.showErrorMessage("Download failed: " + e.getMessage());
                } finally {
                    view.setDownloadButtonState(true, "Download");
                }
            }
        }.execute();
    }

    public MovieDto getCurrentlyPlayingMovie() {
        return currentlyPlayingMovie;
    }
}