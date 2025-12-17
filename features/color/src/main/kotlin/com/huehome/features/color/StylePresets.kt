package com.huehome.features.color

import com.huehome.core.domain.model.LabColor

/**
 * Predefined color palettes for different interior design styles
 */
object StylePresets {
    
    /**
     * Modern style - Clean, neutral with bold accents
     */
    val modern = listOf(
        0xFFFFFFFF.toInt(), // Pure White
        0xFF2C3E50.toInt(), // Dark Blue Gray
        0xFFECF0F1.toInt(), // Light Gray
        0xFF3498DB.toInt(), // Bright Blue
        0xFFE74C3C.toInt(), // Red Accent
        0xFF95A5A6.toInt()  // Medium Gray
    )
    
    /**
     * Minimal style - Monochromatic, subtle variations
     */
    val minimal = listOf(
        0xFFFAFAFA.toInt(), // Off White
        0xFFF5F5F5.toInt(), // Light Gray
        0xFFEEEEEE.toInt(), // Lighter Gray
        0xFFBDBDBD.toInt(), // Medium Gray
        0xFF757575.toInt(), // Dark Gray
        0xFF424242.toInt()  // Charcoal
    )
    
    /**
     * Warm style - Earthy, cozy tones
     */
    val warm = listOf(
        0xFFFFF8E1.toInt(), // Cream
        0xFFFFE0B2.toInt(), // Light Peach
        0xFFD7CCC8.toInt(), // Warm Beige
        0xFFBCAAA4.toInt(), // Taupe
        0xFF8D6E63.toInt(), // Brown
        0xFFFF8A65.toInt()  // Coral
    )
    
    /**
     * Luxury style - Rich, sophisticated colors
     */
    val luxury = listOf(
        0xFF1A1A2E.toInt(), // Deep Navy
        0xFF16213E.toInt(), // Dark Blue
        0xFFD4AF37.toInt(), // Gold
        0xFF2C3E50.toInt(), // Slate
        0xFF8B4513.toInt(), // Saddle Brown
        0xFFFFFFFF.toInt()  // Pure White
    )
    
    /**
     * Scandinavian style - Light, airy with natural accents
     */
    val scandinavian = listOf(
        0xFFFFFFFFF.toInt(), // White
        0xFFF5F5DC.toInt(), // Beige
        0xFFD3D3D3.toInt(), // Light Gray
        0xFF8B7355.toInt(), // Natural Wood
        0xFF2F4F4F.toInt(), // Dark Slate Gray
        0xFFB0C4DE.toInt()  // Light Steel Blue
    )
    
    /**
     * Get palette by style name
     */
    fun getPalette(style: String): List<Int> {
        return when (style.lowercase()) {
            "modern" -> modern
            "minimal" -> minimal
            "warm" -> warm
            "luxury" -> luxury
            "scandinavian" -> scandinavian
            else -> modern
        }
    }
    
    /**
     * Get all available styles
     */
    fun getAllStyles(): Map<String, List<Int>> {
        return mapOf(
            "Modern" to modern,
            "Minimal" to minimal,
            "Warm" to warm,
            "Luxury" to luxury,
            "Scandinavian" to scandinavian
        )
    }
    
    /**
     * Find closest color in palette to given color
     */
    fun findClosestInPalette(color: LabColor, palette: List<Int>): Int {
        var closestColor = palette.first()
        var minDistance = Float.MAX_VALUE
        
        palette.forEach { paletteRgb ->
            val paletteColor = LabColor.fromRgb(paletteRgb)
            val distance = colorDistance(color, paletteColor)
            
            if (distance < minDistance) {
                minDistance = distance
                closestColor = paletteRgb
            }
        }
        
        return closestColor
    }
    
    /**
     * Calculate perceptual distance between two colors in LAB space
     */
    private fun colorDistance(color1: LabColor, color2: LabColor): Float {
        val dL = color1.l - color2.l
        val dA = color1.a - color2.a
        val dB = color1.b - color2.b
        
        return kotlin.math.sqrt(dL * dL + dA * dA + dB * dB)
    }
}
