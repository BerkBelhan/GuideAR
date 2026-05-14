package com.berkbelhan.indoornavigation.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berkbelhan.indoornavigation.domain.model.AppSettings
import com.berkbelhan.indoornavigation.domain.model.ThemeMode
import com.berkbelhan.indoornavigation.domain.repository.SettingsRepository
import com.berkbelhan.indoornavigation.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut: StateFlow<Boolean> = _loggedOut.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setLocalizationInterval(ms: Long) {
        viewModelScope.launch { settingsRepository.setLocalizationIntervalMs(ms) }
    }

    fun setBatteryOptimization(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBatteryOptimization(enabled) }
    }

    fun setWifiOnlyDownloads(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setWifiOnlyDownloads(enabled) }
    }

    fun setArNavigationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setArNavigationEnabled(enabled) }
    }

    fun signOut() {
        viewModelScope.launch {
            logoutUseCase()
            _loggedOut.value = true
        }
    }
}
