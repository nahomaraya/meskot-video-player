package com.neu.finalproject.meskot.controller;


import com.neu.finalproject.meskot.model.Movie;
import com.neu.finalproject.meskot.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.io.File;

@RestController
@RequestMapping("/api/upload")
public class Controller {

    @Autowired
    private MovieService movieService;

    @PostMapping
    @Operation(summary = "Upload a video", description = "Uploads a video file with title and resolution")

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
