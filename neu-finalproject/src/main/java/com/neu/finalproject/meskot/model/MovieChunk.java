package com.neu.finalproject.meskot.model;

import jakarta.persistence.*;

@Entity
@Table(name = "movie_chunks")
public class MovieChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer chunkIndex;
    private String chunkPath;
    private Long size;
    private String checksum;

    @ManyToOne
    @JoinColumn(name = "metadata_id")
    private MovieMetadata movieMetadata;

    // getters and setters
}
