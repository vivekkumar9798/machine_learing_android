package com.example.machinelearningapp.helper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.machinelearningapp.helper.TensorFlowFaceDetector
import com.google.mlkit.vision.face.Face

class FaceLandmarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.GREEN
        strokeWidth = 4f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 30f
        isAntiAlias = true
    }

    private val fillPaint = Paint().apply {
        color = Color.argb(50, 0, 255, 0)
        style = Paint.Style.FILL
    }

    private var faces: List<Face> = emptyList()
    private var tensorFlowDetections: List<TensorFlowFaceDetector.Detection> = emptyList()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var isTensorFlowMode: Boolean = false

    fun setFaces(faces: List<Face>, imageWidth: Int, imageHeight: Int) {
        this.faces = faces
        this.tensorFlowDetections = emptyList()
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.isTensorFlowMode = false
        invalidate()
    }

    fun setTensorFlowDetections(detections: List<TensorFlowFaceDetector.Detection>, imageWidth: Int, imageHeight: Int) {
        this.tensorFlowDetections = detections
        this.faces = emptyList()
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        this.isTensorFlowMode = true
        Log.d("FaceLandmarkView", "Setting TensorFlow detections: ${detections.size} detections, image: ${imageWidth}x${imageHeight}")
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isTensorFlowMode) {
            drawTensorFlowDetections(canvas)
        } else {
            drawMLKitFaces(canvas)
        }
    }

    private fun drawTensorFlowDetections(canvas: Canvas) {
        if (tensorFlowDetections.isEmpty()) {
            Log.d("FaceLandmarkView", "No TensorFlow detections to draw")
            return
        }

        val scaleX = width.toFloat() / imageWidth
        val scaleY = height.toFloat() / imageHeight

        Log.d("FaceLandmarkView", "Drawing ${tensorFlowDetections.size} TensorFlow detections with scale: ${scaleX}x${scaleY}")

        for (detection in tensorFlowDetections) {
            // Draw bounding box
            val rect = detection.boundingBox
            val scaledRect = RectF(
                rect.left * scaleX,
                rect.top * scaleY,
                rect.right * scaleX,
                rect.bottom * scaleY
            )

            canvas.drawRect(scaledRect, paint)
            canvas.drawRect(scaledRect, fillPaint)

            // Draw confidence text
            val confidence = "TensorFlow: ${(detection.confidence * 100).toInt()}%"
            canvas.drawText(confidence, scaledRect.left, scaledRect.top - 10, textPaint)

            // Draw corner points
            val landmarkPaint = Paint().apply {
                color = Color.RED
                strokeWidth = 8f
                style = Paint.Style.FILL
            }

            canvas.drawCircle(scaledRect.left, scaledRect.top, 8f, landmarkPaint) // Top-left
            canvas.drawCircle(scaledRect.right, scaledRect.top, 8f, landmarkPaint) // Top-right
            canvas.drawCircle(scaledRect.left, scaledRect.bottom, 8f, landmarkPaint) // Bottom-left
            canvas.drawCircle(scaledRect.right, scaledRect.bottom, 8f, landmarkPaint) // Bottom-right

            Log.d("FaceLandmarkView", "Drew TensorFlow detection: confidence=${detection.confidence}, box=${scaledRect}")
        }
    }

    private fun drawMLKitFaces(canvas: Canvas) {
        if (faces.isEmpty()) return

        val scaleX = width.toFloat() / imageWidth
        val scaleY = height.toFloat() / imageHeight

        for (face in faces) {
            // Draw bounding box
            val rect = face.boundingBox
            val scaledRect = RectF(
                rect.left * scaleX,
                rect.top * scaleY,
                rect.right * scaleX,
                rect.bottom * scaleY
            )

            canvas.drawRect(scaledRect, paint)
            canvas.drawRect(scaledRect, fillPaint)

            // Draw face landmarks
            drawLandmarks(canvas, face, scaleX, scaleY)

            // Draw confidence text
            val confidence = "Face: ${(face.trackingId ?: 0)}"
            canvas.drawText(confidence, scaledRect.left, scaledRect.top - 10, textPaint)
        }
    }

    private fun drawLandmarks(canvas: Canvas, face: Face, scaleX: Float, scaleY: Float) {
        val landmarkPaint = Paint().apply {
            color = Color.RED
            strokeWidth = 8f
            style = Paint.Style.FILL
        }

        // Draw eyes with different colors based on openness
        face.leftEyeOpenProbability?.let { prob ->
            val color = if (prob > 0.5f) Color.GREEN else Color.RED
            landmarkPaint.color = color
        }

        // Draw bounding box corners as landmarks
        val rect = face.boundingBox
        val scaledRect = RectF(
            rect.left * scaleX,
            rect.top * scaleY,
            rect.right * scaleX,
            rect.bottom * scaleY
        )

        // Draw corner points
        canvas.drawCircle(scaledRect.left, scaledRect.top, 8f, landmarkPaint) // Top-left
        canvas.drawCircle(scaledRect.right, scaledRect.top, 8f, landmarkPaint) // Top-right
        canvas.drawCircle(scaledRect.left, scaledRect.bottom, 8f, landmarkPaint) // Bottom-left
        canvas.drawCircle(scaledRect.right, scaledRect.bottom, 8f, landmarkPaint) // Bottom-right
    }
}