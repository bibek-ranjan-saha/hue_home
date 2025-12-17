package com.huehome.core.domain.model

/**
 * AI color recommendation with reasoning
 */
data class ColorRecommendation(
    /** Recommended color (ARGB Int) */
    val color: Int,
    
    /** LAB representation */
    val labColor: LabColor,
    
    /** Human-readable reason for this recommendation */
    val reason: String,
    
    /** Confidence score (0.0 - 1.0) */
    val confidence: Float,
    
    /** Category of recommendation */
    val category: RecommendationCategory
)

/**
 * AI processing mode selection
 */
enum class ProcessingMode {
    /** All processing on-device */
    ON_DEVICE,
    
    /** Cloud-based AI processing */
    CLOUD,
    
    /** Hybrid: on-device detection, cloud recommendations */
    HYBRID
}

/**
 * Room context for AI recommendations
 */
data class RoomContext(
    /** Lighting conditions */
    val lightingIntensity: Float = 0.5f,
    
    /** Color temperature of lighting (RGBA) */
    val lightingColor: FloatArray = floatArrayOf(1f, 1f, 1f, 1f),
    
    /** Estimated room size (small, medium, large) */
    val roomSize: RoomSize = RoomSize.MEDIUM,
    
    /** Color temperature (warm/cool) */
    val colorTemperature: Float = 0.5f,
    
    /** Number of detected walls */
    val wallCount: Int = 0,
    
    /** Dominant existing colors in the room */
    val existingColors: List<Int> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomContext

        if (lightingIntensity != other.lightingIntensity) return false
        if (!lightingColor.contentEquals(other.lightingColor)) return false
        if (roomSize != other.roomSize) return false
        if (colorTemperature != other.colorTemperature) return false
        if (wallCount != other.wallCount) return false
        if (existingColors != other.existingColors) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lightingIntensity.hashCode()
        result = 31 * result + lightingColor.contentHashCode()
        result = 31 * result + roomSize.hashCode()
        result = 31 * result + colorTemperature.hashCode()
        result = 31 * result + wallCount
        result = 31 * result + existingColors.hashCode()
        return result
    }
}

enum class RoomSize {
    SMALL,
    MEDIUM,
    LARGE
}
