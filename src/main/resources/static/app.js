// Set constraints for the video stream
var constraints = { video: { facingMode: "user" }, audio: false };
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
            // Update mirror effect based on camera facing mode
            updateCameraMirror();
        })
        .catch(function (error) {
            console.error("Oops. Something is broken.", error);
        });
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
    cameraSensor.width = cameraView.videoWidth;
    cameraSensor.height = cameraView.videoHeight;
    const context = cameraSensor.getContext("2d");
    
    // Only flip horizontally for front camera (user mode) to fix mirror effect
    if (constraints.video.facingMode === "user") {
        context.scale(-1, 1);
        context.drawImage(cameraView, -cameraSensor.width, 0);
        context.scale(-1, 1); // Reset the scale
    } else {
        // For back camera (environment mode), draw normally without flipping
        context.drawImage(cameraView, 0, 0);
    }
    
    cameraOutput.src = cameraSensor.toDataURL("image/webp");
    cameraOutput.classList.add("taken");
    // Show the photo in a sweet alert
    showPhoto(cameraOutput.src);
};

// Show the photo in a sweet alert
cameraOutput.onclick = function () {
    showPhoto(cameraOutput.src);
}

// Toggle between front and back camera
cameraToggle.onclick = function () {
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

// Start the video stream when the window loads
window.addEventListener("load", cameraStart, false);

//show photo in a sweet alert
function showPhoto(photoUrl) {
    Swal.fire({
        title: 'Foto tomada',
        imageUrl: photoUrl,
        imageWidth: '100%',
        imageHeight: 'auto',
        imageAlt: 'Foto tomada',
        showCloseButton: true,
        confirmButtonText: 'Aceptar',
        width: '90%',
        padding: '1rem'
    });
}