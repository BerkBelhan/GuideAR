package com.berkbelhan.indoornavigation.presentation.ar

import com.berkbelhan.indoornavigation.domain.model.LocalizedPose
import com.berkbelhan.indoornavigation.domain.model.TrackingState

data class ArUiState(
    val isLoading: Boolean = false,
    val isTracking: Boolean = false,
    val trackingState: TrackingState = TrackingState.INITIALIZING,
    val currentPose: LocalizedPose? = null,
    val error: String? = null,
    val cameraPermissionGranted: Boolean = false
)
