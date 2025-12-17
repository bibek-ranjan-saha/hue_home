package com.huehome.features.detection

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ViewModel for object detection
 * Manages segmentation and detection state
 */
@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val segmentationEngine: SegmentationEngine,
    private val objectDetector: ObjectDetector
) : ViewModel() {
    
    private val _detectionState = MutableStateFlow<DetectionState>(DetectionState.Idle)
    val detectionState: StateFlow<DetectionState> = _detectionState.asStateFlow()
    
    private val _detectedObjects = MutableStateFlow<List<DetectedObject>>(emptyList())
    val detectedObjects: StateFlow<List<DetectedObject>> = _detectedObjects.asStateFlow()
    
    private var isInitialized = false
    
    /**
     * Initialize segmentation engine
     */
    fun initialize() {
        if (isInitialized) return
        
        viewModelScope.launch {
            try {
                _detectionState.value = DetectionState.Initializing
                
                withContext(Dispatchers.IO) {
                    segmentationEngine.initialize()
                }
                
                isInitialized = true
                _detectionState.value = DetectionState.Ready
                
            } catch (e: SegmentationException) {
                _detectionState.value = DetectionState.Error(e.message ?: "Initialization failed")
            }
        }
    }
    
    /**
     * Detect objects in frame
     */
    fun detectObjects(frame: Bitmap) {
        if (!isInitialized) {
            _detectionState.value = DetectionState.Error("Engine not initialized")
            return
        }
        
        viewModelScope.launch {
            try {
                _detectionState.value = DetectionState.Processing
                
                // Run segmentation and detection on IO thread
                val objects = withContext(Dispatchers.IO) {
                    val segmentationResult = segmentationEngine.segment(frame)
                    objectDetector.detectObjects(frame, segmentationResult)
                }
                
                _detectedObjects.value = objects
                _detectionState.value = DetectionState.Success(objects.size)
                
            } catch (e: SegmentationException) {
                _detectionState.value = DetectionState.Error(e.message ?: "Detection failed")
            }
        }
    }
    
    /**
     * Clear detected objects
     */
    fun clearDetections() {
        _detectedObjects.value = emptyList()
        _detectionState.value = DetectionState.Ready
    }
    
    /**
     * Release resources
     */
    override fun onCleared() {
        super.onCleared()
        segmentationEngine.release()
    }
}

/**
 * Detection state
 */
sealed class DetectionState {
    object Idle : DetectionState()
    object Initializing : DetectionState()
    object Ready : DetectionState()
    object Processing : DetectionState()
    data class Success(val objectCount: Int) : DetectionState()
    data class Error(val message: String) : DetectionState()
}
