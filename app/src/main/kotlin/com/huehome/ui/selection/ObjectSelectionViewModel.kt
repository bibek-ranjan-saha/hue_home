package com.huehome.ui.selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huehome.core.data.repository.SceneRepository
import com.huehome.core.domain.model.SceneObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for object selection
 */
@HiltViewModel
class ObjectSelectionViewModel @Inject constructor(
    private val sceneRepository: SceneRepository
) : ViewModel() {
    
    private val _objects = MutableStateFlow<List<SceneObject>>(emptyList())
    val objects: StateFlow<List<SceneObject>> = _objects.asStateFlow()
    
    private val _selectedObject = MutableStateFlow<SceneObject?>(null)
    val selectedObject: StateFlow<SceneObject?> = _selectedObject.asStateFlow()
    
    init {
        // Observe active objects from repository
        viewModelScope.launch {
            sceneRepository.getActiveObjects().collect { objects ->
                _objects.value = objects
            }
        }
    }
    
    /**
     * Select object
     */
    fun selectObject(obj: SceneObject) {
        _selectedObject.value = obj
    }
    
    /**
     * Toggle object visibility
     */
    fun toggleObject(obj: SceneObject) {
        viewModelScope.launch {
            sceneRepository.toggleObject(obj.id)
        }
    }
    
    /**
     * Update object label
     */
    fun updateLabel(obj: SceneObject, label: String) {
        viewModelScope.launch {
            val updated = obj.copy(userLabel = label)
            sceneRepository.updateObject(updated)
        }
    }
    
    /**
     * Clear selection
     */
    fun clearSelection() {
        _selectedObject.value = null
    }
}
