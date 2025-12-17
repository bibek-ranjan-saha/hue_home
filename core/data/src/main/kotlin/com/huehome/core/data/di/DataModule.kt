package com.huehome.core.data.di

import android.content.Context
import androidx.room.Room
import com.huehome.core.data.local.HueHomeDatabase
import com.huehome.core.data.local.SceneObjectDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for data layer dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): HueHomeDatabase {
        return Room.databaseBuilder(
            context,
            HueHomeDatabase::class.java,
            HueHomeDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideSceneObjectDao(
        database: HueHomeDatabase
    ): SceneObjectDao {
        return database.sceneObjectDao()
    }
}
