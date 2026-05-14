package com.berkbelhan.indoornavigation.domain.usecase

import com.berkbelhan.indoornavigation.core.common.AppError
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.core.flags.FeatureFlags
import com.berkbelhan.indoornavigation.domain.repository.LocalizationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Validates feature flag, then initialises the localization SDK and
 * starts the AR guidance loop.
 */
class StartArGuidanceUseCase @Inject constructor(
    private val localizationRepository: LocalizationRepository,
    private val featureFlags: FeatureFlags
) {
    suspend operator fun invoke(): Result<Unit> {
        val enabled = featureFlags.arNavigationEnabled.first()
        if (!enabled) {
            return Result.Failure(AppError.Localization("AR navigation is disabled by feature flag"))
        }
        return localizationRepository.initialize()
    }
}
