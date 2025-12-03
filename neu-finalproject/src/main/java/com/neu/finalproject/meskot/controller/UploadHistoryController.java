package com.neu.finalproject.meskot.controller;

import com.neu.finalproject.meskot.model.UploadHistory;
import com.neu.finalproject.meskot.interceptor.RequiresAdmin;
import com.neu.finalproject.meskot.interceptor.RequiresAuth;
import com.neu.finalproject.meskot.security.SessionManager;
import com.neu.finalproject.meskot.service.UploadHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/history/uploads")
public class UploadHistoryController {
    private final UploadHistoryService uploadHistoryService;
    private final SessionManager sessionManager;
    private static final int DEFAULT_LIMIT = 50;

    @Autowired
    public UploadHistoryController(UploadHistoryService uploadHistoryService, SessionManager sessionManager) {
        this.uploadHistoryService = uploadHistoryService;
        this.sessionManager = sessionManager;
    }

    @RequiresAuth
    @GetMapping("/my")
    public ResponseEntity<List<UploadHistory>> getMyUploads(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        Long userId = sessionManager.getCurrentUser().getId();
        return ResponseEntity.ok(uploadHistoryService.getHistoryForUser(userId, limit));
    }

    @RequiresAuth
    @GetMapping("/job/{jobId}")
    public ResponseEntity<?> getUploadByJobId(@PathVariable String jobId) {
        return uploadHistoryService.getByJobId(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @RequiresAdmin
    @GetMapping("/all")
    public ResponseEntity<List<UploadHistory>> getAllUploads(@RequestParam(value = "limit", defaultValue = "50") int limit) {
        return ResponseEntity.ok(uploadHistoryService.getAllHistory(limit));
    }

    @RequiresAdmin
    @GetMapping("/stats")
    public ResponseEntity<?> getUploadStats() {
        // Stub: Implement stats aggregation if desired
        return ResponseEntity.ok("Stats endpoint stub");
    }

    @RequiresAuth
    @PostMapping("/cancel/{jobId}")
    public ResponseEntity<?> cancelUpload(@PathVariable String jobId) {
        uploadHistoryService.markCancelled(jobId);
        return ResponseEntity.ok("Upload cancelled");
    }
}
