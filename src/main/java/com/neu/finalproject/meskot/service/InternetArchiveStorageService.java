package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.model.StoredObject;
import com.neu.finalproject.meskot.repository.StoredObjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class InternetArchiveStorageService implements StorageService {

    @Value("${app.storage.local.base-dir:./data/objects}")
    private String baseDir;

    private final StoredObjectRepository storedObjectRepository;
    private S3Client s3Client;

    public InternetArchiveStorageService(StoredObjectRepository storedObjectRepository) {
        this.storedObjectRepository = storedObjectRepository;
    }

    private S3Client getS3Client() {
        if (s3Client == null) {
            // Anonymous access for reading public files
            s3Client = S3Client.builder()
                    .endpointOverride(URI.create("https://s3.us.archive.org"))
                    .region(Region.US_EAST_1)
                    .credentialsProvider(AnonymousCredentialsProvider.create())
                    .build();
        }
        return s3Client;
    }

    /**
     * Download a file from Internet Archive and store locally
     * @param itemIdentifier The Internet Archive item identifier
     * @param fileKey The file name within the item
     * @return The local storage key
     */
    @Transactional
    public String downloadFromArchive(String itemIdentifier, String fileKey) throws IOException {
        try {
            // Download from Internet Archive
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(itemIdentifier)
                    .key(fileKey)
                    .build();

            byte[] data = getS3Client().getObjectAsBytes(getObjectRequest).asByteArray();

            // Store locally
            String localKey = UUID.randomUUID().toString() + "-" + fileKey;
            Path dest = Paths.get(baseDir, localKey);
            Files.createDirectories(dest.getParent());
            Files.write(dest, data);

            // Get metadata
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(itemIdentifier)
                    .key(fileKey)
                    .build();
            HeadObjectResponse headResponse = getS3Client().headObject(headRequest);

            // Save to database
            StoredObject obj = new StoredObject();
            obj.setObjectKey(localKey);
            obj.setContentType(headResponse.contentType());
            obj.setSize(headResponse.contentLength());
            obj.setLocationType("LOCAL");
            obj.setName(fileKey);
            obj.setBucketName(itemIdentifier); // Store original IA item for reference
            storedObjectRepository.save(obj);

            return localKey;
        } catch (NoSuchKeyException e) {
            throw new FileNotFoundException("File not found in Internet Archive: " + itemIdentifier + "/" + fileKey);
        } catch (S3Exception e) {
            throw new IOException("Error downloading from Internet Archive: " + e.getMessage(), e);
        }
    }

    /**
     * Stream a file directly from Internet Archive without storing locally
     * @param itemIdentifier The Internet Archive item identifier
     * @param fileKey The file name within the item
     */
    public Resource streamFromArchive(String itemIdentifier, String fileKey) throws IOException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(itemIdentifier)
                    .key(fileKey)
                    .build();

            byte[] data = getS3Client().getObjectAsBytes(getObjectRequest).asByteArray();

            return new ByteArrayResource(data);
        } catch (NoSuchKeyException e) {
            throw new FileNotFoundException("File not found in Internet Archive: " + itemIdentifier + "/" + fileKey);
        } catch (S3Exception e) {
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
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(itemIdentifier)
                    .key(fileKey)
                    .build();
            getS3Client().headObject(headRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
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