package com.neu.finalproject.meskot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "movie_metadata")
@Setter
@Getter
public class MovieMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resolution;
    private String format;
    private String codec;
    private String compressionAlgorithm;
    private String filePath;
    private Long size;
    private Double duration;
    private Integer bitrate;
    private LocalDateTime uploadDate = LocalDateTime.now();
    private Long sizeInBytes;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @OneToMany(mappedBy = "movieMetadata", cascade = CascadeType.ALL)
    private List<MovieChunk> chunks;

    // getters and setters
}
