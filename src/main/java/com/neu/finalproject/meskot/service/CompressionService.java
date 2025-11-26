package com.neu.finalproject.meskot.service;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.function.Consumer;

/**
 * Local video compression service that runs entirely on the client.
 * Uses JavaCV/FFmpeg for encoding.
 */
@Service
public class CompressionService {

    /**
     * Compress a video file
     *
     * @param inputFile Source video file
     * @param outputFile Destination file
     * @param resolution Target resolution (e.g., "720p", "480p") or null for original
     * @param codec "h264" or "h265"
     * @param crf Quality (18=high, 23=medium, 28=low, 32=very low)
     * @param progressCallback Progress callback (0-100)
     * @return The output file
     */
    public File compress(File inputFile, File outputFile, String resolution,
                         String codec, int crf, Consumer<Integer> progressCallback)
            throws Exception {

        System.out.println("=== LOCAL COMPRESSION ===");
        System.out.println("Input: " + inputFile.getAbsolutePath());
        System.out.println("Output: " + outputFile.getAbsolutePath());
        System.out.println("Resolution: " + (resolution != null ? resolution : "Original"));
        System.out.println("Codec: " + codec);
        System.out.println("CRF: " + crf);

        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputFile);
        grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        try {
            grabber.start();

            long totalFrames = grabber.getLengthInFrames();
            int currentFrame = 0;

            // Calculate target dimensions
            int targetWidth = grabber.getImageWidth();
            int targetHeight = grabber.getImageHeight();

            if (resolution != null && !resolution.isEmpty()) {
                targetHeight = parseResolution(resolution);
                double aspectRatio = (double) grabber.getImageWidth() / grabber.getImageHeight();
                targetWidth = (int) (targetHeight * aspectRatio);

                // Ensure even dimensions (required by most codecs)
                if (targetWidth % 2 != 0) targetWidth++;
                if (targetHeight % 2 != 0) targetHeight++;
            }

            System.out.println("Output dimensions: " + targetWidth + "x" + targetHeight);

            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(
                    outputFile, targetWidth, targetHeight, grabber.getAudioChannels()
            );

            try {
                // Configure recorder
                recorder.setFormat("mp4");
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

                // Set video codec
                if ("h265".equalsIgnoreCase(codec)) {
                    recorder.setVideoCodec(avcodec.AV_CODEC_ID_HEVC);
                } else {
                    recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                }

                // Set audio codec
                recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
                if (grabber.getAudioChannels() > 0) {
                    recorder.setSampleRate(grabber.getSampleRate());
                    recorder.setAudioChannels(grabber.getAudioChannels());
                }

                // Set quality
                recorder.setVideoOption("crf", String.valueOf(crf));
                recorder.setVideoOption("preset", "medium");

                recorder.start();

                System.out.println("Encoding started...");

                // Process frames
                Frame frame;
                int lastReportedPercent = -1;

                while ((frame = grabber.grab()) != null) {
                    recorder.record(frame);
                    currentFrame++;

                    // Report progress
                    if (totalFrames > 0 && currentFrame % 30 == 0) {
                        int percent = (int) (((double) currentFrame / totalFrames) * 100);
                        if (percent != lastReportedPercent && progressCallback != null) {
                            progressCallback.accept(percent);
                            lastReportedPercent = percent;
                        }
                    }
                }

                // Final progress update
                if (progressCallback != null) {
                    progressCallback.accept(100);
                }

                System.out.println("Encoding complete!");

            } finally {
                recorder.stop();
                recorder.release();
            }
        } finally {
            grabber.stop();
            grabber.release();
        }

        return outputFile;
    }

    /**
     * Parse resolution string to height in pixels
     */
    private int parseResolution(String resolution) {
        if (resolution == null) return 0;

        String cleaned = resolution.toLowerCase().replace("p", "").trim();
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            // Default mappings
            switch (resolution.toLowerCase()) {
                case "4k":
                case "2160p": return 2160;
                case "1440p": return 1440;
                case "1080p": return 1080;
                case "720p": return 720;
                case "480p": return 480;
                case "360p": return 360;
                case "240p": return 240;
                default: return 720;
            }
        }
    }

    /**
     * Get video info without processing
     */
    public VideoInfo getVideoInfo(File file) throws Exception {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(file);
        try {
            grabber.start();

            VideoInfo info = new VideoInfo();
            info.width = grabber.getImageWidth();
            info.height = grabber.getImageHeight();
            info.frameRate = grabber.getFrameRate();
            info.duration = grabber.getLengthInTime() / 1000000.0; // Convert to seconds
            info.totalFrames = grabber.getLengthInFrames();
            info.audioChannels = grabber.getAudioChannels();
            info.sampleRate = grabber.getSampleRate();
            info.fileSize = file.length();

            return info;
        } finally {
            grabber.stop();
            grabber.release();
        }
    }

    /**
     * Video info container
     */
    public static class VideoInfo {
        public int width;
        public int height;
        public double frameRate;
        public double duration;
        public long totalFrames;
        public int audioChannels;
        public int sampleRate;
        public long fileSize;

        public String getResolutionString() {
            return width + "x" + height;
        }

        public String getDurationString() {
            int hours = (int) (duration / 3600);
            int minutes = (int) ((duration % 3600) / 60);
            int seconds = (int) (duration % 60);

            if (hours > 0) {
                return String.format("%d:%02d:%02d", hours, minutes, seconds);
            } else {
                return String.format("%d:%02d", minutes, seconds);
            }
        }

        public String getFileSizeString() {
            double mb = fileSize / (1024.0 * 1024.0);
            if (mb > 1024) {
                return String.format("%.2f GB", mb / 1024.0);
            }
            return String.format("%.2f MB", mb);
        }
    }
}