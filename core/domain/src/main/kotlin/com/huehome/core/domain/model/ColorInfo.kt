package com.huehome.core.domain.model

/**
 * Color information in LAB color space
 * LAB is perceptually uniform and better for color manipulation
 * 
 * @param l Lightness (0-100)
 * @param a Green-Red axis (-128 to 127)
 * @param b Blue-Yellow axis (-128 to 127)
 * @param rgb Original RGB value (ARGB Int)
 */
data class LabColor(
    val l: Float,
    val a: Float,
    val b: Float,
    val rgb: Int
) {
    companion object {
        /**
         * Convert RGB to LAB color space
         * Uses D65 illuminant and 2° observer
         */
        fun fromRgb(rgb: Int): LabColor {
            // Extract RGB components
            val r = ((rgb shr 16) and 0xFF) / 255.0
            val g = ((rgb shr 8) and 0xFF) / 255.0
            val b = (rgb and 0xFF) / 255.0
            
            // Convert to linear RGB
            val rLinear = if (r > 0.04045) Math.pow((r + 0.055) / 1.055, 2.4) else r / 12.92
            val gLinear = if (g > 0.04045) Math.pow((g + 0.055) / 1.055, 2.4) else g / 12.92
            val bLinear = if (b > 0.04045) Math.pow((b + 0.055) / 1.055, 2.4) else b / 12.92
            
            // Convert to XYZ (D65 illuminant)
            val x = rLinear * 0.4124564 + gLinear * 0.3575761 + bLinear * 0.1804375
            val y = rLinear * 0.2126729 + gLinear * 0.7151522 + bLinear * 0.0721750
            val z = rLinear * 0.0193339 + gLinear * 0.1191920 + bLinear * 0.9503041
            
            // Normalize for D65 white point
            val xn = x / 0.95047
            val yn = y / 1.00000
            val zn = z / 1.08883
            
            // Convert to LAB
            val fx = if (xn > 0.008856) Math.pow(xn, 1.0 / 3.0) else (7.787 * xn + 16.0 / 116.0)
            val fy = if (yn > 0.008856) Math.pow(yn, 1.0 / 3.0) else (7.787 * yn + 16.0 / 116.0)
            val fz = if (zn > 0.008856) Math.pow(zn, 1.0 / 3.0) else (7.787 * zn + 16.0 / 116.0)
            
            val l = (116.0 * fy - 16.0).toFloat()
            val a = (500.0 * (fx - fy)).toFloat()
            val bValue = (200.0 * (fy - fz)).toFloat()
            
            return LabColor(l, a, bValue, rgb)
        }
        
        /**
         * Convert LAB to RGB color space
         */
        fun toRgb(l: Float, a: Float, b: Float): Int {
            // Convert LAB to XYZ
            val fy = (l + 16.0) / 116.0
            val fx = a / 500.0 + fy
            val fz = fy - b / 200.0
            
            val xn = if (Math.pow(fx, 3.0) > 0.008856) Math.pow(fx, 3.0) else (fx - 16.0 / 116.0) / 7.787
            val yn = if (Math.pow(fy, 3.0) > 0.008856) Math.pow(fy, 3.0) else (fy - 16.0 / 116.0) / 7.787
            val zn = if (Math.pow(fz, 3.0) > 0.008856) Math.pow(fz, 3.0) else (fz - 16.0 / 116.0) / 7.787
            
            // Denormalize for D65 white point
            val x = xn * 0.95047
            val y = yn * 1.00000
            val z = zn * 1.08883
            
            // Convert to linear RGB
            val rLinear = x * 3.2404542 + y * -1.5371385 + z * -0.4985314
            val gLinear = x * -0.9692660 + y * 1.8760108 + z * 0.0415560
            val bLinear = x * 0.0556434 + y * -0.2040259 + z * 1.0572252
            
            // Convert to sRGB
            val r = if (rLinear > 0.0031308) 1.055 * Math.pow(rLinear, 1.0 / 2.4) - 0.055 else 12.92 * rLinear
            val g = if (gLinear > 0.0031308) 1.055 * Math.pow(gLinear, 1.0 / 2.4) - 0.055 else 12.92 * gLinear
            val bValue = if (bLinear > 0.0031308) 1.055 * Math.pow(bLinear, 1.0 / 2.4) - 0.055 else 12.92 * bLinear
            
            // Clamp and convert to int
            val rInt = (r.coerceIn(0.0, 1.0) * 255).toInt()
            val gInt = (g.coerceIn(0.0, 1.0) * 255).toInt()
            val bInt = (bValue.coerceIn(0.0, 1.0) * 255).toInt()
            
            return (0xFF shl 24) or (rInt shl 16) or (gInt shl 8) or bInt
        }
    }
    
    /** Convert this LAB color back to RGB */
    fun toRgb(): Int = toRgb(l, a, b)
}

/**
 * Color information detected from an object
 */
data class ColorInfo(
    val rgb: Int,
    val lab: LabColor,
    val confidence: Float
)
