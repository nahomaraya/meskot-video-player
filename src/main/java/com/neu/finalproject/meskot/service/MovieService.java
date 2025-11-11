package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.model.MovieMetadata;
import com.neu.finalproject.meskot.repository.MovieMetadataRepository;
import com.neu.finalproject.meskot.repository.MovieRepository;
import com.neu.finalproject.meskot.service.impl.MovieServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService implements MovieServiceImpl {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieMetadataRepository movieMetadataRepository;

    private final EncodingService encodingService;

    private final CompressionService compressionService;

    private final StorageService storageService;

    private final LocalStorageService localStorageService;

    private final CacheService cacheService;

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public List<Movie> listMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    public Movie handleUpload(File uploadedFile, String title, String versionResolution) throws Exception {
        // 1. Encode to versions
        File encoded720 =  encodingService.encode(uploadedFile, versionResolution, "mp4");
        // 2. Compress version
        File compressed = compressionService.compress(encoded720);
        // 3. Store in object store
        String storagePath = localStorageService.store(compressed, "movies/" + title.getClass().getName() + "/720p-" + compressed.getName());
        // Save Movie entity
        Movie movie = new Movie();
        movie.setTitle(title);
        movie.setFilePath(storagePath);
        movie.setUploadedDate(LocalDateTime.now());
        movieRepository.save(movie);

        // Save MovieMetaData
        MovieMetadata meta = new MovieMetadata();
        meta.setMovie(movie);
        meta.setResolution(versionResolution);
        meta.setSizeInBytes(compressed.length());
        meta.setFormat("mp4");
        meta.setDuration(0.0); // Optional: calculate via JavaCV if needed
        movieMetadataRepository.save(meta);
        return movie;
    }
}

