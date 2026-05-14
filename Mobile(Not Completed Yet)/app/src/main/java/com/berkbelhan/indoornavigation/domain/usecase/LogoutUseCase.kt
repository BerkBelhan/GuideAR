package com.berkbelhan.indoornavigation.domain.usecase

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.repository.AuthRepository
import com.berkbelhan.indoornavigation.domain.repository.LocalizationRepository
import javax.inject.Inject

/** Sign out the current user and shut down the SDK. */
class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val localizationRepository: LocalizationRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        localizationRepository.stopTracking()
        return authRepository.logout()
    }
}
