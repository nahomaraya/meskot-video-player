package com.neu.finalproject.meskot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MovieItemDto {
    private Long id;
    private String title;
    private String filePath;
    private String resolution;
    private String format;
    private long sizeInBytes;
    private LocalDateTime uploadedDate;
}
