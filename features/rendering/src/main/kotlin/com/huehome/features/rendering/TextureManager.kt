package com.huehome.features.rendering

import android.graphics.Bitmap
import android.opengl.GLES30
import android.opengl.GLUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages OpenGL textures for rendering
 */
@Singleton
class TextureManager @Inject constructor() {
    
    private val textureCache = mutableMapOf<String, Int>()
    
    /**
     * Create texture from bitmap
     */
    fun createTexture(bitmap: Bitmap, key: String? = null): Int {
        val textureIds = IntArray(1)
        GLES30.glGenTextures(1, textureIds, 0)
        
        val textureId = textureIds[0]
        
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
        
        // Set texture parameters
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        
        // Upload bitmap to texture
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        
        // Cache if key provided
        if (key != null) {
            textureCache[key] = textureId
        }
        
        return textureId
    }
    
    /**
     * Get cached texture
     */
    fun getTexture(key: String): Int? {
        return textureCache[key]
    }
    
    /**
     * Delete texture
     */
    fun deleteTexture(textureId: Int) {
        GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
        
        // Remove from cache
        textureCache.entries.removeIf { it.value == textureId }
    }
    
    /**
     * Delete texture by key
     */
    fun deleteTexture(key: String) {
        textureCache[key]?.let { textureId ->
            deleteTexture(textureId)
        }
    }
    
    /**
     * Clear all textures
     */
    fun clearAll() {
        textureCache.values.forEach { textureId ->
            GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
        }
        textureCache.clear()
    }
}
