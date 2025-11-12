package com.neu.finalproject.meskot.service;

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

    public ResponseEntity<Resource> streamMovie(Long id, String rangeHeader) {
        Optional<Movie> movieOpt = getMovieById(id);
        if (movieOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Movie movie = movieOpt.get();
        Path path = Paths.get(movie.getFilePath());
        if (!Files.exists(path)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            File file = path.toFile();
            File streamFile = file;

            // ✅ If it's a .zst file, decompress it to a temporary .mp4 first
            if (file.getName().endsWith(".zst")) {
                streamFile = compressionService.decompressZst(file);
            }

            long fileLength = streamFile.length();
            long rangeStart = 0;
            long rangeEnd = fileLength - 1;

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                rangeStart = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    rangeEnd = Long.parseLong(ranges[1]);
                }
            }

            if (rangeEnd > fileLength - 1) rangeEnd = fileLength - 1;
            long contentLength = rangeEnd - rangeStart + 1;

            InputStream inputStream = new BufferedInputStream(new FileInputStream(streamFile));
            inputStream.skip(rangeStart);
            Resource resource = new InputStreamResource(inputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.set("Accept-Ranges", "bytes");
            headers.setContentLength(contentLength);
            headers.setContentType(MediaType.valueOf("video/mp4"));

            return ResponseEntity.status(rangeHeader == null ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT)
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Resource> buildStreamingResponse(File file, String rangeHeader) throws IOException, MalformedURLException {
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
        HttpStatus status = (rangeHeader == null) ? HttpStatus.OK : HttpStatus.PARTIAL_CONTENT;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaTypeFactory.getMediaType(file.getName())
                .orElse(MediaType.APPLICATION_OCTET_STREAM));
        headers.setContentLength(contentLength);
        headers.add("Accept-Ranges", "bytes");
        headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);

        // Use UrlResource for efficient streaming
        Resource resource = new UrlResource(file.toURI());

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

