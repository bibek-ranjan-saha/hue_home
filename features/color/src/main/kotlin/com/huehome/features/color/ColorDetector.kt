package com.huehome.features.color

import android.graphics.Bitmap
import android.graphics.Color
import com.huehome.core.domain.model.ColorInfo
import com.huehome.core.domain.model.LabColor
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

/**
 * Material-aware color detector using OpenCV
 * Detects base color of objects while ignoring shadows and highlights
 */
@Singleton
class ColorDetector @Inject constructor() {
    
    companion object {
        // Load OpenCV native library
        init {
            System.loadLibrary("opencv_java4")
        }
    }
    
    /**
     * Detect base color from frame using object mask
     * Ignores shadows and highlights for accurate material color
     */
    fun detectBaseColor(
        frame: Bitmap,
        mask: Bitmap
    ): ColorInfo {
        try {
            // Convert to OpenCV Mat
            val frameMat = Mat()
            val maskMat = Mat()
            Utils.bitmapToMat(frame, frameMat)
            Utils.bitmapToMat(mask, maskMat)
            
            // Convert mask to grayscale
            if (maskMat.channels() > 1) {
                Imgproc.cvtColor(maskMat, maskMat, Imgproc.COLOR_RGBA2GRAY)
            }
            
            // Extract masked region
            val maskedRegion = Mat()
            frameMat.copyTo(maskedRegion, maskMat)
            
            // Convert to LAB color space for perceptual uniformity
            val labMat = Mat()
            Imgproc.cvtColor(maskedRegion, labMat, Imgproc.COLOR_RGB2Lab)
            
            // Remove shadows and highlights
            val filteredMat = removeShadowsAndHighlights(labMat, maskMat)
            
            // Extract dominant color using histogram clustering
            val dominantColor = extractDominantColor(filteredMat, maskMat)
            
            // Convert to RGB
            val rgb = labToRgb(dominantColor)
            
            // Calculate confidence based on color variance
            val confidence = calculateConfidence(filteredMat, maskMat, dominantColor)
            
            // Clean up
            frameMat.release()
            maskMat.release()
            maskedRegion.release()
            labMat.release()
            filteredMat.release()
            
            return ColorInfo(
                rgb = rgb,
                lab = LabColor.fromRgb(rgb),
                confidence = confidence
            )
            
        } catch (e: Exception) {
            throw ColorDetectionException("Color detection failed", e)
        }
    }
    
    /**
     * Remove shadows and highlights by filtering extreme luminance values
     */
    private fun removeShadowsAndHighlights(labMat: Mat, maskMat: Mat): Mat {
        val filtered = Mat()
        labMat.copyTo(filtered)
        
        // Extract L channel (luminance)
        val channels = ArrayList<Mat>()
        Core.split(labMat, channels)
        val lChannel = channels[0]
        
        // Calculate mean and std dev of L channel in masked region
        val mean = MatOfDouble()
        val stdDev = MatOfDouble()
        Core.meanStdDev(lChannel, mean, stdDev, maskMat)
        
        val meanL = mean.get(0, 0)[0]
        val stdL = stdDev.get(0, 0)[0]
        
        // Define thresholds (mean ± 1.5 * std dev)
        val lowerThreshold = (meanL - 1.5 * stdL).coerceAtLeast(0.0)
        val upperThreshold = (meanL + 1.5 * stdL).coerceAtMost(255.0)
        
        // Create mask for valid pixels (not shadows/highlights)
        val validMask = Mat()
        Core.inRange(lChannel, Scalar(lowerThreshold), Scalar(upperThreshold), validMask)
        
        // Combine with original mask
        val combinedMask = Mat()
        Core.bitwise_and(maskMat, validMask, combinedMask)
        
        // Apply combined mask
        val result = Mat()
        labMat.copyTo(result, combinedMask)
        
        // Clean up
        lChannel.release()
        mean.release()
        stdDev.release()
        validMask.release()
        combinedMask.release()
        
        return result
    }
    
