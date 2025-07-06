// Set constraints for the video stream
var constraints = { 
    video: { 
        facingMode: "user",
        focusMode: "continuous",
        width: { ideal: 1920 },
        height: { ideal: 1080 },
        frameRate: { ideal: 30, min: 24 },
        // Anti-blur settings
        exposureMode: "continuous",
        whiteBalanceMode: "continuous",
        imageStabilization: true
    }, 
    audio: false 
};
var track = null;

// Define constants
const cameraView = document.querySelector("#camera--view"),
    cameraOutput = document.querySelector("#camera--output"),
    cameraSensor = document.querySelector("#camera--sensor"),
    cameraTrigger = document.querySelector("#camera--trigger"),
    cameraToggle = document.querySelector("#camera--toggle");

// Access the device camera and stream to cameraView
function cameraStart() {
    navigator.mediaDevices
        .getUserMedia(constraints)
        .then(function (stream) {
            track = stream.getTracks()[0];
            cameraView.srcObject = stream;
            
            // Configure camera settings for better image quality
            if (track.getCapabilities) {
                const capabilities = track.getCapabilities();
                console.log('Camera capabilities:', capabilities);
                
                // Apply anti-blur settings
                applyAntiBlurSettings(capabilities);
            }
            
            // Update mirror effect based on camera facing mode
            updateCameraMirror();
            
            // Start motion detection after camera is ready
            cameraView.addEventListener('loadedmetadata', () => {
                setTimeout(() => {
                    startMotionDetection();
                }, 1000);
            });
        })
        .catch(function (error) {
            console.error("Oops. Something is broken.", error);
        });
}

// Apply settings to reduce motion blur
function applyAntiBlurSettings(capabilities) {
    const settings = {};
    
    // Set higher shutter speed if available
    if (capabilities.exposureTime) {
        settings.exposureTime = capabilities.exposureTime.min || 1/60; // Fast shutter speed
    }
    
    // Set lower ISO if available to reduce noise
    if (capabilities.iso) {
        settings.iso = capabilities.iso.min || 100;
    }
    
    // Enable image stabilization if available
    if (capabilities.imageStabilization) {
        settings.imageStabilization = true;
    }
    
    // Set focus mode for sharp images
    if (capabilities.focusMode && capabilities.focusMode.includes('single-shot')) {
        settings.focusMode = 'single-shot';
    }
    
    // Apply the settings
    if (Object.keys(settings).length > 0) {
        track.applyConstraints({
            advanced: [settings]
        }).then(() => {
            console.log('Anti-blur settings applied successfully');
        }).catch((error) => {
            console.warn('Could not apply anti-blur settings:', error);
        });
    }
}

// Update mirror effect for video preview based on camera facing mode
function updateCameraMirror() {
    if (constraints.video.facingMode === "user") {
        // Mirror effect for front camera
        cameraView.style.transform = "scaleX(-1)";
    } else {
        // No mirror effect for back camera
        cameraView.style.transform = "scaleX(1)";
    }
}

// Take a picture when cameraTrigger is tapped
cameraTrigger.onclick = function () {
    // Check if there's too much movement using average motion
    if (motionHistory.length >= 4) { // Need fewer frames for faster response (was 5)
        const avgMotion = motionHistory.reduce((sum, val) => sum + val, 0) / motionHistory.length;
        
        // Use a lower threshold for blocking photos (more sensitive)
        const photoBlockThreshold = motionThreshold * 2.0; // 200% higher threshold (was 250%)
        
        if (avgMotion > photoBlockThreshold) {
            // Show warning for excessive movement
            Swal.fire({
                title: 'Mucho movimiento',
                text: 'Mantén la cámara estable para evitar fotos borrosas',
                icon: 'warning',
                timer: 2000,
                showConfirmButton: false
            });
            return;
        }
    }
    
    // Add a slightly longer delay for better stabilization
    setTimeout(() => {
        capturePhoto();
    }, 200); // Increased delay for better stability
};

