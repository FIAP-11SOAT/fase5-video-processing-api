package com.example.demo.adapters.outbound.notification_queue;

import com.example.demo.adapters.outbound.model.NotificationPayload;

public interface NotificationPort {
    void send(NotificationPayload message);
}
