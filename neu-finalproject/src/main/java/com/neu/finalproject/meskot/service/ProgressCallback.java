package com.neu.finalproject.meskot.service;

@FunctionalInterface
public interface ProgressCallback {
    void onProgress(int percent);
}