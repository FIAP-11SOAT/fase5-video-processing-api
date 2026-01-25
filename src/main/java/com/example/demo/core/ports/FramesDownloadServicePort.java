package com.example.demo.core.ports;

import com.example.demo.core.model.S3File;

public interface FramesDownloadServicePort {
    S3File downloadZip(String videoKey, String bucketName);
    String getUrl(String videoKey, String bucketName);
}
