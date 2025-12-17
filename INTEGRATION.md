# HueHome AR Integration Guide

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      MainActivity                            │
│  ┌───────────────────────────────────────────────────────┐  │
│  │              IntegratedArScreen                       │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │           ArCameraView (OpenGL)                 │  │  │
│  │  │  - Plane detection                              │  │  │
│  │  │  - Light estimation                             │  │  │
│  │  │  - Rendering engine                             │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │                                                         │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │      ObjectSelectionBottomSheet                 │  │  │
│  │  │  - Detected objects list                        │  │  │
│  │  │  - Toggle visibility                            │  │  │
│  │  │  - Label editing                                │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  │                                                         │  │
│  │  ┌─────────────────────────────────────────────────┐  │  │
│  │  │       ColorPaletteBottomSheet                   │  │  │
│  │  │  - AI recommendations                           │  │  │
│  │  │  - Preview/Compare                              │  │  │
│  │  │  - Apply color                                  │  │  │
│  │  └─────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Data Flow

### 1. AR Session Initialization
```
MainActivity
  └─> ArViewModel.initialize()
       └─> ArCoreSession.initialize()
            └─> Configure plane detection, light estimation
```

### 2. Object Detection Pipeline
```
AR Frame Update
  └─> DetectionViewModel.detectObjects(frame)
       └─> SegmentationEngine.segment(frame)
            └─> ObjectDetector.detectObjects(frame, masks)
                 └─> SceneRepository.saveObjects(objects)
```

### 3. Color Detection & Recommendation
```
Object Selected
  └─> ColorDetector.detectBaseColor(frame, mask)
       └─> RecommendationEngine.recommend(baseColor, context)
            └─> ColorPaletteViewModel.loadRecommendations()
                 └─> Display in ColorPaletteBottomSheet
```

### 4. Color Application
```
User Applies Color
  └─> ColorPaletteViewModel.applyColor()
       └─> SceneRepository.applyColor(objectId, color)
            └─> RenderingViewModel.applyColor()
                 └─> ColorRenderingEngine.renderColoredObject()
```

## Integration Steps

### Step 1: Update MainActivity
Replace `ArCameraScreen` with `IntegratedArScreen` that includes all components.

### Step 2: Create Unified ViewModel
Combine AR, Detection, Color, and Rendering ViewModels into a coordinator.

### Step 3: Connect Bottom Sheets
Wire up object selection and color palette to AR camera view.

### Step 4: Implement Frame Processing
Connect AR frames to detection pipeline.

### Step 5: Add Rendering Integration
Apply colors to AR view using rendering engine.

## State Management

### Global App State
```kotlin
data class AppState(
    val arState: ArState,
    val detectedObjects: List<SceneObject>,
    val selectedObject: SceneObject?,
    val recommendations: List<ColorRecommendation>,
    val selectedColor: ColorRecommendation?,
    val isPreviewMode: Boolean
)
```

### ViewModel Coordination
```kotlin
IntegratedViewModel {
    - arViewModel: ArViewModel
    - detectionViewModel: DetectionViewModel
    - colorPaletteViewModel: ColorPaletteViewModel
    - objectSelectionViewModel: ObjectSelectionViewModel
    - renderingViewModel: RenderingViewModel
}
```

## Performance Considerations

1. **Frame Processing**: Run detection every 5-10 frames, not every frame
2. **Texture Management**: Reuse textures, clean up after use
3. **UI Updates**: Debounce rapid state changes
4. **Memory**: Release resources when not in use

## Error Handling

1. **AR Session Errors**: Show error UI, offer retry
2. **Detection Failures**: Continue with previous results
3. **Rendering Errors**: Fallback to original view
4. **Permission Denied**: Show permission request UI

## Testing Checklist

- [ ] AR camera initializes correctly
- [ ] Plane detection works
- [ ] Objects are detected and segmented
- [ ] Colors are detected accurately
- [ ] Recommendations are generated
- [ ] Colors can be applied
- [ ] Preview/compare works
- [ ] Undo/redo functions
- [ ] Bottom sheets open/close smoothly
- [ ] Performance is 30+ FPS
