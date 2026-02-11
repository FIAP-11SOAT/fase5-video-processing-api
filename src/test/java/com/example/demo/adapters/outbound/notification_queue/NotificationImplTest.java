package com.example.demo.adapters.outbound.notification_queue;

import com.example.demo.adapters.outbound.model.NotificationPayload;
import com.example.demo.adapters.outbound.model.Payload;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationImplTest {

    @Mock
    private SqsTemplate sqsTemplate;

    @Mock
    private SqsAsyncClient sqsAsyncClient;

    private NotificationImpl notification;

    private static final String QUEUE = "notification-queue";
    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789012/notification-queue";
    private static final String REGION = "us-east-1";

    @BeforeEach
    void setup() {
        notification = new NotificationImpl(sqsTemplate, sqsAsyncClient, QUEUE, REGION);

        // Mock getQueueUrl response
        GetQueueUrlResponse response = GetQueueUrlResponse.builder()
                .queueUrl(QUEUE_URL)
                .build();

        when(sqsAsyncClient.getQueueUrl(any(Consumer.class)))
                .thenReturn(CompletableFuture.completedFuture(response));
    }

    @Test
    void shouldSendMessageToSqsWithCorrectQueueAndPayload() {
        // arrange
        NotificationPayload notificationPayload = new NotificationPayload(
                "video-key-1",
                "video.mp4",
                "user-1",
                "PROCESSED"
        );

        ArgumentCaptor<Payload> payloadCaptor =
                ArgumentCaptor.forClass(Payload.class);

        // act
        notification.send(notificationPayload);

        // assert
        verify(sqsTemplate).send(eq(QUEUE_URL), payloadCaptor.capture());

        Payload sentPayload = payloadCaptor.getValue();
        assertNotNull(sentPayload);

        NotificationPayload captured = sentPayload.payload();
        assertEquals("video-key-1", captured.videoKey());
        assertEquals("video.mp4", captured.videoName());
        assertEquals("user-1", captured.userId());
        assertEquals("PROCESSED", captured.status());
    }

    @Test
    void shouldCallSqsTemplateSendExactlyOnce() {
        // arrange
        NotificationPayload notificationPayload = new NotificationPayload(
                "video-key-2",
                "video2.mp4",
                "user-2",
                "FAILED"
        );

        // act
        notification.send(notificationPayload);

        // assert
        verify(sqsTemplate, times(1))
                .send(eq(QUEUE_URL), any(Payload.class));
        verifyNoMoreInteractions(sqsTemplate);
    }
}

