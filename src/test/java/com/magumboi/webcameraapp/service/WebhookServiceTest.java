package com.magumboi.webcameraapp.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void testUploadPhotoToWebhook_NoWebhookUrl() {
        // Set empty webhook URL
        ReflectionTestUtils.setField(webhookService, "webhookUrl", "");

        // Create a mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Test the upload
        Mono<String> result = webhookService.uploadPhotoToWebhook(file);

        // Verify it throws an error
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException &&
                        throwable.getMessage().contains("Webhook URL is not configured"))
                .verify();
    }

    @Test
    void testUploadPhotoToWebhook_NullWebhookUrl() {
        // Set null webhook URL
        ReflectionTestUtils.setField(webhookService, "webhookUrl", null);

        // Create a mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Test the upload
        Mono<String> result = webhookService.uploadPhotoToWebhook(file);

        // Verify it throws an error
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException &&
                        throwable.getMessage().contains("Webhook URL is not configured"))
                .verify();
    }

    @Test
    void testUploadPhotoToWebhook_WhitespaceWebhookUrl() {
        // Set whitespace-only webhook URL
        ReflectionTestUtils.setField(webhookService, "webhookUrl", "   ");

        // Create a mock file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // Test the upload
        Mono<String> result = webhookService.uploadPhotoToWebhook(file);

        // Verify it throws an error
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof IllegalStateException &&
                        throwable.getMessage().contains("Webhook URL is not configured"))
                .verify();
    }
}
