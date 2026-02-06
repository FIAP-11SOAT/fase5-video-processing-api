package com.example.demo.core.model;

import org.springframework.web.multipart.MultipartFile;

public record VideoPostingRequest(
        String fileName,
        String userId,
        String userName,
        MultipartFile file
) {
}
