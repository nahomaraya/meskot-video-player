package com.neu.finalproject.meskot.repository;

import com.neu.finalproject.meskot.model.UploadHistory;
import com.neu.finalproject.meskot.model.UploadHistory.UploadStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UploadHistoryRepository extends JpaRepository<UploadHistory, Integer> {
    Optional<UploadHistory> findByJobId(String jobId);
    List<UploadHistory> findByUserIdOrderByUploadedAtDesc(Integer userId);
    @Query("SELECT u FROM UploadHistory u WHERE u.userId = :userId ORDER BY u.uploadedAt DESC")
    List<UploadHistory> findByUserIdWithLimit(Integer userId, Pageable pageable);
    List<UploadHistory> findByStatus(UploadStatus status);
    @Query("SELECT u FROM UploadHistory u ORDER BY u.uploadedAt DESC")
    List<UploadHistory> findRecentUploads(Pageable pageable);
    @Query("SELECT u FROM UploadHistory u WHERE u.status IN ('PENDING','UPLOADING','ENCODING')")
    List<UploadHistory> findIncompleteUploads();
    long countByUserId(Integer userId);
    long countByStatus(UploadStatus status);
}