// Capture photo with anti-blur techniques
function capturePhoto() {
    // Ensure video is ready and has stable dimensions
    if (!cameraView.videoWidth || !cameraView.videoHeight) {
        console.warn('Video not ready for capture');
        return;
    }
    
    // Set canvas size to match video dimensions exactly
    cameraSensor.width = cameraView.videoWidth;
    cameraSensor.height = cameraView.videoHeight;
    const context = cameraSensor.getContext("2d");
    
    // Configure context for high quality rendering
    context.imageSmoothingEnabled = true;
    context.imageSmoothingQuality = 'high';
    
    // Apply focus before capture if supported
    if (track && track.getCapabilities) {
        const capabilities = track.getCapabilities();
        if (capabilities.focusMode && capabilities.focusMode.includes('single-shot')) {
            track.applyConstraints({
                advanced: [{ focusMode: 'single-shot' }]
            }).then(() => {
                // Wait for focus to complete before capturing
                setTimeout(() => {
                    performCapture(context);
                }, 200);
            }).catch(() => {
                // If focus fails, capture anyway
                performCapture(context);
            });
        } else {
            performCapture(context);
        }
    } else {
        performCapture(context);
    }
}

// Perform the actual photo capture
function performCapture(context) {
    // Only flip horizontally for front camera (user mode) to fix mirror effect
    if (constraints.video.facingMode === "user") {
        context.scale(-1, 1);
        context.drawImage(cameraView, -cameraSensor.width, 0);
        context.scale(-1, 1); // Reset the scale
    } else {
        // For back camera (environment mode), draw normally without flipping
        context.drawImage(cameraView, 0, 0);
    }
    
    // Convert to high quality image
    cameraOutput.src = cameraSensor.toDataURL("image/jpeg", 0.95); // High quality JPEG
    cameraOutput.classList.add("taken");
    
    // Show the photo in a sweet alert
    showPhoto(cameraOutput.src, true); // Auto-upload on close since this is a new photo
}

// Photo click handled by the double-tap listener above

// Toggle between front and back camera
cameraToggle.onclick = function () {
    // Stop motion detection temporarily and reset history
    isMotionDetectionActive = false;
    lastFrameData = null;
    motionHistory = []; // Clear motion history
    
    if (constraints.video.facingMode === "user") {
        constraints.video.facingMode = "environment"; // Switch to back camera
    } else {
        constraints.video.facingMode = "user"; // Switch to front camera
    }
    // Restart the camera with the new constraints
    if (track) {
        track.stop(); // Stop the current track
    }
    cameraStart(); // Start the camera with the new facing mode
};

// Focus the camera at a specific point
function focusCamera(x, y) {
    if (track && track.getCapabilities) {
        const capabilities = track.getCapabilities();
        console.log('Camera capabilities:', capabilities);
        
        // Try different focus approaches
        let focusAttempts = [];
        
        // Method 1: Try focus with coordinates
        if (capabilities.focusMode && capabilities.focusMode.includes('manual')) {
            const constraintsWithCoords = {
                advanced: [{
                    focusMode: 'manual',
                    pointsOfInterest: [{ x: x, y: y }]
                }]
            };
            focusAttempts.push(constraintsWithCoords);
        }
        
        // Method 2: Try single-shot focus
        if (capabilities.focusMode && capabilities.focusMode.includes('single-shot')) {
            const constraintsSingleShot = {
                advanced: [{
                    focusMode: 'single-shot'
                }]
            };
            focusAttempts.push(constraintsSingleShot);
        }
        
        // Method 3: Try continuous focus as fallback
        if (capabilities.focusMode && capabilities.focusMode.includes('continuous')) {
            const constraintsContinuous = {
                advanced: [{
                    focusMode: 'continuous'
                }]
            };
            focusAttempts.push(constraintsContinuous);
        }
        
        // Try each method in sequence
        async function tryFocusMethods() {
            for (let i = 0; i < focusAttempts.length; i++) {
                try {
                    await track.applyConstraints(focusAttempts[i]);
                    console.log(`Focus method ${i + 1} succeeded`);
                    return;
                } catch (error) {
                    console.warn(`Focus method ${i + 1} failed:`, error);
                }
            }
            console.warn('All focus methods failed');
        }
        
        tryFocusMethods();
    } else {
        console.warn('Focus not supported on this device');
    }
}

// Add tap-to-focus functionality
cameraView.addEventListener('click', function(event) {
    event.preventDefault();
    
    const rect = cameraView.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    
    // Convert to normalized coordinates (0-1)
    const normalizedX = Math.max(0, Math.min(1, x / rect.width));
    const normalizedY = Math.max(0, Math.min(1, y / rect.height));
    
    console.log('Tap to focus at:', { x: normalizedX, y: normalizedY });
    focusCamera(normalizedX, normalizedY);
});

