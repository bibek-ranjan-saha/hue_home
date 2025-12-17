package com.huehome.features.color

import com.huehome.core.domain.model.*
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RecommendationEngine
 */
class RecommendationEngineTest {
    
    private lateinit var engine: RecommendationEngine
    
    @Before
    fun setup() {
        engine = RecommendationEngine()
    }
    
    @Test
    fun `on-device recommendations should return 6 colors`() {
        val baseColor = ColorInfo(
            rgb = 0xFF0000FF.toInt(),
            lab = LabColor(l = 53.23f, a = 80.11f, b = 67.22f),
            confidence = 0.9f
        )
        
        val context = RoomContext(
            lightingIntensity = 0.5f,
            lightingColor = floatArrayOf(1f, 1f, 1f, 1f)
        )
        
        val recommendations = engine.recommend(
            baseColor = baseColor,
            context = context,
            mode = ProcessingMode.ON_DEVICE
        )
        
        assertEquals(6, recommendations.size)
        assertTrue(recommendations.all { it.confidence > 0f })
    }
    
    @Test
    fun `recommendations should include complementary color`() {
        val baseColor = ColorInfo(
            rgb = 0xFF0000FF.toInt(),
            lab = LabColor(l = 53.23f, a = 80.11f, b = 67.22f),
            confidence = 0.9f
        )
        
        val context = RoomContext(
            lightingIntensity = 0.5f,
            lightingColor = floatArrayOf(1f, 1f, 1f, 1f)
        )
        
        val recommendations = engine.recommend(baseColor, context)
        
        val hasComplementary = recommendations.any { 
            it.category == RecommendationCategory.COMPLEMENTARY 
        }
        assertTrue(hasComplementary)
    }
    
    @Test
    fun `style preference should influence recommendations`() {
        val baseColor = ColorInfo(
            rgb = 0xFF0000FF.toInt(),
            lab = LabColor(l = 53.23f, a = 80.11f, b = 67.22f),
            confidence = 0.9f
        )
        
        val context = RoomContext(
            lightingIntensity = 0.5f,
            lightingColor = floatArrayOf(1f, 1f, 1f, 1f)
        )
        
        val recommendations = engine.recommend(
            baseColor = baseColor,
            context = context,
            stylePreference = "Modern"
        )
        
        val hasStyleRec = recommendations.any { 
            it.category == RecommendationCategory.MODERN 
        }
        assertTrue(hasStyleRec)
    }
    
    @Test
    fun `low lighting should suggest lighter colors`() {
        val baseColor = ColorInfo(
            rgb = 0xFF0000FF.toInt(),
            lab = LabColor(l = 30f, a = 80.11f, b = 67.22f),
            confidence = 0.9f
        )
        
        val lowLightContext = RoomContext(
            lightingIntensity = 0.2f,
            lightingColor = floatArrayOf(1f, 1f, 1f, 1f)
        )
        
        val recommendations = engine.recommend(baseColor, lowLightContext)
        
        // Should have at least one lighter color recommendation
        val hasLighterColor = recommendations.any { rec ->
            rec.labColor.l > baseColor.lab.l
        }
        assertTrue(hasLighterColor)
    }
    
    @Test
    fun `filter by contrast should remove low contrast colors`() {
        val recommendations = listOf(
            ColorRecommendation(
                color = 0xFFFFFFFF.toInt(),
                labColor = LabColor(l = 100f, a = 0f, b = 0f),
                reason = "High contrast",
                confidence = 0.9f,
                category = RecommendationCategory.CONTRAST
            ),
            ColorRecommendation(
                color = 0xFFF0F0F0.toInt(),
                labColor = LabColor(l = 95f, a = 0f, b = 0f),
                reason = "Low contrast",
                confidence = 0.8f,
                category = RecommendationCategory.MONOCHROMATIC
            )
        )
        
        val background = LabColor(l = 90f, a = 0f, b = 0f)
        
        val filtered = engine.filterByContrast(
            recommendations = recommendations,
            backgroundColor = background,
            minRatio = 3.0f
        )
        
        assertTrue(filtered.size < recommendations.size)
    }
}
