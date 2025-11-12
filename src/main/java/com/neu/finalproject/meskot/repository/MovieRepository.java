package com.neu.finalproject.meskot.repository;

import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.awt.print.Pageable;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findAllByOrderByUploadedDateDesc();
    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT m FROM Movie m ORDER BY m.uploadedDate DESC")
    List<Movie> findRecentMovies(org.springframework.data.domain.Pageable pageable);


    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY m.uploadedDate DESC")
    List<Movie> searchMovies(@Param("query") String query);
}