pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // For OpenCV
        maven { url = uri("https://maven.google.com") }
    }
}

rootProject.name = "HueHome"
include(":app")
include(":core:common")
include(":core:data")
include(":core:domain")
include(":core:ui")
include(":features:ar")
include(":features:detection")
include(":features:color")
include(":features:rendering")
include(":features:selection")
