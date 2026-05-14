package com.berkbelhan.indoornavigation.presentation.downloads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkbelhan.indoornavigation.domain.repository.MapBundleRepository
import com.berkbelhan.indoornavigation.domain.usecase.ClearBundleCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val mapBundleRepository: MapBundleRepository,
    private val clearBundleCacheUseCase: ClearBundleCacheUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DownloadsUiState())
    val uiState: StateFlow<DownloadsUiState> = _uiState.asStateFlow()

    init {
        mapBundleRepository.observeDownloads()
            .onEach { downloads ->
                val cacheSize = mapBundleRepository.totalCacheSizeBytes()
                _uiState.value = _uiState.value.copy(
                    downloads = downloads,
                    totalCacheBytes = cacheSize
                )
            }
            .launchIn(viewModelScope)
    }

    fun pause(mapId: String) {
        viewModelScope.launch {
            mapBundleRepository.pauseDownload(mapId)
        }
    }

    fun resume(mapId: String) {
        viewModelScope.launch {
            mapBundleRepository.resumeDownload(mapId)
        }
    }

    fun delete(mapId: String) {
        viewModelScope.launch {
            mapBundleRepository.removeBundle(mapId)
        }
    }

    fun clearAllCaches() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            clearBundleCacheUseCase()
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }
}
