package com.neu.finalproject.meskot.controller;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.model.UploadJob;
import com.neu.finalproject.meskot.repository.MovieRepository;
import com.neu.finalproject.meskot.repository.UploadJobRepository;
import com.neu.finalproject.meskot.service.InternetArchiveMovieService;
import com.neu.finalproject.meskot.service.LocalStorageService;
import com.neu.finalproject.meskot.service.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class Controller {

    @Autowired
    private UploadJobRepository uploadJobRepository;

    @Autowired
    private LocalStorageService localStorageService;

    private final MovieService movieService;
    private final MovieRepository movieRepository;
    private final InternetArchiveMovieService iaMovieService;

    // =========================================================================
    // MOVIE LISTING - All Sources
    // =========================================================================

    @Tag(name = "1. Movies - All Sources")
    @Operation(summary = "Get all movies from all sources")
    @GetMapping("/movies")
    public ResponseEntity<List<MovieDto>> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Tag(name = "1. Movies - All Sources")
    @Operation(summary = "Search movies across all sources")
    @GetMapping("/movies/search")
    public ResponseEntity<List<MovieDto>> searchAllMovies(@RequestParam("q") String query) {
        return movieService.searchMovies(query);
    }

    @Tag(name = "1. Movies - All Sources")
    @Operation(summary = "Get movie by ID")
    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieDto> getMovieById(@PathVariable Long id) {
        return movieRepository.findById(id)
                .map(movie -> ResponseEntity.ok(MovieDto.fromEntity(movie)))
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================================
    // MOVIE LISTING - By Source Type
    // =========================================================================

    @Tag(name = "2. Movies - Local Storage")
    @Operation(summary = "Get all local movies")
    @GetMapping("/movies/local")
    public ResponseEntity<List<MovieDto>> getLocalMovies() {
        List<Movie> movies = movieRepository.findBySourceType("LOCAL");
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Tag(name = "2. Movies - Local Storage")
    @Operation(summary = "Search local movies")
    @GetMapping("/movies/local/search")
    public ResponseEntity<List<MovieDto>> searchLocalMovies(@RequestParam("q") String q) {
        List<Movie> movies = movieRepository.searchMoviesBySourceType(q, "LOCAL");
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Tag(name = "3. Movies - Internet Archive")
    @Operation(summary = "Get all Internet Archive movies")
    @GetMapping("/movies/internet-archive")
    public ResponseEntity<List<MovieDto>> getInternetArchiveMovies() {
        List<Movie> movies = movieRepository.findBySourceType("INTERNET_ARCHIVE");
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Tag(name = "3. Movies - Internet Archive")
    @Operation(summary = "Search Internet Archive movies")
    @GetMapping("/movies/internet-archive/search")
    public ResponseEntity<List<MovieDto>> searchInternetArchiveMovies(@RequestParam("q") String q) {
        List<Movie> movies = movieRepository.searchMoviesBySourceType(q, "INTERNET_ARCHIVE");
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Tag(name = "4. Movies - Supabase")
    @Operation(summary = "Get all Supabase movies")
    @GetMapping("/movies/supabase")
    public ResponseEntity<List<MovieDto>> getSupabaseMovies() {
        List<Movie> movies = movieRepository.findBySourceType("SUPABASE");
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Tag(name = "4. Movies - Supabase")
    @Operation(summary = "Search Supabase movies")
    @GetMapping("/movies/supabase/search")
    public ResponseEntity<List<MovieDto>> searchSupabaseMovies(@RequestParam("q") String q) {
        List<Movie> movies = movieRepository.searchMoviesBySourceType(q, "SUPABASE");
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // =========================================================================
    // STREAMING
    // =========================================================================

    @Tag(name = "5. Streaming")
    @Operation(summary = "Stream a movie with range support")
    @GetMapping("/movies/{id}/stream")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            return movieService.streamMovie(id, rangeHeader);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // =========================================================================
    // DOWNLOAD
    // =========================================================================

    @Tag(name = "6. Download")
    @Operation(summary = "Download a movie (optionally with resolution conversion)")
    @GetMapping("/movies/{id}/download")
    public ResponseEntity<Resource> downloadMovie(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "Original") String resolution) {

        Optional<Movie> movieOpt = movieService.getMovieById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Movie movie = movieOpt.get();

        // For original resolution or Internet Archive, use direct download
        if ("Original".equalsIgnoreCase(resolution)) {
            return movieService.downloadMovie(id);
        }

        // For other resolutions, convert on-the-fly (synchronous)
        return movieService.downloadMovieWithResolution(id, resolution);
    }

    // =========================================================================
    // UPLOAD
    // =========================================================================

    @Tag(name = "7. Upload")
    @Operation(summary = "Upload a new video file")
    @PostMapping("/movies/upload")
    public ResponseEntity<?> uploadMovie(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "resolution", defaultValue = "720p") String resolution,
            @RequestParam(value = "sourceType", defaultValue = "LOCAL") String sourceType) {
        try {
            File tempFile = localStorageService.saveTempFile(file);

            UploadJob job = new UploadJob();
            String jobId = UUID.randomUUID().toString();
            job.setId(jobId);
            job.setStatus("PENDING");
            job.setProgress(0);
            uploadJobRepository.save(job);

            // Pass source type to handle upload
            movieService.handleUpload(tempFile, title, resolution, jobId, 1L, sourceType);

            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("status", "PENDING");
            response.put("message", "Upload started");

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Tag(name = "7. Upload")
    @Operation(summary = "Check upload/encoding job status")
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        return uploadJobRepository.findById(jobId)
                .map(job -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("jobId", job.getId());
                    response.put("status", job.getStatus());
                    response.put("progress", job.getProgress());
                    response.put("errorMessage", job.getErrorMessage());
                    response.put("resultingMovieId", job.getResultingMovieId());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================================
    // INTERNET ARCHIVE IMPORT
    // =========================================================================

    @Tag(name = "8. Internet Archive Import")
    @Operation(summary = "Import a single movie from Internet Archive")
    @PostMapping("/import/internet-archive")
    public ResponseEntity<?> importFromInternetArchive(
            @RequestParam("itemIdentifier") String itemIdentifier,
            @RequestParam(value = "uploaderId", defaultValue = "1") Long uploaderId) {
        try {
            Movie movie = iaMovieService.importMovie(itemIdentifier, uploaderId);
            return ResponseEntity.ok(MovieDto.fromEntity(movie));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Tag(name = "8. Internet Archive Import")
    @Operation(summary = "Import movies from an Internet Archive collection")
    @PostMapping("/import/internet-archive/collection")
    public ResponseEntity<?> importIACollection(
            @RequestParam("collectionId") String collectionId,
            @RequestParam(value = "uploaderId", defaultValue = "1") Long uploaderId,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        try {
            List<Movie> movies = iaMovieService.importFromCollection(collectionId, uploaderId, limit);
            List<MovieDto> dtos = movies.stream()
                    .map(MovieDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of(
                    "imported", movies.size(),
                    "movies", dtos
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // =========================================================================
    // MOVIE MANAGEMENT
    // =========================================================================

    @Tag(name = "9. Movie Management")
    @Operation(summary = "Delete a movie")
    @DeleteMapping("/movies/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        if (movieRepository.existsById(id)) {
            movieRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("deleted", id));
        }
        return ResponseEntity.notFound().build();
    }

    @Tag(name = "9. Movie Management")
    @Operation(summary = "Update movie status")
    @PatchMapping("/movies/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status) {
        return movieRepository.findById(id)
                .map(movie -> {
                    movie.setStatus(status);
                    movieRepository.save(movie);
                    return ResponseEntity.ok(MovieDto.fromEntity(movie));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Tag(name = "9. Movie Management")
    @Operation(summary = "Get movies by genre")
    @GetMapping("/movies/genre/{genre}")
    public ResponseEntity<List<MovieDto>> getMoviesByGenre(@PathVariable String genre) {
        List<Movie> movies = movieRepository.findByGenre(genre);
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @Tag(name = "9. Movie Management")
    @Operation(summary = "Get movies by year")
    @GetMapping("/movies/year/{year}")
    public ResponseEntity<List<MovieDto>> getMoviesByYear(@PathVariable Integer year) {
        List<Movie> movies = movieRepository.findByReleaseYear(year);
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}