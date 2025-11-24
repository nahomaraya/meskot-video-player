package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.model.StoredObject;
import com.neu.finalproject.meskot.repository.StoredObjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class InternetArchiveStorageService implements StorageService {

    @Value("${app.storage.local.base-dir:./data/objects}")
    private String baseDir;

    private final StoredObjectRepository storedObjectRepository;

    public InternetArchiveStorageService(StoredObjectRepository storedObjectRepository) {
        this.storedObjectRepository = storedObjectRepository;
    }

    /**
     * Stream a file from Internet Archive with range support
     * Supports partial content (HTTP 206) for video seeking
     */
    public Resource streamFromArchiveWithRange(String itemIdentifier, String fileKey, String rangeHeader) throws IOException {
        try {
            String encodedFileKey = java.net.URLEncoder.encode(fileKey, "UTF-8")
                    .replace("+", "%20");

            String url = String.format("https://archive.org/download/%s/%s", itemIdentifier, encodedFileKey);

            System.out.println("Streaming with range from: " + url);
            System.out.println("Range: " + rangeHeader);

            java.net.URL downloadUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);
            connection.setInstanceFollowRedirects(true);

            // Add Range header if provided
            if (rangeHeader != null && !rangeHeader.isEmpty()) {
                connection.setRequestProperty("Range", rangeHeader);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response code: " + responseCode);

            // Accept both 200 (full content) and 206 (partial content)
            if (responseCode == 200 || responseCode == 206) {
                // Return the input stream directly without buffering entire file
                InputStream inputStream = connection.getInputStream();
                return new InputStreamResource(inputStream) {
                    @Override
                    public long contentLength() throws IOException {
                        String contentLength = connection.getHeaderField("Content-Length");
                        return contentLength != null ? Long.parseLong(contentLength) : -1;
                    }
                };
            } else {
                connection.disconnect();
                throw new IOException("Failed to stream from Internet Archive. HTTP " + responseCode);
            }

        } catch (Exception e) {
            throw new IOException("Error streaming from Internet Archive: " + e.getMessage(), e);
        }
    }

    /**
     * Get content length and metadata for a file without downloading it
     */
    public long getContentLength(String itemIdentifier, String fileKey) throws IOException {
        try {
            String encodedFileKey = java.net.URLEncoder.encode(fileKey, "UTF-8")
                    .replace("+", "%20");

            String url = String.format("https://archive.org/download/%s/%s", itemIdentifier, encodedFileKey);

            java.net.URL downloadUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                long length = connection.getContentLengthLong();
                connection.disconnect();
                return length;
            } else {
                connection.disconnect();
                throw new IOException("Failed to get file info. HTTP " + responseCode);
            }
        } catch (Exception e) {
            throw new IOException("Error getting file info: " + e.getMessage(), e);
        }
    }

    /**
     * Stream a file directly from Internet Archive without storing locally
     * Downloads entire file - use streamFromArchiveWithRange for better performance
     */
    public Resource streamFromArchive(String itemIdentifier, String fileKey) throws IOException {
        try {
            // URL encode the file key to handle spaces and special characters
            String encodedFileKey = java.net.URLEncoder.encode(fileKey, "UTF-8")
                    .replace("+", "%20");

            String url = String.format("https://archive.org/download/%s/%s", itemIdentifier, encodedFileKey);

            System.out.println("Downloading from: " + url);

            // Download the file
            java.net.URL downloadUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                InputStream inputStream = connection.getInputStream();

                byte[] data = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }

                inputStream.close();
                connection.disconnect();

                return new ByteArrayResource(buffer.toByteArray());
            } else {
                throw new IOException("Failed to download from Internet Archive. HTTP " + responseCode);
            }

        } catch (Exception e) {
            throw new IOException("Error streaming from Internet Archive: " + e.getMessage(), e);
        }
    }

    /**
     * Get the public URL for a file on Internet Archive
     */
    public String getArchiveUrl(String itemIdentifier, String fileKey) {
        return String.format("https://archive.org/download/%s/%s", itemIdentifier, fileKey);
    }

    /**
     * Check if a file exists in Internet Archive
     */
    public boolean existsInArchive(String itemIdentifier, String fileKey) {
        try {
            String encodedFileKey = java.net.URLEncoder.encode(fileKey, "UTF-8")
                    .replace("+", "%20");

            String url = String.format("https://archive.org/download/%s/%s", itemIdentifier, encodedFileKey);

            java.net.URL downloadUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setInstanceFollowRedirects(true);

            int responseCode = connection.getResponseCode();
            connection.disconnect();

            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    // Original local storage methods for uploading user content

    private Path toPath(String key) {
        return Paths.get(baseDir, key);
    }

    @Override
    @Transactional
    public String store(File file, String targetKey) throws IOException {
        String key = targetKey != null ? targetKey : UUID.randomUUID().toString() + "-" + file.getName();
        Path dest = toPath(key);
        Files.createDirectories(dest.getParent());
        Files.copy(file.toPath(), dest);

        StoredObject obj = new StoredObject();
        obj.setObjectKey(key);
        obj.setContentType(Files.probeContentType(file.toPath()));
        obj.setSize(file.length());
        obj.setLocationType("LOCAL");
        obj.setName(file.getName());
        storedObjectRepository.save(obj);
        return dest.toAbsolutePath().toString();
    }

    @Override
    @Transactional
    public String store(InputStream data, String targetKey, long contentLength, String contentType) throws IOException {
        String key = targetKey != null ? targetKey : UUID.randomUUID().toString();
        Path dest = toPath(key);
        Files.createDirectories(dest.getParent());
        try (OutputStream os = Files.newOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int r;
            while ((r = data.read(buf)) != -1) {
                os.write(buf, 0, r);
            }
        }

        StoredObject obj = new StoredObject();
        obj.setObjectKey(key);
        obj.setContentType(contentType);
        obj.setSize(contentLength);
        obj.setLocationType("LOCAL");
        obj.setName(key);
        storedObjectRepository.save(obj);

        return key;
    }

    @Override
    public Resource loadAsResource(String objectKey) throws IOException {
        Path p = toPath(objectKey);
        if (!Files.exists(p)) throw new FileNotFoundException("Object not found: " + objectKey);
        return new org.springframework.core.io.FileSystemResource(p.toFile());
    }

    @Override
    @Transactional
    public void delete(String objectKey) throws IOException {
        Path p = toPath(objectKey);
        Files.deleteIfExists(p);
        storedObjectRepository.findByObjectKey(objectKey).ifPresent(storedObjectRepository::delete);
    }

    public File saveTempFile(MultipartFile multipartFile) throws IOException {
        File tempFile = File.createTempFile(
                UUID.randomUUID().toString() + "-",
                "-" + multipartFile.getOriginalFilename()
        );

        tempFile.deleteOnExit();
        multipartFile.transferTo(tempFile);

        return tempFile;
    }
}