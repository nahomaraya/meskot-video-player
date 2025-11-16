package com.neu.finalproject.meskot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data // From Lombok
public class UploadJob {

    @Id
    private Long id;

    private String status; // e.g., PENDING, ENCODING, COMPLETED, FAILED

    private int progress; // 0-100

    private Long resultingMovieId; // The ID of the movie once complete

    private String errorMessage; // If it failed
}