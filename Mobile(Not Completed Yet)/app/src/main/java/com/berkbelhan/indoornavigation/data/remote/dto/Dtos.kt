package com.berkbelhan.indoornavigation.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("user_id") val userId: String,
    @SerializedName("email") val email: String,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("license_expiry") val licenseExpiry: Long?
)

data class VenueDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("address") val address: String,
    @SerializedName("thumbnail_url") val thumbnailUrl: String?,
    @SerializedName("floors") val floors: Int,
    @SerializedName("bundle_version") val bundleVersion: String?
)

data class PoiDto(
    @SerializedName("id") val id: String,
    @SerializedName("map_id") val mapId: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("x_meters") val xMeters: Double,
    @SerializedName("y_meters") val yMeters: Double,
    @SerializedName("floor") val floor: Int,
    @SerializedName("icon_url") val iconUrl: String?
)

data class BundleInfoDto(
    @SerializedName("map_id") val mapId: String,
    @SerializedName("version") val version: String,
    @SerializedName("download_url") val downloadUrl: String,
    @SerializedName("checksum_sha256") val checksumSha256: String,
    @SerializedName("size_bytes") val sizeBytes: Long
)
