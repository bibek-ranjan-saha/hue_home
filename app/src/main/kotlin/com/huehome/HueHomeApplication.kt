package com.huehome

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * HueHome Application class
 * Entry point for dependency injection with Hilt
 */
@HiltAndroidApp
class HueHomeApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize any global configurations here
        // e.g., Logging, Analytics, Crash reporting
    }
}
