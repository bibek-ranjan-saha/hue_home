# HueHome AR

**AI-Powered Interior Color Assistant for Android**

HueHome AR is a cutting-edge Android application that uses ARCore, AI-powered segmentation, and material-aware color detection to help users visualize and choose the perfect colors for their home interiors.

## 🎯 Features

### MVP (v1.0)
- **AR Scene Understanding**: Real-time plane detection and spatial tracking with ARCore
- **Object Detection**: AI-powered segmentation of walls, doors, and windows using TensorFlow Lite
- **Material-Aware Color Detection**: Accurate base color detection using LAB color space
- **AI Color Recommendations**: Smart color suggestions based on color theory and room context
- **Realistic Color Application**: GPU-accelerated rendering with texture and shadow preservation
- **State Management**: Undo/redo, toggle changes, and session persistence
- **Dual AI Modes**: On-device and cloud processing options

### Planned (v2-v4)
- Furniture and appliance detection
- Advanced lighting simulation
- XR headset support (Android XR, Samsung XR, OpenXR)
- Personalized AI learning from user preferences

## 🏗️ Architecture

HueHome uses **Clean Architecture** with a multi-module structure designed for scalability and future XR support:

```
huehome/
├── app/                    # Application module
├── core/
│   ├── common/            # Shared utilities
│   ├── data/              # Data layer (Room, DataStore)
│   ├── domain/            # Business logic & models
│   └── ui/                # Shared UI components
└── features/
    ├── ar/                # AR session management
    ├── detection/         # Object detection & segmentation
    ├── color/             # Color detection & recommendations
    ├── rendering/         # Color application & rendering
    └── selection/         # Object selection system
```

### XR-Ready Design
The AR abstraction layer (`ArSession` interface) allows seamless migration to XR platforms in future versions without core rewrites.

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.1.0 |
| Build | Gradle 8.11.1, AGP 8.7.3 |
| UI | Jetpack Compose (BOM 2025.12.00) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt 2.54 |
| AR | ARCore 1.47.0 |
| AI/ML | TensorFlow Lite 2.16.1 (LiteRT) |
| Computer Vision | OpenCV 4.10.0 |
| Rendering | OpenGL ES 3.2 |
| Database | Room 2.8.4 |
| Async | Coroutines 1.9.0 + Flow |
| Camera | CameraX 1.4.1 |

## 📋 Requirements

- **Android**: API 26+ (Android 8.0 Oreo)
- **ARCore**: Device must support ARCore
- **OpenGL ES**: 3.2 or higher
- **Camera**: Required for AR functionality
- **Internet**: Optional (for cloud AI mode)

## 🚀 Getting Started

### Prerequisites
- Android Studio Iguana (2023.2.1) or later
- JDK 17
- Android SDK with API 35

### Build & Run

1. Clone the repository:
```bash
git clone <repository-url>
cd huehome
```

2. Open in Android Studio

3. Sync Gradle dependencies

4. Run on ARCore-supported device:
```bash
./gradlew installDebug
```

### Testing

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumentation tests
./gradlew connectedDebugAndroidTest
```

## 📱 Usage

1. **Launch** the app and grant camera permissions
2. **Scan** your room by moving the device to detect surfaces
3. **Select** an object from the list or tap directly on it
4. **View** the detected original color
5. **Browse** AI-recommended colors
6. **Apply** a color to see realistic preview
7. **Toggle** between original and applied colors
8. **Save** your session for later

## 🎨 Color Detection Pipeline

1. **Segmentation**: TensorFlow Lite DeepLab v3+ generates pixel-level masks
2. **Sampling**: Extract pixels from masked region, excluding edges
3. **Color Space**: Convert to LAB for perceptual uniformity
4. **Clustering**: K-means clustering to find dominant color
5. **Output**: Base color ignoring shadows and highlights

## 🤖 AI Recommendation Engine

### On-Device Mode
- Color theory algorithms (complementary, analogous, triadic)
- Style presets (Modern, Minimal, Warm, Luxury, Scandinavian)
- Room context analysis

### Cloud Mode
- Enhanced AI models
- Personalized recommendations (future)
- Trend analysis (future)

## 🔒 Privacy

- **On-Device First**: Default mode processes everything locally
- **Explicit Consent**: Cloud mode requires user permission
- **No Storage**: Images are not stored on servers
- **Transparent**: Users choose their processing mode

## 📊 Performance Targets

- **FPS**: 30-60 during AR session
- **Memory**: < 500MB peak usage
- **Thermal**: No throttling in 5-minute sessions
- **Latency**: < 100ms for color detection

## 🗺️ Roadmap

### Phase 1 (MVP) - ✅ In Progress
- Core AR functionality
- Walls & doors detection
- Color detection & recommendations
- Basic UI

### Phase 2
- Furniture detection
- Advanced lighting simulation
- More style presets
- Performance optimizations

### Phase 3/4 (XR Support)
- Android XR integration
- Samsung XR support
- OpenXR compatibility
- Immersive editing modes

## 🤝 Contributing

This is currently a private project. Contribution guidelines will be added when the project goes open source.

## 📄 License

Copyright © 2024 HueHome. All rights reserved.

## 🙏 Acknowledgments

- Google ARCore team
- TensorFlow Lite team
- OpenCV community
- Material Design team

---

**Built with ❤️ using Kotlin and Jetpack Compose**
# hue_home
