package com.berkbelhan.indoornavigation.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.berkbelhan.indoornavigation.domain.model.AppSettings
import com.berkbelhan.indoornavigation.domain.model.ThemeMode
import com.berkbelhan.indoornavigation.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private val dataStore = context.dataStore

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val LOCALIZATION_INTERVAL_MS = longPreferencesKey("localization_interval_ms")
        val BATTERY_OPTIMIZATION = booleanPreferencesKey("battery_optimization")
        val WIFI_ONLY_DOWNLOADS = booleanPreferencesKey("wifi_only_downloads")
        val AR_NAVIGATION_ENABLED = booleanPreferencesKey("ar_navigation_enabled")
        val OFFLINE_LOCALIZATION_ENABLED = booleanPreferencesKey("offline_localization_enabled")
    }

    override fun observeSettings(): Flow<AppSettings> =
        dataStore.data
            .catch { e ->
                if (e is IOException) {
                    Timber.e(e, "Error reading settings DataStore")
                    emit(emptyPreferences())
                } else throw e
            }
            .map { prefs ->
                AppSettings(
                    themeMode = ThemeMode.valueOf(
                        prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
                    ),
                    localizationIntervalMs = prefs[Keys.LOCALIZATION_INTERVAL_MS] ?: 2_000L,
                    batteryOptimizationEnabled = prefs[Keys.BATTERY_OPTIMIZATION] ?: false,
                    wifiOnlyDownloads = prefs[Keys.WIFI_ONLY_DOWNLOADS] ?: true,
                    arNavigationEnabled = prefs[Keys.AR_NAVIGATION_ENABLED] ?: true,
                    offlineLocalizationEnabled = prefs[Keys.OFFLINE_LOCALIZATION_ENABLED] ?: false
                )
            }

    override suspend fun setThemeMode(mode: ThemeMode) =
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }

    override suspend fun setLocalizationIntervalMs(ms: Long) =
        dataStore.edit { it[Keys.LOCALIZATION_INTERVAL_MS] = ms }

    override suspend fun setBatteryOptimization(enabled: Boolean) =
        dataStore.edit { it[Keys.BATTERY_OPTIMIZATION] = enabled }

    override suspend fun setWifiOnlyDownloads(enabled: Boolean) =
        dataStore.edit { it[Keys.WIFI_ONLY_DOWNLOADS] = enabled }

    override suspend fun setArNavigationEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.AR_NAVIGATION_ENABLED] = enabled }

    override suspend fun setOfflineLocalizationEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.OFFLINE_LOCALIZATION_ENABLED] = enabled }
}
