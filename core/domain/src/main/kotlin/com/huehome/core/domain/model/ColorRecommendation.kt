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
 * Categories of color recommendations
 */
enum class RecommendationCategory {
    /** Complementary color theory */
    COMPLEMENTARY,
    
    /** Analogous color theory */
    ANALOGOUS,
    
    /** High contrast */
    CONTRAST,
    
    /** Monochromatic variations */
    MONOCHROMATIC,
    
    /** Modern style preset */
    MODERN,
    
    /** Minimal style preset */
    MINIMAL,
    
    /** Warm style preset */
    WARM,
    
    /** Luxury style preset */
    LUXURY,
    
    /** Scandinavian style preset */
    SCANDINAVIAN
}

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
    /** Estimated room size (small, medium, large) */
    val roomSize: RoomSize = RoomSize.MEDIUM,
    
    /** Lighting conditions */
    val lightingIntensity: Float = 0.5f,
    
    /** Color temperature of lighting (warm/cool) */
    val colorTemperature: Float = 0.5f,
    
    /** Number of detected walls */
    val wallCount: Int = 0,
    
    /** Dominant existing colors in the room */
    val existingColors: List<Int> = emptyList()
)

enum class RoomSize {
    SMALL,
    MEDIUM,
    LARGE
}
