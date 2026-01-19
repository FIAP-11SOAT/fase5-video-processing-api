package com.example.demo.core.services;

import com.example.demo.adapters.outbound.repository.RepositoryPort;
import com.example.demo.core.model.Video;
import com.example.demo.core.model.VideoPostingRequest;
import com.example.demo.core.ports.VideoPostingServicePort;
import com.example.demo.shared.exceptions.ErrorType;
import com.example.demo.shared.exceptions.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
public class VideoPostingService implements VideoPostingServicePort {

    private final S3Client s3Client;
    private final String bucketName;
    private final RepositoryPort repository;
    private final String STATUS_UPLOADED = "uploaded";

    private static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024; // 500MB

    public VideoPostingService(
            S3Client s3Client,
            @Value("${aws.s3.bucket.videos}") String bucketName,
            RepositoryPort repository
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.repository = repository;
    }

    @Override
    public void upload(MultipartFile file, VideoPostingRequest request) throws IOException {

        try {
            validateFile(file);
            UUID uuid = UUID.randomUUID();
            String key = String.format("%s/%s", request.userId(), uuid);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            Video video = buildVideo(key, uuid, request);
            repository.save(video);
        } catch (Exception e){
            log.error("[VideoPostingService]: Error save() {}", e.getMessage());
            throw e;
        }
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

    private Video buildVideo(String key, UUID uuid, VideoPostingRequest request){
        Video video = new Video();
        video.setId(uuid);
        video.setVideoKey(key);
        video.setName(request.fileName());
        video.setUserId(request.userId());
        video.setStatus(STATUS_UPLOADED);
        video.setCreatedAt(OffsetDateTime.now());
        video.setUpdatedAt(OffsetDateTime.now());

        return video;
    }
}
