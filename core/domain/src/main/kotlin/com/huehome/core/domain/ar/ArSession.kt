package com.huehome.core.domain.ar

import android.content.Context

/**
 * AR abstraction layer - XR-ready architecture
 * This interface abstracts AR functionality to support multiple platforms:
 * - ARCore (mobile) - MVP
 * - OpenXR (headsets) - v3/v4
 * - Android XR - v3/v4
 */
interface ArSession {
    /**
     * Initialize AR session with configuration
     */
    fun initialize(context: Context, config: ArConfig)
    
    /**
     * Update AR session and get current frame
     * Should be called every frame
     */
    fun update(): ArFrame?
    
    /**
     * Detect planes in the environment
     */
    fun detectPlanes(): List<ArPlane>
    
    /**
     * Estimate environmental lighting
     */
    fun estimateLight(): LightEstimate
    
    /**
     * Get depth data if available
     */
    fun getDepthData(): DepthData?
    
    /**
     * Perform raycast from screen coordinates
     * Used for tap-to-select functionality
     */
    fun performRaycast(x: Float, y: Float): ArHitResult?
    
    /**
     * Pause AR session
     */
    fun pause()
    
    /**
     * Resume AR session
     */
    fun resume()
    
    /**
     * Release AR resources
     */
    fun release()
}

/**
 * AR configuration
 */
data class ArConfig(
    /** Enable plane detection */
    val enablePlaneDetection: Boolean = true,
    
    /** Enable depth */
    val enableDepth: Boolean = true,
    
    /** Enable light estimation */
    val enableLightEstimation: Boolean = true,
    
    /** Focus mode */
    val focusMode: FocusMode = FocusMode.AUTO
)

enum class FocusMode {
    AUTO,
    FIXED
}

/**
 * AR frame data
 */
data class ArFrame(
    /** Frame timestamp */
    val timestamp: Long,
    
    /** Camera pose */
    val cameraPose: Pose,
    
    /** Frame image data */
    val imageData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArFrame

        if (timestamp != other.timestamp) return false
        if (cameraPose != other.cameraPose) return false
        if (imageData != null) {
            if (other.imageData == null) return false
            if (!imageData.contentEquals(other.imageData)) return false
        } else if (other.imageData != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + cameraPose.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * Detected plane
 */
data class ArPlane(
    val id: String,
    val type: PlaneType,
    val centerPose: Pose,
    val extentX: Float,
    val extentZ: Float
)

enum class PlaneType {
    HORIZONTAL_UPWARD,
    HORIZONTAL_DOWNWARD,
    VERTICAL
}

/**
 * 3D pose (position + rotation)
 */
data class Pose(
    val tx: Float,
    val ty: Float,
    val tz: Float,
    val qx: Float,
    val qy: Float,
    val qz: Float,
    val qw: Float
)

/**
 * Light estimation data
 */
data class LightEstimate(
    /** Pixel intensity (0.0 - 1.0) */
    val pixelIntensity: Float,
    
    /** Color correction RGBA */
    val colorCorrection: FloatArray = floatArrayOf(1f, 1f, 1f, 1f)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LightEstimate

        if (pixelIntensity != other.pixelIntensity) return false
        if (!colorCorrection.contentEquals(other.colorCorrection)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pixelIntensity.hashCode()
        result = 31 * result + colorCorrection.contentHashCode()
        return result
    }
}

/**
 * Depth data
 */
data class DepthData(
    val width: Int,
    val height: Int,
    val data: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DepthData

        if (width != other.width) return false
        if (height != other.height) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + data.contentHashCode()
        return result
    }
}

/**
 * Raycast hit result
 */
data class ArHitResult(
    val hitPose: Pose,
    val distance: Float,
    val plane: ArPlane?
)
