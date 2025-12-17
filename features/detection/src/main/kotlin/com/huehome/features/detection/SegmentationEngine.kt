package com.huehome.features.detection

import android.content.Context
import android.graphics.Bitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TensorFlow Lite segmentation engine for object detection
 * Uses DeepLab v3+ model for pixel-level segmentation of walls, doors, windows
 */
@Singleton
class SegmentationEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    
    // Model input/output dimensions
    private val inputSize = 513 // DeepLab v3+ standard input
    private val numClasses = 21 // PASCAL VOC classes
    
    // Object class indices (PASCAL VOC)
    companion object {
        const val CLASS_BACKGROUND = 0
        const val CLASS_WALL = 15  // Wall/building
        const val CLASS_DOOR = 8   // Door
        const val CLASS_WINDOW = 20 // Window/TV (approximate)
        const val CLASS_FLOOR = 9   // Floor
        const val CLASS_CEILING = 15 // Ceiling (treated as wall)
    }
    
    /**
     * Initialize TensorFlow Lite interpreter with GPU delegate
     */
    fun initialize() {
        try {
            // Check GPU compatibility
            val compatibilityList = CompatibilityList()
            
            val options = Interpreter.Options().apply {
                if (compatibilityList.isDelegateSupportedOnThisDevice) {
                    // Use GPU delegate for acceleration
                    gpuDelegate = GpuDelegate()
                    addDelegate(gpuDelegate)
                } else {
                    // Fallback to CPU with optimizations
                    setNumThreads(4)
                    setUseNNAPI(true)
                }
            }
            
            // Load model from assets
            // TODO: Add actual model file to assets/models/deeplabv3_513_mv_gpu.tflite
            val modelPath = "models/deeplabv3_513_mv_gpu.tflite"
            val modelBuffer = loadModelFile(modelPath)
            
            interpreter = Interpreter(modelBuffer, options)
            
        } catch (e: Exception) {
            throw SegmentationException("Failed to initialize segmentation engine", e)
        }
    }
    
    /**
     * Segment image and return pixel-level masks
     */
    fun segment(frame: Bitmap): SegmentationResult {
        val interpreter = interpreter ?: throw SegmentationException("Engine not initialized")
        
        try {
            // Preprocess image
            val inputBuffer = preprocessImage(frame)
            
            // Prepare output buffer
            val outputBuffer = ByteBuffer.allocateDirect(inputSize * inputSize * 4)
            outputBuffer.order(ByteOrder.nativeOrder())
            
            // Run inference
            val startTime = System.currentTimeMillis()
            interpreter.run(inputBuffer, outputBuffer)
            val inferenceTime = System.currentTimeMillis() - startTime
            
            // Post-process results
            outputBuffer.rewind()
            val segmentationMap = IntArray(inputSize * inputSize)
            for (i in segmentationMap.indices) {
                segmentationMap[i] = outputBuffer.getInt()
            }
            
            // Extract object masks
            val masks = extractObjectMasks(segmentationMap, frame.width, frame.height)
            
            return SegmentationResult(
                masks = masks,
                inferenceTimeMs = inferenceTime,
                inputSize = inputSize
            )
            
        } catch (e: Exception) {
            throw SegmentationException("Segmentation failed", e)
        }
    }
    
    /**
     * Preprocess image for model input
     * Resize to 513x513 and normalize
     */
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        // Resize to model input size
        val resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
        
        // Allocate buffer (513 * 513 * 3 channels * 4 bytes per float)
        val buffer = ByteBuffer.allocateDirect(inputSize * inputSize * 3 * 4)
        buffer.order(ByteOrder.nativeOrder())
        
        val pixels = IntArray(inputSize * inputSize)
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        
        // Normalize to [-1, 1] (ImageNet normalization)
        for (pixel in pixels) {
            val r = ((pixel shr 16 and 0xFF) / 127.5f - 1.0f)
            val g = ((pixel shr 8 and 0xFF) / 127.5f - 1.0f)
            val b = ((pixel and 0xFF) / 127.5f - 1.0f)
            
            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }
        
        buffer.rewind()
        return buffer
    }
    
    /**
     * Extract individual object masks from segmentation map
     */
    private fun extractObjectMasks(
        segmentationMap: IntArray,
        originalWidth: Int,
        originalHeight: Int
    ): List<ObjectMask> {
        val masks = mutableListOf<ObjectMask>()
        
        // Resize segmentation map to original size
        val resizedMap = resizeSegmentationMap(segmentationMap, inputSize, inputSize, originalWidth, originalHeight)
        
        // Extract masks for each object class
        val classesToDetect = listOf(
            CLASS_WALL to "Wall",
            CLASS_DOOR to "Door",
            CLASS_WINDOW to "Window"
        )
        
        for ((classId, className) in classesToDetect) {
            val mask = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888)
            val pixels = IntArray(originalWidth * originalHeight)
            
            var pixelCount = 0
            for (i in resizedMap.indices) {
                if (resizedMap[i] == classId) {
                    pixels[i] = 0xFFFFFFFF.toInt() // White for mask
                    pixelCount++
                } else {
                    pixels[i] = 0x00000000 // Transparent
                }
            }
            
            // Only add mask if significant pixels detected
            if (pixelCount > originalWidth * originalHeight * 0.01) { // At least 1% of image
                mask.setPixels(pixels, 0, originalWidth, 0, 0, originalWidth, originalHeight)
                
                masks.add(
                    ObjectMask(
                        className = className,
                        classId = classId,
                        mask = mask,
                        pixelCount = pixelCount,
                        confidence = calculateConfidence(pixelCount, originalWidth * originalHeight)
                    )
                )
            }
        }
        
        return masks
    }
    
    /**
     * Resize segmentation map using nearest neighbor
     */
    private fun resizeSegmentationMap(
        map: IntArray,
        srcWidth: Int,
        srcHeight: Int,
        dstWidth: Int,
        dstHeight: Int
    ): IntArray {
        val resized = IntArray(dstWidth * dstHeight)
        
        val xRatio = srcWidth.toFloat() / dstWidth
        val yRatio = srcHeight.toFloat() / dstHeight
        
        for (y in 0 until dstHeight) {
            for (x in 0 until dstWidth) {
                val srcX = (x * xRatio).toInt().coerceIn(0, srcWidth - 1)
                val srcY = (y * yRatio).toInt().coerceIn(0, srcHeight - 1)
                resized[y * dstWidth + x] = map[srcY * srcWidth + srcX]
            }
        }
        
        return resized
    }
    
    /**
     * Calculate confidence score based on pixel coverage
     */
    private fun calculateConfidence(pixelCount: Int, totalPixels: Int): Float {
        val coverage = pixelCount.toFloat() / totalPixels
        return (coverage * 10).coerceIn(0.3f, 0.95f)
    }
    
    /**
     * Load model file from assets
     */
    private fun loadModelFile(modelPath: String): ByteBuffer {
        val assetFileDescriptor = context.assets.openFd(modelPath)
        val inputStream = assetFileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        return fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    
    /**
     * Release resources
     */
    fun release() {
        interpreter?.close()
        interpreter = null
        gpuDelegate?.close()
        gpuDelegate = null
    }
}

/**
 * Segmentation result containing object masks
 */
data class SegmentationResult(
    val masks: List<ObjectMask>,
    val inferenceTimeMs: Long,
    val inputSize: Int
)

/**
 * Individual object mask
 */
data class ObjectMask(
    val className: String,
    val classId: Int,
    val mask: Bitmap,
    val pixelCount: Int,
    val confidence: Float
)

/**
 * Exception thrown when segmentation fails
 */
class SegmentationException(message: String, cause: Throwable? = null) : Exception(message, cause)