// Also add touch event for mobile devices
cameraView.addEventListener('touchstart', function(event) {
    event.preventDefault();
    
    if (event.touches.length === 1) {
        const touch = event.touches[0];
        const rect = cameraView.getBoundingClientRect();
        const x = touch.clientX - rect.left;
        const y = touch.clientY - rect.top;
        
        // Convert to normalized coordinates (0-1)
        const normalizedX = Math.max(0, Math.min(1, x / rect.width));
        const normalizedY = Math.max(0, Math.min(1, y / rect.height));
        
        console.log('Touch to focus at:', { x: normalizedX, y: normalizedY });
        focusCamera(normalizedX, normalizedY);
    }
});

// Motion detection variables
let lastFrameData = null;
let motionThreshold = 80; // More sensitive (was 120)
let isMotionDetectionActive = false;
let motionHistory = []; // Track motion over time for better stability
let maxMotionHistory = 6; // Fewer frames for faster response (was 8)

// Start motion detection when camera starts
function startMotionDetection() {
    if (!isMotionDetectionActive) {
        isMotionDetectionActive = true;
        detectMotion();
    }
}

// Detect motion in the video stream
function detectMotion() {
    if (!cameraView.videoWidth || !cameraView.videoHeight) {
        setTimeout(detectMotion, 150);
        return;
    }
    
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    canvas.width = 100; // Slightly higher resolution for more sensitivity (was 80)
    canvas.height = 75;
    
    // Draw current frame
    context.drawImage(cameraView, 0, 0, canvas.width, canvas.height);
    const currentFrameData = context.getImageData(0, 0, canvas.width, canvas.height);
    
    if (lastFrameData) {
        const motionLevel = calculateMotionLevel(lastFrameData, currentFrameData);
        
        // Add to motion history
        motionHistory.push(motionLevel);
        if (motionHistory.length > maxMotionHistory) {
            motionHistory.shift(); // Remove oldest entry
        }
        
        // Calculate average motion over recent frames for better stability
        const avgMotion = motionHistory.reduce((sum, val) => sum + val, 0) / motionHistory.length;
        
        // Update UI based on average motion level
        updateMotionIndicator(avgMotion);
    }
    
    lastFrameData = currentFrameData;
    
    // Continue motion detection
    if (isMotionDetectionActive) {
        setTimeout(detectMotion, 200); // Faster check for more sensitivity (was 250)
    }
}

// Calculate motion level between two frames
function calculateMotionLevel(frame1, frame2) {
    let totalDiff = 0;
    const data1 = frame1.data;
    const data2 = frame2.data;
    let pixelCount = 0;
    
    // Sample every 3rd pixel for better sensitivity (was every 4th)
    for (let i = 0; i < data1.length; i += 12) { // Less skipping for more sensitivity (was 16)
        const diff = Math.abs(data1[i] - data2[i]) + 
                    Math.abs(data1[i + 1] - data2[i + 1]) + 
                    Math.abs(data1[i + 2] - data2[i + 2]);
        
        // Lower threshold for counting differences
        if (diff > 20) { // More sensitive (was 30)
            totalDiff += diff;
        }
        pixelCount++;
    }
    
    return pixelCount > 0 ? totalDiff / pixelCount : 0;
}

// Update motion indicator
function updateMotionIndicator(motionLevel) {
    // Use a lower threshold for the visual indicator for more sensitivity
    const visualThreshold = motionThreshold * 1.1; // Only 10% higher than base threshold (was 1.3)
    const isMoving = motionLevel > visualThreshold;
    
    if (isMoving) {
        cameraTrigger.style.backgroundColor = '#ff8888'; // Even softer red color
        cameraTrigger.textContent = 'Estabilizando...';
    } else {
        cameraTrigger.style.backgroundColor = 'black';
        cameraTrigger.textContent = 'Tomar foto';
    }
    
    // Reduced logging frequency for less console spam
    if (Math.random() < 0.1) { // Only log 10% of the time
        console.log('Motion level:', motionLevel.toFixed(2), 'Visual threshold:', visualThreshold.toFixed(2));
    }
}

// Start the video stream when the window loads
window.addEventListener("load", cameraStart, false);

