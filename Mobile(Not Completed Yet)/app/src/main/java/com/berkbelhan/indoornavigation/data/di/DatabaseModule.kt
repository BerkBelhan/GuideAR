package com.berkbelhan.indoornavigation.data.di

import android.content.Context
import androidx.room.Room
import com.berkbelhan.indoornavigation.data.local.room.AppDatabase
import com.berkbelhan.indoornavigation.data.local.room.dao.MapBundleDao
import com.berkbelhan.indoornavigation.data.local.room.dao.PoiDao
import com.berkbelhan.indoornavigation.data.local.room.dao.VenueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMapBundleDao(db: AppDatabase): MapBundleDao = db.mapBundleDao()

    @Provides
    fun provideVenueDao(db: AppDatabase): VenueDao = db.venueDao()

    @Provides
    fun providePoiDao(db: AppDatabase): PoiDao = db.poiDao()
}
