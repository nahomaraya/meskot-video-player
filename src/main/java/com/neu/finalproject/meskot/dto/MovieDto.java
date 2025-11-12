package com.neu.finalproject.meskot.dto;


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
}
