package com.berkbelhan.indoornavigation.data.remote.api

import com.berkbelhan.indoornavigation.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface IndoorNavApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<LoginResponseDto>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") bearerToken: String
    ): Response<LoginResponseDto>

    @GET("venues")
    suspend fun getVenues(
        @Header("Authorization") bearerToken: String
    ): Response<List<VenueDto>>

    @GET("venues/{venueId}/pois")
    suspend fun getPois(
        @Header("Authorization") bearerToken: String,
        @Path("venueId") venueId: String
    ): Response<List<PoiDto>>

    @GET("maps/{mapId}/bundle")
    suspend fun getBundleInfo(
        @Header("Authorization") bearerToken: String,
        @Path("mapId") mapId: String
    ): Response<BundleInfoDto>
}
