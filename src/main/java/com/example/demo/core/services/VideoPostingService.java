package com.example.demo.core.services;

import com.example.demo.core.model.VideoPostingRequest;
import com.example.demo.core.ports.VideoPostingServicePort;
import com.example.demo.shared.exceptions.ErrorType;
import com.example.demo.shared.exceptions.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

public class VideoPostingService implements VideoPostingServicePort {

    private final S3Client s3Client;
    private final String bucketName;

    private static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024; // 500MB

    public VideoPostingService(
            S3Client s3Client,
            @Value("${aws.s3.bucket.videos}") String bucketName
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void upload(MultipartFile file, VideoPostingRequest request) throws IOException {

        validateFile(file);

        String key = String.format("user/%s/videos/%s", request.userId(), UUID.randomUUID());

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(
                putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );
    }

    private void validateFile(MultipartFile file){
        if (file.isEmpty()) {
            throw ExceptionUtils.badRequest(ErrorType.FILE_CANNOT_BE_EMPTY, new IllegalArgumentException(ErrorType.FILE_CANNOT_BE_EMPTY.getMessage()));
        }

        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw ExceptionUtils.badRequest(ErrorType.MAX_SIZE_EXCEEDED, new IllegalArgumentException(ErrorType.MAX_SIZE_EXCEEDED.getMessage()));
        }

        if (!file.getContentType().startsWith("video/")) {
            throw ExceptionUtils.badRequest(ErrorType.FILE_FORMAT_INVALID, new IllegalArgumentException(ErrorType.FILE_FORMAT_INVALID.getMessage()));
        }
    }
}
