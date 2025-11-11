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

    public File encode(File input, String resolution, String outputFormat)
            throws IOException, FrameGrabber.Exception, FrameRecorder.Exception {
        String baseName = input.getName().substring(0, input.getName().lastIndexOf('.'));
        File output = new File(input.getParentFile(), baseName + "_" + resolution + "." + outputFormat);

        // Initialize grabber
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);
        grabber.start();

        // Setup recorder
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output,
                getWidthForResolution(grabber.getImageWidth(), resolution),
                getHeightForResolution(grabber.getImageHeight(), resolution),
                grabber.getAudioChannels());
        recorder.setFormat(outputFormat);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoBitrate(grabber.getVideoBitrate());
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        recorder.start();

        Frame frame;
        while ((frame = grabber.grab()) != null) {
            recorder.record(frame);
        }

        recorder.stop();
        grabber.stop();

        return output;
    }
   }
