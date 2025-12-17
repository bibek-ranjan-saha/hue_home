package com.huehome.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.huehome.core.domain.model.SceneObject

/**
 * Room database for HueHome AR
 */
@Database(
    entities = [SceneObject::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HueHomeDatabase : RoomDatabase() {
    
    abstract fun sceneObjectDao(): SceneObjectDao
    
    companion object {
        const val DATABASE_NAME = "huehome_db"
    }
}
