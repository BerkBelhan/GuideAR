package com.berkbelhan.indoornavigation.presentation.maps

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import com.berkbelhan.indoornavigation.domain.repository.LocalizationRepository
import com.berkbelhan.indoornavigation.domain.repository.VenueRepository
import com.berkbelhan.indoornavigation.domain.usecase.LocalizeSingleFrameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class IndoorMapViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val venueRepository: VenueRepository,
    private val localizationRepository: LocalizationRepository,
    private val localizeSingleFrameUseCase: LocalizeSingleFrameUseCase
) : ViewModel() {

    private val mapId: String = savedStateHandle["mapId"] ?: ""

    private val _uiState = MutableStateFlow(IndoorMapUiState())
    val uiState: StateFlow<IndoorMapUiState> = _uiState.asStateFlow()

    private var trackingJob: Job? = null

    init {
        loadVenueData()
        startTracking()
    }

    private fun loadVenueData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val venueResult = venueRepository.getVenue(mapId)
            val poisResult = venueRepository.getPointsOfInterest(mapId)

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                venue = (venueResult as? Result.Success)?.value,
                pois = (poisResult as? Result.Success)?.value ?: emptyList(),
                error = when {
                    venueResult is Result.Failure -> venueResult.error.toString()
                    else -> null
                }
            )
        }
    }

    private fun startTracking() {
        trackingJob = localizationRepository.startTracking()
            .onEach { state ->
                _uiState.value = _uiState.value.copy(trackingState = state)
            }
            .launchIn(viewModelScope)
    }

    fun localizeCurrentFrame(frameBytes: ByteArray) {
        if (_uiState.value.isLocalizing) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLocalizing = true)
            when (val result = localizeSingleFrameUseCase(frameBytes)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLocalizing = false,
                        currentPose = result.value,
                        selectedFloor = result.value.floor
                    )
                }
                is Result.Failure -> {
                    Timber.w("Localization failed: ${result.error}")
                    _uiState.value = _uiState.value.copy(isLocalizing = false)
                }
            }
        }
    }

    fun selectFloor(floor: Int) {
        _uiState.value = _uiState.value.copy(selectedFloor = floor)
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
        viewModelScope.launch {
            localizationRepository.stopTracking()
        }
    }
}
