package com.berkbelhan.indoornavigation.data.local.room.dao

import androidx.room.*
import com.berkbelhan.indoornavigation.data.local.room.entity.VenueEntity
import com.berkbelhan.indoornavigation.data.local.room.entity.PoiEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VenueDao {
    @Query("SELECT * FROM venues ORDER BY name ASC")
    fun observeAll(): Flow<List<VenueEntity>>

    @Query("SELECT * FROM venues WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): VenueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<VenueEntity>)

    @Query("DELETE FROM venues")
    suspend fun deleteAll()
}

@Dao
interface PoiDao {
    @Query("SELECT * FROM poi WHERE mapId = :mapId")
    suspend fun getByMapId(mapId: String): List<PoiEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<PoiEntity>)

    @Query("DELETE FROM poi WHERE mapId = :mapId")
    suspend fun deleteByMapId(mapId: String)
}
