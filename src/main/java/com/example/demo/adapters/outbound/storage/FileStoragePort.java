package com.example.demo.adapters.outbound.storage;

import com.example.demo.core.model.S3File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStoragePort {
    S3File downloadAsStream(String videoKey, String bucketName);
    void uploadFile(MultipartFile file, String videoKey, String bucketName) throws IOException;
    String generatePresignedUrl(String videoKey, String bucketName);
}
