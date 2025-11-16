package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.model.StoredObject;
import com.neu.finalproject.meskot.repository.StoredObjectRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
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
public class LocalStorageService implements StorageService {

    @Value("${app.storage.local.base-dir:./data/objects}")
    private String baseDir;

    private final StoredObjectRepository storedObjectRepository;

    public LocalStorageService(StoredObjectRepository storedObjectRepository) {
        this.storedObjectRepository = storedObjectRepository;
    }

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
        return new FileSystemResource(p.toFile());
    }

    @Override
    @Transactional
    public void delete(String objectKey) throws IOException {
        Path p = toPath(objectKey);
        Files.deleteIfExists(p);
        storedObjectRepository.findByObjectKey(objectKey).ifPresent(storedObjectRepository::delete);
    }

    public File saveTempFile(MultipartFile multipartFile) throws IOException {
        // Create temp file in the system's default temp directory
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path tempFile = tempDir.resolve(
                UUID.randomUUID().toString() + "-" + multipartFile.getOriginalFilename()
        );

        // Transfer the file
        multipartFile.transferTo(tempFile);

        return tempFile.toFile();
    }
}
