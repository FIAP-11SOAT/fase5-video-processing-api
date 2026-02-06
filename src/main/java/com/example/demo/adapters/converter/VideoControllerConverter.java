package com.example.demo.adapters.converter;

import com.example.demo.core.model.VideoPostingRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class VideoControllerConverter {

    public VideoPostingRequest convertToVideoPostingRequest(String fileName, String userId, String userName, MultipartFile file){
        return new VideoPostingRequest(fileName, userId, userName, file);
    }
}
