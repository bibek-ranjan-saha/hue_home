package com.huehome.ui.ar

import android.Manifest
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.huehome.R
import com.huehome.features.ar.ArCameraView
import com.huehome.features.ar.ArState
import com.huehome.features.ar.ArViewModel

/**
 * AR Camera Screen - Main AR interface
 * Displays camera feed with AR overlays and controls
 */
@Composable
fun ArCameraScreen(
    viewModel: ArViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    val arState by viewModel.arState.collectAsState()
    val detectedPlanes by viewModel.detectedPlanes.collectAsState()
    val lightEstimate by viewModel.lightEstimate.collectAsState()
    
    var hasCameraPermission by remember { mutableStateOf(false) }
    var arCameraView by remember { mutableStateOf<ArCameraView?>(null) }
    
    // Camera permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (granted) {
            viewModel.initializeAr(context)
        }
    }
    
    // Handle lifecycle events
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (hasCameraPermission) {
                        arCameraView?.resumeAr()
                        viewModel.resumeAr()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    arCameraView?.pauseAr()
                    viewModel.pauseAr()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    arCameraView?.releaseAr()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Request camera permission on first composition
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !hasCameraPermission -> {
                // Permission denied state
                PermissionDeniedContent(
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
            arState is ArState.Error -> {
                // Error state
                ErrorContent(
                    message = (arState as ArState.Error).message,
                    onRetry = { viewModel.initializeAr(context) }
                )
            }
            else -> {
                // AR Camera View
                AndroidView(
                    factory = { ctx ->
                        ArCameraView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            onTap = { x, y ->
                                viewModel.handleTap(x, y)
                            }
                            arCameraView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // AR Overlay UI
                ArOverlay(
                    arState = arState,
                    planeCount = detectedPlanes.size,
                    lightIntensity = lightEstimate.pixelIntensity
                )
            }
        }
    }
}

/**
 * AR overlay with status information
 */
@Composable
fun ArOverlay(
    arState: ArState,
    planeCount: Int,
    lightIntensity: Float
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top status bar
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = when (arState) {
                        is ArState.Initializing -> stringResource(R.string.ar_initializing)
                        is ArState.Running -> stringResource(R.string.ar_scanning)
                        is ArState.Paused -> "Paused"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                
                if (arState is ArState.Running) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (planeCount > 0) {
                            "$planeCount ${stringResource(R.string.ar_plane_detected)}"
                        } else {
                            "Move device to detect surfaces"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom instructions
        if (arState is ArState.Running && planeCount > 0) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = stringResource(R.string.tap_to_select),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

/**
 * Permission denied content
 */
@Composable
fun PermissionDeniedContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.error_camera_permission),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.permission_camera_rationale),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.permission_grant))
        }
    }
}

/**
 * Error content
 */
@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
