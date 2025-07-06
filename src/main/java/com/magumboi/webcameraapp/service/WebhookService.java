package com.magumboi.webcameraapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    @Value("${webhook.url:}")
    private String webhookUrl;

    @Value("${webhook.rate-limit.requests-per-minute:30}")
    private int requestsPerMinute;

    @Value("${webhook.rate-limit.max-queue-size:100}")
    private int maxQueueSize;

    @Value("${webhook.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${webhook.retry.delay-seconds:5}")
    private int retryDelaySeconds;

    private final WebClient webClient;
    private final BlockingQueue<UploadTask> uploadQueue;
    private final ScheduledExecutorService scheduler;
    private final AtomicLong requestCount;
    private volatile long windowStart;

    // Inner class for upload tasks
    private static class UploadTask {
        private final String filename;
        private final byte[] photoData;
        private final String content;
        private final CompletableFuture<String> future;
        private int attempts;

        public UploadTask(String filename, byte[] photoData, String content) {
            this.filename = filename;
            this.photoData = photoData;
            this.content = content;
            this.future = new CompletableFuture<>();
            this.attempts = 0;
        }

        public String getFilename() { return filename; }
        public byte[] getPhotoData() { return photoData; }
        public String getContent() { return content; }
        public CompletableFuture<String> getFuture() { return future; }
        public int getAttempts() { return attempts; }
        public void incrementAttempts() { attempts++; }
    }

    public WebhookService() {
        this.webClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(25 * 1024 * 1024)) // 25MB limit
                .build();
        this.uploadQueue = new LinkedBlockingQueue<>();
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.requestCount = new AtomicLong(0);
        this.windowStart = System.currentTimeMillis();
    }

    @PostConstruct
    public void startQueueProcessor() {
        // Start the queue processor
        scheduler.scheduleWithFixedDelay(this::processQueue, 0, 1, TimeUnit.SECONDS);
        
        // Start rate limit window reset
        scheduler.scheduleWithFixedDelay(this::resetRateLimit, 60, 60, TimeUnit.SECONDS);
        
        logger.info("Webhook service started with rate limit: {} requests/minute, max queue size: {}", 
                   requestsPerMinute, maxQueueSize);
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Webhook service shutdown completed");
    }

    public Mono<String> uploadPhotoToWebhook(MultipartFile photo) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            return Mono.error(new IllegalStateException("Webhook URL is not configured"));
        }

        try {
            // Generate timestamp for filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "camera-photo-" + timestamp + ".jpg";
            
            // Add content message
            String content = "📸 Nueva foto tomada - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            
            // Create upload task
            UploadTask task = new UploadTask(filename, photo.getBytes(), content);
            
            // Check queue capacity
            if (uploadQueue.size() >= maxQueueSize) {
                return Mono.error(new RuntimeException("Upload queue is full. Please try again later."));
            }
            
            // Add to queue
            uploadQueue.offer(task);
            logger.info("Photo upload queued. Queue size: {}", uploadQueue.size());
            
            // Return future as Mono
            return Mono.fromFuture(task.getFuture());
            
        } catch (IOException e) {
            return Mono.error(new RuntimeException("Failed to read photo data: " + e.getMessage(), e));
        }
    }

    private void processQueue() {
        try {
            // Check if we can make a request (rate limiting)
            if (!canMakeRequest()) {
                logger.debug("Rate limit reached, waiting...");
                return;
            }
            
            // Get next task from queue
            UploadTask task = uploadQueue.poll();
            if (task == null) {
                return; // No tasks to process
            }
            
            // Process the upload
            processUploadTask(task);
            
        } catch (Exception e) {
            logger.error("Error processing upload queue", e);
        }
    }

    private boolean canMakeRequest() {
        long now = System.currentTimeMillis();
        
        // Reset window if needed
        if (now - windowStart >= 60000) { // 1 minute window
            resetRateLimit();
        }
        
        return requestCount.get() < requestsPerMinute;
    }

    private void resetRateLimit() {
        requestCount.set(0);
        windowStart = System.currentTimeMillis();
        logger.debug("Rate limit window reset");
    }

    private void processUploadTask(UploadTask task) {
        task.incrementAttempts();
        requestCount.incrementAndGet();
        
        logger.info("Processing upload task: {} (attempt {}/{})", 
                   task.getFilename(), task.getAttempts(), maxRetryAttempts);
        
        // Create multipart body
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new ByteArrayResource(task.getPhotoData()) {
            @Override
            public String getFilename() {
                return task.getFilename();
            }
        }, MediaType.IMAGE_JPEG);
        builder.part("content", task.getContent());

        // Make the request
        webClient.post()
                .uri(webhookUrl)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                        .filter(this::isRetryableError))
                .subscribe(
                    result -> {
                        logger.info("Upload successful for {}", task.getFilename());
                        task.getFuture().complete(result);
                    },
                    error -> handleUploadError(task, error)
                );
    }

    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            HttpStatusCode statusCode = ex.getStatusCode();
            
            // Retry on rate limit, server errors, and timeout
            return statusCode.value() == 429 || // TOO_MANY_REQUESTS
                   statusCode.is5xxServerError() ||
                   statusCode.value() == 408; // REQUEST_TIMEOUT
        }
        
        // Retry on network errors
        return throwable instanceof IOException ||
               throwable.getMessage().contains("Connection") ||
               throwable.getMessage().contains("timeout");
    }

    private void handleUploadError(UploadTask task, Throwable error) {
        logger.warn("Upload failed for {} (attempt {}/{}): {}", 
                   task.getFilename(), task.getAttempts(), maxRetryAttempts, error.getMessage());
        
        // Check if we should retry
        if (task.getAttempts() < maxRetryAttempts && isRetryableError(error)) {
            // Schedule retry
            scheduler.schedule(() -> {
                uploadQueue.offer(task); // Re-queue the task
                logger.info("Re-queued {} for retry", task.getFilename());
            }, retryDelaySeconds, TimeUnit.SECONDS);
        } else {
            // Max attempts reached or non-retryable error
            String errorMessage = String.format("Failed to upload photo after %d attempts: %s", 
                                               task.getAttempts(), error.getMessage());
            task.getFuture().completeExceptionally(new RuntimeException(errorMessage, error));
        }
    }

    // Utility methods for monitoring
    public int getQueueSize() {
        return uploadQueue.size();
    }

    public long getCurrentRequestCount() {
        return requestCount.get();
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public boolean isRateLimited() {
        return !canMakeRequest();
    }
}
