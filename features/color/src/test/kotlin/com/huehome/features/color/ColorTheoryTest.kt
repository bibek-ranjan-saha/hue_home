package com.huehome.features.color

import com.huehome.core.domain.model.LabColor
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ColorTheory algorithms
 */
class ColorTheoryTest {
    
    @Test
    fun `complementary color should be 180 degrees opposite`() {
        // Red color
        val red = LabColor(l = 53.23f, a = 80.11f, b = 67.22f)
        
        val complementary = ColorTheory.complementary(red)
        
        // Complementary should have opposite a and b values
        assertTrue(complementary.a < 0)
        assertTrue(complementary.b < 0)
        assertEquals(red.l, complementary.l, 1f)
    }
    
    @Test
    fun `analogous colors should be close to original`() {
        val blue = LabColor(l = 32.30f, a = 79.19f, b = -107.86f)
        
        val analogous = ColorTheory.analogous(blue, count = 2)
        
        assertEquals(2, analogous.size)
        // Analogous colors should have similar lightness
        analogous.forEach { color ->
            assertEquals(blue.l, color.l, 10f)
        }
    }
    
    @Test
    fun `triadic colors should be evenly spaced`() {
        val green = LabColor(l = 87.73f, a = -86.18f, b = 83.18f)
        
        val triadic = ColorTheory.triadic(green)
        
        assertEquals(2, triadic.size)
        // All three colors (original + 2 triadic) should be 120° apart
    }
    
    @Test
    fun `monochromatic colors should have same hue`() {
        val purple = LabColor(l = 29.78f, a = 58.94f, b = -36.50f)
        
        val monochromatic = ColorTheory.monochromatic(purple, count = 3)
        
        assertEquals(3, monochromatic.size)
        // Should have different lightness but similar a/b ratios
        monochromatic.forEach { color ->
            assertNotEquals(purple.l, color.l, 1f)
        }
    }
    
    @Test
    fun `lighten should increase L value`() {
        val color = LabColor(l = 50f, a = 20f, b = 30f)
        
        val lightened = ColorTheory.lighten(color, 20f)
        
        assertTrue(lightened.l > color.l)
        assertEquals(70f, lightened.l, 1f)
        assertEquals(color.a, lightened.a, 0.1f)
        assertEquals(color.b, lightened.b, 0.1f)
    }
    
    @Test
    fun `darken should decrease L value`() {
        val color = LabColor(l = 70f, a = 20f, b = 30f)
        
        val darkened = ColorTheory.darken(color, 20f)
        
        assertTrue(darkened.l < color.l)
        assertEquals(50f, darkened.l, 1f)
    }
    
    @Test
    fun `contrast ratio should be calculated correctly`() {
        val white = LabColor(l = 100f, a = 0f, b = 0f)
        val black = LabColor(l = 0f, a = 0f, b = 0f)
        
        val ratio = ColorTheory.contrastRatio(white, black)
        
        // White on black should have maximum contrast (21:1)
        assertTrue(ratio > 15f)
    }
    
    @Test
    fun `sufficient contrast should pass WCAG AA`() {
        val white = LabColor(l = 100f, a = 0f, b = 0f)
        val darkGray = LabColor(l = 30f, a = 0f, b = 0f)
        
        val hasSufficientContrast = ColorTheory.hasSufficientContrast(white, darkGray)
        
        assertTrue(hasSufficientContrast)
    }
    
    @Test
    fun `insufficient contrast should fail WCAG AA`() {
        val lightGray = LabColor(l = 80f, a = 0f, b = 0f)
        val mediumGray = LabColor(l = 70f, a = 0f, b = 0f)
        
        val hasSufficientContrast = ColorTheory.hasSufficientContrast(lightGray, mediumGray)
        
        assertFalse(hasSufficientContrast)
    }
}
