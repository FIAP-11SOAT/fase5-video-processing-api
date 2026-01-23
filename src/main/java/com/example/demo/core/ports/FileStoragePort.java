package com.example.demo.core.ports;

import com.example.demo.core.model.S3File;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStoragePort {
    S3File downloadAsStream(String videoKey, String bucketName);
    void uploadFile(MultipartFile file, String videoKey, String bucketName) throws IOException;
}
