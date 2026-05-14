package com.berkbelhan.indoornavigation.data.multiset

import com.berkbelhan.indoornavigation.core.common.AppError
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.core.dispatcher.DispatcherProvider
import com.berkbelhan.indoornavigation.domain.model.LocalizedPose
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Placeholder for the future MultiSet on-device localization provider.
 * This will be activated once the on-device SDK ships (Q1 2026 roadmap).
 *
 * Swap in via [com.berkbelhan.indoornavigation.core.flags.FeatureFlags.offlineLocalizationEnabled]
 * without touching any other layer.
 */
class OnDeviceLocalizationProvider @Inject constructor(
    private val dispatchers: DispatcherProvider
) : LocalizationProvider {

    override suspend fun initSdk(): Result<Unit> =
        Result.Failure(AppError.Localization("On-device localization not yet available"))

    override suspend fun login(accessToken: String): Result<Unit> =
        Result.Failure(AppError.Localization("On-device localization not yet available"))

    override suspend fun localize(frameBytes: ByteArray): Result<LocalizedPose> =
        Result.Failure(AppError.Localization("On-device localization not yet available"))

    override fun trackingStates(): Flow<TrackingState> = flowOf(TrackingState.ERROR)

    override suspend fun shutdown(): Result<Unit> = Result.Success(Unit)

    override fun isReady(): Boolean = false
}
