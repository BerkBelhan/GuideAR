package com.berkbelhan.indoornavigation.data.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Cached map bundle metadata. The actual file lives at [localPath]. */
@Entity(tableName = "map_bundles")
data class MapBundleEntity(
    @PrimaryKey val mapId: String,
    val version: String,
    val localPath: String,
    val checksum: String,
    val sizeBytes: Long,
    val downloadState: String,   // mirrors DownloadState enum name
    val progressPercent: Int = 0,
    val bytesDownloaded: Long = 0,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

/** Venue metadata cached from remote API for offline use. */
@Entity(tableName = "venues")
data class VenueEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val address: String,
    val thumbnailUrl: String?,
    val floors: Int,
    val bundleVersion: String?,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
)

/** Point of interest cached locally. */
@Entity(tableName = "poi", primaryKeys = ["id", "mapId"])
data class PoiEntity(
    val id: String,
    val mapId: String,
    val name: String,
    val category: String,
    val xMeters: Double,
    val yMeters: Double,
    val floor: Int,
    val iconUrl: String?
)
