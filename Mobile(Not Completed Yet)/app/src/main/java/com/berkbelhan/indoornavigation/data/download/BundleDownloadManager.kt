package com.berkbelhan.indoornavigation.data.download

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates bundle downloads via WorkManager.
 * Each download is a unique, pausable WorkManager request.
 */
@Singleton
class BundleDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)
    private val cancelledIds = ConcurrentHashMap.newKeySet<String>()

    fun enqueue(
        mapId: String,
        version: String,
        downloadUrl: String,
        checksum: String,
        destFile: File
    ) {
        cancelledIds.remove(mapId)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            BundleDownloadWorker.KEY_MAP_ID to mapId,
            BundleDownloadWorker.KEY_VERSION to version,
            BundleDownloadWorker.KEY_URL to downloadUrl,
            BundleDownloadWorker.KEY_CHECKSUM to checksum,
            BundleDownloadWorker.KEY_DEST_PATH to destFile.absolutePath
        )

        val request = OneTimeWorkRequestBuilder<BundleDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(mapId)
            .build()

        workManager.enqueueUniqueWork(
            workerId(mapId),
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun pause(mapId: String) {
        cancelledIds.add(mapId)
        workManager.cancelUniqueWork(workerId(mapId))
    }

    fun cancel(mapId: String) {
        cancelledIds.add(mapId)
        workManager.cancelUniqueWork(workerId(mapId))
    }

    private fun workerId(mapId: String) = "bundle_download_$mapId"
}
