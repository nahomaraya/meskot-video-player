package com.neu.finalproject.meskot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "release_year")
    private Integer releaseYear;

    @Column(name = "genre")
    private String genre;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE, PENDING

    @Column(name = "uploader_id", nullable = false)
    private Long uploaderId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "uploaded_date", nullable = false)
    private LocalDateTime uploadedDate;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<MovieMetadata> metadataList;

    @Column(name = "source_type", nullable = false)
    private String sourceType; // LOCAL, SUPABASE, INTERNET_ARCHIVE

    // getters and setters
}
