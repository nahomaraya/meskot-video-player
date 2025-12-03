package com.neu.finalproject.meskot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "upload_history")
@Getter
@Setter
public class UploadHistory {
    public Object getTitle() {
        return null;
    }

    public Object getProgress() {
        return null;
    }

    public Object getSizeBytes() {
        return null;
    }

    public enum UploadStatus {
        PENDING, UPLOADING, ENCODING, COMPLETED, FAILED, CANCELLED;
        public boolean isInProgress() {
            return this == PENDING || this == UPLOADING || this == ENCODING;
        }
        public boolean isTerminal() {
            return this == COMPLETED || this == FAILED || this == CANCELLED;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "upload_id")
    private Integer uploadId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "movie_title", nullable = false)
    private String movieTitle;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private UploadStatus status = UploadStatus.PENDING;

    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "error_message")
    private String errorMessage;

    public String getFormattedFileSize() {
        if (fileSizeBytes == null) return "-";
        double kb = fileSizeBytes / 1024.0;
        double mb = kb / 1024.0;
        if (mb >= 1) return String.format("%.2f MB", mb);
        if (kb >= 1) return String.format("%.2f KB", kb);
        return fileSizeBytes + " bytes";
    }
}
