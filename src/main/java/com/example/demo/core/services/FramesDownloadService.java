package com.example.demo.core.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

@Slf4j
@Service
public class FramesDownloadService {

    private final S3Client s3Client;
    private final String bucketName;

    public FramesDownloadService(
            S3Client s3Client,
            @Value("${aws.s3.bucket.frames}") String bucketName
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }
}
