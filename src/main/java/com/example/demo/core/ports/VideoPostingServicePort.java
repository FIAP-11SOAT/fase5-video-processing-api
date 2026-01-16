package com.example.demo.core.ports;

import com.example.demo.core.model.VideoPostingRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface VideoPostingServicePort {

    void upload(MultipartFile file, VideoPostingRequest request) throws IOException;
}
