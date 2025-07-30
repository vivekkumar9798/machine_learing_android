package com.example.machinelearningapp

import android.graphics.Bitmap
import com.google.mlkit.vision.face.Face

class EmotionDetector {
    
    enum class Emotion {
        HAPPY, SAD, ANGRY, SURPRISED, NEUTRAL, FEAR, DISGUST
    }
    
    data class EmotionResult(
        val emotion: Emotion,
        val confidence: Float,
        val details: Map<String, Float>
    )
    
    fun detectEmotion(face: Face): EmotionResult {
        val details = mutableMapOf<String, Float>()
        
        // Analyze smile
        face.smilingProbability?.let { smileProb ->
            details["smile"] = smileProb
        }
        
        // Analyze eye openness
        face.leftEyeOpenProbability?.let { leftEye ->
            details["leftEye"] = leftEye
        }
        
        face.rightEyeOpenProbability?.let { rightEye ->
            details["rightEye"] = rightEye
        }
        
        // Analyze head rotation for emotion clues
        face.headEulerAngleY?.let { yAngle ->
            details["headY"] = yAngle
        }
        
        face.headEulerAngleZ?.let { zAngle ->
            details["headZ"] = zAngle
        }
        
        // Determine emotion based on facial features
        val emotion = determineEmotion(details)
        val confidence = calculateConfidence(details)
        
        return EmotionResult(emotion, confidence, details)
    }
    
    private fun determineEmotion(details: Map<String, Float>): Emotion {
        val smile = details["smile"] ?: 0f
        val leftEye = details["leftEye"] ?: 0f
        val rightEye = details["rightEye"] ?: 0f
        val headY = details["headY"] ?: 0f
        val headZ = details["headZ"] ?: 0f
        
        return when {
            // Happy: High smile probability
            smile > 0.7f -> Emotion.HAPPY
            
            // Sad: Low smile, closed eyes
            smile < 0.3f && (leftEye < 0.3f || rightEye < 0.3f) -> Emotion.SAD
            
            // Surprised: Wide open eyes
            leftEye > 0.8f && rightEye > 0.8f -> Emotion.SURPRISED
            
            // Angry: Low smile, head tilted
            smile < 0.4f && kotlin.math.abs(headY) > 15f -> Emotion.ANGRY
            
            // Fear: Wide eyes, low smile
            leftEye > 0.7f && rightEye > 0.7f && smile < 0.3f -> Emotion.FEAR
            
            // Disgust: Head turned away
            kotlin.math.abs(headZ) > 20f -> Emotion.DISGUST
            
            // Neutral: Default case
            else -> Emotion.NEUTRAL
        }
    }
    
    private fun calculateConfidence(details: Map<String, Float>): Float {
        val smile = details["smile"] ?: 0f
        val leftEye = details["leftEye"] ?: 0f
        val rightEye = details["rightEye"] ?: 0f
        
        // Calculate confidence based on feature availability and values
        var confidence = 0f
        var count = 0
        
        if (smile > 0) {
            confidence += smile
            count++
        }
        
        if (leftEye > 0) {
            confidence += leftEye
            count++
        }
        
        if (rightEye > 0) {
            confidence += rightEye
            count++
        }
        
        return if (count > 0) confidence / count else 0f
    }
    
    fun getEmotionEmoji(emotion: Emotion): String {
        return when (emotion) {
            Emotion.HAPPY -> "😊"
            Emotion.SAD -> "😢"
            Emotion.ANGRY -> "😠"
            Emotion.SURPRISED -> "😲"
            Emotion.NEUTRAL -> "😐"
            Emotion.FEAR -> "😨"
            Emotion.DISGUST -> "🤢"
        }
    }
    
    fun getEmotionDescription(emotion: Emotion): String {
        return when (emotion) {
            Emotion.HAPPY -> "Happy"
            Emotion.SAD -> "Sad"
            Emotion.ANGRY -> "Angry"
            Emotion.SURPRISED -> "Surprised"
            Emotion.NEUTRAL -> "Neutral"
            Emotion.FEAR -> "Fear"
            Emotion.DISGUST -> "Disgust"
        }
    }
} 