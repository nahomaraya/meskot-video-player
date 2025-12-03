package com.neu.finalproject.meskot.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.ui.dto.JobResponse;
import com.neu.finalproject.meskot.ui.dto.UploadJob;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Consumer;

public class MovieApiService {

    private final String baseUrl = "http://localhost:8080/api";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private String currentDataSource = "Local Storage";

    public MovieApiService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.httpClient = HttpClientBuilder.create().build();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setDataSource(String source) {
        this.currentDataSource = source;
        System.out.println("MovieApiService: Data source set to " + source);
    }

    public String getDataSource() {
        return currentDataSource;
    }

    // =========================================================================
    // URL BUILDERS
    // =========================================================================

    private String getMoviesEndpoint() {
        switch (currentDataSource) {
            case "Internet Archive":
                return baseUrl + "/movies/internet-archive";
            case "Supabase":
                return baseUrl + "/movies/supabase";
            case "Local Storage":
            default:
                return baseUrl + "/movies/local";
        }
    }

    private String getSearchEndpoint(String query) throws UnsupportedEncodingException {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
        switch (currentDataSource) {
            case "Internet Archive":
                return baseUrl + "/movies/internet-archive/search?q=" + encodedQuery;
            case "Supabase":
                return baseUrl + "/movies/supabase/search?q=" + encodedQuery;
            case "Local Storage":
            default:
                return baseUrl + "/movies/local/search?q=" + encodedQuery;
        }
    }

    // =========================================================================
    // MOVIE LISTING
    // =========================================================================

    public List<MovieDto> getMovies() throws Exception {
        String endpoint = getMoviesEndpoint();
        System.out.println("Fetching movies from: " + endpoint);
        return fetchMovieList(endpoint);
    }

    public List<MovieDto> searchMovies(String query) throws Exception {
        String endpoint = getSearchEndpoint(query);
        System.out.println("Searching movies at: " + endpoint);
        return fetchMovieList(endpoint);
    }

    public List<MovieDto> getAllMovies() throws Exception {
        return fetchMovieList(baseUrl + "/movies");
    }

