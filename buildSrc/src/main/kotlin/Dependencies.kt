package com.huehome.buildsrc

object Versions {
    // Build tools
    const val kotlin = "2.1.0"
    const val agp = "8.7.3"
    const val ksp = "2.1.0-1.0.29"
    
    // Compose
    const val composeBom = "2025.12.00"
    const val composeCompiler = "2.1.0"
    
    // AR & ML
    const val arCore = "1.47.0"
    const val tensorFlowLite = "2.16.1"  // Now branded as LiteRT
    const val openCV = "4.10.0"
    
    // Jetpack
    const val hilt = "2.54"
    const val room = "2.8.4"
    const val dataStore = "1.1.1"
    const val cameraX = "1.4.1"
    const val lifecycle = "2.8.7"
    const val navigation = "2.8.5"
    
    // Coroutines
    const val coroutines = "1.9.0"
    
    // OpenGL
    const val openGL = "3.2"
    
    // Testing
    const val junit = "4.13.2"
    const val androidxJunit = "1.2.1"
    const val espresso = "3.6.1"
    const val truth = "1.4.4"
    const val mockk = "1.13.13"
}

object Dependencies {
    // AR & ML
    const val arCore = "com.google.ar:core:${Versions.arCore}"
    const val tensorFlowLite = "org.tensorflow:tensorflow-lite:${Versions.tensorFlowLite}"
    const val tensorFlowLiteGpu = "org.tensorflow:tensorflow-lite-gpu:${Versions.tensorFlowLite}"
    const val tensorFlowLiteSupport = "org.tensorflow:tensorflow-lite-support:0.4.4"
    const val openCV = "org.opencv:opencv:${Versions.openCV}"
    
    // Compose
    const val composeBom = "androidx.compose:compose-bom:${Versions.composeBom}"
    const val composeUi = "androidx.compose.ui:ui"
    const val composeUiGraphics = "androidx.compose.ui:ui-graphics"
    const val composeUiToolingPreview = "androidx.compose.ui:ui-tooling-preview"
    const val composeMaterial3 = "androidx.compose.material3:material3"
    const val composeActivity = "androidx.activity:activity-compose:1.9.3"
    const val composeNavigation = "androidx.navigation:navigation-compose:${Versions.navigation}"
    const val composeLifecycle = "androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycle}"
    
    // Hilt
    const val hilt = "com.google.dagger:hilt-android:${Versions.hilt}"
    const val hiltCompiler = "com.google.dagger:hilt-android-compiler:${Versions.hilt}"
    const val hiltNavigation = "androidx.hilt:hilt-navigation-compose:1.2.0"
    
    // Room
    const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    
    // DataStore
    const val dataStore = "androidx.datastore:datastore-preferences:${Versions.dataStore}"
    
    // CameraX
    const val cameraCore = "androidx.camera:camera-core:${Versions.cameraX}"
    const val camera2 = "androidx.camera:camera-camera2:${Versions.cameraX}"
    const val cameraLifecycle = "androidx.camera:camera-lifecycle:${Versions.cameraX}"
    const val cameraView = "androidx.camera:camera-view:${Versions.cameraX}"
    
    // Lifecycle
    const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}"
    const val lifecycleRuntime = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    
    // Coroutines
    const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
    
    // Core Android
    const val coreKtx = "androidx.core:core-ktx:1.15.0"
    const val appcompat = "androidx.appcompat:appcompat:1.7.0"
    const val material = "com.google.android.material:material:1.12.0"
    
    // Testing
    const val junit = "junit:junit:${Versions.junit}"
    const val androidxJunit = "androidx.test.ext:junit:${Versions.androidxJunit}"
    const val espresso = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val composeUiTest = "androidx.compose.ui:ui-test-junit4"
    const val composeUiTooling = "androidx.compose.ui:ui-tooling"
    const val composeUiTestManifest = "androidx.compose.ui:ui-test-manifest"
    const val truth = "com.google.truth:truth:${Versions.truth}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
}
