package com.neu.finalproject.meskot.service;

import org.springframework.core.io.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface StorageService {
    String store(File file, String targetKey) throws IOException;

    /**
     * Store stream content (useful for chunk upload).
     */
    String store(InputStream data, String targetKey, long contentLength, String contentType) throws IOException;

    /**
     * Retrieve as Spring Resource (so controllers can stream to client).
     */
    Resource loadAsResource(String objectKey) throws IOException;

    /**
     * Delete object
     */
    void delete(String objectKey) throws IOException ;
}
