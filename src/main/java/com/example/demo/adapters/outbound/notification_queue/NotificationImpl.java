package com.example.demo.adapters.outbound.notification_queue;

import com.example.demo.adapters.outbound.model.NotificationPayload;

import com.example.demo.adapters.outbound.model.Payload;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.util.concurrent.CompletableFuture;

@Service
public class NotificationImpl implements NotificationPort {

    private final SqsTemplate sqsTemplate;
    private final SqsAsyncClient sqsAsyncClient;
    private final String queueName;
    private final String region;
    private String queueUrl;

    public NotificationImpl(
            SqsTemplate sqsTemplate,
            SqsAsyncClient sqsAsyncClient,
            @Value("${aws.sqs.notification}") String queueName,
            @Value("${aws.region}") String region
    ) {
        this.sqsTemplate = sqsTemplate;
        this.sqsAsyncClient = sqsAsyncClient;
        this.queueName = queueName;
        this.region = region;
        this.queueUrl = null;
    }

    private CompletableFuture<String> getQueueUrl() {
        if (queueUrl != null) {
            return CompletableFuture.completedFuture(queueUrl);
        }

        return sqsAsyncClient.getQueueUrl(builder -> builder.queueName(queueName))
                .thenApply(response -> {
                    queueUrl = response.queueUrl();
                    return queueUrl;
                });
    }

    @Override
    public void send(NotificationPayload message) {
        Payload payload = new Payload(message);

        getQueueUrl().thenAccept(url -> {
            this.sqsTemplate.send(url, payload);
        }).join();
    }
}
