package com.example.machinelearningapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.machinelearningapp.databinding.ActivityFaceDetectionBinding
import android.graphics.Bitmap
import android.view.View
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.ImageProxy
import com.example.machinelearningapp.helper.EmotionDetector
import com.example.machinelearningapp.helper.PerformanceMonitor
import com.example.machinelearningapp.helper.TensorFlowFaceDetector

class FaceDetectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var faceDetector: FaceDetector
    private lateinit var tensorFlowFaceDetector: TensorFlowFaceDetector
    private lateinit var emotionDetector: EmotionDetector
    private lateinit var performanceMonitor: PerformanceMonitor

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var currentMethod = "ML Kit" // Default method
    private var showLandmarks = true
    private var showPerformance = false

    companion object {
        private const val TAG = "FaceDetection"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        const val EXTRA_DETECTION_METHOD = "detection_method"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get detection method from intent
        currentMethod = intent.getStringExtra(EXTRA_DETECTION_METHOD) ?: "ML Kit"

        // Initialize detectors
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .build()

        faceDetector = FaceDetection.getClient(options)
        tensorFlowFaceDetector = TensorFlowFaceDetector(this)
        
        // Test TensorFlow model
        testTensorFlowDetection()
        
        emotionDetector = EmotionDetector()
        performanceMonitor = PerformanceMonitor()

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Update UI based on method
        updateMethodUI()

        // Set up button listeners
        setupButtonListeners()

        // Start camera if permissions granted
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun updateMethodUI() {
        binding.methodTitle.text = "$currentMethod Face Detection"
        binding.methodInfo.text = when (currentMethod) {
            "ML Kit" -> "Using Google's ML Kit for face detection with advanced features like emotion detection and landmarks."
            "TensorFlow" -> "Using TensorFlow Lite for face detection with custom model support."
            else -> "Unknown detection method"
        }
        
        // Clear any existing detections when switching methods
        binding.faceInfoText.text = "Initializing $currentMethod detection..."
        binding.faceLandmarkView.setTensorFlowDetections(emptyList(), 0, 0)
        binding.faceLandmarkView.setFaces(emptyList(), 0, 0)
        
        // Force UI refresh
        forceUIRefresh()
    }

    private fun setupButtonListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            finish()
        }

        // Landmarks toggle
        binding.landmarksButton.setOnClickListener {
            showLandmarks = !showLandmarks
            val buttonText = if (showLandmarks) "Hide Landmarks" else "Show Landmarks"
            binding.landmarksButton.text = buttonText
            binding.faceLandmarkView.visibility = if (showLandmarks) View.VISIBLE else View.INVISIBLE
            Toast.makeText(this, if (showLandmarks) "Landmarks Visible" else "Landmarks Hidden", Toast.LENGTH_SHORT).show()
        }

        // Performance toggle
        binding.performanceButton.setOnClickListener {
            showPerformance = !showPerformance
            val buttonText = if (showPerformance) "Hide Performance" else "Show Performance"
            binding.performanceButton.text = buttonText
            Toast.makeText(this, if (showPerformance) "Performance Monitoring On" else "Performance Monitoring Off", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

                            val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, FaceAnalyzer())
                    }

            try {
                cameraProvider.unbindAll()

                camera = cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalyzer
                )

                binding.statusText.text = "$currentMethod Detection Active"

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    @androidx.camera.core.ExperimentalGetImage
    private inner class FaceAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            val startTime = System.currentTimeMillis()
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                if (currentMethod == "TensorFlow") {
                    // Use TensorFlow Lite
                    processWithTensorFlow(mediaImage, imageProxy.imageInfo.rotationDegrees, startTime)
                    // Close the image proxy after TensorFlow processing
                    imageProxy.close()
                } else {
                    // Use ML Kit
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    faceDetector.process(image)
                        .addOnSuccessListener { faces ->
                            performanceMonitor.recordDetectionTime(startTime)
                            runOnUiThread {
                                if (faces.isNotEmpty()) {
                                    val face = faces[0]
                                    val info = buildFaceInfo(face)
                                    val performanceInfo = if (showPerformance) "\n\n${performanceMonitor.getPerformanceSummary()}" else ""
                                    binding.faceInfoText.text = info + performanceInfo

                                    binding.faceLandmarkView.setFaces(faces, image.width, image.height)
                                } else {
                                    val performanceInfo = if (showPerformance) "\n\n${performanceMonitor.getPerformanceSummary()}" else ""
                                    binding.faceInfoText.text = "No face detected$performanceInfo"
                                    binding.faceLandmarkView.setFaces(emptyList(), image.width, image.height)
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Face detection failed", e)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                }
            } else {
                imageProxy.close()
            }
        }

        private fun processWithTensorFlow(mediaImage: android.media.Image, rotationDegrees: Int, startTime: Long) {
            try {
                val bitmap = mediaImageToBitmap(mediaImage, rotationDegrees)
                Log.d(TAG, "Processing TensorFlow detection for bitmap: ${bitmap.width}x${bitmap.height}")
                
                val detections = tensorFlowFaceDetector.detectFaces(bitmap)
                Log.d(TAG, "TensorFlow detection completed. Found ${detections.size} detections")

                performanceMonitor.recordDetectionTime(startTime)

                runOnUiThread {
                    try {
                        Log.d(TAG, "Updating UI with TensorFlow results on main thread")
                        if (detections.isNotEmpty()) {
                            val detection = detections[0]
                            val info = buildTensorFlowFaceInfo(detection)
                            val performanceInfo = if (showPerformance) "\n\n${performanceMonitor.getPerformanceSummary()}" else ""
                            binding.faceInfoText.text = info + performanceInfo
                            
                            // Update landmark view with TensorFlow detections
                            binding.faceLandmarkView.setTensorFlowDetections(detections, bitmap.width, bitmap.height)
                            Log.d(TAG, "UI updated with TensorFlow detection: ${detection.confidence}")
                        } else {
                            val performanceInfo = if (showPerformance) "\n\n${performanceMonitor.getPerformanceSummary()}" else ""
                            binding.faceInfoText.text = "No face detected (TensorFlow)$performanceInfo"
                            binding.faceLandmarkView.setTensorFlowDetections(emptyList(), bitmap.width, bitmap.height)
                            Log.d(TAG, "UI updated with no TensorFlow detections")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating UI with TensorFlow results", e)
                        binding.faceInfoText.text = "Error updating UI"
                        binding.faceLandmarkView.setTensorFlowDetections(emptyList(), bitmap.width, bitmap.height)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "TensorFlow face detection failed", e)
                // Ensure UI is updated even on error
                runOnUiThread {
                    binding.faceInfoText.text = "TensorFlow detection error: ${e.message}"
                    binding.faceLandmarkView.setTensorFlowDetections(emptyList(), 0, 0)
                    Log.d(TAG, "UI updated with TensorFlow error")
                }
            } finally {
                // Always close the image proxy
                try {
                    // Note: imageProxy.close() should be called by the caller
                } catch (e: Exception) {
                    Log.e(TAG, "Error in TensorFlow processing cleanup", e)
                }
            }
        }

        private fun mediaImageToBitmap(mediaImage: android.media.Image, rotationDegrees: Int): Bitmap {
            val yBuffer = mediaImage.planes[0].buffer
            val uBuffer = mediaImage.planes[1].buffer
            val vBuffer = mediaImage.planes[2].buffer

            val ySize = yBuffer.remaining()
            val uSize = uBuffer.remaining()
            val vSize = vBuffer.remaining()

            val nv21 = ByteArray(ySize + uSize + vSize)

            yBuffer.get(nv21, 0, ySize)
            vBuffer.get(nv21, ySize, vSize)
            uBuffer.get(nv21, ySize + vSize, uSize)

            val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, mediaImage.width, mediaImage.height, null)
            val out = java.io.ByteArrayOutputStream()
            yuvImage.compressToJpeg(android.graphics.Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
            val imageBytes = out.toByteArray()
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            return bitmap
        }

        private fun buildTensorFlowFaceInfo(detection: TensorFlowFaceDetector.Detection): String {
            val info = StringBuilder()
            info.append("TensorFlow Face Detected!\n")
            info.append("Confidence: ${(detection.confidence * 100).toInt()}%\n")
            info.append("Bounding Box: ${detection.boundingBox}\n")
            info.append("Class ID: ${detection.classId}\n")
            return info.toString()
        }

        private fun buildFaceInfo(face: Face): String {
            val info = StringBuilder()
            info.append("Face Detected!\n")

            // Emotion detection
            val emotionResult = emotionDetector.detectEmotion(face)
            val emoji = emotionDetector.getEmotionEmoji(emotionResult.emotion)
            val emotionDesc = emotionDetector.getEmotionDescription(emotionResult.emotion)
            val emotionConfidence = (emotionResult.confidence * 100).toInt()

            info.append("$emoji $emotionDesc: $emotionConfidence%\n\n")

            // Smile probability
            if (face.smilingProbability != null) {
                val smileProb = (face.smilingProbability!! * 100).toInt()
                info.append("Smile: $smileProb%\n")
            }

            // Left eye open probability
            if (face.leftEyeOpenProbability != null) {
                val leftEyeProb = (face.leftEyeOpenProbability!! * 100).toInt()
                info.append("Left Eye: $leftEyeProb%\n")
            }

            // Right eye open probability
            if (face.rightEyeOpenProbability != null) {
                val rightEyeProb = (face.rightEyeOpenProbability!! * 100).toInt()
                info.append("Right Eye: $rightEyeProb%\n")
            }

            // Head rotation
            if (face.headEulerAngleY != null) {
                info.append("Head Y: ${face.headEulerAngleY!!.toInt()}°\n")
            }

            if (face.headEulerAngleZ != null) {
                info.append("Head Z: ${face.headEulerAngleZ!!.toInt()}°\n")
            }

            return info.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        faceDetector.close()
        tensorFlowFaceDetector.close()
    }

    // Force UI refresh method
    private fun forceUIRefresh() {
        runOnUiThread {
            binding.faceLandmarkView.invalidate()
            binding.faceInfoText.invalidate()
        }
    }

    // Test TensorFlow detection
    private fun testTensorFlowDetection() {
        Log.d(TAG, "Testing TensorFlow detection...")
        if (tensorFlowFaceDetector.isModelLoaded()) {
            Log.d(TAG, "TensorFlow model is loaded")
            if (tensorFlowFaceDetector.testModel()) {
                Log.d(TAG, "TensorFlow model test passed")
                binding.statusText.text = "TensorFlow Model Ready"
            } else {
                Log.e(TAG, "TensorFlow model test failed")
                binding.statusText.text = "TensorFlow Model Error"
            }
        } else {
            Log.e(TAG, "TensorFlow model not loaded")
            binding.statusText.text = "TensorFlow Model Not Loaded"
        }
    }
} 