package com.berkbelhan.indoornavigation.data.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.berkbelhan.indoornavigation.data.local.room.dao.MapBundleDao
import com.berkbelhan.indoornavigation.data.local.room.dao.PoiDao
import com.berkbelhan.indoornavigation.data.local.room.dao.VenueDao
import com.berkbelhan.indoornavigation.data.local.room.entity.MapBundleEntity
import com.berkbelhan.indoornavigation.data.local.room.entity.PoiEntity
import com.berkbelhan.indoornavigation.data.local.room.entity.VenueEntity

@Database(
    entities = [
        MapBundleEntity::class,
        VenueEntity::class,
        PoiEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mapBundleDao(): MapBundleDao
    abstract fun venueDao(): VenueDao
    abstract fun poiDao(): PoiDao

    companion object {
        const val DATABASE_NAME = "indoor_navigation.db"
    }
}
