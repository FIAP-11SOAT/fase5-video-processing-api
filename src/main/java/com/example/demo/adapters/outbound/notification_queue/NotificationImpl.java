package com.example.demo.adapters.outbound.notification_queue;

import com.example.demo.adapters.outbound.model.NotificationPayload;

import com.example.demo.adapters.outbound.model.Payload;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationImpl implements NotificationPort {

    private final SqsTemplate sqsTemplate;
    private final String queue;

    public NotificationImpl(
            SqsTemplate sqsTemplate,
            @Value("${aws.sqs.notification}") String queue
    ) {
        this.sqsTemplate = sqsTemplate;
        this.queue = queue;
    }

    @Override
    public void send(NotificationPayload message) {
        Payload payload = new Payload(message);
        this.sqsTemplate.send(queue, payload);
    }
}