    /**
     * Extract dominant color using K-means clustering
     */
    private fun extractDominantColor(labMat: Mat, maskMat: Mat): DoubleArray {
        // Extract non-zero pixels
        val pixels = mutableListOf<DoubleArray>()
        
        for (y in 0 until labMat.rows()) {
            for (x in 0 until labMat.cols()) {
                if (maskMat.get(y, x)[0] > 0) {
                    val pixel = labMat.get(y, x)
                    if (pixel != null && pixel.isNotEmpty()) {
                        pixels.add(pixel)
                    }
                }
            }
        }
        
        if (pixels.isEmpty()) {
            return doubleArrayOf(128.0, 128.0, 128.0) // Default gray
        }
        
        // Convert to Mat for K-means
        val samples = Mat(pixels.size, 3, CvType.CV_32F)
        for (i in pixels.indices) {
            samples.put(i, 0, floatArrayOf(pixels[i][0].toFloat(), pixels[i][1].toFloat(), pixels[i][2].toFloat()))
        }
        
        // K-means clustering (k=3 to find dominant, shadow, highlight)
        val labels = Mat()
        val centers = Mat()
        val criteria = TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER, 100, 0.2)
        
        Core.kmeans(samples, 3, labels, criteria, 3, Core.KMEANS_PP_CENTERS, centers)
        
        // Find cluster with most pixels (dominant color)
        val clusterCounts = IntArray(3)
        for (i in 0 until labels.rows()) {
            val label = labels.get(i, 0)[0].toInt()
            clusterCounts[label]++
        }
        
        val dominantCluster = clusterCounts.indices.maxByOrNull { clusterCounts[it] } ?: 0
        val dominantColor = centers.get(dominantCluster, 0)
        
        // Clean up
        samples.release()
        labels.release()
        centers.release()
        
        return dominantColor
    }
    
    /**
     * Calculate confidence based on color variance
     */
    private fun calculateConfidence(
        labMat: Mat,
        maskMat: Mat,
        dominantColor: DoubleArray
    ): Float {
        // Calculate variance from dominant color
        var totalVariance = 0.0
        var pixelCount = 0
        
        for (y in 0 until labMat.rows()) {
            for (x in 0 until labMat.cols()) {
                if (maskMat.get(y, x)[0] > 0) {
                    val pixel = labMat.get(y, x)
                    if (pixel != null && pixel.isNotEmpty()) {
                        val variance = colorDistance(pixel, dominantColor)
                        totalVariance += variance
                        pixelCount++
                    }
                }
            }
        }
        
        if (pixelCount == 0) return 0.5f
        
        val avgVariance = totalVariance / pixelCount
        
        // Convert variance to confidence (lower variance = higher confidence)
        // Typical variance range: 0-50, map to confidence 1.0-0.3
        val confidence = (1.0 - (avgVariance / 50.0).coerceIn(0.0, 1.0)).toFloat()
        return confidence.coerceIn(0.3f, 0.95f)
    }
    
    /**
     * Calculate Euclidean distance between two colors in LAB space
     */
    private fun colorDistance(color1: DoubleArray, color2: DoubleArray): Double {
        val dL = color1[0] - color2[0]
        val dA = color1[1] - color2[1]
        val dB = color1[2] - color2[2]
        return sqrt(dL * dL + dA * dA + dB * dB)
    }
    
    /**
     * Convert LAB to RGB
     */
    private fun labToRgb(lab: DoubleArray): Int {
        // Create single pixel Mat
        val labMat = Mat(1, 1, CvType.CV_8UC3)
        labMat.put(0, 0, byteArrayOf(lab[0].toInt().toByte(), lab[1].toInt().toByte(), lab[2].toInt().toByte()))
        
        // Convert to RGB
        val rgbMat = Mat()
        Imgproc.cvtColor(labMat, rgbMat, Imgproc.COLOR_Lab2RGB)
        
        val rgb = rgbMat.get(0, 0)
        
        labMat.release()
        rgbMat.release()
        
        // Convert to Android color int
        return Color.rgb(
            rgb[0].toInt().coerceIn(0, 255),
            rgb[1].toInt().coerceIn(0, 255),
            rgb[2].toInt().coerceIn(0, 255)
        )
    }
}

/**
 * Exception thrown when color detection fails
 */
class ColorDetectionException(message: String, cause: Throwable? = null) : Exception(message, cause)
