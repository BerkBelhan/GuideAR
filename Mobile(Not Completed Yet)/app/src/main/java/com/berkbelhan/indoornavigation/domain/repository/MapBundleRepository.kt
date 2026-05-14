package com.berkbelhan.indoornavigation.domain.repository

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.model.MapBundleDownload
import kotlinx.coroutines.flow.Flow

/** Domain contract for offline map bundle lifecycle management. */
interface MapBundleRepository {
    /** Live stream of all tracked downloads. */
    fun observeDownloads(): Flow<List<MapBundleDownload>>

    /** Queue a new bundle download (or resume if already queued). */
    suspend fun queueDownload(mapId: String, version: String): Result<Unit>

    /** Pause an in-progress download. */
    suspend fun pauseDownload(mapId: String): Result<Unit>

    /** Resume a paused download. */
    suspend fun resumeDownload(mapId: String): Result<Unit>

    /** Delete local bundle files and metadata. */
    suspend fun removeBundle(mapId: String): Result<Unit>

    /** Verify the locally cached bundle matches the expected checksum and version. */
    suspend fun validateBundle(mapId: String, version: String): Result<Boolean>

    /** Return the local filesystem path for a downloaded bundle, or null. */
    suspend fun getLocalBundlePath(mapId: String): String?

    /** Total bytes consumed by all locally cached bundles. */
    suspend fun totalCacheSizeBytes(): Long

    /** Remove all completed bundle caches to reclaim storage. */
    suspend fun clearAllCaches(): Result<Unit>
}
