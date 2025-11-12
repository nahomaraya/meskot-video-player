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
    private long sizeInBytes;
    private String format;

    public MovieDto(Long id, String title, String filePath, LocalDateTime uploadedDate) {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.uploadedDate = uploadedDate;
    }

    public MovieDto() {

    }

    // ✅ Static factory method
    public static MovieDto fromEntity(Movie movie) {
        return new MovieDto(
                movie.getId(),
                movie.getTitle(),
                movie.getFilePath(),
                movie.getUploadedDate()
        );
    }
}
