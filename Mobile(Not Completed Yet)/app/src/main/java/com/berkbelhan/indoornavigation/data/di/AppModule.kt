package com.berkbelhan.indoornavigation.data.di

import androidx.work.Configuration
import com.berkbelhan.indoornavigation.core.dispatcher.DefaultDispatcherProvider
import com.berkbelhan.indoornavigation.core.dispatcher.DispatcherProvider
import com.berkbelhan.indoornavigation.core.flags.FeatureFlags
import com.berkbelhan.indoornavigation.data.local.datastore.DataStoreFeatureFlags
import com.berkbelhan.indoornavigation.data.local.datastore.SettingsDataStore
import com.berkbelhan.indoornavigation.data.multiset.CloudLocalizationProvider
import com.berkbelhan.indoornavigation.data.multiset.HybridLocalizationProvider
import com.berkbelhan.indoornavigation.data.multiset.LocalizationProvider
import com.berkbelhan.indoornavigation.data.repository.AuthRepositoryImpl
import com.berkbelhan.indoornavigation.data.repository.LocalizationRepositoryImpl
import com.berkbelhan.indoornavigation.data.repository.MapBundleRepositoryImpl
import com.berkbelhan.indoornavigation.data.repository.VenueRepositoryImpl
import com.berkbelhan.indoornavigation.domain.repository.AuthRepository
import com.berkbelhan.indoornavigation.domain.repository.LocalizationRepository
import com.berkbelhan.indoornavigation.domain.repository.MapBundleRepository
import com.berkbelhan.indoornavigation.domain.repository.SettingsRepository
import com.berkbelhan.indoornavigation.domain.repository.VenueRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    @Binds
    @Singleton
    abstract fun bindFeatureFlags(impl: DataStoreFeatureFlags): FeatureFlags

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsDataStore): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLocalizationRepository(impl: LocalizationRepositoryImpl): LocalizationRepository

    @Binds
    @Singleton
    abstract fun bindMapBundleRepository(impl: MapBundleRepositoryImpl): MapBundleRepository

    @Binds
    @Singleton
    abstract fun bindVenueRepository(impl: VenueRepositoryImpl): VenueRepository

    /**
     * Bind the hybrid provider as the active localization provider.
     * Swap this to [CloudLocalizationProvider] or [OnDeviceLocalizationProvider]
     * to lock to a specific mode.
     */
    @Binds
    @Singleton
    abstract fun bindLocalizationProvider(impl: HybridLocalizationProvider): LocalizationProvider
}
