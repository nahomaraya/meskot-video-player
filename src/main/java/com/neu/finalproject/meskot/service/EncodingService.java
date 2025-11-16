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

    public File encode(File input, String resolution, String outputFormat, String codec, ProgressCallback callback)
            throws IOException, FrameGrabber.Exception, FrameRecorder.Exception {

        String baseName = input.getName().substring(0, input.getName().lastIndexOf('.'));
        File output = new File(input.getParentFile(), baseName + "_" + resolution + "_" + codec + "." + outputFormat);

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
        try {
            grabber.start();

            // --- Get Total Frames for Progress ---
            long totalFrames = grabber.getLengthInFrames();
            int currentFrame = 0;

            // --- (Your existing aspect ratio logic) ---
            int targetHeight = Integer.parseInt(resolution.replaceAll("p", ""));
            double aspectRatio = (double) grabber.getImageWidth() / grabber.getImageHeight();
            int targetWidth = (int) (targetHeight * aspectRatio);
            if (targetWidth % 2 != 0) {
                targetWidth++;
            }

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output,
                    targetWidth, targetHeight, grabber.getAudioChannels());

            try {
                // --- (Your existing recorder setup) ---
                recorder.setFormat(outputFormat);
                if ("h265".equalsIgnoreCase(codec)) {
                    recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                } else {
                    recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                    recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                }
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
                recorder.setVideoOption("crf", "23");

                recorder.start();

                // --- Main Encoding Loop ---
                Frame frame;
                while ((frame = grabber.grab()) != null) {
                    recorder.record(frame);

                    // --- Report Progress ---
                    currentFrame++;
                    if (totalFrames > 0 && currentFrame % 100 == 0) { // Report every 100 frames
                        int percent = (int) (((double) currentFrame / totalFrames) * 100);
                        if(callback != null) {
                            callback.onProgress(percent);
                        }
                    }
                }

                // --- Final progress update ---
                if(callback != null) {
                    callback.onProgress(100);
                }

            } finally {
                recorder.stop();
                recorder.release();
            }
        } finally {
            grabber.stop();
            grabber.release();
        }

        return output;
    }

// You can now delete your getWidthForResolution and getHeightForResolution methods
   }
