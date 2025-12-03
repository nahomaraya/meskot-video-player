package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.model.StoredObject;
import com.neu.finalproject.meskot.repository.StoredObjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class SupabaseStorageService implements StorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.access.key}")
    private String supabaseKey;

    @Value("${supabase.storage.bucket}")
    private String bucketName;

    private final StoredObjectRepository storedObjectRepository;
    private final RestTemplate restTemplate;

    public SupabaseStorageService(StoredObjectRepository storedObjectRepository) {
        this.storedObjectRepository = storedObjectRepository;
        this.restTemplate = new RestTemplate();
    }

    private String getStorageUrl() {
        return supabaseUrl + "/storage/v1/object/" + bucketName;
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.set("apikey", supabaseKey);
        return headers;
    }

    @Override
    @Transactional
    public String store(File file, String targetKey) throws IOException {
        String key = targetKey != null ? targetKey : UUID.randomUUID().toString() + "-" + file.getName();

        byte[] fileBytes = Files.readAllBytes(file.toPath());
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        uploadToSupabase(key, fileBytes, contentType);

        StoredObject obj = new StoredObject();
        obj.setObjectKey(key);
        obj.setContentType(contentType);
        obj.setSize(file.length());
        obj.setLocationType("SUPABASE");
        obj.setName(file.getName());
        storedObjectRepository.save(obj);

        return key;
    }

    @Override
    @Transactional
    public String store(InputStream data, String targetKey, long contentLength, String contentType) throws IOException {
        String key = targetKey != null ? targetKey : UUID.randomUUID().toString();

        byte[] fileBytes = data.readAllBytes();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        uploadToSupabase(key, fileBytes, contentType);

        StoredObject obj = new StoredObject();
        obj.setObjectKey(key);
        obj.setContentType(contentType);
        obj.setSize(contentLength);
        obj.setLocationType("SUPABASE");
        obj.setName(key);
        storedObjectRepository.save(obj);

        return key;
    }

    private void uploadToSupabase(String key, byte[] data, String contentType) throws IOException {
        HttpHeaders headers = createHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(data, headers);

        String url = getStorageUrl() + "/" + key;

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IOException("Failed to upload to Supabase: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new IOException("Error uploading to Supabase: " + e.getMessage(), e);
        }
    }

    @Override
    public Resource loadAsResource(String objectKey) throws IOException {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = getStorageUrl() + "/" + objectKey;

        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    byte[].class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new FileNotFoundException("Object not found: " + objectKey);
            }

            return new ByteArrayResource(response.getBody());
        } catch (Exception e) {
            throw new IOException("Error loading from Supabase: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void delete(String objectKey) throws IOException {
        HttpHeaders headers = createHeaders();
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        String url = getStorageUrl() + "/" + objectKey;

        try {
            restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            storedObjectRepository.findByObjectKey(objectKey).ifPresent(storedObjectRepository::delete);
        } catch (Exception e) {
            throw new IOException("Error deleting from Supabase: " + e.getMessage(), e);
        }
    }

    public File saveTempFile(MultipartFile multipartFile) throws IOException {
        // Create temp file in the system's default temp directory
        File tempFile = File.createTempFile(
                UUID.randomUUID().toString() + "-",
                "-" + multipartFile.getOriginalFilename()
        );

        // Set to delete on exit
        tempFile.deleteOnExit();

        // Transfer the file
        multipartFile.transferTo(tempFile);

        return tempFile;
    }
}