    private List<MovieDto> fetchMovieList(String endpoint) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode);

        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return objectMapper.readValue(response.toString(),
                        new TypeReference<List<MovieDto>>() {});
            }
        } else {
            // Read error response
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                throw new Exception("HTTP " + responseCode + ": " + error.toString());
            }
        }
    }

    // =========================================================================
    // UPLOAD
    // =========================================================================

    public JobResponse startUpload(File file, String title, String resolution,
                                   Consumer<Integer> progressCallback) throws IOException {
        return startUpload(file, title, resolution, "LOCAL", progressCallback);
    }

    public JobResponse startUpload(File file, String title, String resolution,
                                   String sourceType, Consumer<Integer> progressCallback) throws IOException {
        HttpPost post = new HttpPost(baseUrl + "/movies/upload");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", new FileBody(file, ContentType.DEFAULT_BINARY));
        builder.addTextBody("title", title, ContentType.TEXT_PLAIN);
        builder.addTextBody("resolution", resolution, ContentType.TEXT_PLAIN);
        builder.addTextBody("sourceType", sourceType, ContentType.TEXT_PLAIN);

        HttpEntity entity = builder.build();
        ProgressiveHttpEntity progressiveEntity = new ProgressiveHttpEntity(entity, progressCallback);
        post.setEntity(progressiveEntity);

        HttpResponse response = httpClient.execute(post);
        String json = EntityUtils.toString(response.getEntity());

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200 && statusCode != 202) {
            throw new IOException("Upload failed (HTTP " + statusCode + "): " + json);
        }
        return objectMapper.readValue(json, JobResponse.class);
    }

    public UploadJob getUploadStatus(String jobId) throws Exception {
        String endpoint = baseUrl + "/jobs/" + jobId;

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                // Parse manually to handle field name differences
                JsonNode root = objectMapper.readTree(response.toString());
                UploadJob job = new UploadJob();
                job.setId(root.has("jobId") ? root.get("jobId").asText() : jobId);
                job.setStatus(root.has("status") ? root.get("status").asText() : "UNKNOWN");
                job.setProgress(root.has("progress") ? root.get("progress").asInt() : 0);
                job.setErrorMessage(root.has("errorMessage") && !root.get("errorMessage").isNull()
                        ? root.get("errorMessage").asText() : null);
                return job;
            }
        } else {
            throw new Exception("Failed to get job status. HTTP " + responseCode);
        }
    }

    // =========================================================================
    // DOWNLOAD
    // =========================================================================

    public void downloadMovie(Long movieId, File outputFile, String resolution,
                              Consumer<Long> progressCallback) throws IOException {
        String encodedResolution;
        try {
            encodedResolution = URLEncoder.encode(resolution, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            encodedResolution = resolution;
        }

        String endpoint = baseUrl + "/movies/" + movieId + "/download?resolution=" + encodedResolution;
        System.out.println("Downloading from: " + endpoint);

        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(0); // No timeout for large downloads
        conn.setInstanceFollowRedirects(true);

        int responseCode = conn.getResponseCode();
        System.out.println("Download response code: " + responseCode);

        // Handle redirects manually if needed
        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER ||
                responseCode == 307 || responseCode == 308) {

            String newUrl = conn.getHeaderField("Location");
            System.out.println("Redirecting to: " + newUrl);
            conn = (HttpURLConnection) new URL(newUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(true);
            responseCode = conn.getResponseCode();
        }

        if (responseCode == 200) {
            long contentLength = conn.getContentLengthLong();
            System.out.println("Content length: " + contentLength);

            try (InputStream in = conn.getInputStream();
                 FileOutputStream out = new FileOutputStream(outputFile)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;

                    if (progressCallback != null) {
                        progressCallback.accept(totalBytes);
                    }
                }

                System.out.println("Download complete: " + totalBytes + " bytes");
            }
        } else {
            // Read error message
            String errorMessage = "HTTP " + responseCode;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream()))) {
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                errorMessage += ": " + error.toString();
            } catch (Exception e) {
                // Ignore if can't read error stream
            }
            throw new IOException("Download failed. " + errorMessage);
        }
    }

    public void downloadMovie(Long movieId, File outputFile,
                              Consumer<Long> progressCallback) throws IOException {
        downloadMovie(movieId, outputFile, "Original", progressCallback);
    }

    // =========================================================================
    // HELPER CLASSES FOR UPLOAD PROGRESS
    // =========================================================================

    private static class ProgressiveHttpEntity implements HttpEntity {
        private final HttpEntity delegate;
        private final Consumer<Integer> progressCallback;

        public ProgressiveHttpEntity(HttpEntity delegate, Consumer<Integer> progressCallback) {
            this.delegate = delegate;
            this.progressCallback = progressCallback;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            delegate.writeTo(new ProgressOutputStream(out, progressCallback, getContentLength()));
        }

        @Override public boolean isRepeatable() { return delegate.isRepeatable(); }
        @Override public boolean isChunked() { return delegate.isChunked(); }
        @Override public long getContentLength() { return delegate.getContentLength(); }
        @Override public org.apache.http.Header getContentType() { return delegate.getContentType(); }
        @Override public org.apache.http.Header getContentEncoding() { return delegate.getContentEncoding(); }
        @Override public InputStream getContent() throws IOException { return delegate.getContent(); }
        @Override public boolean isStreaming() { return delegate.isStreaming(); }
        @Override public void consumeContent() throws IOException { }
    }

    private static class ProgressOutputStream extends FilterOutputStream {
        private final Consumer<Integer> progressCallback;
        private final long totalSize;
        private long transferred;
        private int lastPercent = -1;

        public ProgressOutputStream(OutputStream out, Consumer<Integer> progressCallback, long totalSize) {
            super(out);
            this.progressCallback = progressCallback;
            this.totalSize = totalSize;
            this.transferred = 0;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            updateProgress(len);
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            updateProgress(1);
        }

        private void updateProgress(long len) {
            this.transferred += len;
            if (totalSize > 0) {
                int percent = (int) ((this.transferred * 100) / this.totalSize);
                if (percent > lastPercent && progressCallback != null) {
                    lastPercent = percent;
                    progressCallback.accept(percent);
                }
            }
        }
    }
}