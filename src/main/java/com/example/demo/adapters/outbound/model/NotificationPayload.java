package com.example.demo.adapters.outbound.model;

public record NotificationPayload(
        String videoKey,
        String videoName,
        String userId,
        String status
) {
}
