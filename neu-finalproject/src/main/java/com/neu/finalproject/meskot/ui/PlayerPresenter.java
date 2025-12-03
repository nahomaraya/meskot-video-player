package com.neu.finalproject.meskot.ui;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.ui.dto.JobResponse;
import com.neu.finalproject.meskot.ui.dto.UploadJob;
import com.neu.finalproject.meskot.ui.dialog.*;
import com.neu.finalproject.meskot.service.CompressionService;

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
    private final CompressionService compressionService;

    private List<MovieDto> currentMovieList;
    private MovieDto currentlyPlayingMovie;
    private String currentDataSource = VideoPlayerUI.SOURCE_LOCAL;
    private String currentPage = VideoPlayerUI.PAGE_SEARCH;  // Track current page

    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
    private Timer currentPoller = null;

    // Dialog references for progress updates
    private UploadDialog currentUploadDialog;
    private CompressDialog currentCompressDialog;
    private DownloadDialog currentDownloadDialog;

    public PlayerPresenter(VideoPlayerUI view, MovieApiService apiService) {
        this.view = view;
        this.apiService = apiService;
        this.compressionService = new CompressionService();
    }

    // =========================================================================
    // DATA SOURCE
    // =========================================================================

    public void onDataSourceChanged(String newSource) {
        if (newSource == null || newSource.equals(currentDataSource)) return;

        currentDataSource = newSource;
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
                    view.showGlobalProgressComplete("Failed to load", false);
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
        view.showInfoMessage("Operation cancelled.");
    }

    // =========================================================================
    // NAVIGATION & LOADING
    // =========================================================================

    public void loadInitialMovies() {
        onNavigate(VideoPlayerUI.PAGE_SEARCH);
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
        onNavigate(VideoPlayerUI.PAGE_LIBRARY);
        view.showGlobalProgress("DOWNLOAD", "Library", -1, "Loading...");

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
                    view.showGlobalProgressComplete("Failed", false);
                    view.showErrorMessage("Failed to load: " + e.getMessage());
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
                return query.isEmpty() ? apiService.getMovies() : apiService.searchMovies(query);
            }
            @Override
            protected void done() {
                try {
                    currentMovieList = get();
                    view.updateMainMovieList(currentMovieList);
                    onNavigate(VideoPlayerUI.PAGE_LIST);
                    view.showGlobalProgressComplete("Found " + currentMovieList.size() + " results", true);
                } catch (Exception e) {
                    view.showGlobalProgressComplete("Search failed", false);
                    view.showErrorMessage("Failed to search: " + e.getMessage());
                }
            }
        }.execute();
    }

    /**
     * Called when user selects a movie from the list.
     * Does NOT auto-play - just prepares the player page.
     */
    public void onMovieSelected(MovieDto selectedMovie) {
        if (selectedMovie == null) return;

        currentlyPlayingMovie = selectedMovie;
        view.updateNowPlayingLabel(selectedMovie.getTitle());
        view.updatePlayerMovieList(currentMovieList);

        // Navigate to player page (this will stop any currently playing video)
        onNavigate(VideoPlayerUI.PAGE_PLAYER);

        // Prepare the stream URL but DON'T auto-play
        String streamUrl = apiService.getBaseUrl() + "/movies/" + selectedMovie.getId() + "/stream";
        System.out.println("▶ Ready to play: " + selectedMovie.getTitle() + " (" + streamUrl + ")");

        // Load media but don't play yet - user must click play button
        view.getMediaPlayer().mediaPlayer().media().prepare(streamUrl, ":network-caching=300");
    }

    /**
     * Called when user clicks the Play button
     */
    public void onPlay() {
        // If already playing, just resume
        if (view.getMediaPlayer().mediaPlayer().status().isPlaying()) {
            return;
        }

        // If we have a movie loaded, play it
        if (currentlyPlayingMovie != null) {
            view.getMediaPlayer().mediaPlayer().controls().play();
            return;
        }

        // Otherwise, need to select from list first
        MovieDto selected = view.getSelectedMovieFromList();
        if (selected == null) {
            view.showInfoMessage("Please select a movie from the list first.");
            return;
        }

        // Select and prepare the movie, then play
        onMovieSelected(selected);
        view.getMediaPlayer().mediaPlayer().controls().play();
    }

    public void onPause() {
        view.getMediaPlayer().mediaPlayer().controls().pause();
    }

    public void onStop() {
        stopPlayback();
    }

    /**
     * Stop playback and clear the current movie
     */
    private void stopPlayback() {
        view.getMediaPlayer().mediaPlayer().controls().stop();
        System.out.println("⏹ Playback stopped");
    }

    /**
     * Navigate to a page - stops playback if leaving player page
     */
    public void onNavigate(String pageName) {
        // If leaving the player page, stop playback
        if (currentPage.equals(VideoPlayerUI.PAGE_PLAYER) && !pageName.equals(VideoPlayerUI.PAGE_PLAYER)) {
            stopPlayback();
            currentlyPlayingMovie = null;
            view.updateNowPlayingLabel("Select a movie");
        }

        currentPage = pageName;
        view.showPage(pageName);
    }

    public void onOpenLocalVideo() {
        File file = view.showLocalVideoDialog();
        if (file != null) {
            // Create a local movie DTO
            MovieDto localMovie = new MovieDto();
            localMovie.setId(-1L);
            localMovie.setTitle(file.getName() + " (Local)");
            localMovie.setUploadedDate(LocalDateTime.now());

            currentlyPlayingMovie = localMovie;
            view.updatePlayerMovieList(Collections.singletonList(localMovie));
            view.updateNowPlayingLabel(localMovie.getTitle());

            // Navigate to player
            onNavigate(VideoPlayerUI.PAGE_PLAYER);

            // For local files, prepare and auto-play
            view.getMediaPlayer().mediaPlayer().media().play(file.getAbsolutePath());
        }
    }

    public void onSkip(int seconds) {
        long time = view.getMediaPlayer().mediaPlayer().status().time();
        long newTime = Math.max(0, time + (seconds * 1000L));
        view.getMediaPlayer().mediaPlayer().controls().setTime(newTime);
    }

    // =========================================================================
    // UPLOAD DIALOG
    // =========================================================================

    public void onShowUploadDialog() {
        currentUploadDialog = new UploadDialog(view);
        currentUploadDialog.setOnUploadStart(this::handleUploadStart);
        currentUploadDialog.setVisible(true);
    }

    private void handleUploadStart(UploadDialog.UploadResult result) {
        cancelRequested.set(false);
        final String fileName = result.file.getName();
        final long fileSize = result.file.length();

        currentUploadDialog.setProgress(0, "Starting upload...");
        view.showGlobalProgress("UPLOAD", fileName, 0, "Starting...");

        new SwingWorker<String, Integer>() {
            @Override
            protected String doInBackground() throws Exception {
                JobResponse job = apiService.startUpload(result.file, result.title, result.resolution,
                        percent -> { if (!cancelRequested.get()) publish(percent); });
                return job.getJobId();
            }

            @Override
            protected void process(List<Integer> chunks) {
                if (cancelRequested.get()) return;
                int percent = chunks.get(chunks.size() - 1);
                currentUploadDialog.setProgress(percent, "Uploading: " + percent + "%");
                view.showGlobalProgress("UPLOAD", fileName, percent,
                        String.format("%.1f MB", (fileSize * percent / 100.0) / (1024 * 1024)));
            }

            @Override
            protected void done() {
                if (cancelRequested.get()) return;
                try {
                    String jobId = get();
                    currentUploadDialog.setIndeterminate("Encoding on server...");
                    view.showGlobalProgress("UPLOAD", fileName, 100, "Encoding...");
                    startUploadEncodingPoller(jobId, fileName);
                } catch (Exception e) {
                    currentUploadDialog.setComplete(false, "Upload failed: " + e.getMessage());
                    view.showGlobalProgressComplete("Upload failed", false);
                }
            }
        }.execute();
    }

    private void startUploadEncodingPoller(String jobId, String fileName) {
        currentPoller = new Timer(true);
        currentPoller.schedule(new TimerTask() {
            @Override
            public void run() {
                if (cancelRequested.get()) { currentPoller.cancel(); return; }
                try {
                    UploadJob job = apiService.getUploadStatus(jobId);
                    SwingUtilities.invokeLater(() -> {
                        switch (job.getStatus()) {
                            case "PENDING":
                                currentUploadDialog.setIndeterminate("Waiting in queue...");
                                break;
                            case "ENCODING":
                                currentUploadDialog.setProgress(job.getProgress(),
                                        "Encoding: " + job.getProgress() + "%");
                                view.showGlobalProgress("UPLOAD", fileName, job.getProgress(), "Encoding...");
                                break;
                            case "COMPLETED":
                                currentPoller.cancel();
                                currentUploadDialog.setComplete(true, "Upload complete!");
                                view.showGlobalProgressComplete("Upload complete!", true);
                                break;
                            case "FAILED":
                                currentPoller.cancel();
                                currentUploadDialog.setComplete(false, "Failed: " + job.getErrorMessage());
                                view.showGlobalProgressComplete("Upload failed", false);
                                break;
                        }
                    });
                } catch (Exception e) {
                    currentPoller.cancel();
                    SwingUtilities.invokeLater(() -> {
                        currentUploadDialog.setComplete(false, "Error: " + e.getMessage());
                        view.showGlobalProgressComplete("Error", false);
                    });
                }
            }
        }, 0, 2000);
    }

    // =========================================================================
    // COMPRESS DIALOG
    // =========================================================================

    public void onShowCompressDialog() {
        currentCompressDialog = new CompressDialog(view);
        currentCompressDialog.setOnCompressStart(this::handleCompressStart);
        currentCompressDialog.setVisible(true);
    }

    private void handleCompressStart(CompressDialog.CompressResult result) {
        currentCompressDialog.setProgress(0, "Starting compression...");
        view.showGlobalProgress("COMPRESS", result.source.getName(), 0, "Compressing...");

        new SwingWorker<File, Integer>() {
            @Override
            protected File doInBackground() throws Exception {
                return compressionService.compress(
                        result.source, result.output,
                        result.resolution, result.codec, result.quality,
                        percent -> publish(percent)
                );
            }

            @Override
            protected void process(List<Integer> chunks) {
                int percent = chunks.get(chunks.size() - 1);
                currentCompressDialog.setProgress(percent, "Compressing: " + percent + "%");
                view.showGlobalProgress("COMPRESS", result.source.getName(), percent, "Compressing...");
            }

            @Override
            protected void done() {
                try {
                    File output = get();
                    long origSize = result.source.length();
                    long newSize = output.length();
                    double ratio = (1.0 - ((double) newSize / origSize)) * 100;

                    String message = String.format("Complete! Reduced by %.1f%%", ratio);
                    currentCompressDialog.setComplete(true, message);
                    view.showGlobalProgressComplete("Compression complete!", true);

                    JOptionPane.showMessageDialog(currentCompressDialog,
                            String.format("Compression complete!\n\n" +
                                            "Original: %.2f MB\n" +
                                            "Compressed: %.2f MB\n" +
                                            "Reduced by: %.1f%%\n\n" +
                                            "Saved to: %s",
                                    origSize / (1024.0 * 1024.0),
                                    newSize / (1024.0 * 1024.0),
                                    ratio,
                                    output.getAbsolutePath()),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    currentCompressDialog.setComplete(false, "Failed: " + e.getMessage());
                    view.showGlobalProgressComplete("Compression failed", false);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    // =========================================================================
    // DOWNLOAD DIALOG
    // =========================================================================

    public void onShowDownloadDialog() {
        MovieDto selected = currentlyPlayingMovie != null ? currentlyPlayingMovie : view.getSelectedMovieFromList();

        if (selected == null) {
            view.showInfoMessage("Please select a movie to download.");
            return;
        }
        if (selected.getId() != null && selected.getId() == -1L) {
            view.showInfoMessage("This is a local file - no download needed.");
            return;
        }

        currentDownloadDialog = new DownloadDialog(view, selected);
        currentDownloadDialog.setOnDownloadStart(this::handleDownloadStart);
        currentDownloadDialog.setOnCancel(() -> cancelRequested.set(true));
        currentDownloadDialog.setVisible(true);
    }

    private void handleDownloadStart(DownloadDialog.DownloadResult result) {
        cancelRequested.set(false);
        final String fileName = result.movie.getTitle();
        final Long movieId = result.movie.getId();

        currentDownloadDialog.setProgress(0, "Connecting...");
        view.showGlobalProgress("DOWNLOAD", fileName, 0, "Connecting...");

        new SwingWorker<Void, Long>() {
            @Override
            protected Void doInBackground() throws Exception {
                apiService.downloadMovie(movieId, result.outputFile, result.resolution,
                        bytes -> { if (!cancelRequested.get()) publish(bytes); });
                return null;
            }

            @Override
            protected void process(List<Long> chunks) {
                if (cancelRequested.get()) return;
                long bytes = chunks.get(chunks.size() - 1);
                currentDownloadDialog.setProgress(bytes, "Downloading...");
                view.showGlobalProgress("DOWNLOAD", fileName, -1,
                        String.format("%.2f MB", bytes / (1024.0 * 1024.0)));
            }

            @Override
            protected void done() {
                if (cancelRequested.get()) {
                    currentDownloadDialog.setComplete(false, "Cancelled");
                    if (result.outputFile.exists()) result.outputFile.delete();
                    return;
                }
                try {
                    get();
                    currentDownloadDialog.setComplete(true, "Download complete!");
                    view.showGlobalProgressComplete("Download complete!", true);
                } catch (Exception e) {
                    currentDownloadDialog.setComplete(false, "Failed: " + e.getMessage());
                    view.showGlobalProgressComplete("Download failed", false);
                    if (result.outputFile.exists()) result.outputFile.delete();
                }
            }
        }.execute();
    }

    // Legacy method - now redirects to dialog
    public void onDownloadWithResolution() {
        onShowDownloadDialog();
    }

    // =========================================================================
    // PLAYBACK HELPERS
    // =========================================================================

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
        long sec = millis / 1000;
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    public MovieDto getCurrentlyPlayingMovie() { return currentlyPlayingMovie; }
    public String getCurrentDataSource() { return currentDataSource; }
    public String getCurrentPage() { return currentPage; }
}