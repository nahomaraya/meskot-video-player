package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.model.UploadHistory;
import java.util.List;
import java.util.Optional;

public interface UploadHistoryService {
    // Factory method
    UploadHistory create(Integer userId, String movieTitle, String fileName, Long fileSizeBytes, String resolution, String jobId);

    // Status updates
    void markUploading(String jobId);
    void markEncoding(String jobId);
    void markCompleted(String jobId, Long movieId);
    void markFailed(String jobId, String errorMessage);
    void markCancelled(String jobId);

    // Queries
    List<UploadHistory> getHistoryForUser(Long userId, int limit);
    List<UploadHistory> getAllHistory(int limit);
    Optional<UploadHistory> getByJobId(String jobId);
}
