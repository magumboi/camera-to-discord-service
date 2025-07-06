package com.magumboi.webcameraapp.controller;

import com.magumboi.webcameraapp.service.WebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PhotoUploadController.class)
class PhotoUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebhookService webhookService;

    @Test
    void testUploadPhoto_Success() throws Exception {
        // Mock the webhook service
        when(webhookService.uploadPhotoToWebhook(any()))
                .thenReturn(Mono.just("Upload successful"));

        // Create a mock image file
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test-image.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "fake image content".getBytes()
        );

        // Perform the request
        mockMvc.perform(multipart("/api/upload-photo")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Photo uploaded successfully"));
    }

    @Test
    void testUploadPhoto_EmptyFile() throws Exception {
        // Create an empty file
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "empty.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                new byte[0]
        );

        // Perform the request
        mockMvc.perform(multipart("/api/upload-photo")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No file provided"));
    }

    @Test
    void testUploadPhoto_InvalidFileType() throws Exception {
        // Create a non-image file
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test.txt", 
                MediaType.TEXT_PLAIN_VALUE, 
                "not an image".getBytes()
        );

        // Perform the request
        mockMvc.perform(multipart("/api/upload-photo")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("File must be an image"));
    }

    @Test
    void testUploadPhoto_WebhookNotConfigured() throws Exception {
        // Mock the webhook service to throw webhook configuration error
        when(webhookService.uploadPhotoToWebhook(any()))
                .thenReturn(Mono.error(new IllegalStateException("Webhook URL is not configured")));

        // Create a mock image file
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test-image.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "fake image content".getBytes()
        );

        // Perform the request
        mockMvc.perform(multipart("/api/upload-photo")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Webhook no configurado. La URL del webhook no está configurada en el servidor."));
    }

    @Test
    void testUploadPhoto_NoFileParameter() throws Exception {
        // Perform request without file parameter
        mockMvc.perform(multipart("/api/upload-photo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No file provided"));
    }

    @Test
    void testUploadPhoto_LargeFile() throws Exception {
        // Mock successful upload
        when(webhookService.uploadPhotoToWebhook(any()))
                .thenReturn(Mono.just("Upload successful"));

        // Create a large mock image file (simulate large photo)
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "large-image.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                largeContent
        );

        // Perform the request
        mockMvc.perform(multipart("/api/upload-photo")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Photo uploaded successfully"));
    }

    @Test
    void testUploadPhoto_WebhookError() throws Exception {
        // Mock the webhook service to throw a network error
        when(webhookService.uploadPhotoToWebhook(any()))
                .thenReturn(Mono.error(new RuntimeException("Network connection failed")));

        // Create a mock image file
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "test-image.jpg", 
                MediaType.IMAGE_JPEG_VALUE, 
                "fake image content".getBytes()
        );

        // Perform the request
        mockMvc.perform(multipart("/api/upload-photo")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Failed to upload photo: Network connection failed"));
    }
}
