package com.huehome.core.data.local

import androidx.room.*
import com.huehome.core.domain.model.SceneObject
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for SceneObject persistence
 */
@Dao
interface SceneObjectDao {
    
    @Query("SELECT * FROM scene_objects WHERE isActive = 1 ORDER BY timestamp DESC")
    fun getActiveObjects(): Flow<List<SceneObject>>
    
    @Query("SELECT * FROM scene_objects ORDER BY timestamp DESC")
    fun getAllObjects(): Flow<List<SceneObject>>
    
    @Query("SELECT * FROM scene_objects WHERE id = :id")
    suspend fun getObjectById(id: String): SceneObject?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObject(obj: SceneObject)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObjects(objects: List<SceneObject>)
    
    @Update
    suspend fun updateObject(obj: SceneObject)
    
    @Delete
    suspend fun deleteObject(obj: SceneObject)
    
    @Query("DELETE FROM scene_objects")
    suspend fun deleteAll()
    
    @Query("UPDATE scene_objects SET isActive = :isActive WHERE id = :id")
    suspend fun setObjectActive(id: String, isActive: Boolean)
    
    @Query("UPDATE scene_objects SET appliedColor = :color WHERE id = :id")
    suspend fun updateAppliedColor(id: String, color: Int?)
    
    @Query("SELECT COUNT(*) FROM scene_objects WHERE isActive = 1")
    suspend fun getActiveObjectCount(): Int
}
