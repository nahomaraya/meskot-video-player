package com.neu.finalproject.meskot.repository;


import com.neu.finalproject.meskot.model.CacheEntry;
import com.neu.finalproject.meskot.model.MovieMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CacheEntryRepository extends JpaRepository<CacheEntry, Long> {
    List<CacheEntry> findByMovieMetadata(MovieMetadata metadata);
    Optional<CacheEntry> findByCacheKey(String cacheKey);
    void deleteByExpiresAtBefore(java.time.LocalDateTime t);
}
