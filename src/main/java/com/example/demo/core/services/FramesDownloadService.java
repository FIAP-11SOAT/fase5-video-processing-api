package com.example.demo.core.services;

import com.example.demo.adapters.outbound.repository.RepositoryPort;
import com.example.demo.core.model.S3File;
import com.example.demo.core.model.Video;
import com.example.demo.adapters.outbound.storage.FileStoragePort;
import com.example.demo.core.ports.FramesDownloadServicePort;
import com.example.demo.shared.exceptions.ErrorType;
import com.example.demo.shared.exceptions.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class FramesDownloadService implements FramesDownloadServicePort {

    private final FileStoragePort fileStorage;
    private final RepositoryPort repository;

    public FramesDownloadService(FileStoragePort fileStorage, RepositoryPort repository) {
        this.fileStorage = fileStorage;
        this.repository = repository;
    }

    @Override
    public S3File downloadZip(String videoId, String bucketName) {
        Optional<Video> video = repository.findById(videoId);

        if (video.isPresent()){
            String fileName = video.get().getName();
            String s3Key = video.get().getVideoKey() + ".zip";
            S3File zip = fileStorage.downloadAsStream(s3Key, bucketName);
            zip.setFileName(fileName);
            return zip;
        }

        throw ExceptionUtils.badRequest(ErrorType.FILE_NOT_FOUND, new RuntimeException(ErrorType.FILE_NOT_FOUND.getMessage()));
    }

    @Override
    public String getUrl(String videoId, String bucketName, String userId) {
        Optional<Video> videoOptional = repository.findById(videoId);

        if (videoOptional.isEmpty()){
            throw ExceptionUtils.badRequest(ErrorType.FILE_NOT_FOUND, new RuntimeException(ErrorType.FILE_NOT_FOUND.getMessage()));
        }

        Video video = videoOptional.get();

        if (!userId.equals(video.getUserId())){
            throw ExceptionUtils.forbidden(ErrorType.FORBIDDEN, new RuntimeException(ErrorType.FORBIDDEN.getMessage()));
        }

        String s3Key = video.getVideoKey() + ".zip";
        return fileStorage.generatePresignedUrl(s3Key, bucketName);
    }
}
