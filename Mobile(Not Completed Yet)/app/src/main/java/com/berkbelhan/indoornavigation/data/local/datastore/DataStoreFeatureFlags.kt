package com.berkbelhan.indoornavigation.data.local.datastore

import com.berkbelhan.indoornavigation.core.flags.FeatureFlags
import com.berkbelhan.indoornavigation.core.flags.MapEngine
import com.berkbelhan.indoornavigation.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Feature flags backed by the same DataStore settings.
 * In a production app these could be backed by Firebase Remote Config or a
 * dedicated flags service; here we derive them from user settings for simplicity.
 */
@Singleton
class DataStoreFeatureFlags @Inject constructor(
    private val settingsRepository: SettingsRepository
) : FeatureFlags {

    override val arNavigationEnabled: Flow<Boolean> =
        settingsRepository.observeSettings().map { it.arNavigationEnabled }

    override val offlineLocalizationEnabled: Flow<Boolean> =
        settingsRepository.observeSettings().map { it.offlineLocalizationEnabled }

    override val mapRenderingEngine: Flow<MapEngine> =
        settingsRepository.observeSettings().map { MapEngine.DEFAULT_2D }
}
