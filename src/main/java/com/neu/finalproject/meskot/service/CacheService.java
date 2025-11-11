package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.model.CacheEntry;
import com.neu.finalproject.meskot.repository.CacheEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {


    private final CacheEntryRepository cacheEntryRepository;

    // small in-memory index for fast reads
    private final Map<String, CacheEntry> index = new ConcurrentHashMap<>();

    public CacheService(CacheEntryRepository cacheEntryRepository) {
        this.cacheEntryRepository = cacheEntryRepository;
        // TODO: optionally preload recent cache entries
    }
    @Transactional
    public CacheEntry addCache(String cacheKey, String path, long size, long ttlSeconds) {
        CacheEntry e = new CacheEntry();
        e.setCacheKey(cacheKey);
        e.setPath(path);
        e.setSize(size);
        e.setCreatedAt(LocalDateTime.now());
        e.setExpiresAt(LocalDateTime.now().plusSeconds(ttlSeconds));
        CacheEntry saved = cacheEntryRepository.save(e);
        index.put(cacheKey, saved);
        return saved;
    }

    public Optional<CacheEntry> getCache(String cacheKey) {
        CacheEntry cached = index.get(cacheKey);
        if (cached != null && (cached.getExpiresAt() == null || cached.getExpiresAt().isAfter(LocalDateTime.now()))) {
            return Optional.of(cached);
        }
        // fallback to DB
        Optional<CacheEntry> dbEntry = cacheEntryRepository.findByCacheKey(cacheKey);
        dbEntry.ifPresent(e -> {
            if (e.getExpiresAt() == null || e.getExpiresAt().isAfter(LocalDateTime.now())) {
                index.put(cacheKey, e);
            }
        });
        return dbEntry.filter(e -> e.getExpiresAt() == null || e.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Transactional
    public void evict(String cacheKey) {
        index.remove(cacheKey);
        cacheEntryRepository.findByCacheKey(cacheKey).ifPresent(cacheEntryRepository::delete);
    }

    @Transactional
    public void evictExpired() {
        cacheEntryRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        index.entrySet().removeIf(e -> e.getValue().getExpiresAt() != null && e.getValue().getExpiresAt().isBefore(LocalDateTime.now()));
    }
}
