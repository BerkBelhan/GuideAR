package com.berkbelhan.indoornavigation.core.flags

import kotlinx.coroutines.flow.Flow

/** Runtime feature-flag contracts – backed by DataStore at runtime. */
interface FeatureFlags {
    val arNavigationEnabled: Flow<Boolean>
    val offlineLocalizationEnabled: Flow<Boolean>
    val mapRenderingEngine: Flow<MapEngine>
}

enum class MapEngine { MAP_LIBRE, FILAMENT, DEFAULT_2D }
