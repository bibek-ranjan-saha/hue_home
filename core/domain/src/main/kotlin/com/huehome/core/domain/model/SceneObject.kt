package com.huehome.core.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core domain model representing a detected object in the AR scene
 * 
 * This model is persisted in Room database and tracks:
 * - Original detected color
 * - Applied color (if any)
 * - Object state (active/inactive)
 * - Mask data for rendering
 */
@Entity(tableName = "scene_objects")
data class SceneObject(
    @PrimaryKey
    val id: String,
    
    /** Type of object (wall, door, window, etc.) */
    val type: ObjectType,
    
    /** Detected color information with confidence */
    val detectedColor: ColorInfo,
    
    /** Path to stored mask bitmap */
    val maskPath: String = "",
    
    /** Original detected color (ARGB Int) - derived from detectedColor */
    val originalColor: Int = detectedColor.rgb,
    
    /** Applied color (ARGB Int), null if no color applied */
    val appliedColor: Int? = null,
    
    /** User-provided label for this object */
    val userLabel: String? = null,
    
    /** Whether this object is currently active in the scene */
    val isActive: Boolean = true,
    
    /** Timestamp when object was detected */
    val timestamp: Long = System.currentTimeMillis(),
    
    /** Bounding box coordinates [x, y, width, height] */
    val boundingBox: FloatArray = floatArrayOf(0f, 0f, 0f, 0f),
    
    /** Confidence score from segmentation model (0.0 - 1.0) */
    val confidence: Float = detectedColor.confidence
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SceneObject

        if (id != other.id) return false
        if (type != other.type) return false
        if (detectedColor != other.detectedColor) return false
        if (maskPath != other.maskPath) return false
        if (originalColor != other.originalColor) return false
        if (appliedColor != other.appliedColor) return false
        if (userLabel != other.userLabel) return false
        if (isActive != other.isActive) return false
        if (timestamp != other.timestamp) return false
        if (!boundingBox.contentEquals(other.boundingBox)) return false
        if (confidence != other.confidence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + detectedColor.hashCode()
        result = 31 * result + maskPath.hashCode()
        result = 31 * result + originalColor
        result = 31 * result + (appliedColor ?: 0)
        result = 31 * result + (userLabel?.hashCode() ?: 0)
        result = 31 * result + isActive.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + boundingBox.contentHashCode()
        result = 31 * result + confidence.hashCode()
        return result
    }
}

/**
 * Types of objects that can be detected and colored
 * MVP focuses on walls and doors
 */
enum class ObjectType {
    WALL,
    DOOR,
    WINDOW,
    CEILING,
    FLOOR,
    UNKNOWN
}
