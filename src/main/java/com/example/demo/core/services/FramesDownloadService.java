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
        log.info("Solicitação de download - id={}", videoId);
        Optional<Video> video = repository.findById(videoId);

        if (video.isPresent()){
            String fileName = video.get().getName();
            // No S3, os frames processados têm extensão .zip
            String s3Key = video.get().getVideoKey() + ".zip";
            S3File zip = fileStorage.downloadAsStream(bucketName, s3Key);
            zip.setFileName(fileName);
            return zip;
        }

        throw ExceptionUtils.badRequest(ErrorType.FILE_NOT_FOUND, new RuntimeException(ErrorType.FILE_NOT_FOUND.getMessage()));
    }

    @Override
    public String getUrl(String videoId, String bucketName) {
        log.info("Solicitação de URL download - id={}", videoId);
        Optional<Video> video = repository.findById(videoId);

        if (video.isPresent()){
            // No S3, os frames processados têm extensão .zip
            String s3Key = video.get().getVideoKey() + ".zip";
            return fileStorage.generatePresignedUrl(s3Key, bucketName);
        }

        throw ExceptionUtils.badRequest(ErrorType.FILE_NOT_FOUND, new RuntimeException(ErrorType.FILE_NOT_FOUND.getMessage()));
    }
}
