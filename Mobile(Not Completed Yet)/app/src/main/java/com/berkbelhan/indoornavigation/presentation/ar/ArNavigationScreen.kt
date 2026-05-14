package com.berkbelhan.indoornavigation.presentation.ar

import android.Manifest
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.*
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import com.berkbelhan.indoornavigation.presentation.components.ErrorState
import com.berkbelhan.indoornavigation.presentation.components.GlassCard
import com.berkbelhan.indoornavigation.presentation.components.StatusChip
import com.berkbelhan.indoornavigation.presentation.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ArNavigationScreen(
    mapId: String,
    onStop: () -> Unit,
    viewModel: ArNavigationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(cameraPermissionState.status) {
        if (cameraPermissionState.status.isGranted) {
            viewModel.onCameraPermissionGranted()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when {
            !cameraPermissionState.status.isGranted -> {
                CameraPermissionRequest(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                    onCancel = onStop
                )
            }
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error!!,
                    onRetry = { viewModel.retry() }
                )
            }
            else -> {
                // AR camera preview placeholder
                // In production: mount ARCore SceneView/ArSceneView here
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(color = Color.White)
                            Text(
                                "Initializing AR…",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        ArCameraPlaceholder(trackingState = uiState.trackingState)
                    }
                }

                // HUD overlay
                ArHud(
                    uiState = uiState,
                    onStop = onStop
                )
            }
        }
    }
}

@Composable
private fun ArHud(
    uiState: ArUiState,
    onStop: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Top status bar
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "AR Navigation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                ArTrackingChip(trackingState = uiState.trackingState)
            }
        }

        // Pose info overlay (debug / info)
        uiState.currentPose?.let { pose ->
            AnimatedVisibility(
                visible = pose.confidence > 0.5f,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
            ) {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Position: (${String.format("%.1f", pose.xMeters)}m, ${String.format("%.1f", pose.yMeters)}m)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Heading: ${String.format("%.0f", pose.headingDegrees)}°  Floor: ${pose.floor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Confidence: ${String.format("%.0f", pose.confidence * 100)}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Stop button
        FloatingActionButton(
            onClick = onStop,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            containerColor = MaterialTheme.colorScheme.error,
            shape = CircleShape
        ) {
            Icon(Icons.Filled.Stop, contentDescription = "Stop AR")
        }
    }
}

@Composable
private fun ArTrackingChip(trackingState: TrackingState) {
    val (text, color) = when (trackingState) {
        TrackingState.TRACKING -> "Tracking" to TrackingGreen
        TrackingState.INITIALIZING -> "Initializing" to WarningAmber
        TrackingState.LOST -> "Lost" to ErrorRed
        TrackingState.ERROR -> "Error" to ErrorRed
    }
    StatusChip(text = text, color = color)
}

@Composable
private fun ArCameraPlaceholder(trackingState: TrackingState) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            Icons.Filled.CameraAlt,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = when (trackingState) {
                TrackingState.INITIALIZING -> "Point camera at your surroundings"
                TrackingState.TRACKING -> "Tracking active"
                TrackingState.LOST -> "Tracking lost – move camera slowly"
                TrackingState.ERROR -> "Localization error"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun CameraPermissionRequest(
    onRequestPermission: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
                Text(
                    "Camera Required",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "Camera access is required for AR localization and indoor navigation.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Access")
                }
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}
