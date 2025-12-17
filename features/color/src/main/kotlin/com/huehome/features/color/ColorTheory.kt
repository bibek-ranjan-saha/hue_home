package com.huehome.features.color

import com.huehome.core.domain.model.LabColor
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Color theory algorithms for generating harmonious color combinations
 * Uses LAB color space for perceptually accurate results
 */
object ColorTheory {
    
    /**
     * Generate complementary color (180° on color wheel)
     * Provides maximum contrast
     */
    fun complementary(color: LabColor): LabColor {
        // In LAB space, complementary means inverting a and b
        return LabColor(
            l = color.l,
            a = -color.a,
            b = -color.b,
            rgb = LabColor.toRgb(color.l, -color.a, -color.b)
        )
    }
    
    /**
     * Generate analogous colors (±30° on color wheel)
     * Creates harmonious, low-contrast combinations
     */
    fun analogous(color: LabColor, count: Int = 2): List<LabColor> {
        val colors = mutableListOf<LabColor>()
        val angleStep = 30.0 // degrees
        
        for (i in 1..count) {
            val angle = Math.toRadians(angleStep * i)
            
            // Rotate in LAB a-b plane
            val newA = color.a * cos(angle) - color.b * sin(angle)
            val newB = color.a * sin(angle) + color.b * cos(angle)
            
            colors.add(
                LabColor(
                    l = color.l,
                    a = newA.toFloat(),
                    b = newB.toFloat(),
                    rgb = LabColor.toRgb(color.l, newA.toFloat(), newB.toFloat())
                )
            )
        }
        
        return colors
    }
    
    /**
     * Generate triadic colors (120° apart on color wheel)
     * Provides vibrant, balanced combinations
     */
    fun triadic(color: LabColor): List<LabColor> {
        val colors = mutableListOf<LabColor>()
        val angles = listOf(120.0, 240.0)
        
        for (angleDeg in angles) {
            val angle = Math.toRadians(angleDeg)
            
            val newA = color.a * cos(angle) - color.b * sin(angle)
            val newB = color.a * sin(angle) + color.b * cos(angle)
            
            colors.add(
                LabColor(
                    l = color.l,
                    a = newA.toFloat(),
                    b = newB.toFloat(),
                    rgb = LabColor.toRgb(color.l, newA.toFloat(), newB.toFloat())
                )
            )
        }
        
        return colors
    }
    
    /**
     * Generate monochromatic variations (same hue, different lightness)
     * Creates subtle, sophisticated combinations
     */
    fun monochromatic(color: LabColor, count: Int = 3): List<LabColor> {
        val colors = mutableListOf<LabColor>()
        val lightnessRange = 20f
        
        for (i in 1..count) {
            val factor = i.toFloat() / (count + 1)
            val newL = (color.l + lightnessRange * (factor - 0.5f) * 2).coerceIn(0f, 100f)
            
            colors.add(
                LabColor(
                    l = newL,
                    a = color.a,
                    b = color.b,
                    rgb = LabColor.toRgb(newL, color.a, color.b)
                )
            )
        }
        
        return colors
    }
    
    /**
     * Generate split-complementary colors
     * Complementary with slight variation for more options
     */
    fun splitComplementary(color: LabColor): List<LabColor> {
        val colors = mutableListOf<LabColor>()
        val angles = listOf(150.0, 210.0) // ±30° from complementary
        
        for (angleDeg in angles) {
            val angle = Math.toRadians(angleDeg)
            
            val newA = color.a * cos(angle) - color.b * sin(angle)
            val newB = color.a * sin(angle) + color.b * cos(angle)
            
            colors.add(
                LabColor(
                    l = color.l,
                    a = newA.toFloat(),
                    b = newB.toFloat(),
                    rgb = LabColor.toRgb(color.l, newA.toFloat(), newB.toFloat())
                )
            )
        }
        
        return colors
    }
    
    /**
     * Lighten a color by increasing L value
     */
    fun lighten(color: LabColor, amount: Float = 10f): LabColor {
        val newL = (color.l + amount).coerceIn(0f, 100f)
        return LabColor(
            l = newL,
            a = color.a,
            b = color.b,
            rgb = LabColor.toRgb(newL, color.a, color.b)
        )
    }
    
    /**
     * Darken a color by decreasing L value
     */
    fun darken(color: LabColor, amount: Float = 10f): LabColor {
        val newL = (color.l - amount).coerceIn(0f, 100f)
        return LabColor(
            l = newL,
            a = color.a,
            b = color.b,
            rgb = LabColor.toRgb(newL, color.a, color.b)
        )
    }
    
    /**
     * Calculate color contrast ratio (WCAG standard)
     * Returns value between 1 and 21
     */
    fun contrastRatio(color1: LabColor, color2: LabColor): Float {
        val l1 = color1.l / 100f
        val l2 = color2.l / 100f
        
        val lighter = maxOf(l1, l2)
        val darker = minOf(l1, l2)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * Check if two colors have sufficient contrast for readability
     * WCAG AA standard requires 4.5:1 for normal text
     */
    fun hasSufficientContrast(
        color1: LabColor,
        color2: LabColor,
        minRatio: Float = 4.5f
    ): Boolean {
        return contrastRatio(color1, color2) >= minRatio
    }
}
