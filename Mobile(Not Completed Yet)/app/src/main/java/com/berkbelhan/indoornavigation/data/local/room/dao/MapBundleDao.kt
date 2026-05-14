package com.berkbelhan.indoornavigation.data.local.room.dao

import androidx.room.*
import com.berkbelhan.indoornavigation.data.local.room.entity.MapBundleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapBundleDao {
    @Query("SELECT * FROM map_bundles")
    fun observeAll(): Flow<List<MapBundleEntity>>

    @Query("SELECT * FROM map_bundles WHERE mapId = :mapId LIMIT 1")
    suspend fun getById(mapId: String): MapBundleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MapBundleEntity)

    @Query("UPDATE map_bundles SET downloadState = :state, progressPercent = :progress, bytesDownloaded = :bytes, updatedAtEpochMs = :ts WHERE mapId = :mapId")
    suspend fun updateProgress(mapId: String, state: String, progress: Int, bytes: Long, ts: Long = System.currentTimeMillis())

    @Query("DELETE FROM map_bundles WHERE mapId = :mapId")
    suspend fun delete(mapId: String)

    @Query("DELETE FROM map_bundles")
    suspend fun deleteAll()

    @Query("SELECT SUM(sizeBytes) FROM map_bundles WHERE downloadState = 'COMPLETED'")
    suspend fun totalCacheSizeBytes(): Long?
}
