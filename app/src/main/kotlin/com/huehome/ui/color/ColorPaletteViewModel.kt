package com.huehome.ui.color

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huehome.core.data.repository.SceneRepository
import com.huehome.core.domain.model.ColorInfo
import com.huehome.core.domain.model.ColorRecommendation
import com.huehome.core.domain.model.RoomContext
import com.huehome.core.domain.model.SceneObject
import com.huehome.features.color.RecommendationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for color palette UI
 */
@HiltViewModel
class ColorPaletteViewModel @Inject constructor(
    private val recommendationEngine: RecommendationEngine,
    private val sceneRepository: SceneRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<ColorPaletteUiState>(ColorPaletteUiState.Idle)
    val uiState: StateFlow<ColorPaletteUiState> = _uiState.asStateFlow()
    
    private val _recommendations = MutableStateFlow<List<ColorRecommendation>>(emptyList())
    val recommendations: StateFlow<List<ColorRecommendation>> = _recommendations.asStateFlow()
    
    private val _selectedColor = MutableStateFlow<ColorRecommendation?>(null)
    val selectedColor: StateFlow<ColorRecommendation?> = _selectedColor.asStateFlow()
    
    private val _isPreviewMode = MutableStateFlow(false)
    val isPreviewMode: StateFlow<Boolean> = _isPreviewMode.asStateFlow()
    
    // Undo/Redo stacks
    private val undoStack = mutableListOf<ColorRecommendation>()
    private val redoStack = mutableListOf<ColorRecommendation>()
    
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()
    
    private var currentObject: SceneObject? = null
    
    /**
     * Load recommendations for object
     */
    fun loadRecommendations(
        sceneObject: SceneObject,
        roomContext: RoomContext,
        stylePreference: String? = null
    ) {
        currentObject = sceneObject
        
        viewModelScope.launch {
            try {
                _uiState.value = ColorPaletteUiState.Loading
                
                val recs = recommendationEngine.recommend(
                    baseColor = sceneObject.detectedColor,
                    context = roomContext,
                    stylePreference = stylePreference
                )
                
                _recommendations.value = recs
                _uiState.value = ColorPaletteUiState.Success
                
            } catch (e: Exception) {
                _uiState.value = ColorPaletteUiState.Error(e.message ?: "Failed to load recommendations")
            }
        }
    }
    
    /**
     * Select color
     */
    fun selectColor(recommendation: ColorRecommendation) {
        _selectedColor.value = recommendation
    }
    
    /**
     * Apply selected color
     */
    fun applyColor() {
        val color = _selectedColor.value ?: return
        val obj = currentObject ?: return
        
        viewModelScope.launch {
            try {
                // Add to undo stack
                currentObject?.appliedColor?.let { currentColor ->
                    val currentRec = _recommendations.value.find { it.color == currentColor }
                    currentRec?.let { undoStack.add(it) }
                }
                redoStack.clear()
                updateUndoRedoState()
                
                // Apply color to object
                sceneRepository.applyColor(obj.id, color.color)
                
                _isPreviewMode.value = false
                _uiState.value = ColorPaletteUiState.Applied
                
            } catch (e: Exception) {
                _uiState.value = ColorPaletteUiState.Error(e.message ?: "Failed to apply color")
            }
        }
    }
    
    /**
     * Toggle preview mode
     */
    fun togglePreview() {
        _isPreviewMode.value = !_isPreviewMode.value
    }
    
    /**
     * Compare original vs new color
     */
    fun compare() {
        _isPreviewMode.value = !_isPreviewMode.value
    }
    
    /**
     * Undo last color change
     */
    fun undo() {
        if (undoStack.isEmpty()) return
        
        val obj = currentObject ?: return
        val previousColor = undoStack.removeLastOrNull() ?: return
        
        viewModelScope.launch {
            // Add current to redo stack
            _selectedColor.value?.let { redoStack.add(it) }
            
            // Apply previous color
            sceneRepository.applyColor(obj.id, previousColor.color)
            _selectedColor.value = previousColor
            
            updateUndoRedoState()
        }
    }
    
    /**
     * Redo last undone change
     */
    fun redo() {
        if (redoStack.isEmpty()) return
        
        val obj = currentObject ?: return
        val nextColor = redoStack.removeLastOrNull() ?: return
        
        viewModelScope.launch {
            // Add current to undo stack
            _selectedColor.value?.let { undoStack.add(it) }
            
            // Apply next color
            sceneRepository.applyColor(obj.id, nextColor.color)
            _selectedColor.value = nextColor
            
            updateUndoRedoState()
        }
    }
    
    /**
     * Update undo/redo button states
     */
    private fun updateUndoRedoState() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }
    
    /**
     * Reset state
     */
    fun reset() {
        _selectedColor.value = null
        _isPreviewMode.value = false
        undoStack.clear()
        redoStack.clear()
        updateUndoRedoState()
    }
}

/**
 * Color palette UI state
 */
sealed class ColorPaletteUiState {
    object Idle : ColorPaletteUiState()
    object Loading : ColorPaletteUiState()
    object Success : ColorPaletteUiState()
    object Applied : ColorPaletteUiState()
    data class Error(val message: String) : ColorPaletteUiState()
}
