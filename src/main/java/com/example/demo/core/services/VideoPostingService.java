package com.example.demo.core.services;

import com.example.demo.adapters.converter.VideoModelMapper;
import com.example.demo.adapters.dto.VideoResponseDto;
import com.example.demo.adapters.outbound.model.NotificationPayload;
import com.example.demo.adapters.outbound.notification_queue.NotificationPort;
import com.example.demo.adapters.outbound.repository.RepositoryPort;
import com.example.demo.core.model.Video;
import com.example.demo.core.model.VideoPostingRequest;
import com.example.demo.adapters.outbound.storage.FileStoragePort;
import com.example.demo.core.ports.VideoPostingServicePort;
import com.example.demo.shared.exceptions.ErrorType;
import com.example.demo.shared.exceptions.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class VideoPostingService implements VideoPostingServicePort {

    private final FileStoragePort fileStorage;
    private final RepositoryPort repository;
    private final NotificationPort notificationService;
    private final String STATUS_UPLOADED = "uploaded";

    private static final long MAX_VIDEO_SIZE = 500L * 1024 * 1024; // 500MB

    public VideoPostingService(FileStoragePort fileStorage, RepositoryPort repository, NotificationPort notificationService) {
        this.fileStorage = fileStorage;
        this.repository = repository;
        this.notificationService = notificationService;
    }

    @Override
    public void upload(VideoPostingRequest request, String bucketName) throws IOException {

        try {
            MultipartFile file = request.file();
            validateFile(file);
            UUID uuid = UUID.randomUUID();
            String key = String.format("%s/%s", request.userName(), uuid);
            fileStorage.uploadFile(file, key, bucketName);
            Video video = buildVideo(key, uuid, request);
            repository.save(video);
            sendNotification(request, key);
        } catch (Exception e){
            log.error("[VideoPostingService]: Error upload() {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<VideoResponseDto> getVideos(String userId) {
        return repository.findByUserId(userId).stream().map(VideoModelMapper::toDto).toList();
    }

    @Override
    public VideoResponseDto getVideoByVideoKey(String videoKey) {
        Video video = repository.findByVideoKey(videoKey)
                .orElseThrow(() -> ExceptionUtils.notFound(
                        ErrorType.VIDEO_NOT_FOUND,
                        new IllegalArgumentException("Video not found with key: " + videoKey)
                ));
        return VideoModelMapper.toDto(video);
    }

    private void validateFile(MultipartFile file){
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
        video.setName(request.file().getOriginalFilename());
        video.setUserId(request.userId());
        video.setStatus(STATUS_UPLOADED);
        video.setCreatedAt(OffsetDateTime.now());
        video.setUpdatedAt(OffsetDateTime.now());

        return video;
    }

    private void sendNotification(VideoPostingRequest video, String videoKey){
        NotificationPayload message = new NotificationPayload(videoKey, video.file().getOriginalFilename(), video.userId(), STATUS_UPLOADED);
        notificationService.send(message);
    }
}
