package com.magumboi.webcameraapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WebhookService {

    @Value("${webhook.url:}")
    private String webhookUrl;

    private final WebClient webClient;

    public WebhookService() {
        this.webClient = WebClient.builder()
                // 25MB limit
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(25 * 1024 * 1024)) // 25MB limit
                .build();
    }

    public Mono<String> uploadPhotoToWebhook(MultipartFile photo) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            return Mono.error(new IllegalStateException("Webhook URL is not configured"));
        }

        try {
            // Generate timestamp for filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "camera-photo-" + timestamp + ".jpg";
            
            // Create multipart body
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", new ByteArrayResource(photo.getBytes()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            }, MediaType.IMAGE_JPEG);
            
            // Add content message
            String content = "📸 Nueva foto tomada - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            builder.part("content", content);

            // Make the request to the webhook
            return webClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorMap(ex -> new RuntimeException("Failed to upload photo to webhook: " + ex.getMessage(), ex));

        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to read photo data: " + e.getMessage(), e));
        }
    }
}
