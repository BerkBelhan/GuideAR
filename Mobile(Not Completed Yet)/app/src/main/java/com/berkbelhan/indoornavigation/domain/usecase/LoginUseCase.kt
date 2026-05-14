package com.berkbelhan.indoornavigation.domain.usecase

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.model.UserSession
import com.berkbelhan.indoornavigation.domain.repository.AuthRepository
import com.berkbelhan.indoornavigation.domain.repository.LocalizationRepository
import javax.inject.Inject

/** Authenticate the user and initialise the localization SDK with their token. */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val localizationRepository: LocalizationRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<UserSession> {
        val sessionResult = authRepository.login(email, password)
        if (sessionResult is Result.Success) {
            localizationRepository.authenticate(sessionResult.value.accessToken)
        }
        return sessionResult
    }
}
