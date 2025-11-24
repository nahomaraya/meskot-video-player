package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.model.MovieMetadata;
import com.neu.finalproject.meskot.model.UploadJob;
import com.neu.finalproject.meskot.repository.MovieMetadataRepository;
import com.neu.finalproject.meskot.repository.MovieRepository;
import com.neu.finalproject.meskot.repository.UploadJobRepository;
import com.neu.finalproject.meskot.service.impl.MovieServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import com.github.luben.zstd.ZstdInputStream;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService implements MovieServiceImpl {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private MovieMetadataRepository movieMetadataRepository;
    @Autowired
    private UploadJobRepository uploadJobRepository;

    private final EncodingService encodingService;

    private final CompressionService compressionService;

    private final LocalStorageService localStorageService;

    private final InternetArchiveMovieService  iaMovieService;

    private final SupabaseStorageService supabaseStorageService;

    private final CacheService cacheService;

    @Override
    public Movie saveMovie(Movie movie) {
        return movieRepository.save(movie);
    }

    @Override
    public List<Movie> listMovies() {
        return movieRepository.findAll();
    }


//    public List<Movie> getMovieByTitle(String title) {
//        return movieRepository.findByTitleContainingIgnoreCase(title);
//    }

    @Override
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    public ResponseEntity<List<MovieDto>> searchMovies( String query) {
        List<Movie> movies = movieRepository.searchMovies(query);
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    public ResponseEntity<Resource> streamMovie(Long id, String rangeHeader) {
        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Movie movie = movieOpt.get();
        String sourceType = movie.getSourceType();

        try {
            // Route based on source type
            if ("INTERNET_ARCHIVE".equals(sourceType)) {
                // Stream from Internet Archive with range support
                return iaMovieService.streamMovieWithRange(id, rangeHeader);

            } else if ("SUPABASE".equals(sourceType)) {
                // Stream from Supabase S3
                Resource resource = supabaseStorageService.loadAsResource(movie.getFilePath());
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("video/mp4"))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + movie.getTitle() + ".mp4\"")
                        .body(resource);

            } else {
                // LOCAL - existing logic with range support
                Path moviePath = Paths.get(movie.getFilePath());
                if (!Files.exists(moviePath)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }

                File file = moviePath.toFile();
                return buildStreamingResponse(file, rangeHeader);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Resource> buildStreamingResponse(File file, String rangeHeader) throws IOException {
        long fileLength = file.length();
        long rangeStart = 0;
        long rangeEnd = fileLength - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            try {
                rangeStart = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException ignored) {
                // Malformed range, just ignore and serve full content or default range
                rangeStart = 0;
                rangeEnd = fileLength - 1;
            }
        }

        if (rangeEnd > fileLength - 1) {
            rangeEnd = fileLength - 1;
        }

        long contentLength = rangeEnd - rangeStart + 1;

        // Make 'raf' final so it can be accessed by the anonymous inner class
        final RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(rangeStart);

        //Create an InputStream that also closes 'raf' when it is closed
        InputStream inputStream = new BufferedInputStream(new FileInputStream(raf.getFD())) {
            @Override
            public int available() throws IOException {
                return (int) contentLength;
            }
            @Override
            public void close() throws IOException {
                super.close();
                raf.close();
                // System.out.println("Closed raf"); // For debugging
            }
        };

        InputStreamResource resource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept-Ranges", "bytes");
        headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
        headers.setContentLength(contentLength);
        headers.setContentType(MediaType.valueOf("video/mp4"));

        HttpStatus status = (rangeHeader == "not-allowed") ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT;
        if (rangeStart >= fileLength) {
            status = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
            headers.add("Content-Range", "bytes */" + fileLength);
            return new ResponseEntity<>(null, headers, status);
        }
        return new ResponseEntity<>(resource, headers, status);
    }

    @Async("encodingTaskExecutor")
    @Transactional // Good practice for methods that save multiple entities
    public void handleUpload(File uploadedFile, String title, String versionResolution, String jobId) {

        UploadJob job = uploadJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            System.err.println("Job not found: " + jobId);
            return;
        }

        try {
            // 1. Update job status
            job.setStatus("ENCODING");
            job.setProgress(0);
            uploadJobRepository.save(job);

            // 2. Define the progress callback
            ProgressCallback callback = (percent) -> {
                // We find a fresh instance in this new thread
                UploadJob currentJob = uploadJobRepository.findById(jobId).get();
                currentJob.setProgress(percent);
                uploadJobRepository.save(currentJob);
                System.out.println("Job " + jobId + " progress: " + percent + "%");
            };

            // 3. Encode to versions, passing the callback
            System.out.println("Starting background encoding for: " + title);
            File encoded720 = encodingService.encode(
                    uploadedFile,
                    versionResolution,
                    "mp4",
                    "h265",
                    callback // Pass the callback here
            );

            // 4. Store in object store
            String storagePath = localStorageService.store(encoded720, "movies/" + title + "/" + encoded720.getName());

            // 5. Save Movie entity
            Movie movie = new Movie();
            movie.setTitle(title);
            movie.setFilePath(storagePath);
            movie.setUploadedDate(LocalDateTime.now());
            movie.setCreatedAt(LocalDateTime.now());
            movie.setStatus("ACTIVE");
            movie.setSourceType("LOCAL");
            Movie savedMovie = movieRepository.save(movie); // Save and get the ID

            // 6. Save MovieMetaData
            MovieMetadata meta = new MovieMetadata();
            meta.setMovie(savedMovie);
            meta.setResolution(versionResolution);
            meta.setSizeInBytes(encoded720.length());
            meta.setFormat("mp4-h265");
            meta.setDuration(0.0);
            movieMetadataRepository.save(meta);

            // 7. Mark job as complete
            job.setStatus("COMPLETED");
            job.setProgress(100);
            job.setResultingMovieId(savedMovie.getId());
            uploadJobRepository.save(job);

            System.out.println("Finished background encoding for: " + title);

        } catch (Exception e) {
            System.err.println("Failed to encode file " + title + ": " + e.getMessage());
            e.printStackTrace();
            // Mark job as failed
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            uploadJobRepository.save(job);
        } finally {
            // Clean up the original uploaded file
            uploadedFile.delete();
        }
    }


    public ResponseEntity<Resource> downloadMovie(Long id) {
        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        Movie movie = movieOpt.get();
        Path filePath = Paths.get(movie.getFilePath());
        if (!Files.exists(filePath)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        try {
            Resource resource = new UrlResource(filePath.toUri());
            String filename = filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Async("encodingTaskExecutor")
    @Transactional
    public void handleDownloadConversion(Movie sourceMovie, String targetResolution, String jobId) {
        UploadJob job = uploadJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            System.err.println("Job not found: " + jobId);
            return;
        }

        try {
            job.setStatus("ENCODING");
            job.setProgress(0);
            uploadJobRepository.save(job);

            // Get source file
            File sourceFile = new File(sourceMovie.getFilePath());

            ProgressCallback callback = (percent) -> {
                UploadJob currentJob = uploadJobRepository.findById(jobId).get();
                currentJob.setProgress(percent);
                uploadJobRepository.save(currentJob);
            };

            // Encode to target resolution
            File encodedFile = encodingService.encode(
                    sourceFile,
                    targetResolution,
                    "mp4",
                    "h265",
                    callback
            );

            // Store converted file
            String storagePath = localStorageService.store(
                    encodedFile,
                    "downloads/" + sourceMovie.getTitle() + "-" + targetResolution + ".mp4"
            );

            // Create new movie entry for the download
            Movie downloadMovie = new Movie();
            downloadMovie.setTitle(sourceMovie.getTitle() + " (" + targetResolution + ")");
            downloadMovie.setFilePath(storagePath);
            downloadMovie.setUploadedDate(LocalDateTime.now());
            downloadMovie.setCreatedAt(LocalDateTime.now());
            downloadMovie.setStatus("DOWNLOAD_READY");
            downloadMovie.setSourceType("LOCAL");
            Movie savedMovie = movieRepository.save(downloadMovie);

            // Mark job as complete
            job.setStatus("COMPLETED");
            job.setProgress(100);
            job.setResultingMovieId(savedMovie.getId());
            uploadJobRepository.save(job);

            // Schedule cleanup after 1 hour
            scheduleDownloadCleanup(savedMovie.getId(), 3600000); // 1 hour

        } catch (Exception e) {
            System.err.println("Conversion failed: " + e.getMessage());
            e.printStackTrace();
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            uploadJobRepository.save(job);
        }
    }

    private void scheduleDownloadCleanup(Long movieId, long delayMs) {
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                try {
                    movieRepository.findById(movieId).ifPresent(movie -> {
                        try {
                            Files.deleteIfExists(Paths.get(movie.getFilePath()));
                            movieRepository.delete(movie);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, delayMs);
    }
}

