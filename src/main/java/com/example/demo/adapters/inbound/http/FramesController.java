package com.example.demo.adapters.inbound.http;

import com.example.demo.core.model.S3File;
import com.example.demo.core.ports.FramesDownloadServicePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/videos")
public class FramesController {

    private final FramesDownloadServicePort framesService;
    private final String bucketName;

    public FramesController(
            FramesDownloadServicePort framesService,
            @Value("${aws.s3.bucket.frames}") String bucketName
    ) {
        this.framesService = framesService;
        this.bucketName = bucketName;

    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadZip(
            @RequestParam String videoKey,
            @AuthenticationPrincipal Jwt jwt
    ) {
        S3File file = framesService.downloadZip(videoKey, bucketName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.getContentLength())
                .body(new InputStreamResource(file.getInputStream()));
    }

    @GetMapping("/download-url")
    public ResponseEntity<String> generateDownloadUrl(
            @RequestParam String videoKey,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String url = framesService.getUrl(videoKey, bucketName);
        return ResponseEntity.ok(url);
    }
}
