package com.huehome.features.rendering

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huehome.core.domain.model.LabColor
import com.huehome.core.domain.model.SceneObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for rendering colored objects
 */
@HiltViewModel
class RenderingViewModel @Inject constructor(
    private val renderingEngine: ColorRenderingEngine,
    private val textureManager: TextureManager
) : ViewModel() {
    
    private val _renderingState = MutableStateFlow<RenderingState>(RenderingState.Idle)
    val renderingState: StateFlow<RenderingState> = _renderingState.asStateFlow()
    
    private var isInitialized = false
    
    /**
     * Initialize rendering engine
     */
    fun initialize() {
        if (isInitialized) return
        
        viewModelScope.launch {
            try {
                _renderingState.value = RenderingState.Initializing
                
                withContext(Dispatchers.Main) {
                    renderingEngine.initialize()
                }
                
                isInitialized = true
                _renderingState.value = RenderingState.Ready
                
            } catch (e: RenderingException) {
                _renderingState.value = RenderingState.Error(e.message ?: "Initialization failed")
            }
        }
    }
    
    /**
     * Apply color to object
     */
    fun applyColor(
        sceneObject: SceneObject,
        frame: Bitmap,
        mask: Bitmap,
        targetColor: LabColor,
        lightIntensity: Float,
        blendFactor: Float = 1.0f
    ) {
        if (!isInitialized) {
            _renderingState.value = RenderingState.Error("Engine not initialized")
            return
        }
        
        viewModelScope.launch {
            try {
                _renderingState.value = RenderingState.Rendering
                
                withContext(Dispatchers.Main) {
                    // Create textures
                    val frameTexture = textureManager.createTexture(frame, "frame_${sceneObject.id}")
                    val maskTexture = textureManager.createTexture(mask, "mask_${sceneObject.id}")
                    
                    // Render colored object
                    renderingEngine.renderColoredObject(
                        texture = frameTexture,
                        mask = maskTexture,
                        originalColor = sceneObject.detectedColor.lab,
                        targetColor = targetColor,
                        lightIntensity = lightIntensity,
                        blendFactor = blendFactor
                    )
                }
                
                _renderingState.value = RenderingState.Success
                
            } catch (e: RenderingException) {
                _renderingState.value = RenderingState.Error(e.message ?: "Rendering failed")
            }
        }
    }
    
    /**
     * Clear textures for object
     */
    fun clearObject(objectId: String) {
        textureManager.deleteTexture("frame_$objectId")
        textureManager.deleteTexture("mask_$objectId")
    }
    
    /**
     * Release resources
     */
    override fun onCleared() {
        super.onCleared()
        textureManager.clearAll()
        renderingEngine.release()
    }
}

/**
 * Rendering state
 */
sealed class RenderingState {
    object Idle : RenderingState()
    object Initializing : RenderingState()
    object Ready : RenderingState()
    object Rendering : RenderingState()
    object Success : RenderingState()
    data class Error(val message: String) : RenderingState()
}
