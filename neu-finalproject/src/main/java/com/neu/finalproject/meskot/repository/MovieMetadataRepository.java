package com.neu.finalproject.meskot.repository;

import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.model.MovieMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovieMetadataRepository extends JpaRepository<MovieMetadata, Long> {
    List<MovieMetadata> findByMovie(Movie movie);
    List<MovieMetadata> findByResolution(String resolution);
}