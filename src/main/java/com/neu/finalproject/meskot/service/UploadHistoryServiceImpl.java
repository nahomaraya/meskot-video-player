package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.model.UploadHistory;
import com.neu.finalproject.meskot.model.UploadHistory.UploadStatus;
import com.neu.finalproject.meskot.repository.UploadHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UploadHistoryServiceImpl implements UploadHistoryService {
    private final UploadHistoryRepository uploadHistoryRepository;

    @Autowired
    public UploadHistoryServiceImpl(UploadHistoryRepository uploadHistoryRepository) {
        this.uploadHistoryRepository = uploadHistoryRepository;
    }

    @Override
    @Transactional
    public UploadHistory create(Integer userId, String movieTitle, String fileName, Long fileSizeBytes, String resolution, String jobId) {
        UploadHistory history = new UploadHistory();
        history.setUserId(userId);
        history.setMovieTitle(movieTitle);
        history.setFileName(fileName);
        history.setFileSizeBytes(fileSizeBytes);
        history.setResolution(resolution);
        history.setJobId(jobId);
        history.setStatus(UploadStatus.PENDING);
        history.setUploadedAt(LocalDateTime.now());
        return uploadHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void markUploading(String jobId) {
        updateStatus(jobId, UploadStatus.UPLOADING, null, null);
    }

    @Override
    @Transactional
    public void markEncoding(String jobId) {
        updateStatus(jobId, UploadStatus.ENCODING, null, null);
    }

    @Override
    @Transactional
    public void markCompleted(String jobId, Long movieId) {
        updateStatus(jobId, UploadStatus.COMPLETED, movieId, null);
    }

    @Override
    @Transactional
    public void markFailed(String jobId, String errorMessage) {
        updateStatus(jobId, UploadStatus.FAILED, null, errorMessage);
    }

    @Override
    @Transactional
    public void markCancelled(String jobId) {
        updateStatus(jobId, UploadStatus.CANCELLED, null, null);
    }

    @Override
    public List<UploadHistory> getHistoryForUser(Long userId, int limit) {
        return List.of();
    }

    @Override
    public List<UploadHistory> getAllHistory(int limit) {
        return uploadHistoryRepository.findRecentUploads(PageRequest.of(0, limit));
    }

    @Override
    public Optional<UploadHistory> getByJobId(String jobId) {
        return uploadHistoryRepository.findByJobId(jobId);
    }

    private void updateStatus(String jobId, UploadStatus status, Long movieId, String errorMessage) {
        Optional<UploadHistory> opt = uploadHistoryRepository.findByJobId(jobId);
        if (opt.isPresent()) {
            UploadHistory uh = opt.get();
            uh.setStatus(status);
            if (status == UploadStatus.COMPLETED) {
                uh.setMovieId(movieId);
                uh.setCompletedAt(LocalDateTime.now());
            }
            if (errorMessage != null) {
                uh.setErrorMessage(errorMessage);
            }
            uploadHistoryRepository.save(uh);
        }
    }
}
