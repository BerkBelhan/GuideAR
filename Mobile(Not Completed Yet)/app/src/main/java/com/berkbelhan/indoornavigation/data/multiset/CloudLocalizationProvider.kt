package com.berkbelhan.indoornavigation.data.multiset

import com.berkbelhan.indoornavigation.core.common.AppError
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.core.dispatcher.DispatcherProvider
import com.berkbelhan.indoornavigation.domain.model.LocalizedPose
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud VPS localization backed by the MultiSet Android SDK.
 *
 * INTEGRATION NOTE:
 *   Replace the TODO stubs with real MultiSet SDK calls once the dependency
 *   is added from https://github.com/MultiSet-AI/multiset-android-sdk.
 *   The provider pattern ensures zero changes are needed in domain or UI layers.
 */
@Singleton
class CloudLocalizationProvider @Inject constructor(
    private val dispatchers: DispatcherProvider
) : LocalizationProvider {

    private val _trackingState = MutableStateFlow(TrackingState.INITIALIZING)
    private var sdkReady = false
    private var authenticated = false

    override suspend fun initSdk(): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            // TODO: MultiSetSDK.initialize(context)
            Timber.d("MultiSet SDK initialized (stub)")
            sdkReady = true
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = {
                Timber.e(it, "MultiSet SDK init failed")
                Result.Failure(AppError.Localization("SDK init failed: ${it.message}"))
            }
        )
    }

    override suspend fun login(accessToken: String): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            // TODO: MultiSetSDK.authenticate(accessToken)
            Timber.d("MultiSet SDK authenticated (stub)")
            authenticated = true
            _trackingState.value = TrackingState.INITIALIZING
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = {
                Timber.e(it, "MultiSet auth failed")
                Result.Failure(AppError.Auth("Authentication failed: ${it.message}"))
            }
        )
    }

    override suspend fun localize(frameBytes: ByteArray): Result<LocalizedPose> =
        withContext(dispatchers.io) {
            if (!authenticated) {
                return@withContext Result.Failure(
                    AppError.Localization("SDK not authenticated")
                )
            }
            runCatching {
                // TODO: Replace with real MultiSetSDK.localize(frameBytes) response parsing.
                // The SDK returns a pose with mapId, position, heading, confidence.
                // For now we return a stub with zero confidence so UI shows "initializing".
                LocalizedPose(
                    mapId = "default-map",
                    xMeters = 0.0,
                    yMeters = 0.0,
                    zMeters = 0.0,
                    headingDegrees = 0f,
                    confidence = 0.0f
                )
            }.fold(
                onSuccess = { pose ->
                    _trackingState.value = if (pose.confidence > 0.5f)
                        TrackingState.TRACKING else TrackingState.LOST
                    Result.Success(pose)
                },
                onFailure = {
                    Timber.e(it, "Localization failed")
                    _trackingState.value = TrackingState.ERROR
                    Result.Failure(AppError.Localization(it.message))
                }
            )
        }

    override fun trackingStates(): Flow<TrackingState> = _trackingState.asStateFlow()

    override suspend fun shutdown(): Result<Unit> = withContext(dispatchers.io) {
        runCatching {
            // TODO: MultiSetSDK.shutdown()
            sdkReady = false
            authenticated = false
            _trackingState.value = TrackingState.INITIALIZING
        }.fold(
            onSuccess = { Result.Success(Unit) },
            onFailure = { Result.Failure(AppError.Unknown(it)) }
        )
    }

    override fun isReady(): Boolean = sdkReady && authenticated
}
