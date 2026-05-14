package com.berkbelhan.indoornavigation.data.multiset

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.model.LocalizedPose
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import kotlinx.coroutines.flow.Flow

/**
 * Provider strategy interface for VPS localization.
 * Swap between cloud, hybrid, and on-device implementations at runtime
 * without changing domain or presentation code.
 */
interface LocalizationProvider {
    suspend fun initSdk(): Result<Unit>
    suspend fun login(accessToken: String): Result<Unit>
    suspend fun localize(frameBytes: ByteArray): Result<LocalizedPose>
    fun trackingStates(): Flow<TrackingState>
    suspend fun shutdown(): Result<Unit>
    fun isReady(): Boolean
}
