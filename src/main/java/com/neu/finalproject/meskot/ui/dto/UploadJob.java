package com.neu.finalproject.meskot.ui.dto;

import lombok.Getter;

@Getter
public class UploadJob {
    private String id;
    private String status;
    private int progress;
    private Long resultingMovieId;
    private String errorMessage;
}