package com.berkbelhan.indoornavigation.domain.repository

import com.berkbelhan.indoornavigation.domain.model.AppSettings
import com.berkbelhan.indoornavigation.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** Domain contract for user-visible app preferences. */
interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setLocalizationIntervalMs(ms: Long)
    suspend fun setBatteryOptimization(enabled: Boolean)
    suspend fun setWifiOnlyDownloads(enabled: Boolean)
    suspend fun setArNavigationEnabled(enabled: Boolean)
    suspend fun setOfflineLocalizationEnabled(enabled: Boolean)
}
