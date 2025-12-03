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
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
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
    @Autowired
    private UploadHistoryService uploadHistoryService;

    private final EncodingService encodingService;

    private final CompressionService compressionService;
    private final LocalStorageService localStorageService;
    private final InternetArchiveMovieService iaMovieService;
    private final SupabaseStorageService supabaseStorageService;
    private final InternetArchiveStorageService iaStorageService;
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

    public ResponseEntity<List<MovieDto>> searchMovies(String query) {
        List<Movie> movies = movieRepository.searchMovies(query);
        List<MovieDto> dtos = movies.stream()
                .map(MovieDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // =========================================================================
    // STREAMING
    // =========================================================================

    public ResponseEntity<Resource> streamMovie(Long id, String rangeHeader) {
        System.out.println("=== STREAM REQUEST ===");
        System.out.println("Movie ID: " + id);
        System.out.println("Range Header: " + rangeHeader);

        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            System.out.println("ERROR: Movie not found in database");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Movie movie = movieOpt.get();
        String sourceType = movie.getSourceType();
        String filePath = movie.getFilePath();

        System.out.println("Title: " + movie.getTitle());
        System.out.println("Source Type: " + sourceType);
        System.out.println("File Path: " + filePath);

        try {
            if ("INTERNET_ARCHIVE".equals(sourceType)) {
                System.out.println("Routing to Internet Archive streaming...");
                return iaMovieService.streamMovieWithRange(id, rangeHeader);

            } else if ("SUPABASE".equals(sourceType)) {
                System.out.println("Routing to Supabase streaming...");
                Resource resource = supabaseStorageService.loadAsResource(filePath);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("video/mp4"))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + movie.getTitle() + ".mp4\"")
                        .body(resource);

            } else {
                // LOCAL storage
                System.out.println("Routing to Local storage streaming...");
                Path moviePath = Paths.get(filePath);
                System.out.println("Resolved path: " + moviePath.toAbsolutePath());
                System.out.println("File exists: " + Files.exists(moviePath));

                if (!Files.exists(moviePath)) {
                    System.out.println("ERROR: File does not exist at path: " + moviePath);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }

                System.out.println("File size: " + Files.size(moviePath) + " bytes");
                return buildStreamingResponse(moviePath.toFile(), rangeHeader);
            }
        } catch (Exception e) {
            System.out.println("ERROR: Exception during streaming");
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
                rangeStart = 0;
                rangeEnd = fileLength - 1;
            }
        }

        if (rangeEnd > fileLength - 1) {
            rangeEnd = fileLength - 1;
        }

        long contentLength = rangeEnd - rangeStart + 1;
        final RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(rangeStart);

        final long finalContentLength = contentLength;
        InputStream inputStream = new BufferedInputStream(new FileInputStream(raf.getFD())) {
            @Override
            public int available() throws IOException {
                return (int) finalContentLength;
            }
            @Override
            public void close() throws IOException {
                super.close();
                raf.close();
            }
        };

        InputStreamResource resource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept-Ranges", "bytes");
        headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
        headers.setContentLength(contentLength);
        headers.setContentType(MediaType.valueOf("video/mp4"));

        HttpStatus status = (rangeHeader != null) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;
        return new ResponseEntity<>(resource, headers, status);
    }

    // =========================================================================
    // DOWNLOAD
    // =========================================================================

    public ResponseEntity<Resource> downloadMovie(Long id) {
        System.out.println("=== DOWNLOAD REQUEST ===");
        System.out.println("Movie ID: " + id);

        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            System.out.println("ERROR: Movie not found in database");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Movie movie = movieOpt.get();
        String sourceType = movie.getSourceType();
        String filePath = movie.getFilePath();

        System.out.println("Title: " + movie.getTitle());
        System.out.println("Source Type: " + sourceType);
        System.out.println("File Path: " + filePath);

        try {
            if ("INTERNET_ARCHIVE".equals(sourceType)) {
                System.out.println("Redirecting to Internet Archive...");
                String downloadUrl = iaMovieService.getMovieDownloadUrl(id);
                System.out.println("Redirect URL: " + downloadUrl);
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, downloadUrl)
                        .build();

            } else if ("SUPABASE".equals(sourceType)) {
                System.out.println("Loading from Supabase...");
                Resource resource = supabaseStorageService.loadAsResource(filePath);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + movie.getTitle() + ".mp4\"")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);

            } else {
                // LOCAL storage
                System.out.println("Loading from Local storage...");
                Path path = Paths.get(filePath);
                System.out.println("Resolved path: " + path.toAbsolutePath());
                System.out.println("File exists: " + Files.exists(path));

                if (!Files.exists(path)) {
                    System.out.println("ERROR: File does not exist!");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }

                long fileSize = Files.size(path);
                System.out.println("File size: " + fileSize + " bytes");

                Resource resource = new UrlResource(path.toUri());
                String filename = movie.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".mp4";

                System.out.println("Sending file as: " + filename);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            }
        } catch (Exception e) {
            System.out.println("ERROR: Exception during download");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    public ResponseEntity<Resource> downloadMovieWithResolution(Long id, String resolution) {
        System.out.println("=== DOWNLOAD WITH RESOLUTION ===");
        System.out.println("Movie ID: " + id + ", Resolution: " + resolution);

        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Movie movie = movieOpt.get();

        if (resolution == null || resolution.isEmpty() || "Original".equalsIgnoreCase(resolution)) {
            return downloadMovie(id);
        }

        if ("INTERNET_ARCHIVE".equals(movie.getSourceType())) {
            return downloadAndConvertIAMovie(movie, resolution);
        }

        return downloadAndConvertLocalMovie(movie, resolution);
    }

    private ResponseEntity<Resource> downloadAndConvertLocalMovie(Movie movie, String resolution) {
        System.out.println("Converting local movie to " + resolution);
        try {
            File sourceFile = new File(movie.getFilePath());
            System.out.println("Source file: " + sourceFile.getAbsolutePath());
            System.out.println("Source exists: " + sourceFile.exists());

            if (!sourceFile.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            File encodedFile = encodingService.encode(
                    sourceFile,
                    resolution,
                    "mp4",
                    "h265",
                    null
            );

            System.out.println("Encoded file: " + encodedFile.getAbsolutePath());
            System.out.println("Encoded size: " + encodedFile.length());

            Resource resource = new UrlResource(encodedFile.toURI());
            String filename = movie.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + "-" + resolution + ".mp4";

            encodedFile.deleteOnExit();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(encodedFile.length()))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            System.out.println("ERROR during conversion:");
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Resource> downloadAndConvertIAMovie(Movie movie, String resolution) {
        System.out.println("Converting IA movie to " + resolution);
        File downloadedFile = null;
        File encodedFile = null;

        try {
            String filePath = movie.getFilePath();
            String[] parts = filePath.split("/", 2);

            if (parts.length != 2) {
                System.out.println("ERROR: Invalid IA file path format: " + filePath);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            String itemIdentifier = parts[0];
            String fileName = parts[1];
            System.out.println("IA Item: " + itemIdentifier + ", File: " + fileName);

            downloadedFile = File.createTempFile("ia-download-", ".mp4");
            Resource iaResource = iaStorageService.streamFromArchive(itemIdentifier, fileName);

            System.out.println("Downloading from IA...");
            try (InputStream in = iaResource.getInputStream();
                 FileOutputStream out = new FileOutputStream(downloadedFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                long total = 0;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    total += bytesRead;
                }
                System.out.println("Downloaded " + total + " bytes");
            }

            System.out.println("Converting...");
            encodedFile = encodingService.encode(
                    downloadedFile,
                    resolution,
                    "mp4",
                    "h265",
                    null
            );

            Resource resource = new UrlResource(encodedFile.toURI());
            String outputFilename = movie.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + "-" + resolution + ".mp4";

            downloadedFile.delete();
            encodedFile.deleteOnExit();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + outputFilename + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(encodedFile.length()))
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);

        } catch (Exception e) {
            System.out.println("ERROR during IA conversion:");
            e.printStackTrace();
            if (downloadedFile != null && downloadedFile.exists()) downloadedFile.delete();
            if (encodedFile != null && encodedFile.exists()) encodedFile.delete();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // =========================================================================
    // UPLOAD
    // =========================================================================

    @Async("encodingTaskExecutor")
    @Transactional
    public void handleUpload(File uploadedFile, String title, String versionResolution,
                             String jobId, Long uploaderId, String sourceType) {
        System.out.println("=== UPLOAD PROCESSING ===");
        System.out.println("Title: " + title);
        System.out.println("Resolution: " + versionResolution);
        System.out.println("Job ID: " + jobId);
        System.out.println("Source Type: " + sourceType);
        System.out.println("Uploaded File: " + uploadedFile.getAbsolutePath());
        System.out.println("File exists: " + uploadedFile.exists());
        System.out.println("File size: " + uploadedFile.length());

        UploadJob job = uploadJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            System.err.println("ERROR: Job not found: " + jobId);
            return;
        }

        // Step 1: create UploadHistory record at start
        uploadHistoryService.create(uploaderId != null ? uploaderId.intValue() : 1, title,
            uploadedFile.getName(), uploadedFile.length(), versionResolution, jobId);

        try {
            // Step 2: markUploading (right before starting encoding)
            uploadHistoryService.markUploading(jobId);
            job.setStatus("ENCODING");
            job.setProgress(0);
            uploadJobRepository.save(job);
            // Step 3: markEncoding as soon as encoding starts
            uploadHistoryService.markEncoding(jobId);

            ProgressCallback callback = (percent) -> {
                UploadJob currentJob = uploadJobRepository.findById(jobId).get();
                currentJob.setProgress(percent);
                uploadJobRepository.save(currentJob);
                System.out.println("Encoding progress: " + percent + "%");
            };

            System.out.println("Starting encoding...");
            File encodedFile = encodingService.encode(
                    uploadedFile,
                    versionResolution,
                    "mp4",
                    "h265",
                    callback
            );
            System.out.println("Encoding complete: " + encodedFile.getAbsolutePath());

            // Store based on source type
            String storagePath;
            String finalSourceType = (sourceType != null && !sourceType.isEmpty()) ? sourceType : "LOCAL";

            System.out.println("Storing with source type: " + finalSourceType);

            if ("SUPABASE".equals(finalSourceType)) {
                storagePath = supabaseStorageService.store(encodedFile,
                        "movies/" + title + "/" + encodedFile.getName());
            } else {
                storagePath = localStorageService.store(encodedFile,
                        "movies/" + title + "/" + encodedFile.getName());
                finalSourceType = "LOCAL";
            }

            System.out.println("Stored at: " + storagePath);

            Movie movie = new Movie();
            movie.setTitle(title);
            movie.setFilePath(storagePath);
            movie.setUploadedDate(LocalDateTime.now());
            movie.setCreatedAt(LocalDateTime.now());
            movie.setStatus("ACTIVE");
            movie.setSourceType(finalSourceType);
            movie.setUploaderId(uploaderId != null ? uploaderId : 1L);

            Movie savedMovie = movieRepository.save(movie);
            System.out.println("Movie saved with ID: " + savedMovie.getId());

            MovieMetadata meta = new MovieMetadata();
            meta.setMovie(savedMovie);
            meta.setResolution(versionResolution);
            meta.setSizeInBytes(encodedFile.length());
            meta.setFormat("mp4-h265");
            meta.setDuration(0.0);
            movieMetadataRepository.save(meta);

            job.setStatus("COMPLETED");
            job.setProgress(100);
            job.setResultingMovieId(savedMovie.getId());
            uploadJobRepository.save(job);

            // Step 4: markCompleted after movie, meta, and job all saved
            uploadHistoryService.markCompleted(jobId, savedMovie.getId());

            System.out.println("=== UPLOAD COMPLETE ===");

            // Clean up encoded file (storage service already copied it)
            encodedFile.delete();

        } catch (Exception e) {
            System.err.println("=== UPLOAD FAILED ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            uploadJobRepository.save(job);
            // Step 5: markFailed
            uploadHistoryService.markFailed(jobId, e.getMessage());
        } finally {
            // Clean up original uploaded file
            if (uploadedFile.exists()) {
                uploadedFile.delete();
            }
        }
    }

    // Backward compatible overloads
    @Async("encodingTaskExecutor")
    @Transactional
    public void handleUpload(File uploadedFile, String title, String versionResolution,
                             String jobId, Long uploaderId) {
        handleUpload(uploadedFile, title, versionResolution, jobId, uploaderId, "LOCAL");
    }

    @Async("encodingTaskExecutor")
    @Transactional
    public void handleUpload(File uploadedFile, String title, String versionResolution, String jobId) {
        handleUpload(uploadedFile, title, versionResolution, jobId, 1L, "LOCAL");
    }
}