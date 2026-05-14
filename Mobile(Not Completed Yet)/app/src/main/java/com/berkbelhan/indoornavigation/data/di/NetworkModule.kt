package com.berkbelhan.indoornavigation.data.di

import com.berkbelhan.indoornavigation.data.remote.api.IndoorNavApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Replace with the real backend base URL for production deployment.
    private const val BASE_URL = "https://api.indoornavigation.example.com/v1/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Only log bodies in debug builds; never in release to protect tokens.
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            // TODO: Add CertificatePinner for SSL pinning in production:
            // .certificatePinner(CertificatePinner.Builder()
            //     .add("api.indoornavigation.example.com", "sha256/<pin>")
            //     .build())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideIndoorNavApi(retrofit: Retrofit): IndoorNavApi =
        retrofit.create(IndoorNavApi::class.java)
}
