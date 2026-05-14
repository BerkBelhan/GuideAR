package com.berkbelhan.indoornavigation.presentation.dashboard

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.repository.VenueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val venueRepository: VenueRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        observeVenues()
        refresh()
    }

    private fun observeVenues() {
        venueRepository.observeVenues()
            .onEach { venues ->
                _uiState.value = _uiState.value.copy(
                    venues = venues,
                    downloadedVenues = venues.filter { it.hasOfflineBundle }
                )
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        val offline = !isNetworkAvailable()
        _uiState.value = _uiState.value.copy(isOffline = offline)

        if (offline) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = venueRepository.refreshVenues()) {
                is Result.Success -> _uiState.value = _uiState.value.copy(isLoading = false)
                is Result.Failure -> {
                    Timber.w("Venues refresh failed: ${result.error}")
                    _uiState.value = _uiState.value.copy(isLoading = false, error = result.error.toString())
                }
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
