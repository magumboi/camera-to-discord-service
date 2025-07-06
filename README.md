# Camera to Discord Service

A modern web camera application built with Spring Boot and vanilla JavaScript that captures photos and automatically uploads them to Discord or any webhook service. Features an intelligent photo gallery with localStorage persistence and advanced camera controls for optimal photo quality.

## 🚀 Features

### 📸 Camera Functionality
- **Smart Motion Detection**: Prevents blurry photos by detecting camera movement
- **Tap-to-Focus**: Touch or click anywhere on the camera view to focus on specific areas
- **Anti-Blur Technology**: Automatic camera settings optimization for sharp images
- **Dual Camera Support**: Switch between front and back cameras
- **Real-time Motion Indicator**: Visual feedback when camera movement is detected

### 🖼️ Photo Gallery
- **Persistent Storage**: Photos are saved in localStorage and restored on page reload
- **Smart Storage Management**: Displays up to 6 photos, stores up to 3 persistently
- **Dynamic Space Optimization**: Automatically adjusts storage based on available space
- **Session Photos**: Supports temporary photos that exist only during the current session
- **Photo Metadata**: Each photo includes timestamp and unique ID

### 📤 Upload & Download
- **Backend Upload Service**: Photos are uploaded via a secure Java REST endpoint
- **Webhook Integration**: Backend forwards photos to configured webhook services (Discord, Slack, etc.)
- **Intelligent Queue System**: Automatic queuing when webhook is busy or rate-limited
- **Rate Limiting**: Configurable requests per minute to respect webhook limits
- **Retry Logic**: Automatic retry with exponential backoff for failed uploads
- **Asynchronous Processing**: Non-blocking uploads with background processing
- **Queue Monitoring**: Real-time queue status and metrics via API
- **Robust Error Handling**: Comprehensive error handling with automatic recovery
- **Download Support**: Download photos directly to device with formatted filenames
- **Silent Upload Option**: Background uploads without user notification

### 🎨 User Interface
- **Responsive Design**: Optimized for both desktop and mobile devices
- **Modern UI**: Clean, dark theme with Inter font family
- **Touch-Friendly**: Optimized for mobile touch interactions
- **Landscape Support**: Special layouts for landscape orientation
- **Accessible**: ARIA labels and keyboard navigation support

### ⚙️ Advanced Features
- **Configurable Webhook**: Server-side webhook configuration via application properties
- **Intelligent Upload Queue**: Automatic queuing system with rate limiting and retry logic
- **Queue Monitoring**: Real-time queue status and metrics via REST API
- **Rate Limiting**: Configurable requests per minute to respect webhook service limits
- **Retry Mechanism**: Automatic retry with exponential backoff for failed uploads
- **Storage Analytics**: Debug console commands for storage management
- **Quality Control**: High-quality JPEG compression (95% quality)
- **Browser Compatibility**: Works across modern browsers with camera API support

## 🛠️ Technologies Used

- **Backend**: Spring Boot 3.x, Java 17+
- **Frontend**: Vanilla JavaScript (ES6+), HTML5, CSS3
- **UI Library**: SweetAlert2 for modal dialogs
- **Build Tool**: Maven
- **Template Engine**: Thymeleaf

## 🏗️ Architecture

### Upload Flow
1. **Frontend**: JavaScript captures photo and creates form data
2. **Backend API**: Spring Boot REST endpoint (`/api/upload-photo`) receives and validates the photo
3. **Queue System**: Photo is queued for processing with rate limiting and retry logic
4. **Background Processing**: Dedicated service processes uploads asynchronously
5. **Webhook Service**: Java service forwards the photo to configured webhook with retry on failure
6. **Response**: Immediate response to frontend with upload status and queue information

### Components
- **WebController**: Main controller serving the UI and home page
- **PhotoUploadController**: Dedicated REST controller handling photo upload API and queue monitoring
- **WebhookService**: Service class handling webhook communication, queuing, rate limiting, and retry logic
- **Upload Queue**: Intelligent queue system with background processing and monitoring
- **Frontend**: JavaScript camera interface with localStorage photo management

