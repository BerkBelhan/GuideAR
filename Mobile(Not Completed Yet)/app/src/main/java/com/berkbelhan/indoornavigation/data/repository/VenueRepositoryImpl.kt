package com.berkbelhan.indoornavigation.data.repository

import com.berkbelhan.indoornavigation.core.common.AppError
import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.core.security.SecureTokenStore
import com.berkbelhan.indoornavigation.data.local.room.dao.PoiDao
import com.berkbelhan.indoornavigation.data.local.room.dao.VenueDao
import com.berkbelhan.indoornavigation.data.local.room.entity.PoiEntity
import com.berkbelhan.indoornavigation.data.local.room.entity.VenueEntity
import com.berkbelhan.indoornavigation.data.remote.api.IndoorNavApi
import com.berkbelhan.indoornavigation.domain.model.PoiCategory
import com.berkbelhan.indoornavigation.domain.model.PointOfInterest
import com.berkbelhan.indoornavigation.domain.model.Venue
import com.berkbelhan.indoornavigation.domain.repository.MapBundleRepository
import com.berkbelhan.indoornavigation.domain.repository.VenueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VenueRepositoryImpl @Inject constructor(
    private val api: IndoorNavApi,
    private val venueDao: VenueDao,
    private val poiDao: PoiDao,
    private val mapBundleRepository: MapBundleRepository,
    private val tokenStore: SecureTokenStore
) : VenueRepository {

    override fun observeVenues(): Flow<List<Venue>> =
        venueDao.observeAll().map { entities ->
            buildList {
                for (entity in entities) {
                    add(entity.toDomain())
                }
            }
        }

    override suspend fun refreshVenues(): Result<Unit> {
        val token = tokenStore.getAccessToken()
            ?: return Result.Failure(AppError.Auth("Not authenticated"))
        return try {
            val response = api.getVenues("Bearer $token")
            if (response.isSuccessful) {
                val entities = response.body()!!.map {
                    VenueEntity(
                        id = it.id,
                        name = it.name,
                        description = it.description,
                        address = it.address,
                        thumbnailUrl = it.thumbnailUrl,
                        floors = it.floors,
                        bundleVersion = it.bundleVersion
                    )
                }
                venueDao.upsertAll(entities)
                Result.Success(Unit)
            } else {
                Result.Failure(AppError.Network("Venues fetch failed: ${response.code()}", response.code()))
            }
        } catch (e: Exception) {
            Timber.e(e, "refreshVenues error")
            Result.Failure(AppError.Network(e.message))
        }
    }

    override suspend fun getPointsOfInterest(mapId: String): Result<List<PointOfInterest>> {
        val cached = poiDao.getByMapId(mapId)
        if (cached.isNotEmpty()) return Result.Success(cached.map { it.toDomain() })

        val token = tokenStore.getAccessToken()
            ?: return Result.Failure(AppError.Auth("Not authenticated"))
        return try {
            val response = api.getPois("Bearer $token", mapId)
            if (response.isSuccessful) {
                val entities = response.body()!!.map {
                    PoiEntity(it.id, it.mapId, it.name, it.category, it.xMeters, it.yMeters, it.floor, it.iconUrl)
                }
                poiDao.upsertAll(entities)
                Result.Success(entities.map { it.toDomain() })
            } else {
                Result.Failure(AppError.Network("POI fetch failed: ${response.code()}", response.code()))
            }
        } catch (e: Exception) {
            Timber.e(e, "getPOI error")
            Result.Failure(AppError.Network(e.message))
        }
    }

    override suspend fun getVenue(venueId: String): Result<Venue> {
        val entity = venueDao.getById(venueId)
            ?: return Result.Failure(AppError.Network("Venue not found locally"))
        return Result.Success(entity.toDomain())
    }

    // ---------- Mappers ----------

    private suspend fun VenueEntity.toDomain(): Venue {
        val localPath = mapBundleRepository.getLocalBundlePath(id)
        return Venue(
            id = id,
            name = name,
            description = description,
            address = address,
            thumbnailUrl = thumbnailUrl,
            floors = floors,
            hasOfflineBundle = localPath != null,
            bundleVersion = bundleVersion
        )
    }

    private fun PoiEntity.toDomain() = PointOfInterest(
        id = id,
        mapId = mapId,
        name = name,
        category = runCatching { PoiCategory.valueOf(category) }.getOrDefault(PoiCategory.GENERAL),
        xMeters = xMeters,
        yMeters = yMeters,
        floor = floor,
        iconUrl = iconUrl
    )
}
