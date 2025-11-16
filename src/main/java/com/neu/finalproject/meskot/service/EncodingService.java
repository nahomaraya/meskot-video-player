package com.neu.finalproject.meskot.service;

import org.springframework.beans.factory.annotation.Value;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EncodingService {

    @Value("${app.ffmpeg.path:ffmpeg}")
    private String ffmpegPath; // allow override via properties

//    File encode(File input, String resolution, String outputFormat) throws IOException, InterruptedException {
//        return null;
//    }

    private int getWidthForResolution(int originalWidth, String resolution) {
        switch (resolution.toLowerCase()) {
            case "720p":
                return 1280;
            case "480p":
                return 854;
            case "1080p":
                return 1920;
            default:
                return originalWidth;
        }
    }

    private int getHeightForResolution(int originalHeight, String resolution) {
        switch (resolution.toLowerCase()) {
            case "720p":
                return 720;
            case "480p":
                return 480;
            case "1080p":
                return 1080;
            default:
                return originalHeight;
        }
    }

    // In EncodingService.java

    public File encode(File input, String resolution, String outputFormat, String codec)
            throws IOException, FrameGrabber.Exception, FrameRecorder.Exception {

        String baseName = input.getName().substring(0, input.getName().lastIndexOf('.'));
        File output = new File(input.getParentFile(), baseName + "_" + resolution + "_" + codec + "." + outputFormat);

        // Initialize grabber
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
        grabber.start();

        // --- 1. Fix Aspect Ratio ---
        // Get the target height from the resolution string (e.g., "720p" -> 720)
        int targetHeight = Integer.parseInt(resolution.replaceAll("p", ""));
        double aspectRatio = (double) grabber.getImageWidth() / grabber.getImageHeight();

        // Calculate new width to maintain aspect ratio
        // Must be divisible by 2 for many codecs
        int targetWidth = (int) (targetHeight * aspectRatio);
        if (targetWidth % 2 != 0) {
            targetWidth++;
        }

        // Setup recorder
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output,
                targetWidth,
                targetHeight,
                grabber.getAudioChannels());

        recorder.setFormat(outputFormat);

        // --- 2. Set the Codec ---
        if ("h265".equalsIgnoreCase(codec)) {
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC); // AAC is fine for H.265
        } else {
            // Default H.264
            recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        }

        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P); // Good default for compatibility

        // --- 3. Fix Bitrate ---
        // REMOVE: recorder.setVideoBitrate(grabber.getVideoBitrate());

        // USE THIS INSTEAD:
        // A CRF (Constant Rate Factor) of 23 is a good balance of quality/size.
        // Lower is better quality (e.g., 18). Higher is worse quality (e.g., 28).
        recorder.setVideoOption("crf", "23");

        recorder.start();

        Frame frame;
        while ((frame = grabber.grab()) != null) {
            recorder.record(frame);
        }

        recorder.stop();
        recorder.release(); // Good practice to release resources
        grabber.stop();
        grabber.release();

        return output;
    }

// You can now delete your getWidthForResolution and getHeightForResolution methods
   }
