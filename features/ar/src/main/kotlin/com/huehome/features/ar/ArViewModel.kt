package com.huehome.features.ar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huehome.core.domain.ar.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for AR camera screen
 * Manages AR session state, plane detection, and user interactions
 */
@HiltViewModel
class ArViewModel @Inject constructor(
    private val arSession: ArSession
) : ViewModel() {
    
    private val _arState = MutableStateFlow<ArState>(ArState.Uninitialized)
    val arState: StateFlow<ArState> = _arState.asStateFlow()
    
    private val _detectedPlanes = MutableStateFlow<List<ArPlane>>(emptyList())
    val detectedPlanes: StateFlow<List<ArPlane>> = _detectedPlanes.asStateFlow()
    
    private val _lightEstimate = MutableStateFlow(LightEstimate(0.5f))
    val lightEstimate: StateFlow<LightEstimate> = _lightEstimate.asStateFlow()
    
    private val _selectedPoint = MutableStateFlow<ArHitResult?>(null)
    val selectedPoint: StateFlow<ArHitResult?> = _selectedPoint.asStateFlow()
    
    /**
     * Initialize AR session
     */
    fun initializeAr(context: android.content.Context) {
        viewModelScope.launch {
            try {
                _arState.value = ArState.Initializing
                
                val config = ArConfig(
                    enablePlaneDetection = true,
                    enableDepth = true,
                    enableLightEstimation = true,
                    focusMode = FocusMode.AUTO
                )
                
                arSession.initialize(context, config)
                _arState.value = ArState.Running
                
            } catch (e: ArSessionException) {
                _arState.value = ArState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Update AR frame and detect planes
     */
    fun updateArFrame() {
        viewModelScope.launch {
            if (_arState.value != ArState.Running) return@launch
            
            try {
                // Update frame
                arSession.update()
                
                // Detect planes
                val planes = arSession.detectPlanes()
                _detectedPlanes.value = planes
                
                // Update light estimate
                val light = arSession.estimateLight()
                _lightEstimate.value = light
                
            } catch (e: Exception) {
                _arState.value = ArState.Error(e.message ?: "AR update failed")
            }
        }
    }
    
    /**
     * Handle tap on screen for object selection
     */
    fun handleTap(x: Float, y: Float) {
        viewModelScope.launch {
            if (_arState.value != ArState.Running) return@launch
            
            val hitResult = arSession.performRaycast(x, y)
            _selectedPoint.value = hitResult
        }
    }
    
    /**
     * Pause AR session
     */
    fun pauseAr() {
        arSession.pause()
        _arState.value = ArState.Paused
    }
    
    /**
     * Resume AR session
     */
    fun resumeAr() {
        arSession.resume()
        _arState.value = ArState.Running
    }
    
    /**
     * Release AR resources
     */
    override fun onCleared() {
        super.onCleared()
        arSession.release()
        _arState.value = ArState.Uninitialized
    }
}

/**
 * AR session state
 */
sealed class ArState {
    object Uninitialized : ArState()
    object Initializing : ArState()
    object Running : ArState()
    object Paused : ArState()
    data class Error(val message: String) : ArState()
}
