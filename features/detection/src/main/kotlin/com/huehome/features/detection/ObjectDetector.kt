package com.huehome.features.detection

import android.graphics.Bitmap
import android.graphics.Rect
import com.huehome.core.domain.model.ObjectType
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Object detector that performs instance-aware detection
 * Separates individual instances of the same class (e.g., multiple walls)
 */
@Singleton
class ObjectDetector @Inject constructor() {
    
    /**
     * Detect individual object instances from segmentation masks
     */
    fun detectObjects(
        frame: Bitmap,
        segmentationResult: SegmentationResult
    ): List<DetectedObject> {
        val detectedObjects = mutableListOf<DetectedObject>()
        
        for (objectMask in segmentationResult.masks) {
            // Find connected components (individual instances)
            val instances = findConnectedComponents(objectMask.mask)
            
            instances.forEachIndexed { index, instanceMask ->
                val boundingBox = calculateBoundingBox(instanceMask)
                
                // Only add if bounding box is valid
                if (boundingBox.width() > 10 && boundingBox.height() > 10) {
                    detectedObjects.add(
                        DetectedObject(
                            id = "${objectMask.className}_${System.currentTimeMillis()}_$index",
                            type = mapClassNameToType(objectMask.className),
                            mask = instanceMask,
                            boundingBox = boundingBox,
                            confidence = objectMask.confidence,
                            area = calculateArea(instanceMask)
                        )
                    )
                }
            }
        }
        
        return detectedObjects.sortedByDescending { it.area }
    }
    
    /**
     * Find connected components using flood fill algorithm
     * Separates individual instances of the same class
     */
    private fun findConnectedComponents(mask: Bitmap): List<Bitmap> {
        val width = mask.width
        val height = mask.height
        val pixels = IntArray(width * height)
        mask.getPixels(pixels, 0, width, 0, 0, width, height)
        
        val visited = BooleanArray(width * height)
        val components = mutableListOf<Bitmap>()
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                
                if (!visited[index] && pixels[index] != 0) {
                    // Found new component, flood fill
                    val componentPixels = IntArray(width * height)
                    floodFill(pixels, visited, componentPixels, x, y, width, height)
                    
                    // Create bitmap for this component
                    val componentBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    componentBitmap.setPixels(componentPixels, 0, width, 0, 0, width, height)
                    components.add(componentBitmap)
                }
            }
        }
        
        return components
    }
    
    /**
     * Flood fill algorithm to find connected pixels
     */
    private fun floodFill(
        pixels: IntArray,
        visited: BooleanArray,
        output: IntArray,
        startX: Int,
        startY: Int,
        width: Int,
        height: Int
    ) {
        val stack = mutableListOf(Pair(startX, startY))
        
        while (stack.isNotEmpty()) {
            val (x, y) = stack.removeAt(stack.size - 1)
            
            if (x < 0 || x >= width || y < 0 || y >= height) continue
            
            val index = y * width + x
            if (visited[index] || pixels[index] == 0) continue
            
            visited[index] = true
            output[index] = pixels[index]
            
            // Add neighbors
            stack.add(Pair(x + 1, y))
            stack.add(Pair(x - 1, y))
            stack.add(Pair(x, y + 1))
            stack.add(Pair(x, y - 1))
        }
    }
    
    /**
     * Calculate bounding box for a mask
     */
    private fun calculateBoundingBox(mask: Bitmap): Rect {
        val width = mask.width
        val height = mask.height
        val pixels = IntArray(width * height)
        mask.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var minX = width
        var minY = height
        var maxX = 0
        var maxY = 0
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (pixels[y * width + x] != 0) {
                    minX = minOf(minX, x)
                    minY = minOf(minY, y)
                    maxX = maxOf(maxX, x)
                    maxY = maxOf(maxY, y)
                }
            }
        }
        
        return Rect(minX, minY, maxX, maxY)
    }
    
    /**
     * Calculate area (number of pixels) in mask
     */
    private fun calculateArea(mask: Bitmap): Int {
        val pixels = IntArray(mask.width * mask.height)
        mask.getPixels(pixels, 0, mask.width, 0, 0, mask.width, mask.height)
        return pixels.count { it != 0 }
    }
    
    /**
     * Map class name to ObjectType enum
     */
    private fun mapClassNameToType(className: String): ObjectType {
        return when (className.lowercase()) {
            "wall" -> ObjectType.WALL
            "door" -> ObjectType.DOOR
            "window" -> ObjectType.WINDOW
            "floor" -> ObjectType.FLOOR
            "ceiling" -> ObjectType.CEILING
            else -> ObjectType.UNKNOWN
        }
    }
}

/**
 * Detected object with instance information
 */
data class DetectedObject(
    val id: String,
    val type: ObjectType,
    val mask: Bitmap,
    val boundingBox: Rect,
    val confidence: Float,
    val area: Int
)
