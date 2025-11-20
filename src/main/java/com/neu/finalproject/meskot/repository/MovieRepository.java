package com.neu.finalproject.meskot.repository;

import com.neu.finalproject.meskot.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Your existing methods
    List<Movie> findAllByOrderByUploadedDateDesc();
    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT m FROM Movie m ORDER BY m.uploadedDate DESC")
    List<Movie> findRecentMovies(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY m.uploadedDate DESC")
    List<Movie> searchMovies(@Param("query") String query);

    // New methods for Internet Archive support
    List<Movie> findByStatus(String status);

    List<Movie> findByUploaderId(Long uploaderId);

    Optional<Movie> findByFilePath(String filePath);

    List<Movie> findByGenre(String genre);

    List<Movie> findByReleaseYear(Integer year);

    List<Movie> findByReleaseYearBetween(Integer startYear, Integer endYear);

    List<Movie> findByStatusOrderByUploadedDateDesc(String status);

    List<Movie> findBySourceType(String sourceType);

    // Find all user-uploaded movies (non-IA)
    @Query("SELECT m FROM Movie m WHERE m.sourceType IN ('LOCAL', 'SUPABASE') ORDER BY m.uploadedDate DESC")
    List<Movie> findUserUploadedMovies();

    // Find all Internet Archive movies
    @Query("SELECT m FROM Movie m WHERE m.sourceType = 'INTERNET_ARCHIVE' ORDER BY m.uploadedDate DESC")
    List<Movie> findInternetArchiveMovies();
}