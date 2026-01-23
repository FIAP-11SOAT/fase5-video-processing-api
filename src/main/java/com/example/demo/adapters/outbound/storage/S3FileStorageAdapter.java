package com.example.demo.adapters.outbound.storage;

import com.example.demo.core.model.S3File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import com.example.demo.core.ports.FileStoragePort;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Slf4j
@Component
public class S3FileStorageAdapter implements FileStoragePort {

    private final S3Client s3Client;

    public S3FileStorageAdapter(
            S3Client s3Client
    ) {
        this.s3Client = s3Client;
    }

    @Override
    public S3File downloadAsStream(String videoKey, String bucketName) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object =
                    s3Client.getObject(request);

            return new S3File(
                    s3Object,
                    s3Object.response().contentLength(),
                    null
            );

        } catch (NoSuchKeyException e) {
            log.error("[S3FileStorageAdapter]: Error downloadAsStream() {}", e.getMessage());
            throw new RuntimeException("Arquivo não encontrado no S3", e);
        }
    }

    @Override
    public void uploadFile(MultipartFile file, String videoKey, String bucketName) throws IOException {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(videoKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch(Exception e){
            log.error("[S3FileStorageAdapter]: Error uploadFile() {}", e.getMessage());
            throw e;

        }
    }
}
