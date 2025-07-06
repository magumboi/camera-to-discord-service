package com.magumboi.webcameraapp.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "webhook.url=http://localhost:9999/webhook" // Mock webhook URL
})
class WebControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testMainPageLoads() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("camera--view");
        assertThat(response.getBody()).contains("camera--trigger");
    }

    @Test
    void testUploadPhotoEndpoint_NoFile() {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/upload-photo",
            HttpMethod.POST,
            requestEntity,
            String.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("No file provided");
    }

    @Test
    void testUploadPhotoEndpoint_ConnectionError() {
        // This test verifies the webhook connection fails (since localhost:9999 is not running)
        byte[] imageContent = "fake image content".getBytes();
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(imageContent) {
            @Override
            public String getFilename() {
                return "test.jpg";
            }
        };
        
        body.add("file", resource);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/upload-photo",
            HttpMethod.POST,
            requestEntity,
            String.class
        );
        
        // The mock webhook URL is configured but not reachable, so we expect a connection error
        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
        assertThat(response.getBody()).contains("Failed to upload photo");
    }
}

// Test class for webhook not configured scenario
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "webhook.url=" // Empty webhook URL
})
class WebhookNotConfiguredIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testUploadPhotoEndpoint_WebhookNotConfigured() {
        // Create a simple test image file with proper headers
        byte[] imageContent = "fake image content".getBytes();
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        org.springframework.core.io.ByteArrayResource resource = new org.springframework.core.io.ByteArrayResource(imageContent) {
            @Override
            public String getFilename() {
                return "test.jpg";
            }
        };
        
        body.add("file", resource);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = 
            new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "http://localhost:" + port + "/api/upload-photo",
            HttpMethod.POST,
            requestEntity,
            String.class
        );
        
        // Should return 400 with webhook configuration error
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Webhook no configurado");
    }
}
