package com.berkbelhan.indoornavigation.presentation.ar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import com.berkbelhan.indoornavigation.domain.usecase.StartArGuidanceUseCase
import com.berkbelhan.indoornavigation.domain.usecase.LocalizeSingleFrameUseCase
import com.berkbelhan.indoornavigation.domain.repository.LocalizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArNavigationViewModel @Inject constructor(
    private val startArGuidanceUseCase: StartArGuidanceUseCase,
    private val localizeSingleFrameUseCase: LocalizeSingleFrameUseCase,
    private val localizationRepository: LocalizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArUiState())
    val uiState: StateFlow<ArUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null

    fun onCameraPermissionGranted() {
        _uiState.value = _uiState.value.copy(cameraPermissionGranted = true)
        startAr()
    }

    private fun startAr() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = startArGuidanceUseCase()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, isTracking = true)
                    observeTracking()
                }
                is Result.Failure -> {
                    Timber.e("AR start failed: ${result.error}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isTracking = false,
                        error = result.error.toString()
                    )
                }
            }
        }
    }

    private fun observeTracking() {
        trackingJob?.cancel()
        trackingJob = localizationRepository.startTracking()
            .onEach { state ->
                _uiState.value = _uiState.value.copy(trackingState = state)
            }
            .launchIn(viewModelScope)
    }

    fun submitFrame(frameBytes: ByteArray) {
        viewModelScope.launch {
            when (val result = localizeSingleFrameUseCase(frameBytes)) {
                is Result.Success -> _uiState.value = _uiState.value.copy(currentPose = result.value)
                is Result.Failure -> Timber.w("Frame localization failed: ${result.error}")
            }
        }
    }

    fun retry() {
        _uiState.value = _uiState.value.copy(error = null)
        startAr()
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
        viewModelScope.launch {
            localizationRepository.stopTracking()
        }
    }
}
