package com.berkbelhan.indoornavigation.presentation.downloads

import com.berkbelhan.indoornavigation.domain.model.MapBundleDownload

data class DownloadsUiState(
    val downloads: List<MapBundleDownload> = emptyList(),
    val totalCacheBytes: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null
)
