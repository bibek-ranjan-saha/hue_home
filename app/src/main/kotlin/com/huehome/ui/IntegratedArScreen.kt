package com.huehome.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.Lifecycle
import com.huehome.features.ar.ArCameraView
import com.huehome.ui.color.ColorPaletteBottomSheet
import com.huehome.ui.selection.ObjectSelectionBottomSheet
import com.huehome.features.ar.ArViewModel
import com.huehome.features.detection.DetectionViewModel
import com.huehome.ui.color.ColorPaletteViewModel
import com.huehome.ui.selection.ObjectSelectionViewModel
import com.huehome.features.rendering.RenderingViewModel

/**
 * Integrated AR screen with all features
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegratedArScreen() {
    // Get individual ViewModels from Hilt
    val arViewModel: ArViewModel = hiltViewModel()
    val detectionViewModel: DetectionViewModel = hiltViewModel()
    val colorPaletteViewModel: ColorPaletteViewModel = hiltViewModel()
    val objectSelectionViewModel: ObjectSelectionViewModel = hiltViewModel()
    val renderingViewModel: RenderingViewModel = hiltViewModel()

    // Manually create IntegratedViewModel
    val viewModel = remember {
        IntegratedViewModel(
            arViewModel = arViewModel,
            detectionViewModel = detectionViewModel,
            colorPaletteViewModel = colorPaletteViewModel,
            objectSelectionViewModel = objectSelectionViewModel,
            renderingViewModel = renderingViewModel
        )
    }

    val context = LocalContext.current
    val appState by viewModel.appState.collectAsState()

    var showObjectSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }

    // Initialize on first composition
    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    // TODO: Handle lifecycle events when ArViewModel implements resume/pause
    // LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    //     viewModel.arViewModel.resume()
    // }
    // 
    // LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
    //     viewModel.arViewModel.pause()
    // }

    Box(modifier = Modifier.fillMaxSize()) {
        // AR Camera View
        // TODO: Implement ArCameraView with tap listener
        AndroidView(
            factory = { ctx ->
                ArCameraView(ctx)
                // TODO: Add tap listener when ArCameraView supports it
                // .apply {
                //     setOnTapListener { x, y ->
                //         viewModel.onArTap(x, y)
                //     }
                // }
            },
            modifier = Modifier.fillMaxSize()
        )

        // AR Status Overlay
        ArStatusOverlay(
            arState = appState.arState,
            objectCount = appState.detectedObjects.size,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
        )

        // Bottom Action Bar
        BottomActionBar(
            onShowObjects = { showObjectSheet = true },
            onShowColors = { showColorSheet = true },
            hasSelectedObject = appState.selectedObject != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )

        // Object Selection Bottom Sheet
        if (showObjectSheet) {
            ModalBottomSheet(
                onDismissRequest = { showObjectSheet = false }
            ) {
                ObjectSelectionBottomSheet(
                    objects = appState.detectedObjects,
                    selectedObject = appState.selectedObject,
                    onObjectSelected = { obj ->
                        viewModel.objectSelectionViewModel.selectObject(obj)
                        showObjectSheet = false
                        showColorSheet = true
                    },
                    onToggleObject = { obj ->
                        viewModel.objectSelectionViewModel.toggleObject(obj)
                    },
                    onLabelObject = { obj ->
                        // TODO: Show label dialog
                    }
                )
            }
        }
        
        // Color Palette Bottom Sheet
        if (showColorSheet && appState.selectedObject != null) {
            ModalBottomSheet(
                onDismissRequest = { showColorSheet = false }
            ) {
                ColorPaletteBottomSheet(
                    recommendations = appState.recommendations,
                    selectedColor = appState.selectedColor,
                    onColorSelected = { color ->
                        viewModel.colorPaletteViewModel.selectColor(color)
                    },
                    onApply = {
                        viewModel.applyColor()
                    },
                    onPreview = {
                        viewModel.colorPaletteViewModel.togglePreview()
                    },
                    onCompare = {
                        viewModel.colorPaletteViewModel.compare()
                    },
                    onUndo = {
                        viewModel.colorPaletteViewModel.undo()
                    },
                    onRedo = {
                        viewModel.colorPaletteViewModel.redo()
                    },
                    canUndo = viewModel.colorPaletteViewModel.canUndo.collectAsState().value,
                    canRedo = viewModel.colorPaletteViewModel.canRedo.collectAsState().value,
                    isPreviewMode = appState.isPreviewMode
                )
            }
        }
    }
}

/**
 * AR status overlay showing plane count and state
 */
@Composable
private fun ArStatusOverlay(
    arState: com.huehome.features.ar.ArState,
    objectCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (arState) {
                is com.huehome.features.ar.ArState.Initializing -> {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Initializing AR...")
                }
                is com.huehome.features.ar.ArState.Running -> {
                    Text(
                        text = "$objectCount objects detected",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is com.huehome.features.ar.ArState.Error -> {
                    Text(
                        text = "AR Error: ${arState.message}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
        }
    }
}

/**
 * Bottom action bar with object and color buttons
 */
@Composable
private fun BottomActionBar(
    onShowObjects: () -> Unit,
    onShowColors: () -> Unit,
    hasSelectedObject: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onShowObjects,
                modifier = Modifier.weight(1f)
            ) {
                Text("Objects")
            }
            
            Button(
                onClick = onShowColors,
                enabled = hasSelectedObject,
                modifier = Modifier.weight(1f)
            ) {
                Text("Colors")
            }
        }
    }
}

