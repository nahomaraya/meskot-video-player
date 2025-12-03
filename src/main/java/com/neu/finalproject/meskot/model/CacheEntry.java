package com.neu.finalproject.meskot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "cache_entries")
@Getter
@Setter
public class CacheEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cacheKey;          // e.g. movieId:resolution:chunkIndex
    private String path;              // filesystem path or object key
    private Long size;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt;

    @ManyToOne
    @JoinColumn(name = "metadata_id")
    private MovieMetadata movieMetadata;
}