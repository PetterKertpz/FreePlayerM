package com.example.freeplayerm.di

import android.content.Context
import coil.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        @ImageClient okHttpClient: OkHttpClient
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .allowRgb565(true)
            .dispatcher(Dispatchers.IO)
            .okHttpClient(okHttpClient)
            .build()
    }

    // Proveedor de OkHttpClient específico para imágenes
    @Provides
    @Singleton
    @ImageClient
    fun provideImageOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }
}