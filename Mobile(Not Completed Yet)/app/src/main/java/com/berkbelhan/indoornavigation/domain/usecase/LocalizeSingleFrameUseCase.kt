package com.berkbelhan.indoornavigation.domain.usecase

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.model.LocalizedPose
import com.berkbelhan.indoornavigation.domain.repository.LocalizationRepository
import javax.inject.Inject

/** Submit a single camera frame for VPS localization. */
class LocalizeSingleFrameUseCase @Inject constructor(
    private val repository: LocalizationRepository
) {
    suspend operator fun invoke(frameBytes: ByteArray): Result<LocalizedPose> =
        repository.localizeSingleFrame(frameBytes)
}