### Security & Reliability
- **File Validation**: Backend validates file type and size
- **Webhook Validation**: Backend validates webhook configuration before processing
- **Rate Limiting**: Configurable requests per minute to prevent webhook overload
- **Retry Logic**: Automatic retry with exponential backoff for failed uploads
- **Queue Management**: Configurable queue size with overflow protection
- **Error Handling**: Comprehensive error handling at all levels with user-friendly messages
- **Async Processing**: Non-blocking photo upload processing with background queue

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- A modern web browser with camera access support
- HTTPS connection (required for camera access in production)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd camera-to-discord-service
   ```

2. **Configure the webhook URL and queue settings**
   
   Edit `src/main/resources/application.properties`:
   ```properties
   # Webhook configuration
   webhook.url=https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN
   
   # Queue and rate limiting configuration
   webhook.rate-limit.requests-per-minute=30
   webhook.rate-limit.max-queue-size=100
   
   # Retry configuration
   webhook.retry.max-attempts=3
   webhook.retry.delay-seconds=5
   ```
   
   Or set as environment variables:
   ```bash
   export WEBHOOK_URL=https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN
   export WEBHOOK_RATE_LIMIT_REQUESTS_PER_MINUTE=30
   export WEBHOOK_RATE_LIMIT_MAX_QUEUE_SIZE=100
   export WEBHOOK_RETRY_MAX_ATTEMPTS=3
   export WEBHOOK_RETRY_DELAY_SECONDS=5
   ```

3. **Build the application**
   ```bash
   mvn clean package
   ```

4. **Run the application**
   ```bash
   java -jar target/camera-to-discord-service-*.jar
   ```
   
   Or using Maven:
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   
   Open your browser and navigate to:
   ```
   http://localhost:8080
   ```
   
   **Note**: For camera access, you may need HTTPS. For local development, you can:
   - Use `localhost` (some browsers allow camera access on localhost)
   - Set up SSL certificates for local development
   - Use ngrok or similar tools to create HTTPS tunnels

### Configuration Options

#### Application Properties

```properties
# Webhook configuration
webhook.url=https://your-webhook-url.com

# Queue and rate limiting configuration
webhook.rate-limit.requests-per-minute=30
webhook.rate-limit.max-queue-size=100

# Retry configuration
webhook.retry.max-attempts=3
webhook.retry.delay-seconds=5

# Server configuration
server.port=8080
server.ssl.enabled=false

# Logging
logging.level.com.magumboi.webcameraapp=DEBUG
```

#### Environment Variables

- `WEBHOOK_URL`: Discord webhook URL or any other webhook service
- `WEBHOOK_RATE_LIMIT_REQUESTS_PER_MINUTE`: Maximum requests per minute (default: 30)
- `WEBHOOK_RATE_LIMIT_MAX_QUEUE_SIZE`: Maximum queue size (default: 100)
- `WEBHOOK_RETRY_MAX_ATTEMPTS`: Maximum retry attempts (default: 3)
- `WEBHOOK_RETRY_DELAY_SECONDS`: Retry delay in seconds (default: 5)
- `SERVER_PORT`: Server port (default: 8080)
- `SSL_ENABLED`: Enable SSL/HTTPS (default: false)

## 📱 Usage

### Taking Photos

1. **Allow camera access** when prompted by your browser
2. **Point the camera** at your subject
3. **Wait for stabilization** - the button will turn red if there's too much movement
4. **Tap to focus** on specific areas if needed
5. **Click "Tomar foto"** when ready
6. **Choose your action**:
   - **Upload**: Send to configured webhook
   - **Download**: Save to your device
   - **Close**: Keep in gallery only

### Managing Photos

- **View Gallery**: Click on the camera output thumbnail
- **View Individual Photos**: Click on any photo in the gallery
- **Navigate**: Use "Volver a Galería" to return to gallery view
- **Download from Gallery**: Use the download button on any photo
- **Upload from Gallery**: Use the upload button on any photo

### Upload Queue System

The application features an intelligent upload queue system that automatically handles:

- **Automatic Queuing**: When the webhook is busy or rate-limited, uploads are automatically queued
- **Rate Limiting**: Respects webhook service limits (default: 30 requests per minute)
- **Retry Logic**: Failed uploads are automatically retried with exponential backoff
- **Queue Monitoring**: Monitor queue status at `/api/queue-status`
- **Background Processing**: Uploads happen in the background without blocking the UI

**Queue Status Indicators:**
- Photos are queued when webhook is busy or rate-limited
- Queue processes uploads automatically in the background
- Failed uploads are retried up to 3 times with increasing delays
- Queue can hold up to 100 pending uploads (configurable)

### Camera Controls

- **Switch Cameras**: Click the camera toggle button (📷)
- **Focus**: Tap or click anywhere on the camera view
- **Motion Detection**: Watch the capture button for movement warnings

### Storage Management

The application includes several debug commands available in the browser console:

```javascript
// View storage information
showStorageInfo()

// Clear all stored photos
clearGalleryFromStorage()

// Optimize storage usage
optimizeStorageSize()

// Add a session-only photo (for testing)
addSessionPhoto(dataUrl)

