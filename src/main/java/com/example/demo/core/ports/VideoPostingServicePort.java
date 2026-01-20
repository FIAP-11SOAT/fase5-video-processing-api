package com.example.demo.core.ports;

import com.example.demo.core.model.VideoPostingRequest;

import java.io.IOException;

public interface VideoPostingServicePort {

    void upload(VideoPostingRequest request) throws IOException;
}
