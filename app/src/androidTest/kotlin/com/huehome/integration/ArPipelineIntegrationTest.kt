package com.huehome.integration

import android.graphics.Bitmap
import com.huehome.core.domain.model.*
import com.huehome.features.color.ColorDetector
import com.huehome.features.color.RecommendationEngine
import com.huehome.features.detection.ObjectDetector
import com.huehome.features.detection.SegmentationEngine
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Integration tests for the complete AR pipeline
 */
class ArPipelineIntegrationTest {
    
    private lateinit var segmentationEngine: SegmentationEngine
    private lateinit var objectDetector: ObjectDetector
    private lateinit var colorDetector: ColorDetector
    private lateinit var recommendationEngine: RecommendationEngine
    
    @Before
    fun setup() {
        segmentationEngine = mockk()
        objectDetector = mockk()
        colorDetector = mockk()
        recommendationEngine = RecommendationEngine() // Real instance
    }
    
    @Test
    fun `complete pipeline should process frame to recommendations`() = runTest {
        // Given: A test frame
        val testFrame = mockk<Bitmap>(relaxed = true)
        val testMask = mockk<Bitmap>(relaxed = true)
        
        // Mock segmentation result
        val segmentationResult = mockk<com.huehome.features.detection.SegmentationResult> {
            every { masks } returns listOf(
                mockk {
                    every { className } returns "Wall"
                    every { classId } returns 15
                    every { mask } returns testMask
                    every { pixelCount } returns 10000
                    every { confidence } returns 0.9f
                }
            )
        }
        
        // Mock detected object
        val detectedObject = mockk<com.huehome.features.detection.DetectedObject> {
            every { id } returns "wall-1"
            every { type } returns ObjectType.WALL
            every { mask } returns testMask
            every { confidence } returns 0.9f
        }
        
        // Mock detected color
        val detectedColor = ColorInfo(
            rgb = 0xFFE0E0E0.toInt(),
            lab = LabColor(l = 88f, a = 0f, b = 0f),
            confidence = 0.85f
        )
        
        every { segmentationEngine.segment(testFrame) } returns segmentationResult
        every { objectDetector.detectObjects(testFrame, segmentationResult) } returns listOf(detectedObject)
        every { colorDetector.detectBaseColor(testFrame, testMask) } returns detectedColor
        
        // When: Process through pipeline
        val segResult = segmentationEngine.segment(testFrame)
        val objects = objectDetector.detectObjects(testFrame, segResult)
        val color = colorDetector.detectBaseColor(testFrame, objects.first().mask)
        
        val roomContext = RoomContext(
            lightingIntensity = 0.5f,
            lightingColor = floatArrayOf(1f, 1f, 1f, 1f)
        )
        
        val recommendations = recommendationEngine.recommend(color, roomContext)
        
        // Then: Should have complete pipeline results
        assertEquals(1, objects.size)
        assertEquals(ObjectType.WALL, objects.first().type)
        assertEquals(0xFFE0E0E0.toInt(), color.rgb)
        assertEquals(6, recommendations.size)
        assertTrue(recommendations.all { it.confidence > 0f })
    }
    
    @Test
    fun `pipeline should handle multiple objects`() = runTest {
        val testFrame = mockk<Bitmap>(relaxed = true)
        val wallMask = mockk<Bitmap>(relaxed = true)
        val doorMask = mockk<Bitmap>(relaxed = true)
        
        val segmentationResult = mockk<com.huehome.features.detection.SegmentationResult> {
            every { masks } returns listOf(
                mockk {
                    every { className } returns "Wall"
                    every { mask } returns wallMask
                    every { confidence } returns 0.9f
                },
                mockk {
                    every { className } returns "Door"
                    every { mask } returns doorMask
                    every { confidence } returns 0.85f
                }
            )
        }
        
        val detectedObjects = listOf(
            mockk<com.huehome.features.detection.DetectedObject> {
                every { type } returns ObjectType.WALL
            },
            mockk {
                every { type } returns ObjectType.DOOR
            }
        )
        
        every { segmentationEngine.segment(testFrame) } returns segmentationResult
        every { objectDetector.detectObjects(testFrame, segmentationResult) } returns detectedObjects
        
        val segResult = segmentationEngine.segment(testFrame)
        val objects = objectDetector.detectObjects(testFrame, segResult)
        
        assertEquals(2, objects.size)
        assertTrue(objects.any { it.type == ObjectType.WALL })
        assertTrue(objects.any { it.type == ObjectType.DOOR })
    }
    
    @Test
    fun `recommendations should adapt to lighting conditions`() {
        val baseColor = ColorInfo(
            rgb = 0xFF808080.toInt(),
            lab = LabColor(l = 50f, a = 0f, b = 0f),
            confidence = 0.9f
        )
        
        // Low light context
        val lowLightContext = RoomContext(
            lightingIntensity = 0.2f,
            lightingColor = floatArrayOf(1f, 1f, 1f, 1f)
        )
        
        // Bright light context
        val brightLightContext = RoomContext(
            lightingIntensity = 0.8f,
            lightingColor = floatArrayOf(1f, 1f, 1f, 1f)
        )
        
        val lowLightRecs = recommendationEngine.recommend(baseColor, lowLightContext)
        val brightLightRecs = recommendationEngine.recommend(baseColor, brightLightContext)
        
        // Both should return recommendations
        assertEquals(6, lowLightRecs.size)
        assertEquals(6, brightLightRecs.size)
        
        // Low light should have lighter colors
        val avgLowLight = lowLightRecs.map { it.labColor.l }.average()
        val avgBrightLight = brightLightRecs.map { it.labColor.l }.average()
        
        // This is a heuristic test - low light recommendations should tend lighter
        assertTrue(avgLowLight >= avgBrightLight - 10f)
    }
}
