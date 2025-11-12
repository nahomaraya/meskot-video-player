package com.neu.finalproject.meskot.controller;


import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor


public class Controller {

    static class  UploadVideoRequest {
        @Schema(type = "string", format = "binary", description = "Video file to upload")
        public MultipartFile file;

        @Schema(defaultValue = "Sample Video", description = "Title of the video")
        public String title;

        @Schema(defaultValue = "720p", description = "Resolution of the video")
        public String resolution;
    }
    private final MovieService movieService;

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) {
        try {
            return movieService.streamMovie(id, rangeHeader);
        }
        catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    @Operation(
            summary = "Upload a video",
            description = "Uploads a video file with title and resolution",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = UploadVideoRequest.class)
                    )
            )
    )
    public String uploadVideo(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Video file to upload",
                    required = true,
                    content = @Content(schema = @Schema(type = "string", format = "binary"))
            )
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Title of the video")
            @RequestParam(defaultValue = "Sample Video") String title,

            @Parameter(description = "Resolution of the video")
            @RequestParam(defaultValue = "720p") String resolution
    ) {
        try {
            // Save temp file
            File tempFile = File.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile);

            // Process with MovieService
            Movie movie = movieService.handleUpload(tempFile, title, resolution);

            return "Uploaded successfully! Stored at: " + movie.getFilePath();
        } catch (Exception e) {
            e.printStackTrace();
            return "Upload failed: " + e.getMessage();
        }
    }
}
