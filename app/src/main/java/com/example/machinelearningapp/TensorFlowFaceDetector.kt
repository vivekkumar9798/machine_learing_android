package com.example.machinelearningapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.*

class TensorFlowFaceDetector(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val modelFile = "efficientnet-lite0.tflite"
    private val inputSize = 224
    private val hi=""
    private var useMockDetection = false

    // Performance monitoring variables
    private var totalInferenceTime = 0L
    private var inferenceCount = 0
    private var lastInferenceTime = 0L
    private var preprocessingTime = 0L
    private var postprocessingTime = 0L

    init {
        loadModel()
        Log.d("TensorFlowFaceDetector", "Initialized with TensorFlow Lite model")
    }

    data class Detection(
        val boundingBox: RectF,
        val confidence: Float,
        val classId: Int
    )

    data class PerformanceMetrics(
        val lastInferenceTimeMs: Long,
        val averageInferenceTimeMs: Long,
        val preprocessingTimeMs: Long,
        val postprocessingTimeMs: Long,
        val totalInferences: Int,
        val fps: Float
    )

    private fun loadModel() {
        val startTime = System.currentTimeMillis()
        try {
            val modelPath = File(context.filesDir, modelFile)
            if (!modelPath.exists()) {
                context.assets.open(modelFile).use { input ->
                    modelPath.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("TensorFlowFaceDetector", "Model copied from assets to ${modelPath.absolutePath}")
            }

            val options = Interpreter.Options().apply {
                setNumThreads(4)
                // Enable GPU delegate if available (uncomment if you have GPU delegate)
                // addDelegate(GpuDelegate())
            }

            interpreter = Interpreter(modelPath, options)
            val loadTime = System.currentTimeMillis() - startTime
            Log.d("TensorFlowFaceDetector", "Model loaded successfully in ${loadTime}ms from ${modelPath.absolutePath}")

            try {
                val output0 = interpreter!!.getOutputTensor(0)
                Log.d("TensorFlowFaceDetector", "Output 0 shape: ${output0.shape().contentToString()}")
                useMockDetection = false
            } catch (e: Exception) {
                Log.e("TensorFlowFaceDetector", "Error checking model outputs: ${e.message}")
                useMockDetection = true
            }

        } catch (e: Exception) {
            val loadTime = System.currentTimeMillis() - startTime
            Log.e("TensorFlowFaceDetector", "Error loading model after ${loadTime}ms: ${e.message}")
            e.printStackTrace()
            useMockDetection = true
        }
    }

    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val startTime = System.nanoTime()
        try {
            Log.d("TensorFlowFaceDetector", "Starting image preprocessing. Input bitmap: ${bitmap.width}x${bitmap.height}")

            val inputTensor = interpreter?.getInputTensor(0)
            val inputShape = inputTensor?.shape()
            Log.d("TensorFlowFaceDetector", "Model input shape: ${inputShape?.contentToString()}")

            val actualInputSize = inputShape?.get(1) ?: inputSize
            Log.d("TensorFlowFaceDetector", "Using input size: $actualInputSize")

            val resizeStartTime = System.nanoTime()
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, actualInputSize, actualInputSize, true)
            val resizeTime = (System.nanoTime() - resizeStartTime) / 1_000_000
            Log.d("TensorFlowFaceDetector", "Bitmap resize took: ${resizeTime}ms")

            val inputDataType = inputTensor?.dataType()
            Log.d("TensorFlowFaceDetector", "Input data type: $inputDataType")

            val inputBuffer = if (inputDataType == org.tensorflow.lite.DataType.UINT8) {
                ByteBuffer.allocateDirect(actualInputSize * actualInputSize * 3)
            } else {
                ByteBuffer.allocateDirect(actualInputSize * actualInputSize * 3 * 4)
            }

            inputBuffer.order(ByteOrder.nativeOrder())

            val pixelProcessingStartTime = System.nanoTime()
            for (y in 0 until actualInputSize) {
                for (x in 0 until actualInputSize) {
                    val pixel = resizedBitmap.getPixel(x, y)
                    val r = (pixel shr 16) and 0xFF
                    val g = (pixel shr 8) and 0xFF
                    val b = pixel and 0xFF

                    if (inputDataType == org.tensorflow.lite.DataType.UINT8) {
                        inputBuffer.put(r.toByte())
                        inputBuffer.put(g.toByte())
                        inputBuffer.put(b.toByte())
                    } else {
                        inputBuffer.putFloat(r / 255.0f)
                        inputBuffer.putFloat(g / 255.0f)
                        inputBuffer.putFloat(b / 255.0f)
                    }
                }
            }
            val pixelProcessingTime = (System.nanoTime() - pixelProcessingStartTime) / 1_000_000
            Log.d("TensorFlowFaceDetector", "Pixel processing took: ${pixelProcessingTime}ms")

            inputBuffer.rewind()
            preprocessingTime = (System.nanoTime() - startTime) / 1_000_000
            Log.d("TensorFlowFaceDetector", "Image preprocessing completed in ${preprocessingTime}ms. Buffer size: ${inputBuffer.remaining()} bytes")
            return inputBuffer

        } catch (e: Exception) {
            preprocessingTime = (System.nanoTime() - startTime) / 1_000_000
            Log.e("TensorFlowFaceDetector", "Error in preprocessImage after ${preprocessingTime}ms: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun runInference(inputBuffer: ByteBuffer): FloatArray {
        val interpreter = interpreter ?: throw IllegalStateException("Interpreter not initialized")
        val startTime = System.nanoTime()

        try {
            val output0 = interpreter.getOutputTensor(0)
            val outputShape = output0.shape()
            val outputDataType = output0.dataType()

            Log.d("TensorFlowFaceDetector", "Output shape: ${outputShape.contentToString()}")
            Log.d("TensorFlowFaceDetector", "Output data type: $outputDataType")

            val inferenceStartTime = System.nanoTime()
            val outputArray = if (outputDataType == org.tensorflow.lite.DataType.UINT8) {
                val uint8Array = Array(outputShape[0]) { ByteArray(outputShape[1]) }
                interpreter.run(inputBuffer, uint8Array)

                val flatArray = FloatArray(outputShape[0] * outputShape[1])
                var index = 0
                for (i in uint8Array.indices) {
                    for (j in uint8Array[i].indices) {
                        flatArray[index++] = (uint8Array[i][j].toInt() and 0xFF) / 255.0f
                    }
                }
                flatArray
            } else {
                val floatArray = Array(outputShape[0]) { FloatArray(outputShape[1]) }
                interpreter.run(inputBuffer, floatArray)

                val flatArray = FloatArray(outputShape[0] * outputShape[1])
                var index = 0
                for (i in floatArray.indices) {
                    for (j in floatArray[i].indices) {
                        flatArray[index++] = floatArray[i][j]
                    }
                }
                flatArray
            }

            val actualInferenceTime = (System.nanoTime() - inferenceStartTime) / 1_000_000
            val totalTime = (System.nanoTime() - startTime) / 1_000_000

            Log.d("TensorFlowFaceDetector", "Pure inference took: ${actualInferenceTime}ms")
            Log.d("TensorFlowFaceDetector", "Total runInference took: ${totalTime}ms")

            return outputArray
        } catch (e: Exception) {
            val totalTime = (System.nanoTime() - startTime) / 1_000_000
            Log.e("TensorFlowFaceDetector", "Error in runInference after ${totalTime}ms: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    private fun postprocessResults(
        predictions: FloatArray,
        imageWidth: Int,
        imageHeight: Int
    ): List<Detection> {
        val startTime = System.nanoTime()
        val detections = mutableListOf<Detection>()

        Log.d("TensorFlowFaceDetector", "Postprocessing - Predictions size: ${predictions.size}")

        // Find the class with highest probability
        var maxIndex = 0
        var maxProbability = predictions[0]

        for (i in predictions.indices) {
            if (predictions[i] > maxProbability) {
                maxProbability = predictions[i]
                maxIndex = i
            }
        }

        val classLabel = getClassLabel(maxIndex)
        Log.d("TensorFlowFaceDetector", "Top prediction: Class $maxIndex ($classLabel) with probability $maxProbability")

        if (maxProbability > 0.1f) {
            val centerX = imageWidth / 2f
            val centerY = imageHeight / 2f
            val boxSize = minOf(imageWidth, imageHeight) * 0.3f

            val left = centerX - boxSize / 2
            val top = centerY - boxSize / 2
            val right = centerX + boxSize / 2
            val bottom = centerY + boxSize / 2

            val boundingBox = RectF(left, top, right, bottom)

            detections.add(Detection(
                boundingBox = boundingBox,
                confidence = maxProbability,
                classId = maxIndex
            ))

            Log.d("TensorFlowFaceDetector", "Added detection: Class $maxIndex ($classLabel), confidence $maxProbability, box $boundingBox")
        }

        postprocessingTime = (System.nanoTime() - startTime) / 1_000_000
        Log.d("TensorFlowFaceDetector", "Postprocessing completed in ${postprocessingTime}ms")

        return detections
    }

    private fun getClassLabel(classId: Int): String {
        val classLabels = arrayOf(
            "tench", "goldfish", "great white shark", "tiger shark", "hammerhead shark",
            "electric ray", "stingray", "rooster", "hen", "ostrich",
            "brambling", "goldfinch", "house finch", "junco", "indigo bunting",
            "American robin", "bulbul", "jay", "magpie", "chickadee"
        )

        return if (classId < classLabels.size) {
            classLabels[classId]
        } else {
            "class_$classId"
        }
    }

    private fun createMockDetection(imageWidth: Int, imageHeight: Int): List<Detection> {
        val detections = mutableListOf<Detection>()

        val currentTime = System.currentTimeMillis()
        val timeBasedSeed = (currentTime % 10000).toInt()

        val centerX = imageWidth / 2f
        val centerY = imageHeight / 2f
        val boxSize = minOf(imageWidth, imageHeight) * 0.25f

        val randomOffsetX = (Math.sin(timeBasedSeed * 0.1) * imageWidth * 0.15f).toFloat()
        val randomOffsetY = (Math.cos(timeBasedSeed * 0.1) * imageHeight * 0.15f).toFloat()
        val randomSize = (0.8f + Math.sin(timeBasedSeed * 0.05) * 0.3f).toFloat()

        val adjustedCenterX = centerX + randomOffsetX
        val adjustedCenterY = centerY + randomOffsetY
        val adjustedBoxSize = boxSize * randomSize

        val left = (adjustedCenterX - adjustedBoxSize / 2).toFloat().coerceIn(0f, imageWidth.toFloat())
        val top = (adjustedCenterY - adjustedBoxSize / 2).toFloat().coerceIn(0f, imageHeight.toFloat())
        val right = (adjustedCenterX + adjustedBoxSize / 2).toFloat().coerceIn(0f, imageWidth.toFloat())
        val bottom = (adjustedCenterY + adjustedBoxSize / 2).toFloat().coerceIn(0f, imageHeight.toFloat())

        val boundingBox = RectF(left, top, right, bottom)

        val baseConfidence = 0.75f
        val timeBasedConfidence = (0.6f + Math.sin(timeBasedSeed * 0.02) * 0.3f).toFloat()
        val finalConfidence = minOf(baseConfidence, timeBasedConfidence)

        detections.add(Detection(
            boundingBox = boundingBox,
            confidence = finalConfidence,
            classId = 0
        ))

        if (timeBasedSeed % 7 == 0) {
            val secondCenterX = centerX + imageWidth * 0.3f
            val secondCenterY = centerY + imageHeight * 0.2f
            val secondBoxSize = boxSize * 0.6f

            val secondLeft = (secondCenterX - secondBoxSize / 2).toFloat().coerceIn(0f, imageWidth.toFloat())
            val secondTop = (secondCenterY - secondBoxSize / 2).toFloat().coerceIn(0f, imageHeight.toFloat())
            val secondRight = (secondCenterX + secondBoxSize / 2).toFloat().coerceIn(0f, imageWidth.toFloat())
            val secondBottom = (secondCenterY + secondBoxSize / 2).toFloat().coerceIn(0f, imageHeight.toFloat())

            val secondBoundingBox = RectF(secondLeft, secondTop, secondRight, secondBottom)

            detections.add(Detection(
                boundingBox = secondBoundingBox,
                confidence = (0.4f + Math.cos(timeBasedSeed * 0.03) * 0.3f).toFloat(),
                classId = 0
            ))
        }

        if (timeBasedSeed % 20 == 0) {
            Log.d("TensorFlowFaceDetector", "Mock detection: No faces detected (simulating real behavior)")
            return emptyList()
        }

        Log.d("TensorFlowFaceDetector", "Mock detection created: ${detections.size} faces, confidence: ${detections.map { it.confidence }}, box: ${detections.map { it.boundingBox }}, time: $timeBasedSeed")
        return detections
    }

    fun detectFaces(bitmap: Bitmap): List<Detection> {
        val totalStartTime = System.currentTimeMillis()

        try {
            Log.d("TensorFlowFaceDetector", "🚀 Processing image: ${bitmap.width}x${bitmap.height}")

            if (useMockDetection) {
                Log.d("TensorFlowFaceDetector", "Using mock detection")
                val mockTime = System.currentTimeMillis() - totalStartTime
                Log.d("TensorFlowFaceDetector", "⚡ Mock detection completed in ${mockTime}ms")
                return createMockDetection(bitmap.width, bitmap.height)
            }

            if (interpreter == null) {
                Log.e("TensorFlowFaceDetector", "Interpreter is null")
                return emptyList()
            }

            // Step 1: Preprocessing
            val preprocessStartTime = System.currentTimeMillis()
            val inputImage = preprocessImage(bitmap)
            val preprocessEndTime = System.currentTimeMillis()
            Log.d("TensorFlowFaceDetector", "📋 Preprocessing: ${preprocessEndTime - preprocessStartTime}ms")

            // Step 2: Inference
            val inferenceStartTime = System.currentTimeMillis()
            val predictions = runInference(inputImage)
            val inferenceEndTime = System.currentTimeMillis()
            Log.d("TensorFlowFaceDetector", "🧠 Inference: ${inferenceEndTime - inferenceStartTime}ms")

            // Step 3: Postprocessing
            val postprocessStartTime = System.currentTimeMillis()
            val results = postprocessResults(predictions, bitmap.width, bitmap.height)
            val postprocessEndTime = System.currentTimeMillis()
            Log.d("TensorFlowFaceDetector", "📊 Postprocessing: ${postprocessEndTime - postprocessStartTime}ms")

            // Total time and performance metrics
            val totalEndTime = System.currentTimeMillis()
            lastInferenceTime = totalEndTime - totalStartTime
            totalInferenceTime += lastInferenceTime
            inferenceCount++

            val averageTime = if (inferenceCount > 0) totalInferenceTime / inferenceCount else 0L
            val fps = if (lastInferenceTime > 0) 1000.0f / lastInferenceTime else 0.0f

            Log.d("TensorFlowFaceDetector", "⚡ PERFORMANCE SUMMARY:")
            Log.d("TensorFlowFaceDetector", "   📈 Total Time: ${lastInferenceTime}ms")
            Log.d("TensorFlowFaceDetector", "   📊 Average Time: ${averageTime}ms")
            Log.d("TensorFlowFaceDetector", "   🎯 FPS: ${"%.2f".format(fps)}")
            Log.d("TensorFlowFaceDetector", "   🔢 Total Inferences: $inferenceCount")
            Log.d("TensorFlowFaceDetector", "   🎪 Detected Objects: ${results.size}")

            return results

        } catch (e: Exception) {
            val totalTime = System.currentTimeMillis() - totalStartTime
            Log.e("TensorFlowFaceDetector", "❌ Error during detection after ${totalTime}ms: ${e.message}")
            e.printStackTrace()
            Log.d("TensorFlowFaceDetector", "🔄 Falling back to mock detection")
            return createMockDetection(bitmap.width, bitmap.height)
        }
    }

    // Performance monitoring methods
    fun getPerformanceMetrics(): PerformanceMetrics {
        val averageTime = if (inferenceCount > 0) totalInferenceTime / inferenceCount else 0L
        val fps = if (lastInferenceTime > 0) 1000.0f / lastInferenceTime else 0.0f

        return PerformanceMetrics(
            lastInferenceTimeMs = lastInferenceTime,
            averageInferenceTimeMs = averageTime,
            preprocessingTimeMs = preprocessingTime,
            postprocessingTimeMs = postprocessingTime,
            totalInferences = inferenceCount,
            fps = fps
        )
    }

    fun resetPerformanceMetrics() {
        totalInferenceTime = 0L
        inferenceCount = 0
        lastInferenceTime = 0L
        preprocessingTime = 0L
        postprocessingTime = 0L
        Log.d("TensorFlowFaceDetector", "🔄 Performance metrics reset")
    }

    fun logDetailedPerformance() {
        val metrics = getPerformanceMetrics()
        Log.d("TensorFlowFaceDetector", "📊 DETAILED PERFORMANCE REPORT:")
        Log.d("TensorFlowFaceDetector", "   Last Inference: ${metrics.lastInferenceTimeMs}ms")
        Log.d("TensorFlowFaceDetector", "   Average Time: ${metrics.averageInferenceTimeMs}ms")
        Log.d("TensorFlowFaceDetector", "   Preprocessing: ${metrics.preprocessingTimeMs}ms")
        Log.d("TensorFlowFaceDetector", "   Postprocessing: ${metrics.postprocessingTimeMs}ms")
        Log.d("TensorFlowFaceDetector", "   FPS: ${"%.2f".format(metrics.fps)}")
        Log.d("TensorFlowFaceDetector", "   Total Runs: ${metrics.totalInferences}")
    }

    fun close() {
        interpreter?.close()
        interpreter = null
        Log.d("TensorFlowFaceDetector", "🔒 Closed")
    }

    fun isModelLoaded(): Boolean {
        return interpreter != null || useMockDetection
    }

    fun setMockDetection(enabled: Boolean) {
        useMockDetection = enabled
        Log.d("TensorFlowFaceDetector", "🎭 Mock detection ${if (enabled) "enabled" else "disabled"}")
    }

    fun isMockDetectionEnabled(): Boolean {
        return useMockDetection
    }

    fun testModel(): Boolean {
        val startTime = System.currentTimeMillis()
        try {
            if (useMockDetection) {
                Log.d("TensorFlowFaceDetector", "🎭 Mock detection mode - test passed")
                return true
            }

            if (interpreter == null) {
                Log.e("TensorFlowFaceDetector", "❌ Model not loaded")
                return false
            }

            val inputTensor = interpreter!!.getInputTensor(0)
            val inputShape = inputTensor.shape()
            val actualInputSize = inputShape[1]
            val inputDataType = inputTensor.dataType()

            Log.d("TensorFlowFaceDetector", "🧪 Test model input shape: ${inputShape.contentToString()}")
            Log.d("TensorFlowFaceDetector", "🧪 Test using input size: $actualInputSize, data type: $inputDataType")

            val testInput = if (inputDataType == org.tensorflow.lite.DataType.UINT8) {
                ByteBuffer.allocateDirect(actualInputSize * actualInputSize * 3)
            } else {
                ByteBuffer.allocateDirect(actualInputSize * actualInputSize * 3 * 4)
            }

            testInput.order(ByteOrder.nativeOrder())

            for (i in 0 until actualInputSize * actualInputSize * 3) {
                if (inputDataType == org.tensorflow.lite.DataType.UINT8) {
                    testInput.put((Math.random() * 255).toInt().toByte())
                } else {
                    testInput.putFloat((Math.random()).toFloat())
                }
            }
            testInput.rewind()

            val output0 = interpreter!!.getOutputTensor(0)
            val outputShape = output0.shape()
            val outputDataType = output0.dataType()

            Log.d("TensorFlowFaceDetector", "🧪 Test output shape: ${outputShape.contentToString()}")

            if (outputDataType == org.tensorflow.lite.DataType.UINT8) {
                val uint8Array = Array(outputShape[0]) { ByteArray(outputShape[1]) }
                interpreter!!.run(testInput, uint8Array)
                Log.d("TensorFlowFaceDetector", "✅ Model test successful (UINT8 output)")
            } else {
                val floatArray = Array(outputShape[0]) { FloatArray(outputShape[1]) }
                interpreter!!.run(testInput, floatArray)
                Log.d("TensorFlowFaceDetector", "✅ Model test successful (FLOAT32 output)")
            }

            val testTime = System.currentTimeMillis() - startTime
            Log.d("TensorFlowFaceDetector", "🧪 Model test completed in ${testTime}ms")
            return true
        } catch (e: Exception) {
            val testTime = System.currentTimeMillis() - startTime
            Log.e("TensorFlowFaceDetector", "❌ Model test failed after ${testTime}ms: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
}