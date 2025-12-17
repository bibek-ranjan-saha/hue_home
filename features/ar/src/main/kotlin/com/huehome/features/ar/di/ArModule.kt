package com.huehome.features.ar.di

import com.huehome.core.domain.ar.ArSession
import com.huehome.features.ar.ArCoreSession
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for AR dependencies
 * Provides ARCore session implementation
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ArModule {
    
    @Binds
    @Singleton
    abstract fun bindArSession(
        arCoreSession: ArCoreSession
    ): ArSession
}
