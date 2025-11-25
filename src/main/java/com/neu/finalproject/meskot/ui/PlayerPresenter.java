package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.ui.dto.JobResponse;
import com.neu.finalproject.meskot.ui.dto.UploadJob;

import javax.swing.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerPresenter {

    private final VideoPlayerUI view;
    private final MovieApiService apiService;

    private List<MovieDto> currentMovieList;
    private String currentUploadStep = "STEP_1";
    private MovieDto currentlyPlayingMovie;
    private String currentDataSource = VideoPlayerUI.SOURCE_LOCAL;

    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
    private Timer currentPoller = null;

    public PlayerPresenter(VideoPlayerUI view, MovieApiService apiService) {
        this.view = view;
        this.apiService = apiService;
    }

    // --- Data Source Handling ---

    public void onDataSourceChanged(String newSource) {
        if (newSource == null || newSource.equals(currentDataSource)) {
            return;
        }

        currentDataSource = newSource;
        System.out.println("📁 Data source changed to: " + newSource);
        apiService.setDataSource(newSource);

        view.showGlobalProgress("INFO", "Switching to " + newSource, -1, "Loading...");

        new SwingWorker<List<MovieDto>, Void>() {
            @Override
            protected List<MovieDto> doInBackground() throws Exception {
                return apiService.getMovies();
            }

            @Override
            protected void done() {
                try {
                    currentMovieList = get();
                    view.updateMainMovieList(currentMovieList);
                    view.showGlobalProgressComplete("Loaded " + currentMovieList.size() + " movies", true);
                } catch (Exception e) {
                    view.showGlobalProgressComplete("Failed to load from " + newSource, false);
                    view.showErrorMessage("Failed to load movies: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void onCancelOperation() {
        cancelRequested.set(true);
        if (currentPoller != null) {
            currentPoller.cancel();
            currentPoller = null;
        }
        view.hideGlobalProgress();
        view.setDownloadButtonState(true, "📥 Download");
        view.showInfoMessage("Operation cancelled.");
    }

    // --- Navigation & Loading ---

    public void loadInitialMovies() {
        view.showPage(VideoPlayerUI.PAGE_SEARCH);

        new SwingWorker<List<MovieDto>, Void>() {
            @Override
            protected List<MovieDto> doInBackground() throws Exception {
                return apiService.getMovies();
            }

            @Override
            protected void done() {
                try {
                    currentMovieList = get();
                    view.updateMainMovieList(currentMovieList);
                } catch (Exception e) {
                    view.showErrorMessage("Failed to load movies: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void onLoadLibrary() {
        view.showPage(VideoPlayerUI.PAGE_LIBRARY);
        view.showGlobalProgress("DOWNLOAD", "Library", -1, "Loading movies...");

        new SwingWorker<List<MovieDto>, Void>() {
            @Override
            protected List<MovieDto> doInBackground() throws Exception {
                return apiService.getMovies();
            }

            @Override
            protected void done() {
                try {
                    currentMovieList = get();
                    view.updateMainMovieList(currentMovieList);
                    view.showGlobalProgressComplete("Loaded " + currentMovieList.size() + " movies", true);
                } catch (Exception e) {
                    view.showGlobalProgressComplete("Failed to load library", false);
                    view.showErrorMessage("Failed to load full library: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void onSearch() {
        String query = view.getSearchQuery();
        view.showGlobalProgress("DOWNLOAD", "Search", -1, "Searching...");

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
                    currentMovieList = get();
                    view.updateMainMovieList(currentMovieList);
                    view.showPage(VideoPlayerUI.PAGE_LIST);
                    view.showGlobalProgressComplete("Found " + currentMovieList.size() + " results", true);
                } catch (Exception e) {
                    view.showGlobalProgressComplete("Search failed", false);
                    view.showErrorMessage("Failed to search: " + e.getMessage());
                }
            }
        }.execute();
    }

    public void onMovieSelected(MovieDto selectedMovie) {
        if (selectedMovie == null) return;

        currentlyPlayingMovie = selectedMovie;
        view.updateNowPlayingLabel(selectedMovie.getTitle());
        view.showPage(VideoPlayerUI.PAGE_PLAYER);
        view.updatePlayerMovieList(currentMovieList);

        String streamUrl = apiService.getBaseUrl() + "/movies/" + selectedMovie.getId() + "/stream";
        System.out.println("▶ Playing: " + selectedMovie.getTitle() + " (" + streamUrl + ")");
        String[] options = {":network-caching=300"};
        view.getMediaPlayer().mediaPlayer().media().play(streamUrl, options);
    }

    public void onPlay() {
        if (currentlyPlayingMovie != null) {
            view.getMediaPlayer().mediaPlayer().controls().play();
            return;
        }

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

            currentlyPlayingMovie = localMovie;
            view.updatePlayerMovieList(Collections.singletonList(localMovie));
            view.getMediaPlayer().mediaPlayer().media().play(file.getAbsolutePath());
        }
    }

    public void onNavigate(String pageName) {
        view.showPage(pageName);
        if (VideoPlayerUI.PAGE_UPLOAD.equals(pageName)) {
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

    // --- Upload ---

    public void onUploadSelectFile() {
        File file = view.showOpenDialog();
        if (file != null) {
            view.setSelectedUploadFile(file);
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
            if (title == null || title.trim().isEmpty()) {
                view.showInfoMessage("Please enter a title.");
                return;
            }
            File fileToUpload = view.getSelectedUploadFile();
            if (fileToUpload == null) {
                view.showInfoMessage("Please go back and re-select your file.");
                return;
            }

            currentUploadStep = "STEP_3";
            view.setUploadWizardPage(currentUploadStep);
            startUploadWorker(fileToUpload, title, resolution);
        }
    }

    private void startUploadWorker(File fileToUpload, String title, String resolution) {
        cancelRequested.set(false);
        final String fileName = fileToUpload.getName();
        final long fileSize = fileToUpload.length();

        view.setUploadProgress("Uploading: 0%", 0, false);
        view.showGlobalProgress("UPLOAD", fileName, 0, "Starting upload...");

        new SwingWorker<String, Integer>() {
            @Override
            protected String doInBackground() throws Exception {
                JobResponse job = apiService.startUpload(fileToUpload, title, resolution,
                        percent -> {
                            if (!cancelRequested.get()) {
                                publish(percent);
                            }
                        }
                );
                return job.getJobId();
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (cancelRequested.get()) return;
                int percent = chunks.get(chunks.size() - 1);
                view.setUploadProgress("Uploading: " + percent + "%", percent, false);
                view.showGlobalProgress("UPLOAD", fileName, percent,
                        String.format("%.1f MB uploaded", (fileSize * percent / 100.0) / (1024 * 1024)));
            }

            @Override
            protected void done() {
                if (cancelRequested.get()) return;
                try {
                    String jobId = get();
                    view.setUploadProgress("Upload Complete. Encoding...", 0, true);
                    view.showGlobalProgress("UPLOAD", fileName, 100, "Encoding...");
                    startEncodingPoller(jobId, fileName);
                } catch (Exception e) {
                    view.showGlobalProgressComplete("Upload failed", false);
                    view.showErrorMessage("Upload failed: " + e.getMessage());
                    onNavigate(VideoPlayerUI.PAGE_SEARCH);
                }
            }
        }.execute();
    }

    private void startEncodingPoller(String jobId, String fileName) {
        currentPoller = new Timer(true);

        currentPoller.schedule(new TimerTask() {
            @Override
            public void run() {
                if (cancelRequested.get()) {
                    currentPoller.cancel();
                    return;
                }

                try {
                    UploadJob job = apiService.getUploadStatus(jobId);

                    SwingUtilities.invokeLater(() -> {
                        switch (job.getStatus()) {
                            case "PENDING":
                                view.setUploadProgress("Encoding: Pending...", 0, true);
                                view.showGlobalProgress("UPLOAD", fileName, -1, "Waiting...");
                                break;
                            case "ENCODING":
                                view.setUploadProgress("Encoding: " + job.getProgress() + "%", job.getProgress(), false);
                                view.showGlobalProgress("UPLOAD", fileName, job.getProgress(), "Encoding video...");
                                break;
                            case "COMPLETED":
                                currentPoller.cancel();
                                view.hideUploadProgressAfterDelay("Complete!");
                                view.showGlobalProgressComplete("Upload complete: " + fileName, true);
                                onNavigate(VideoPlayerUI.PAGE_SEARCH);
                                break;
                            case "FAILED":
                                currentPoller.cancel();
                                view.showGlobalProgressComplete("Encoding failed", false);
                                view.showErrorMessage("Encoding Failed: " + job.getErrorMessage());
                                onNavigate(VideoPlayerUI.PAGE_SEARCH);
                                break;
                        }
                    });

                } catch (Exception e) {
                    currentPoller.cancel();
                    SwingUtilities.invokeLater(() -> {
                        view.showGlobalProgressComplete("Error", false);
                        view.showErrorMessage("Error checking status: " + e.getMessage());
                        onNavigate(VideoPlayerUI.PAGE_SEARCH);
                    });
                }
            }
        }, 0, 2000);
    }

    // --- Playback Controls ---

    public void onSeek(long timeInMillis) {
        view.getMediaPlayer().mediaPlayer().controls().setTime(timeInMillis);
    }

    public long getCurrentTime() {
        return view.getMediaPlayer().mediaPlayer().status().time();
    }

    public long getDuration() {
        return view.getMediaPlayer().mediaPlayer().status().length();
    }

    public String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // --- Download (Direct - No Job System) ---

    public void onDownloadWithResolution() {
        MovieDto selected = currentlyPlayingMovie;
        if (selected == null) {
            selected = view.getSelectedMovieFromList();
        }

        if (selected == null) {
            view.showInfoMessage("Please select a movie to download.");
            return;
        }

        // Can't download local files opened from disk
        if (selected.getId() != null && selected.getId() == -1L) {
            view.showInfoMessage("This is a local file. Use your file manager to copy it.");
            return;
        }

        String[] resolutions = {"Original", "1080p", "720p", "480p", "360p"};
        String selectedResolution = (String) JOptionPane.showInputDialog(
                null, "Select download resolution:", "Download Options",
                JOptionPane.QUESTION_MESSAGE, null, resolutions, resolutions[0]
        );

        if (selectedResolution == null) return;

        File outputFile = view.showSaveDialog(
                selected.getTitle() + "-" + selectedResolution + ".mp4"
        );
        if (outputFile == null) return;

        // Start direct download
        startDirectDownload(selected, selectedResolution, outputFile);
    }

    // Replace the startDirectDownload method in PlayerPresenter.java with this:

    private void startDirectDownload(MovieDto movie, String resolution, File outputFile) {
        cancelRequested.set(false);
        final String fileName = movie.getTitle();
        final Long movieId = movie.getId();

        view.setDownloadButtonState(false, "Starting...");
        view.showGlobalProgress("DOWNLOAD", fileName, 0, "Connecting...");

        // Create worker as a named class to properly access publish()
        class DownloadWorker extends SwingWorker<Void, Long> {

            @Override
            protected Void doInBackground() throws Exception {
                apiService.downloadMovie(movieId, outputFile, resolution,
                        this::publishProgress  // Method reference to our helper
                );
                return null;
            }

            // Helper method that can call publish()
            private void publishProgress(Long bytes) {
                if (!cancelRequested.get()) {
                    publish(bytes);
                }
            }

            @Override
            protected void process(List<Long> chunks) {
                if (cancelRequested.get()) return;
                long bytes = chunks.get(chunks.size() - 1);
                double mb = bytes / (1024.0 * 1024.0);
                view.setDownloadButtonState(false, String.format("%.1f MB", mb));
                view.showGlobalProgress("DOWNLOAD", fileName, -1, String.format("%.2f MB downloaded", mb));
            }

            @Override
            protected void done() {
                if (cancelRequested.get()) return;
                try {
                    get();
                    view.showGlobalProgressComplete("Download complete: " + fileName, true);
                    view.showInfoMessage("Downloaded successfully to:\n" + outputFile.getAbsolutePath());
                } catch (Exception e) {
                    view.showGlobalProgressComplete("Download failed", false);
                    view.showErrorMessage("Download failed: " + e.getMessage());
                    if (outputFile.exists()) {
                        outputFile.delete();
                    }
                } finally {
                    view.setDownloadButtonState(true, "📥 Download");
                }
            }
        }

        new DownloadWorker().execute();
    }
    public MovieDto getCurrentlyPlayingMovie() {
        return currentlyPlayingMovie;
    }

    public String getCurrentDataSource() {
        return currentDataSource;
    }
}