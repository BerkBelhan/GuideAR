package com.berkbelhan.indoornavigation.presentation.dashboard

import com.berkbelhan.indoornavigation.domain.model.Venue

data class DashboardUiState(
    val isLoading: Boolean = false,
    val venues: List<Venue> = emptyList(),
    val downloadedVenues: List<Venue> = emptyList(),
    val isOffline: Boolean = false,
    val error: String? = null
)
