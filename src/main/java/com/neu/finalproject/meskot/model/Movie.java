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

    private String title;
    private String filePath;
    private String description;
    private String thumbnailUrl;
    private LocalDateTime uploadedDate;
    private String status;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<MovieMetadata> metadataList;

    // getters and setters
}
