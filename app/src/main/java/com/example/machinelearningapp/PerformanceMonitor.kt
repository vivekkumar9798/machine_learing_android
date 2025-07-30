package com.example.machinelearningapp

import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.roundToInt

class PerformanceMonitor {
    
    private val detectionTimes = ConcurrentLinkedQueue<Long>()
    private val maxSamples = 30 // Keep last 30 detections
    
    fun recordDetectionTime(startTime: Long) {
        val detectionTime = System.currentTimeMillis() - startTime
        detectionTimes.offer(detectionTime)
        
        // Keep only last maxSamples
        while (detectionTimes.size > maxSamples) {
            detectionTimes.poll()
        }
    }
    
    fun getAverageDetectionTime(): Long {
        if (detectionTimes.isEmpty()) return 0L
        return detectionTimes.average().roundToInt().toLong()
    }
    
    fun getMinDetectionTime(): Long {
        return detectionTimes.minOrNull()?.toLong() ?: 0L
    }
    
    fun getMaxDetectionTime(): Long {
        return detectionTimes.maxOrNull()?.toLong() ?: 0L
    }
    
    fun getDetectionCount(): Int {
        return detectionTimes.size.toInt()
    }
    
    fun getFPS(): Int {
        if (detectionTimes.isEmpty()) return 0
        val avgTime = getAverageDetectionTime()
        return if (avgTime > 0) (1000 / avgTime.toInt()) else 0
    }
    
    fun getPerformanceSummary(): String {
        val avgTime = getAverageDetectionTime()
        val minTime = getMinDetectionTime()
        val maxTime = getMaxDetectionTime()
        val fps = getFPS()
        val count = getDetectionCount()
        
        return """
            Performance Summary:
            Avg Time: ${avgTime}ms
            Min Time: ${minTime}ms
            Max Time: ${maxTime}ms
            FPS: $fps
            Samples: $count
        """.trimIndent()
    }
    
    fun reset() {
        detectionTimes.clear()
    }
} 