import com.huehome.buildsrc.Dependencies

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.huehome.core.domain"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:common"))
    
    implementation(Dependencies.coreKtx)
    implementation(Dependencies.coroutinesCore)
    
    // Room for entity annotations
    implementation(Dependencies.roomRuntime)
    ksp(Dependencies.roomCompiler)
    
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.truth)
}
