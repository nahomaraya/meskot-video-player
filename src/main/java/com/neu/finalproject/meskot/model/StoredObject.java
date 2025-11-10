package com.neu.finalproject.meskot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "stored_objects")
@Getter
@Setter
public class StoredObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // logical key (e.g. object store key, or relative path)
    @Column(nullable = false, unique = true)
    private String objectKey;

    // e.g. "video/mp4", "application/zstd"
    private String contentType;

    // size in bytes
    private Long size;

    // location type: LOCAL or S3 etc
    private String locationType;

    // human readable description or original filename
    private String name;

    private LocalDateTime createdAt = LocalDateTime.now();

    // optional relation to MovieMetadata (if needed)
    @ManyToOne
    @JoinColumn(name = "movie_metadata_id")
    private MovieMetadata movieMetadata;
}
