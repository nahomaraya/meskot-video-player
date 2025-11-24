package com.neu.finalproject.meskot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class InternetArchiveMovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private InternetArchiveStorageService storageService;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public InternetArchiveMovieService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetch metadata from Internet Archive for a specific item
     */
    public JsonNode fetchItemMetadata(String itemIdentifier) throws Exception {
        String url = String.format("https://archive.org/metadata/%s", itemIdentifier);
        String response = restTemplate.getForObject(url, String.class);
        return objectMapper.readTree(response);
    }

    /**
     * Import a movie from Internet Archive into the database
     * @param itemIdentifier The Internet Archive item identifier
     * @param uploaderId The user ID importing this movie
     * @return The saved Movie entity
     */
    @Transactional
    public Movie importMovie(String itemIdentifier, Long uploaderId) throws Exception {
        System.out.println("=== IMPORT MOVIE DEBUG ===");
        System.out.println("Item Identifier: " + itemIdentifier);
        System.out.println("Uploader ID: " + uploaderId);

        JsonNode metadata = fetchItemMetadata(itemIdentifier);

        System.out.println("Metadata fetched successfully");

        Movie movie = new Movie();
        movie.setUploaderId(uploaderId);
        movie.setCreatedAt(LocalDateTime.now());
        movie.setUploadedDate(LocalDateTime.now());
        movie.setStatus("ACTIVE");
        movie.setSourceType("INTERNET_ARCHIVE");

        System.out.println("Movie object created with basic fields");

        // Extract metadata
        JsonNode metadataNode = metadata.get("metadata");
        if (metadataNode != null) {
            System.out.println("Processing metadata node...");

            // Title
            if (metadataNode.has("title")) {
                movie.setTitle(getStringValue(metadataNode.get("title")));
                System.out.println("Title set: " + movie.getTitle());
            } else {
                movie.setTitle(itemIdentifier);
                System.out.println("Title set to identifier: " + itemIdentifier);
            }

            // Description
            if (metadataNode.has("description")) {
                movie.setDescription(getStringValue(metadataNode.get("description")));
                System.out.println("Description set");
            }

            // Year
            if (metadataNode.has("year")) {
                try {
                    movie.setReleaseYear(Integer.parseInt(getStringValue(metadataNode.get("year"))));
                    System.out.println("Year set: " + movie.getReleaseYear());
                } catch (NumberFormatException e) {
                    // Try to extract year from date field
                    if (metadataNode.has("date")) {
                        String date = getStringValue(metadataNode.get("date"));
                        movie.setReleaseYear(extractYear(date));
                        System.out.println("Year extracted from date: " + movie.getReleaseYear());
                    }
                }
            } else if (metadataNode.has("date")) {
                String date = getStringValue(metadataNode.get("date"));
                movie.setReleaseYear(extractYear(date));
                System.out.println("Year extracted from date: " + movie.getReleaseYear());
            }

            // Genre/Subject
            if (metadataNode.has("subject")) {
                movie.setGenre(getStringValue(metadataNode.get("subject")));
                System.out.println("Genre set: " + movie.getGenre());
            }
        } else {
            System.out.println("No metadata node found, using defaults");
            movie.setTitle(itemIdentifier);
        }

        // Find the main video file
        System.out.println("Looking for video files...");
        JsonNode filesNode = metadata.get("files");

        if (filesNode != null && filesNode.isArray()) {
            System.out.println("Files array found with " + filesNode.size() + " files");
            String videoFile = null;
            String thumbnailFile = null;
            int duration = 0;

            for (JsonNode file : filesNode) {
                String fileName = file.has("name") ? file.get("name").asText() : null;
                String format = file.has("format") ? file.get("format").asText() : null;

                System.out.println("  File: " + fileName + " | Format: " + format);

                // Look for video file (prefer mp4, then other formats)
                if (format != null && (format.contains("MPEG4") || format.contains("h.264") ||
                        format.contains("MPEG2") || format.equals("512Kb MPEG4"))) {
                    if (videoFile == null || fileName.endsWith(".mp4")) {
                        videoFile = fileName;
                        System.out.println("    -> Selected as video file");

                        // Try to get duration
                        if (file.has("length")) {
                            try {
                                String lengthStr = file.get("length").asText();
                                duration = (int) (Double.parseDouble(lengthStr) / 60); // Convert seconds to minutes
                            } catch (NumberFormatException e) {
                                // Ignore
                            }
                        }
                    }
                }

                // Look for thumbnail
                if (fileName != null && (fileName.endsWith("_thumbs.jpg") ||
                        (format != null && format.equals("Thumbnail")))) {
                    thumbnailFile = fileName;
                    System.out.println("    -> Selected as thumbnail");
                }
            }

            // Set file path (store as itemIdentifier/filename for retrieval)
            if (videoFile != null) {
                movie.setFilePath(itemIdentifier + "/" + videoFile);
                System.out.println("FilePath set to: " + movie.getFilePath());
            } else {
                // No video file found, just use item identifier
                movie.setFilePath(itemIdentifier);
                System.out.println("No video file found, FilePath set to identifier: " + movie.getFilePath());
            }

            // Set thumbnail URL
            if (thumbnailFile != null) {
                movie.setThumbnailUrl(storageService.getArchiveUrl(itemIdentifier, thumbnailFile));
            } else {
                // Use default Internet Archive thumbnail
                movie.setThumbnailUrl(String.format("https://archive.org/services/img/%s", itemIdentifier));
            }
            System.out.println("ThumbnailURL set to: " + movie.getThumbnailUrl());

            movie.setDurationMinutes(duration > 0 ? duration : null);
            if (duration > 0) {
                System.out.println("Duration set to: " + duration + " minutes");
            }
        } else {
            // No files metadata found, use item identifier as path
            System.out.println("No files node found in metadata");
            movie.setFilePath(itemIdentifier);
            movie.setThumbnailUrl(String.format("https://archive.org/services/img/%s", itemIdentifier));
            System.out.println("FilePath set to identifier: " + movie.getFilePath());
            System.out.println("ThumbnailURL set to default");
        }

        System.out.println("About to save movie...");
        System.out.println("Final check - FilePath: " + movie.getFilePath());
        System.out.println("Final check - Title: " + movie.getTitle());
        System.out.println("Final check - SourceType: " + movie.getSourceType());

        Movie savedMovie = movieRepository.save(movie);
        System.out.println("Movie saved successfully with ID: " + savedMovie.getId());
        System.out.println("=========================");

        return savedMovie;
    }

    /**
     * Stream a movie from Internet Archive with range support
     * @param movieId The database movie ID
     * @param rangeHeader The HTTP Range header for partial content
     * @return ResponseEntity with proper headers for streaming
     */
    public ResponseEntity<Resource> streamMovieWithRange(Long movieId, String rangeHeader) throws Exception {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new Exception("Movie not found"));

        String filePath = movie.getFilePath();
        String[] parts = filePath.split("/", 2);

        if (parts.length != 2) {
            throw new Exception("Invalid file path format for Internet Archive movie");
        }

        String itemIdentifier = parts[0];
        String fileName = parts[1];

        try {
            // Get file size first
            long fileLength = storageService.getContentLength(itemIdentifier, fileName);

            long rangeStart = 0;
            long rangeEnd = fileLength - 1;

            // Parse range header
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] ranges = rangeHeader.substring(6).split("-");
                try {
                    rangeStart = Long.parseLong(ranges[0]);
                    if (ranges.length > 1 && !ranges[1].isEmpty()) {
                        rangeEnd = Long.parseLong(ranges[1]);
                    }
                } catch (NumberFormatException e) {
                    // Invalid range, use defaults
                }
            }

            if (rangeEnd > fileLength - 1) {
                rangeEnd = fileLength - 1;
            }

            long contentLength = rangeEnd - rangeStart + 1;

            // Build range header for Internet Archive request
            String iaRangeHeader = "bytes=" + rangeStart + "-" + rangeEnd;

            // Stream with range
            Resource resource = storageService.streamFromArchiveWithRange(itemIdentifier, fileName, iaRangeHeader);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept-Ranges", "bytes");
            headers.add("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength);
            headers.setContentLength(contentLength);
            headers.setContentType(MediaType.valueOf("video/mp4"));

            HttpStatus status = (rangeHeader != null) ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK;

            return new ResponseEntity<>(resource, headers, status);

        } catch (Exception e) {
            throw new Exception("Error streaming movie: " + e.getMessage(), e);
        }
    }

    /**
     * Stream a movie from Internet Archive (legacy method, downloads full file)
     * @param movieId The database movie ID
     * @return Resource for streaming
     */
    public Resource streamMovie(Long movieId) throws Exception {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new Exception("Movie not found"));

        String filePath = movie.getFilePath();
        String[] parts = filePath.split("/", 2);

        if (parts.length == 2) {
            return storageService.streamFromArchive(parts[0], parts[1]);
        } else {
            // If only item identifier, try to find the main video file
            JsonNode metadata = fetchItemMetadata(filePath);
            JsonNode filesNode = metadata.get("files");

            if (filesNode != null && filesNode.isArray()) {
                for (JsonNode file : filesNode) {
                    String format = file.has("format") ? file.get("format").asText() : null;
                    if (format != null && (format.contains("MPEG4") || format.contains("h.264"))) {
                        String fileName = file.get("name").asText();
                        return storageService.streamFromArchive(filePath, fileName);
                    }
                }
            }

            throw new Exception("No video file found for this movie");
        }
    }

    /**
     * Get direct download URL for a movie
     */
    public String getMovieDownloadUrl(Long movieId) throws Exception {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new Exception("Movie not found"));

        String filePath = movie.getFilePath();
        String[] parts = filePath.split("/", 2);

        if (parts.length == 2) {
            return storageService.getArchiveUrl(parts[0], parts[1]);
        } else {
            return "https://archive.org/details/" + filePath;
        }
    }

    /**
     * Search and import multiple movies from a collection
     * @param collectionId Internet Archive collection (e.g., "opensource_movies")
     * @param uploaderId User ID importing these movies
     * @param limit Maximum number of movies to import
     */
    @Transactional
    public List<Movie> importFromCollection(String collectionId, Long uploaderId, int limit) throws Exception {
        String searchUrl = String.format(
                "https://archive.org/advancedsearch.php?q=collection:(%s)+AND+mediatype:(movies)&fl[]=identifier,title,description,year,date&rows=%d&output=json",
                collectionId, limit
        );

        String response = restTemplate.getForObject(searchUrl, String.class);
        JsonNode searchResults = objectMapper.readTree(response);

        List<Movie> importedMovies = new ArrayList<>();
        JsonNode docs = searchResults.get("response").get("docs");

        if (docs != null && docs.isArray()) {
            for (JsonNode doc : docs) {
                try {
                    String identifier = doc.get("identifier").asText();

                    // Check if already imported
                    if (movieRepository.findByFilePath(identifier).isEmpty()) {
                        Movie movie = importMovie(identifier, uploaderId);
                        importedMovies.add(movie);
                    }
                } catch (Exception e) {
                    // Skip failed imports and continue
                    System.err.println("Failed to import movie: " + e.getMessage());
                }
            }
        }

        return importedMovies;
    }

    // Helper methods

    private String getStringValue(JsonNode node) {
        if (node == null) return null;

        if (node.isArray() && node.size() > 0) {
            return node.get(0).asText();
        }
        return node.asText();
    }

    private Integer extractYear(String dateString) {
        if (dateString == null) return null;

        // Try to extract 4-digit year
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(dateString);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        }

        return null;
    }
}