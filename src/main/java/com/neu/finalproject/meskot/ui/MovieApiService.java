package com.neu.finalproject.meskot.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.service.ProgressCallback;
import com.neu.finalproject.meskot.ui.dto.JobResponse;
import com.neu.finalproject.meskot.ui.dto.UploadJob;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import java.util.List;
import java.util.function.Consumer;

/**
 * The Service (M in MVP, sort of).
 * This class handles all networking and data transformation (JSON).
 * It is completely UI-agnostic.
 */
public class MovieApiService {

    private final String baseUrl = "http://localhost:8080/api";
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public MovieApiService() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.httpClient = HttpClientBuilder.create().build();
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Performs a simple blocking HTTP GET and returns the JSON string.
     */
    private String httpGet(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            return json.toString();
        }
    }

    public List<MovieDto> getMovies() throws IOException {
        String json = httpGet(baseUrl + "/movies");
        return objectMapper.readValue(json, new TypeReference<List<MovieDto>>() {});
    }

    public List<MovieDto> searchMovies(String query) throws IOException {
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String json = httpGet(baseUrl + "/search?query=" + encodedQuery);
        return objectMapper.readValue(json, new TypeReference<List<MovieDto>>() {});
    }

    public void downloadMovie(long movieId, File outputFile, Consumer<Long> progressCallback) throws IOException {
        URL url = new URL(baseUrl + "/" + movieId + "/download");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new IOException("Server responded with: " + conn.getResponseMessage());
        }

        try (InputStream in = new BufferedInputStream(conn.getInputStream());
             FileOutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytesRead = 0;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                if (progressCallback != null) {
                    progressCallback.accept(totalBytesRead);
                }
            }
        }
    }

    public JobResponse startUpload(File file, String title, String resolution, Consumer<Integer> progressCallback) throws IOException {
        HttpPost post = new HttpPost(baseUrl + "/upload");

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addPart("file", new FileBody(file, ContentType.DEFAULT_BINARY));
        builder.addTextBody("title", title, ContentType.TEXT_PLAIN);
        builder.addTextBody("resolution", resolution, ContentType.TEXT_PLAIN);

        HttpEntity entity = builder.build();
        ProgressiveHttpEntity progressiveEntity = new ProgressiveHttpEntity(entity, progressCallback);
        post.setEntity(progressiveEntity);

        HttpResponse response = httpClient.execute(post);
        String json = EntityUtils.toString(response.getEntity());

        if (response.getStatusLine().getStatusCode() != 202) {
            throw new IOException("Upload failed: " + json);
        }
        return objectMapper.readValue(json, JobResponse.class);
    }

    public UploadJob getUploadStatus(String jobId) throws IOException {
        String json = httpGet(baseUrl + "/upload-status/" + jobId);
        return objectMapper.readValue(json, UploadJob.class);
    }

    // --- Helper classes for upload progress ---

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

        // ... (Delegate all other HttpEntity methods) ...
        @Override public boolean isRepeatable() { return delegate.isRepeatable(); }
        @Override public boolean isChunked() { return delegate.isChunked(); }
        @Override public long getContentLength() { return delegate.getContentLength(); }
        @Override public org.apache.http.Header getContentType() { return delegate.getContentType(); }
        @Override public org.apache.http.Header getContentEncoding() { return delegate.getContentEncoding(); }
        @Override public InputStream getContent() throws IOException, IllegalStateException { return delegate.getContent(); }
        @Override public boolean isStreaming() { return delegate.isStreaming(); }
        @Override public void consumeContent() throws IOException { delegate.consumeContent(); }
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

    public String startConversion(Long movieId, String resolution) throws IOException {
        String url = baseUrl + "/" + movieId + "/download?resolution=" + resolution;
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            String response = new String(conn.getInputStream().readAllBytes());
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();
            return json.get("jobId").getAsString();
        }
        throw new IOException("Failed to start conversion: " + responseCode);
    }

    public UploadJob getDownloadStatus(String jobId) throws IOException {
        String url = baseUrl + "/download-status/" + jobId;
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();

        String response = new String(conn.getInputStream().readAllBytes());
        Gson gson = new Gson();
        return gson.fromJson(response, UploadJob.class);
    }

    public void downloadConvertedFile(String jobId, File outputFile, ProgressCallback callback) throws IOException {
        String url = baseUrl + "/download-result/" + jobId;

        // Download directly from the URL instead of calling downloadMovie
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("Failed to download: HTTP " + responseCode);
        }

        long totalBytes = conn.getContentLengthLong();

        try (InputStream in = conn.getInputStream();
             FileOutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192];
            long downloaded = 0;
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                downloaded += bytesRead;
                if (callback != null) {
                    callback.onProgress((int) downloaded);
                }
            }
        }
    }
}