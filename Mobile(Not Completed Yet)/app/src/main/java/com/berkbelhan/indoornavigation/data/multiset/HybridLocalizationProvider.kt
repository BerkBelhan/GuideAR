package com.berkbelhan.indoornavigation.data.multiset

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.core.dispatcher.DispatcherProvider
import com.berkbelhan.indoornavigation.core.flags.FeatureFlags
import com.berkbelhan.indoornavigation.domain.model.LocalizedPose
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Routes localization calls to either cloud or on-device provider
 * based on the runtime feature flag value.
 */
@Singleton
class HybridLocalizationProvider @Inject constructor(
    private val cloud: CloudLocalizationProvider,
    private val onDevice: OnDeviceLocalizationProvider,
    private val featureFlags: FeatureFlags,
    private val dispatchers: DispatcherProvider
) : LocalizationProvider {

    private suspend fun active(): LocalizationProvider =
        if (featureFlags.offlineLocalizationEnabled.first()) onDevice else cloud

    override suspend fun initSdk(): Result<Unit> = active().initSdk()
    override suspend fun login(accessToken: String): Result<Unit> = active().login(accessToken)
    override suspend fun localize(frameBytes: ByteArray): Result<LocalizedPose> = active().localize(frameBytes)
    override fun trackingStates(): Flow<TrackingState> = cloud.trackingStates() // always expose cloud for now
    override suspend fun shutdown(): Result<Unit> = active().shutdown()
    override fun isReady(): Boolean = cloud.isReady()
}
