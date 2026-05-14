package com.berkbelhan.indoornavigation.data.download

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.berkbelhan.indoornavigation.data.local.room.dao.MapBundleDao
import com.berkbelhan.indoornavigation.domain.model.DownloadState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.security.MessageDigest

/**
 * WorkManager worker that streams a bundle download to disk,
 * updating Room progress and verifying SHA-256 checksum on completion.
 */
@HiltWorker
class BundleDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val mapBundleDao: MapBundleDao,
    private val okHttpClient: OkHttpClient
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val mapId = inputData.getString(KEY_MAP_ID) ?: return Result.failure()
        val version = inputData.getString(KEY_VERSION) ?: return Result.failure()
        val url = inputData.getString(KEY_URL) ?: return Result.failure()
        val expectedChecksum = inputData.getString(KEY_CHECKSUM) ?: return Result.failure()
        val destPath = inputData.getString(KEY_DEST_PATH) ?: return Result.failure()

        val destFile = File(destPath)

        return try {
            mapBundleDao.updateProgress(mapId, DownloadState.DOWNLOADING.name, 0, 0)

            val request = Request.Builder().url(url).get().build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Timber.e("Download failed HTTP ${response.code}")
                mapBundleDao.updateProgress(mapId, DownloadState.FAILED.name, 0, 0)
                return Result.retry()
            }

            val body = response.body ?: run {
                mapBundleDao.updateProgress(mapId, DownloadState.FAILED.name, 0, 0)
                return Result.failure()
            }

            val totalBytes = body.contentLength()
            val digest = MessageDigest.getInstance("SHA-256")
            var downloadedBytes = 0L

            destFile.parentFile?.mkdirs()
            destFile.outputStream().use { out ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        out.write(buffer, 0, read)
                        digest.update(buffer, 0, read)
                        downloadedBytes += read

                        val progress = if (totalBytes > 0)
                            ((downloadedBytes * 100) / totalBytes).toInt() else 0
                        mapBundleDao.updateProgress(
                            mapId, DownloadState.DOWNLOADING.name, progress, downloadedBytes
                        )
                        // Respect cancellation
                        if (isStopped) {
                            mapBundleDao.updateProgress(mapId, DownloadState.PAUSED.name, progress, downloadedBytes)
                            return Result.failure()
                        }
                    }
                }
            }

            // Verify checksum
            val actualChecksum = digest.digest().joinToString("") { "%02x".format(it) }
            if (!actualChecksum.equals(expectedChecksum, ignoreCase = true)) {
                Timber.e("Checksum mismatch for $mapId")
                destFile.delete()
                mapBundleDao.updateProgress(mapId, DownloadState.FAILED.name, 0, 0)
                return Result.failure()
            }

            mapBundleDao.updateProgress(mapId, DownloadState.COMPLETED.name, 100, downloadedBytes)
            Timber.d("Bundle $mapId downloaded successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "BundleDownloadWorker error for $mapId")
            mapBundleDao.updateProgress(mapId, DownloadState.FAILED.name, 0, 0)
            Result.retry()
        }
    }

    companion object {
        const val KEY_MAP_ID = "map_id"
        const val KEY_VERSION = "version"
        const val KEY_URL = "download_url"
        const val KEY_CHECKSUM = "checksum"
        const val KEY_DEST_PATH = "dest_path"
    }
}
