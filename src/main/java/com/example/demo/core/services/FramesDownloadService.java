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
    public S3File downloadZip(String videoKey, String bucketName) {
        log.info("Solicitação de download - key={}", videoKey);
        String key = videoKey.replaceFirst("\\.zip$", "");
        Optional<Video> video = repository.findByVideoKey(key);

        if (video.isPresent()){
            String fileName = video.get().getName();
            S3File zip = fileStorage.downloadAsStream(bucketName, videoKey);
            zip.setFileName(fileName);
            return zip;
        }

        throw ExceptionUtils.badRequest(ErrorType.FILE_NOT_FOUND, new RuntimeException(ErrorType.FILE_NOT_FOUND.getMessage()));
    }

    @Override
    public String getUrl(String videoKey, String bucketName) {
        log.info("Solicitação de URL download - key={}", videoKey);
        String key = videoKey.replaceFirst("\\.zip$", "");
        System.out.println("key: " + key);
        Optional<Video> video = repository.findByVideoKey(key);

        if (video.isPresent()){
            return fileStorage.generatePresignedUrl(videoKey, bucketName);
        }

        throw ExceptionUtils.badRequest(ErrorType.FILE_NOT_FOUND, new RuntimeException(ErrorType.FILE_NOT_FOUND.getMessage()));
    }
}
