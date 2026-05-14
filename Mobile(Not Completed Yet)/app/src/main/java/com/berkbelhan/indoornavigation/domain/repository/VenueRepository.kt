package com.berkbelhan.indoornavigation.domain.repository

import com.berkbelhan.indoornavigation.core.common.Result
import com.berkbelhan.indoornavigation.domain.model.Venue
import com.berkbelhan.indoornavigation.domain.model.PointOfInterest
import kotlinx.coroutines.flow.Flow

/** Domain contract for venue and map data. */
interface VenueRepository {
    /** All available venues from remote or cache. */
    fun observeVenues(): Flow<List<Venue>>

    /** Fetch venues from the remote API and refresh local cache. */
    suspend fun refreshVenues(): Result<Unit>

    /** All POIs belonging to a given map. */
    suspend fun getPointsOfInterest(mapId: String): Result<List<PointOfInterest>>

    /** Single venue by ID. */
    suspend fun getVenue(venueId: String): Result<Venue>
}
