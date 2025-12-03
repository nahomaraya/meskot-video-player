package com.neu.finalproject.meskot.dto;

import com.neu.finalproject.meskot.model.Movie;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MovieDto {
    private Long id;
    private String title;
    private String filePath;
    private LocalDateTime uploadedDate;
    private String resolution;
    private Long sizeInBytes;
    private String format;
    private String sourceType;
    private String description;
    private String genre;
    private Integer releaseYear;
    private Integer durationMinutes;
    private String thumbnailUrl;

    // Default constructor
    public MovieDto() {
    }

    // Basic constructor
    public MovieDto(Long id, String title, String filePath, LocalDateTime uploadedDate) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.uploadedDate = uploadedDate;
    }

    /**
     * Convert Movie entity to DTO - FIXED to include all fields
     */
    public static MovieDto fromEntity(Movie movie) {
        MovieDto dto = new MovieDto();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setFilePath(movie.getFilePath());
        dto.setUploadedDate(movie.getUploadedDate());
        dto.setSourceType(movie.getSourceType());  // IMPORTANT: Was missing!
        dto.setDescription(movie.getDescription());
        dto.setGenre(movie.getGenre());
        dto.setReleaseYear(movie.getReleaseYear());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setThumbnailUrl(movie.getThumbnailUrl());

        // Get metadata if available
        if (movie.getMetadataList() != null && !movie.getMetadataList().isEmpty()) {
            var meta = movie.getMetadataList().get(0);
            dto.setResolution(meta.getResolution());
            dto.setSizeInBytes(meta.getSizeInBytes());
            dto.setFormat(meta.getFormat());
        }

        return dto;
    }
}