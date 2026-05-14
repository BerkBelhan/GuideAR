package com.berkbelhan.indoornavigation.presentation.maps

import com.berkbelhan.indoornavigation.domain.model.LocalizedPose
import com.berkbelhan.indoornavigation.domain.model.PointOfInterest
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import com.berkbelhan.indoornavigation.domain.model.Venue

data class IndoorMapUiState(
    val isLoading: Boolean = false,
    val venue: Venue? = null,
    val pois: List<PointOfInterest> = emptyList(),
    val currentPose: LocalizedPose? = null,
    val trackingState: TrackingState = TrackingState.INITIALIZING,
    val selectedFloor: Int = 0,
    val isLocalizing: Boolean = false,
    val error: String? = null
)
