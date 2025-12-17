# HueHome AR Test Suite

## Test Coverage

### Unit Tests (JUnit)

#### Color Theory Tests
- ✅ Complementary color calculation
- ✅ Analogous color generation
- ✅ Triadic color spacing
- ✅ Monochromatic variations
- ✅ Lighten/darken operations
- ✅ Contrast ratio calculation
- ✅ WCAG AA compliance

**Location**: `features/color/src/test/kotlin/com/huehome/features/color/ColorTheoryTest.kt`

#### Recommendation Engine Tests
- ✅ On-device recommendation generation
- ✅ Complementary color inclusion
- ✅ Style preference influence
- ✅ Lighting adaptation (low/bright)
- ✅ Contrast filtering

**Location**: `features/color/src/test/kotlin/com/huehome/features/color/RecommendationEngineTest.kt`

#### Repository Tests
- ✅ Active objects retrieval
- ✅ Object save/update/delete
- ✅ Toggle object visibility
- ✅ Apply/reset color
- ✅ Clear all objects

**Location**: `core/data/src/test/kotlin/com/huehome/core/data/repository/SceneRepositoryTest.kt`

#### ViewModel Tests
- ✅ Recommendation loading
- ✅ Color selection
- ✅ Apply color with persistence
- ✅ Undo/redo functionality
- ✅ Preview mode toggle

**Location**: `app/src/test/kotlin/com/huehome/ui/color/ColorPaletteViewModelTest.kt`

### Integration Tests (AndroidTest)

#### AR Pipeline Integration
- ✅ Complete pipeline (segmentation → detection → color → recommendations)
- ✅ Multiple object handling
- ✅ Lighting adaptation across pipeline

**Location**: `app/src/androidTest/kotlin/com/huehome/integration/ArPipelineIntegrationTest.kt`

## Running Tests

### Run All Unit Tests
```bash
./gradlew test
```

### Run Specific Module Tests
```bash
# Color feature tests
./gradlew :features:color:test

# Data layer tests
./gradlew :core:data:test

# App tests
./gradlew :app:testDebugUnitTest
```

### Run Integration Tests
```bash
./gradlew connectedAndroidTest
```

### Run with Coverage
```bash
./gradlew testDebugUnitTestCoverage
```

## Test Dependencies

Add to module `build.gradle.kts`:

```kotlin
dependencies {
    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.13.8")
    
    // Android testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
}
```

## Test Results

### Expected Coverage
- **Color Theory**: 100% (pure functions)
- **Recommendation Engine**: 90%+
- **Repository**: 95%+
- **ViewModels**: 85%+
- **Integration**: 80%+

### Performance Benchmarks
- Color theory operations: < 1ms
- Recommendation generation: < 50ms
- Repository operations: < 10ms
- ViewModel state updates: < 5ms

## Continuous Integration

### GitHub Actions (Example)
```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## Manual Testing Checklist

### AR Features
- [ ] AR session initializes correctly
- [ ] Plane detection works on surfaces
- [ ] Light estimation updates in real-time
- [ ] Tap-to-select responds accurately

### Object Detection
- [ ] Walls are detected and segmented
- [ ] Doors are identified correctly
- [ ] Multiple objects handled simultaneously
- [ ] Instance separation works (multiple walls)

### Color Detection
- [ ] Base color extracted accurately
- [ ] Shadows/highlights ignored
- [ ] Confidence scores reasonable
- [ ] Works in various lighting

### UI/UX
- [ ] Bottom sheets open/close smoothly
- [ ] Color cards display correctly
- [ ] Preview mode works
- [ ] Undo/redo functions properly
- [ ] Performance is 30+ FPS

### Integration
- [ ] Object selection → color palette flow
- [ ] Color application visible in AR
- [ ] State persists across app restarts
- [ ] No memory leaks after extended use
