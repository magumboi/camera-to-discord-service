package com.magumboi.webcameraapp.controller;

import com.magumboi.webcameraapp.service.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PhotoUploadController {

    @Autowired
    private WebhookService webhookService;

    @PostMapping("/upload-photo")
    public ResponseEntity<Map<String, String>> uploadPhoto(@RequestParam(value = "file", required = false) MultipartFile file) {
        // Validate file
        if (file == null || file.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "No file provided");
            return ResponseEntity.badRequest().body(response);
        }

        // Check if it's an image
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "File must be an image");
            return ResponseEntity.badRequest().body(response);
        }

        // Upload to webhook (blocking call)
        try {
            webhookService.uploadPhotoToWebhook(file).block();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Photo uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception error) {
            Map<String, String> response = new HashMap<>();
            
            // Check if it's a webhook configuration error
            if (error instanceof IllegalStateException && 
                error.getMessage().contains("Webhook URL is not configured")) {
                response.put("error", "Webhook no configurado. La URL del webhook no está configurada en el servidor.");
                return ResponseEntity.badRequest().body(response);
            }
            
            response.put("error", "Failed to upload photo: " + error.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/queue-status")
    public ResponseEntity<Map<String, Object>> getQueueStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("queueSize", webhookService.getQueueSize());
        status.put("currentRequestCount", webhookService.getCurrentRequestCount());
        status.put("requestsPerMinute", webhookService.getRequestsPerMinute());
        status.put("isRateLimited", webhookService.isRateLimited());
        
        return ResponseEntity.ok(status);
    }
}
