package com.example.demo.adapters.inbound.http;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HealthCheckController {

    @GetMapping("/health")
    public String health() {
        return "Video Processing Api is healthy";
    }
}
