package com.berkbelhan.indoornavigation.domain.repository

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.model.LocalizedPose
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import kotlinx.coroutines.flow.Flow

/**
 * Domain contract for all localization operations.
 * Implementations are swapped at runtime via feature flags:
 *   - CloudLocalizationProvider    (default, live VPS)
 *   - HybridLocalizationProvider   (cloud + on-device fallback)
 *   - OnDeviceLocalizationProvider (fully offline, future roadmap)
 */
interface LocalizationRepository {
    /** Initialise the underlying SDK. Must be called before any localize call. */
    suspend fun initialize(): Result<Unit>

    /** Authenticate with the MultiSet service using a stored or injected token. */
    suspend fun authenticate(token: String): Result<Unit>

    /** Submit a single JPEG/YUV frame for one-shot VPS localization. */
    suspend fun localizeSingleFrame(frameBytes: ByteArray): Result<LocalizedPose>

    /** Emit continuous tracking-state updates. Call [stopTracking] to cancel. */
    fun startTracking(): Flow<TrackingState>

    /** Gracefully halt the tracking loop and release camera resources. */
    suspend fun stopTracking(): Result<Unit>

    /** Whether the SDK is currently authenticated and ready. */
    fun isReady(): Boolean
}
