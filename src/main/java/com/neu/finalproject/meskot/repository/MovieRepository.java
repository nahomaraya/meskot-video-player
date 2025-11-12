package com.neu.finalproject.meskot.repository;

import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByUploader(User uploader);
    List<Movie> findByTitleContainingIgnoreCase(String keyword);
//    List<Movie> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
}