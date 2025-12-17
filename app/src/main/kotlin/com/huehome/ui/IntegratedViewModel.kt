package com.huehome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huehome.core.domain.model.ColorRecommendation
import com.huehome.core.domain.model.RoomContext
import com.huehome.core.domain.model.SceneObject
import com.huehome.features.ar.ArViewModel
import com.huehome.features.detection.DetectionViewModel
import com.huehome.features.rendering.RenderingViewModel
import com.huehome.ui.color.ColorPaletteViewModel
import com.huehome.ui.selection.ObjectSelectionViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Integrated ViewModel that coordinates all feature ViewModels
 * Note: Not annotated with @HiltViewModel to avoid injection conflicts
 */
class IntegratedViewModel @Inject constructor(
    val arViewModel: ArViewModel,
    val detectionViewModel: DetectionViewModel,
    val colorPaletteViewModel: ColorPaletteViewModel,
    val objectSelectionViewModel: ObjectSelectionViewModel,
    val renderingViewModel: RenderingViewModel
) : ViewModel() {
    
    // Combined app state
    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    
    // Frame processing control
    private var frameCounter = 0
    private val processEveryNFrames = 5 // Process every 5th frame for performance
    
    init {
        // Observe AR state
        viewModelScope.launch {
            arViewModel.arState.collect { arState ->
                _appState.update { it.copy(arState = arState) }
            }
        }
        
        // Observe detected objects
        viewModelScope.launch {
            objectSelectionViewModel.objects.collect { objects ->
                _appState.update { it.copy(detectedObjects = objects) }
            }
        }
        
        // Observe selected object
        viewModelScope.launch {
            objectSelectionViewModel.selectedObject.collect { obj ->
                _appState.update { it.copy(selectedObject = obj) }
                
                // Load recommendations when object is selected
                obj?.let { loadRecommendationsForObject(it) }
            }
        }
        
        // Observe recommendations
        viewModelScope.launch {
            colorPaletteViewModel.recommendations.collect { recs ->
                _appState.update { it.copy(recommendations = recs) }
            }
        }
        
        // Observe selected color
        viewModelScope.launch {
            colorPaletteViewModel.selectedColor.collect { color ->
                _appState.update { it.copy(selectedColor = color) }
            }
        }
        
        // Observe preview mode
        viewModelScope.launch {
            colorPaletteViewModel.isPreviewMode.collect { isPreview ->
                _appState.update { it.copy(isPreviewMode = isPreview) }
            }
        }
    }
    
    /**
     * Initialize all systems
     */
    fun initialize() {
        // TODO: Add initialize() method to ArViewModel
        // arViewModel.initialize()
        // TODO: Add initialize() method to DetectionViewModel
        // detectionViewModel.initialize()
        renderingViewModel.initialize()
    }
    
    /**
     * Process AR frame
     * Called on every frame update
     */
    fun processFrame(frame: android.graphics.Bitmap) {
        frameCounter++
        
        // Only process every Nth frame for performance
        if (frameCounter % processEveryNFrames == 0) {
            detectionViewModel.detectObjects(frame)
        }
    }
    
    /**
     * Load color recommendations for selected object
     */
    private fun loadRecommendationsForObject(obj: SceneObject) {
        viewModelScope.launch {
            // Get current light estimate from AR
            val lightEstimate = arViewModel.lightEstimate.value
            
            // Create room context
            val roomContext = RoomContext(
                lightingIntensity = lightEstimate.pixelIntensity,
                lightingColor = lightEstimate.colorCorrection
            )
            
            // Load recommendations
            colorPaletteViewModel.loadRecommendations(
                sceneObject = obj,
                roomContext = roomContext,
                stylePreference = "Modern" // TODO: Get from user preferences
            )
        }
    }
    
    /**
     * Apply selected color to object
     */
    fun applyColor() {
        val obj = _appState.value.selectedObject ?: return
        val color = _appState.value.selectedColor ?: return
        val lightEstimate = arViewModel.lightEstimate.value
        
        viewModelScope.launch {
            // Apply through color palette VM (handles persistence)
            colorPaletteViewModel.applyColor()
            
            // TODO: Trigger rendering
            // renderingViewModel.applyColor(obj, frame, mask, color.labColor, lightEstimate.pixelIntensity)
        }
    }
    
    /**
     * Handle tap on AR view
     */
    fun onArTap(x: Float, y: Float) {
        arViewModel.handleTap(x, y)
        
        // TODO: Check if tap hit a detected object
        // If yes, select it in objectSelectionViewModel
    }
    
    /**
     * Cleanup
     */
    override fun onCleared() {
        super.onCleared()
        // ViewModels handle their own cleanup
    }
}

/**
 * Combined application state
 */
data class AppState(
    val arState: com.huehome.features.ar.ArState = com.huehome.features.ar.ArState.Uninitialized,
    val detectedObjects: List<SceneObject> = emptyList(),
    val selectedObject: SceneObject? = null,
    val recommendations: List<ColorRecommendation> = emptyList(),
    val selectedColor: ColorRecommendation? = null,
    val isPreviewMode: Boolean = false
)