//show photo in a sweet alert
function showPhoto(photoUrl, autoUploadOnClose = false) {
    Swal.fire({
        title: 'Foto tomada',
        imageUrl: photoUrl,
        imageWidth: 'auto',
        imageHeight: 'auto',
        imageAlt: 'Foto tomada',
        showCloseButton: true,
        confirmButtonText: 'Subir',
        showCancelButton: true,
        cancelButtonText: 'Cerrar',
        width: '90vw', // 90% of viewport width
        heightAuto: false, // Prevent auto height
        background: 'rgba(0, 0, 0, 0.9)', // Dark semi-transparent background
        color: '#ffffff', // White text
        customClass: {
            popup: 'swal-responsive-popup',
            image: 'swal-responsive-image',
            title: 'swal-title-white',
            actions: 'swal-actions-inline',
            confirmButton: 'swal-confirm-button',
            cancelButton: 'swal-cancel-button'
        },
        didOpen: () => {
            // Ensure image fits within viewport
            const image = document.querySelector('.swal2-image');
            if (image) {
                image.style.maxWidth = '100%';
                image.style.maxHeight = '70vh'; // Max 70% of viewport height
                image.style.objectFit = 'contain';
            }
            
            // Adjust layout for landscape orientation
            const popup = document.querySelector('.swal2-popup');
            if (window.innerWidth > window.innerHeight) {
                popup.classList.add('swal-landscape');
            }
        }
    }).then((result) => {
        if (result.isConfirmed) {
            // User clicked "Subir" - always show success/error messages
            uploadPhoto(photoUrl, true);
        } else if (autoUploadOnClose && (result.dismiss === Swal.DismissReason.close || result.dismiss === Swal.DismissReason.cancel)) {
            // Only upload silently if this was called after taking a new photo
            uploadPhoto(photoUrl, false);
        }
        // If opened from cameraOutput click, don't upload on close/cancel
    });
}

// Show photo in sweet alert when clicked
cameraOutput.addEventListener('click', function() {
    if (cameraOutput.classList.contains("taken")) {
        showPhoto(cameraOutput.src, false); // Don't auto-upload on close since this is just viewing
    }
});

// Function to upload photo to webhook
async function uploadPhoto(imageDataUrl, showMessages = true) {
    // Get webhook URL from Thymeleaf variable
    const webhookUrl = window.webhookUrl;
    
    if (!webhookUrl) {
        if (showMessages) {
            Swal.fire({
                title: 'Webhook no configurado',
                text: 'La URL del webhook no está configurada en el servidor.',
                icon: 'error',
                confirmButtonText: 'Entendido',
                background: 'rgba(0, 0, 0, 0.9)',
                color: '#ffffff',
                customClass: {
                    popup: 'swal-responsive-popup',
                    title: 'swal-title-white',
                    confirmButton: 'swal-confirm-button'
                }
            });
        }
        return;
    }

    try {
        // Show loading indicator only if messages are enabled
        if (showMessages) {
            Swal.fire({
                title: 'Subiendo foto...',
                text: 'Por favor espera mientras se sube la foto',
                allowOutsideClick: false,
                showConfirmButton: false,
                background: 'rgba(0, 0, 0, 0.9)',
                color: '#ffffff',
                customClass: {
                    popup: 'swal-responsive-popup',
                    title: 'swal-title-white'
                },
                didOpen: () => {
                    Swal.showLoading();
                }
            });
        }

        // Convert data URL to blob
        const response = await fetch(imageDataUrl);
        const blob = await response.blob();
        
        // Create form data
        const formData = new FormData();
        const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
        const filename = `camera-photo-${timestamp}.jpg`;
        
        formData.append('file', blob, filename);
        formData.append('content', `📸 Nueva foto tomada - ${new Date().toLocaleString()}`);
        
        // Send to webhook
        const webhookResponse = await fetch(webhookUrl, {
            method: 'POST',
            body: formData
        });
        
        if (webhookResponse.ok) {
            // Success - only show message if enabled
            if (showMessages) {
                Swal.fire({
                    title: '¡Foto subida!',
                    text: 'La foto se ha subido exitosamente',
                    icon: 'success',
                    timer: 2000,
                    showConfirmButton: false,
                    background: 'rgba(0, 0, 0, 0.9)',
                    color: '#ffffff',
                    customClass: {
                        popup: 'swal-responsive-popup',
                        title: 'swal-title-white'
                    }
                });
            }
        } else {
            throw new Error(`Webhook API error: ${webhookResponse.status}`);
        }
    } catch (error) {
        console.error('Error uploading photo:', error);
        
        // Show error message only if enabled
        if (showMessages) {
            Swal.fire({
                title: 'Error al subir',
                text: 'No se pudo subir la foto. Verifica la configuración del servidor.',
                icon: 'error',
                confirmButtonText: 'Entendido',
                background: 'rgba(0, 0, 0, 0.9)',
                color: '#ffffff',
                customClass: {
                    popup: 'swal-responsive-popup',
                    title: 'swal-title-white',
                    confirmButton: 'swal-confirm-button'
                }
            });
        }
    }
}