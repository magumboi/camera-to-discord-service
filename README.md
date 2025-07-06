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
- **Automatic Webhook Upload**: Configurable webhook URL for automatic photo uploads
- **Manual Upload Control**: Users can choose when to upload photos
- **Download Support**: Download photos directly to device with formatted filenames
- **Silent Upload Option**: Background uploads without user notification
- **Error Handling**: Comprehensive error handling with user-friendly messages

### 🎨 User Interface
- **Responsive Design**: Optimized for both desktop and mobile devices
- **Modern UI**: Clean, dark theme with Inter font family
- **Touch-Friendly**: Optimized for mobile touch interactions
- **Landscape Support**: Special layouts for landscape orientation
- **Accessible**: ARIA labels and keyboard navigation support

### ⚙️ Advanced Features
- **Configurable Webhook**: Server-side webhook configuration via application properties
- **Storage Analytics**: Debug console commands for storage management
- **Quality Control**: High-quality JPEG compression (95% quality)
- **Browser Compatibility**: Works across modern browsers with camera API support

## 🛠️ Technologies Used

- **Backend**: Spring Boot 3.x, Java 17+
- **Frontend**: Vanilla JavaScript (ES6+), HTML5, CSS3
- **UI Library**: SweetAlert2 for modal dialogs
- **Build Tool**: Maven
- **Template Engine**: Thymeleaf

## 🚀 Getting Started

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

2. **Configure the webhook URL**
   
   Edit `src/main/resources/application.properties`:
   ```properties
   webhook.url=https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN
   ```
   
   Or set as environment variable:
   ```bash
   export WEBHOOK_URL=https://discord.com/api/webhooks/YOUR_WEBHOOK_ID/YOUR_WEBHOOK_TOKEN
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

# Server configuration
server.port=8080
server.ssl.enabled=false

# Logging
logging.level.com.magumboi.webcameraapp=DEBUG
```

#### Environment Variables

- `WEBHOOK_URL`: Discord webhook URL or any other webhook service
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
│   │       └── controller/
│   │           └── WebController.java
│   └── resources/
│       ├── application.properties
│       ├── static/
│       │   ├── app.js          # Main application logic
│       │   └── style.css       # Responsive styles
│       └── templates/
│           └── index.html      # Main HTML template
└── pom.xml                     # Maven configuration
```

### Key Components

- **WebController.java**: Main Spring Boot controller that serves the application and provides webhook configuration
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
- **Network Errors**: Check webhook URL validity and network connectivity
- **File Size Limits**: Large photos may exceed webhook size limits (Discord: 8MB)

## 📄 License

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
