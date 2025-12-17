package com.huehome.core.data.repository

import com.huehome.core.data.local.SceneObjectDao
import com.huehome.core.domain.model.SceneObject
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for scene object data
 * Manages object persistence and state
 */
@Singleton
class SceneRepository @Inject constructor(
    private val sceneObjectDao: SceneObjectDao
) {
    
    /**
     * Get all active objects as Flow
     */
    fun getActiveObjects(): Flow<List<SceneObject>> {
        return sceneObjectDao.getActiveObjects()
    }
    
    /**
     * Get all objects (including inactive)
     */
    fun getAllObjects(): Flow<List<SceneObject>> {
        return sceneObjectDao.getAllObjects()
    }
    
    /**
     * Get object by ID
     */
    suspend fun getObjectById(id: String): SceneObject? {
        return sceneObjectDao.getObjectById(id)
    }
    
    /**
     * Save new object
     */
    suspend fun saveObject(obj: SceneObject) {
        sceneObjectDao.insertObject(obj)
    }
    
    /**
     * Save multiple objects
     */
    suspend fun saveObjects(objects: List<SceneObject>) {
        sceneObjectDao.insertObjects(objects)
    }
    
    /**
     * Update existing object
     */
    suspend fun updateObject(obj: SceneObject) {
        sceneObjectDao.updateObject(obj)
    }
    
    /**
     * Delete object
     */
    suspend fun deleteObject(obj: SceneObject) {
        sceneObjectDao.deleteObject(obj)
    }
    
    /**
     * Toggle object active state
     */
    suspend fun toggleObject(id: String) {
        val obj = sceneObjectDao.getObjectById(id)
        if (obj != null) {
            sceneObjectDao.setObjectActive(id, !obj.isActive)
        }
    }
    
    /**
     * Apply color to object
     */
    suspend fun applyColor(id: String, color: Int) {
        sceneObjectDao.updateAppliedColor(id, color)
    }
    
    /**
     * Reset object to original color
     */
    suspend fun resetObject(id: String) {
        sceneObjectDao.updateAppliedColor(id, null)
    }
    
    /**
     * Clear all objects
     */
    suspend fun clearAll() {
        sceneObjectDao.deleteAll()
    }
    
    /**
     * Get count of active objects
     */
    suspend fun getActiveObjectCount(): Int {
        return sceneObjectDao.getActiveObjectCount()
    }
}
