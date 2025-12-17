package com.huehome.features.color

import com.huehome.core.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI-powered color recommendation engine
 * Generates intelligent color suggestions based on color theory, room context, and style
 */
@Singleton
class RecommendationEngine @Inject constructor() {
    
    /**
     * Generate color recommendations for a given base color
     */
    fun recommend(
        baseColor: ColorInfo,
        context: RoomContext,
        mode: ProcessingMode = ProcessingMode.ON_DEVICE,
        stylePreference: String? = null
    ): List<ColorRecommendation> {
        return when (mode) {
            ProcessingMode.ON_DEVICE -> onDeviceRecommend(baseColor, context, stylePreference)
            ProcessingMode.CLOUD -> cloudRecommend(baseColor, context, stylePreference)
            ProcessingMode.HYBRID -> hybridRecommend(baseColor, context, stylePreference)
        }
    }
    
    /**
     * On-device recommendations using color theory and style presets
     */
    private fun onDeviceRecommend(
        baseColor: ColorInfo,
        context: RoomContext,
        stylePreference: String?
    ): List<ColorRecommendation> {
        val recommendations = mutableListOf<ColorRecommendation>()
        
        // 1. Color Theory Recommendations
        
        // Complementary
        val complementary = ColorTheory.complementary(baseColor.lab)
        recommendations.add(
            ColorRecommendation(
                color = complementary.rgb,
                labColor = complementary,
                reason = "Complementary color provides maximum contrast and visual interest",
                confidence = 0.85f,
                category = RecommendationCategory.COMPLEMENTARY
            )
        )
        
        // Analogous
        val analogous = ColorTheory.analogous(baseColor.lab, 2)
        analogous.forEachIndexed { index, color ->
            recommendations.add(
                ColorRecommendation(
                    color = color.rgb,
                    labColor = color,
                    reason = "Analogous color creates harmonious, cohesive look",
                    confidence = 0.80f - (index * 0.05f),
                    category = RecommendationCategory.ANALOGOUS
                )
            )
        }
        
        // Monochromatic
        val monochromatic = ColorTheory.monochromatic(baseColor.lab, 2)
        monochromatic.forEachIndexed { index, color ->
            recommendations.add(
                ColorRecommendation(
                    color = color.rgb,
                    labColor = color,
                    reason = "Monochromatic variation provides subtle sophistication",
                    confidence = 0.75f,
                    category = RecommendationCategory.MONOCHROMATIC
                )
            )
        }
        
        // 2. Style-Based Recommendations
        if (stylePreference != null) {
            val palette = StylePresets.getPalette(stylePreference)
            val closestColor = StylePresets.findClosestInPalette(baseColor.lab, palette)
            val closestLab = LabColor.fromRgb(closestColor)
            
            recommendations.add(
                ColorRecommendation(
                    color = closestColor,
                    labColor = closestLab,
                    reason = "Matches $stylePreference style aesthetic",
                    confidence = 0.90f,
                    category = when (stylePreference.lowercase()) {
                        "modern" -> RecommendationCategory.MODERN
                        "minimal" -> RecommendationCategory.MINIMAL
                        "warm" -> RecommendationCategory.WARM
                        "luxury" -> RecommendationCategory.LUXURY
                        "scandinavian" -> RecommendationCategory.SCANDINAVIAN
                        else -> RecommendationCategory.MODERN
                    }
                )
            )
        }
        
        // 3. Context-Based Adjustments
        
        // Adjust for lighting
        if (context.lightingIntensity < 0.3f) {
            // Low light - suggest lighter colors
            val lightened = ColorTheory.lighten(baseColor.lab, 15f)
            recommendations.add(
                ColorRecommendation(
                    color = lightened.rgb,
                    labColor = lightened,
                    reason = "Lighter shade compensates for low ambient lighting",
                    confidence = 0.70f,
                    category = RecommendationCategory.CONTRAST
                )
            )
        } else if (context.lightingIntensity > 0.7f) {
            // Bright light - can use darker colors
            val darkened = ColorTheory.darken(baseColor.lab, 15f)
            recommendations.add(
                ColorRecommendation(
                    color = darkened.rgb,
                    labColor = darkened,
                    reason = "Darker shade works well with bright lighting",
                    confidence = 0.70f,
                    category = RecommendationCategory.CONTRAST
                )
            )
        }
        
        // Sort by confidence and return top 6
        return recommendations
            .sortedByDescending { it.confidence }
            .take(6)
    }
    
    /**
     * Cloud-based recommendations (placeholder for future implementation)
     */
    private fun cloudRecommend(
        baseColor: ColorInfo,
        context: RoomContext,
        stylePreference: String?
    ): List<ColorRecommendation> {
        // TODO: Implement cloud API call
        // For now, fallback to on-device
        return onDeviceRecommend(baseColor, context, stylePreference)
    }
    
    /**
     * Hybrid recommendations - on-device detection, cloud enhancement
     */
    private fun hybridRecommend(
        baseColor: ColorInfo,
        context: RoomContext,
        stylePreference: String?
    ): List<ColorRecommendation> {
        // Start with on-device recommendations
        val onDeviceResults = onDeviceRecommend(baseColor, context, stylePreference)
        
        // TODO: Enhance with cloud API
        // For now, return on-device results
        return onDeviceResults
    }
    
    /**
     * Filter recommendations by minimum contrast ratio
     */
    fun filterByContrast(
        recommendations: List<ColorRecommendation>,
        backgroundColor: LabColor,
        minRatio: Float = 3.0f
    ): List<ColorRecommendation> {
        return recommendations.filter { rec ->
            ColorTheory.contrastRatio(rec.labColor, backgroundColor) >= minRatio
        }
    }
}
