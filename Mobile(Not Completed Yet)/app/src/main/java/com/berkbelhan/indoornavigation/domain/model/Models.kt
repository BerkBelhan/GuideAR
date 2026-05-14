package com.berkbelhan.indoornavigation.domain.model

/** Represents the user's localized position inside a mapped venue. */
data class LocalizedPose(
    val mapId: String,
    val xMeters: Double,
    val yMeters: Double,
    val zMeters: Double,
    val headingDegrees: Float,
    /** Confidence in [0.0, 1.0] – values below 0.5 should be treated as unreliable. */
    val confidence: Float,
    val floor: Int = 0
)

/** Camera / VPS tracking lifecycle states. */
enum class TrackingState {
    INITIALIZING,
    TRACKING,
    LOST,
    ERROR
}

/** A physical indoor venue/building. */
data class Venue(
    val id: String,
    val name: String,
    val description: String,
    val address: String,
    val thumbnailUrl: String?,
    val floors: Int,
    val hasOfflineBundle: Boolean,
    val bundleVersion: String?
)

/** A point of interest on the indoor map. */
data class PointOfInterest(
    val id: String,
    val mapId: String,
    val name: String,
    val category: PoiCategory,
    val xMeters: Double,
    val yMeters: Double,
    val floor: Int,
    val iconUrl: String?
)

enum class PoiCategory {
    ENTRANCE, EXIT, ELEVATOR, STAIRS, RESTROOM,
    CAFETERIA, MEETING_ROOM, OFFICE, GENERAL
}

/** Navigation route between two points. */
data class NavigationRoute(
    val originPoi: PointOfInterest,
    val destinationPoi: PointOfInterest,
    val waypoints: List<Waypoint>,
    val totalDistanceMeters: Double,
    val estimatedTimeSeconds: Int
)

data class Waypoint(
    val xMeters: Double,
    val yMeters: Double,
    val floor: Int,
    val instruction: String,
    val isFloorChange: Boolean = false
)

/** Offline map bundle download information. */
data class MapBundleDownload(
    val mapId: String,
    val version: String,
    val progressPercent: Int,
    val state: DownloadState,
    val bytesDownloaded: Long,
    val bytesTotal: Long,
    val localPath: String? = null
)

enum class DownloadState { QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED }

/** Authenticated user session. */
data class UserSession(
    val userId: String,
    val accessToken: String,
    val email: String,
    val displayName: String,
    val licenseExpiry: Long?
)

/** App settings model surfaced to UI. */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val localizationIntervalMs: Long = 2_000L,
    val batteryOptimizationEnabled: Boolean = false,
    val wifiOnlyDownloads: Boolean = true,
    val arNavigationEnabled: Boolean = true,
    val offlineLocalizationEnabled: Boolean = false
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }
