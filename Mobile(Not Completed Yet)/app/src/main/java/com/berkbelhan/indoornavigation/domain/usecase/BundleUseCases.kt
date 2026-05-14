package com.berkbelhan.indoornavigation.domain.usecase

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.repository.MapBundleRepository
import javax.inject.Inject

/** Queue (or resume) a map bundle download for offline use. */
class DownloadMapBundleUseCase @Inject constructor(
    private val repository: MapBundleRepository
) {
    suspend operator fun invoke(mapId: String, version: String): Result<Unit> =
        repository.queueDownload(mapId, version)
}

/** Validate cached bundle integrity. */
class ValidateBundleUseCase @Inject constructor(
    private val repository: MapBundleRepository
) {
    suspend operator fun invoke(mapId: String, version: String): Result<Boolean> =
        repository.validateBundle(mapId, version)
}

/** Clear all cached bundles to reclaim storage. */
class ClearBundleCacheUseCase @Inject constructor(
    private val repository: MapBundleRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.clearAllCaches()
}