// Calculate optimal storage size
calculateOptimalPersistentSize()
```

## 🔧 Development

### Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/magumboi/webcameraapp/
│   │       ├── WebCameraAppApplication.java
│   │       ├── controller/
│   │       │   ├── WebController.java              # Main page controller
│   │       │   └── PhotoUploadController.java      # Upload API & queue monitoring
│   │       └── service/
│   │           └── WebhookService.java              # Queue, rate limit & webhook logic
│   └── resources/
│       ├── application.properties                   # Configuration including queue settings
│       ├── static/
│       │   ├── app.js                              # Main application logic
│       │   └── style.css                           # Responsive styles
│       └── templates/
│           └── index.html                          # Main HTML template
├── test/
│   └── java/
│       └── com/magumboi/webcameraapp/
│           ├── controller/
│           │   ├── WebControllerTest.java
│           │   └── PhotoUploadControllerTest.java
│           ├── service/
│           │   └── WebhookServiceTest.java
│           └── integration/
│               └── WebControllerIntegrationTest.java
└── pom.xml                                         # Maven configuration
```

### Key Components

- **WebController.java**: Main Spring Boot controller that serves the application
- **PhotoUploadController.java**: REST API controller handling photo uploads and queue monitoring
- **WebhookService.java**: Core service with intelligent queue system, rate limiting, and retry logic
- **app.js**: Core JavaScript with camera controls, photo capture, gallery management, and storage logic
- **style.css**: Responsive CSS with mobile-first design and dark theme
- **index.html**: Thymeleaf template with camera HTML structure

### Building for Production

1. **Enable HTTPS** in `application.properties`:
   ```properties
   server.ssl.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=your-password
   server.ssl.key-store-type=PKCS12
   ```

2. **Set production webhook URL**:
   ```bash
   export WEBHOOK_URL=https://your-production-webhook.com
   ```

3. **Build with production profile**:
   ```bash
   mvn clean package -Pprod
   ```

## 🐛 Troubleshooting

### Camera Access Issues

- **Permission Denied**: Ensure camera permissions are granted in browser settings
- **HTTPS Required**: Most browsers require HTTPS for camera access in production
- **Browser Compatibility**: Use modern browsers (Chrome 53+, Firefox 36+, Safari 11+)

### Storage Issues

- **QuotaExceededError**: The app automatically reduces stored photos when storage is full
- **Data Corruption**: Use `clearGalleryFromStorage()` to reset if localStorage is corrupted
- **Performance**: Use `optimizeStorageSize()` to improve storage efficiency

### Upload Issues

- **Webhook Not Configured**: Ensure `webhook.url` is set in application properties
- **Queue Full**: If queue is full, try again later or increase `webhook.rate-limit.max-queue-size`
- **Rate Limited**: Uploads are automatically queued when rate limit is exceeded
- **Network Errors**: Check webhook URL validity and network connectivity
- **File Size Limits**: Large photos may exceed webhook size limits (Discord: 8MB)
- **Retry Failures**: Check logs for detailed error information if uploads fail after all retries

## � API Documentation

### Upload Photo Endpoint

```
POST /api/upload-photo
Content-Type: multipart/form-data
```

**Parameters:**
- `file`: MultipartFile (required) - The image file to upload

**Responses:**

Success (200 OK):
```json
{
  "message": "Photo uploaded successfully"
}
```

Error (400 Bad Request):
```json
{
  "error": "No file provided"
}
```

Error (400 Bad Request):
```json
{
  "error": "File must be an image"
}
```

Error (400 Bad Request):
```json
{
  "error": "Webhook no configurado. La URL del webhook no está configurada en el servidor."
}
```

Error (500 Internal Server Error):
```json
{
  "error": "Upload queue is full. Please try again later."
}
```

Error (500 Internal Server Error):
```json
{
  "error": "Failed to upload photo: [error details]"
}
```

**Example using curl:**
```bash
curl -X POST \
  -F "file=@photo.jpg" \
  http://localhost:8080/api/upload-photo
```

### Queue Status Endpoint

```
GET /api/queue-status
```

**Response (200 OK):**
```json
{
  "queueSize": 5,
  "currentRequestCount": 12,
  "requestsPerMinute": 30,
  "isRateLimited": false
}
```

**Response Fields:**
- `queueSize`: Current number of photos in the upload queue
- `currentRequestCount`: Number of requests made in the current minute
- `requestsPerMinute`: Maximum requests allowed per minute
- `isRateLimited`: Whether the service is currently rate-limited

**Example using curl:**
```bash
curl -X GET http://localhost:8080/api/queue-status
```

### Queue Monitoring

You can monitor the upload queue in real-time:

```bash
# Monitor queue status every 5 seconds
watch -n 5 curl -s http://localhost:8080/api/queue-status | jq
```

The queue status provides valuable insights for monitoring:
- **Queue Size**: Number of pending uploads
- **Request Count**: Current rate limit usage
- **Rate Limited**: Whether new uploads are being delayed
- **Capacity**: How much room is left in the queue

## �📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For support, please open an issue in the GitHub repository or contact the development team.

---

Made with ❤️ for seamless photo sharing and camera control.
