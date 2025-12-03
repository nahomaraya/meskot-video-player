package com.neu.finalproject.meskot.repository;

import com.neu.finalproject.meskot.model.MovieChunk;
import com.neu.finalproject.meskot.model.MovieMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieChunkRepository extends JpaRepository<MovieChunk, Long> {
    List<MovieChunk> findByMovieMetadata(MovieMetadata metadata);
}