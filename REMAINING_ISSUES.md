# Remaining Build Issues - Quick Fix Guide

## Issues to Fix

### 1. Missing Material Icons Import
**Files affected:**
- `ColorPaletteBottomSheet.kt`
- `ObjectSelectionBottomSheet.kt`

**Fix**: Add to imports:
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
```

### 2. Missing BorderStroke Import
**File**: `ObjectSelectionBottomSheet.kt`

**Fix**: Add to imports:
```kotlin
import androidx.compose.foundation.BorderStroke
```

### 3. ArViewModel Missing Methods
**File**: `IntegratedArScreen.kt`
**Error**: `resume()` and `pause()` don't exist

**Fix**: Remove lifecycle event handlers or add these methods to ArViewModel

### 4. ArCameraView Missing setOnTapListener
**File**: `IntegratedArScreen.kt`
**Error**: Method doesn't exist

**Fix**: Remove or implement tap listener in ArCameraView

### 5. DetectionViewModel Missing initialize()
**File**: `IntegratedViewModel.kt`
**Error**: Method doesn't exist

**Fix**: Remove call or add initialize() method to DetectionViewModel

## Quick Fixes

The easiest approach is to:
1. Add missing imports for Material Icons
2. Comment out unimplemented features (tap listener, lifecycle methods)
3. Focus on getting the app to compile first
4. Implement missing features incrementally

These are integration issues from connecting components that weren't fully implemented yet.
