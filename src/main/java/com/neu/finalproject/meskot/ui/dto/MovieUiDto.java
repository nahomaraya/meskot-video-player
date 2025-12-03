package com.neu.finalproject.meskot.ui.dto;

import lombok.Data;
import java.time.LocalDateTime;

// You already had this, just ensuring it exists
@Data
public class MovieUiDto {
    private Long id;
    private String title;
    private String filePath;
    private LocalDateTime uploadedDate;
    // Add other fields as needed
}