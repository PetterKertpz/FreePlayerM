// di/ImageLoadingModule.kt
package com.example.freeplayerm.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.example.freeplayerm.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ImageLoadingModule {

   @Provides
   @Singleton
   fun provideImageLoader(
      @ApplicationContext context: Context,
      @ImageClient okHttpClient: OkHttpClient,
   ): ImageLoader {
      return ImageLoader.Builder(context)
         // ⚡ MEMORY CACHE - 25% de RAM
         .memoryCache {
            MemoryCache.Builder(context).maxSizePercent(0.25).weakReferencesEnabled(true).build()
         }

         // ⚡ DISK CACHE - 100MB para persistencia
         .diskCache {
            DiskCache.Builder()
               .directory(context.cacheDir.resolve("image_cache"))
               .maxSizeBytes(100 * 1024 * 1024)
               .build()
         }

         // ⚡ NETWORK - OkHttp optimizado
         .okHttpClient(okHttpClient)

         // ⚡ POLÍTICAS DE CACHE
         .respectCacheHeaders(false)
         .diskCachePolicy(CachePolicy.ENABLED)
         .memoryCachePolicy(CachePolicy.ENABLED)

         // ⚡ UX - Crossfade suave por defecto
         .crossfade(300)

         // ⚡ PERFORMANCE
         .allowHardware(true)
         .allowRgb565(true)

         // 🔧 DEBUG (solo en desarrollo)
         .apply {
            if (BuildConfig.DEBUG) {
               logger(DebugLogger())
            }
         }
         .build()
   }

   @Provides
   @Singleton
   @ImageClient // ← Agregar qualifier
   fun provideImageOkHttpClient(): OkHttpClient {
      return OkHttpClient.Builder()
         .connectTimeout(15, TimeUnit.SECONDS)
         .readTimeout(15, TimeUnit.SECONDS)
         .writeTimeout(15, TimeUnit.SECONDS)
         .connectionPool(
            okhttp3.ConnectionPool(maxIdleConnections = 10, keepAliveDuration = 5, TimeUnit.MINUTES)
         )
         .retryOnConnectionFailure(true)
         .build()
   }
}
