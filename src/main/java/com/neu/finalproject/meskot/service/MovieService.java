package com.neu.finalproject.meskot.service;

import com.neu.finalproject.meskot.dto.MovieDto;
import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.model.MovieMetadata;
import com.neu.finalproject.meskot.repository.MovieMetadataRepository;
import com.neu.finalproject.meskot.repository.MovieRepository;
import com.neu.finalproject.meskot.service.impl.MovieServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import com.github.luben.zstd.ZstdInputStream;
import org.springframework.core.io.InputStreamResource;

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
    public ResponseEntity<Resource> streamMovie(Long id, String rangeHeader) {
        Optional<Movie> movieOpt = movieRepository.findById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Movie movie = movieOpt.get();
        Path moviePath = Paths.get(movie.getFilePath());
        if (!Files.exists(moviePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            File file = moviePath.toFile();
            File streamFile = file;

            // ✅ Decompress if needed
            if (file.getName().endsWith(".zst")) {
                streamFile = compressionService.decompressZst(file);
            }

            return buildStreamingResponse(streamFile, rangeHeader);
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
            } catch (NumberFormatException ignored) {}
        }

        if (rangeEnd > fileLength - 1) {
            rangeEnd = fileLength - 1;
        }

        long contentLength = rangeEnd - rangeStart + 1;

        // ✅ Seek to range start
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        raf.seek(rangeStart);

        // ✅ Limit read to requested range
        InputStream inputStream = new BufferedInputStream(new FileInputStream(raf.getFD())) {
            @Override
            public int available() throws IOException {
                return (int) contentLength;
            }
        };

        InputStreamResource resource = new InputStreamResource(inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept-Ranges", "bytes");
        headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
        headers.setContentLength(contentLength);
        headers.setContentType(MediaType.valueOf("video/mp4"));

        HttpStatus status = (rangeHeader == null) ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT;

        return new ResponseEntity<>(resource, headers, status);
    }

    public Movie handleUpload(File uploadedFile, String title, String versionResolution) throws Exception {
        // 1. Encode to versions
        File encoded720 =  encodingService.encode(uploadedFile, versionResolution, "mp4");
        // 2. Compress version
        File compressed = compressionService.compress(encoded720);
        // 3. Store in object store
        String storagePath = localStorageService.store(compressed, "movies/" + title.getClass().getName() +"/"  + compressed.getName());
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

