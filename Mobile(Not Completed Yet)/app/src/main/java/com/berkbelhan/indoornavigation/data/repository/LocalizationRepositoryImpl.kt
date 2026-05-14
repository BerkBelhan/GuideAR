package com.berkbelhan.indoornavigation.data.repository

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.core.security.SecureTokenStore
import com.berkbelhan.indoornavigation.data.multiset.LocalizationProvider
import com.berkbelhan.indoornavigation.domain.model.LocalizedPose
import com.berkbelhan.indoornavigation.domain.model.TrackingState
import com.berkbelhan.indoornavigation.domain.repository.LocalizationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalizationRepositoryImpl @Inject constructor(
    private val provider: LocalizationProvider,
    private val tokenStore: SecureTokenStore
) : LocalizationRepository {

    override suspend fun initialize(): Result<Unit> = provider.initSdk()

    override suspend fun authenticate(token: String): Result<Unit> {
        tokenStore.saveAccessToken(token)
        return provider.login(token)
    }

    override suspend fun localizeSingleFrame(frameBytes: ByteArray): Result<LocalizedPose> =
        provider.localize(frameBytes)

    override fun startTracking(): Flow<TrackingState> = provider.trackingStates()

    override suspend fun stopTracking(): Result<Unit> = provider.shutdown()

    override fun isReady(): Boolean = provider.isReady()
}
