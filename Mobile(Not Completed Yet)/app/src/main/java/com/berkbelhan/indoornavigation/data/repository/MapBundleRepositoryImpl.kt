package com.berkbelhan.indoornavigation.data.repository

import android.content.Context
import com.berkbelhan.indoornavigation.core.common.AppError
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.core.security.SecureTokenStore
import com.berkbelhan.indoornavigation.data.download.BundleDownloadManager
import com.berkbelhan.indoornavigation.data.local.room.dao.MapBundleDao
import com.berkbelhan.indoornavigation.data.local.room.entity.MapBundleEntity
import com.berkbelhan.indoornavigation.data.remote.api.IndoorNavApi
import com.berkbelhan.indoornavigation.domain.model.DownloadState
import com.berkbelhan.indoornavigation.domain.model.MapBundleDownload
import com.berkbelhan.indoornavigation.domain.repository.MapBundleRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapBundleRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mapBundleDao: MapBundleDao,
    private val api: IndoorNavApi,
    private val downloadManager: BundleDownloadManager,
    private val tokenStore: SecureTokenStore
) : MapBundleRepository {

    override fun observeDownloads(): Flow<List<MapBundleDownload>> =
        mapBundleDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun queueDownload(mapId: String, version: String): Result<Unit> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Failure(AppError.Auth("Not authenticated"))
        return try {
            val infoResponse = api.getBundleInfo("Bearer $token", mapId)
            if (!infoResponse.isSuccessful) {
                return Result.Failure(AppError.Network("Bundle info failed: ${infoResponse.code()}", infoResponse.code()))
            }
            val info = infoResponse.body()!!
            val destFile = bundleFile(mapId, version)

            // Record intent in DB
            mapBundleDao.upsert(
                MapBundleEntity(
                    mapId = mapId,
                    version = version,
                    localPath = destFile.absolutePath,
                    checksum = info.checksumSha256,
                    sizeBytes = info.sizeBytes,
                    downloadState = DownloadState.QUEUED.name
                )
            )

            // Delegate actual download to manager (WorkManager-backed)
            downloadManager.enqueue(mapId, version, info.downloadUrl, info.checksumSha256, destFile)
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "queueDownload error")
            Result.Failure(AppError.Download(e.message))
        }
    }

    override suspend fun pauseDownload(mapId: String): Result<Unit> {
        downloadManager.pause(mapId)
        mapBundleDao.updateProgress(
            mapId = mapId,
            state = DownloadState.PAUSED.name,
            progress = mapBundleDao.getById(mapId)?.progressPercent ?: 0,
            bytes = mapBundleDao.getById(mapId)?.bytesDownloaded ?: 0
        )
        return Result.Success(Unit)
    }

    override suspend fun resumeDownload(mapId: String): Result<Unit> {
        val entity = mapBundleDao.getById(mapId)
            ?: return Result.Failure(AppError.Download("Bundle not found: $mapId"))
        return queueDownload(entity.mapId, entity.version)
    }

    override suspend fun removeBundle(mapId: String): Result<Unit> {
        val entity = mapBundleDao.getById(mapId)
        entity?.localPath?.let { File(it).deleteRecursively() }
        mapBundleDao.delete(mapId)
        return Result.Success(Unit)
    }

    override suspend fun validateBundle(mapId: String, version: String): Result<Boolean> {
        val entity = mapBundleDao.getById(mapId)
            ?: return Result.Success(false)
        if (entity.version != version) return Result.Success(false)
        val file = File(entity.localPath)
        if (!file.exists()) return Result.Success(false)

        return try {
            val actual = sha256(file)
            Result.Success(actual.equals(entity.checksum, ignoreCase = true))
        } catch (e: Exception) {
            Timber.e(e, "Bundle validation error")
            Result.Failure(AppError.Storage(e.message))
        }
    }

    override suspend fun getLocalBundlePath(mapId: String): String? {
        val entity = mapBundleDao.getById(mapId) ?: return null
        return if (entity.downloadState == DownloadState.COMPLETED.name &&
            File(entity.localPath).exists()
        ) entity.localPath else null
    }

    override suspend fun totalCacheSizeBytes(): Long =
        mapBundleDao.totalCacheSizeBytes() ?: 0L

    override suspend fun clearAllCaches(): Result<Unit> {
        bundlesDir().deleteRecursively()
        bundlesDir().mkdirs()
        mapBundleDao.deleteAll()
        return Result.Success(Unit)
    }

    // ---------- Helpers ----------

    private fun bundlesDir(): File =
        File(context.filesDir, "map_bundles").also { it.mkdirs() }

    private fun bundleFile(mapId: String, version: String): File =
        File(bundlesDir(), "${mapId}_${version}.bundle")

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8192)
            var read: Int
            while (stream.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun MapBundleEntity.toDomain() = MapBundleDownload(
        mapId = mapId,
        version = version,
        progressPercent = progressPercent,
        state = runCatching { DownloadState.valueOf(downloadState) }.getOrDefault(DownloadState.FAILED),
        bytesDownloaded = bytesDownloaded,
        bytesTotal = sizeBytes,
        localPath = localPath.takeIf { File(it).exists() }
    )
}
