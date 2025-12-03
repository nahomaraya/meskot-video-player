package com.neu.finalproject.meskot.ui.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadJob {
    private String id;
    private String status;
    private int progress;
    private Long resultingMovieId;
    private String errorMessage;
}