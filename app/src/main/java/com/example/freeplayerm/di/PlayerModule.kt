package com.example.freeplayerm.di

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.io.File
import javax.inject.Singleton

@UnstableApi
@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {
   
   private const val DEFAULT_CACHE_SIZE_MB = 500L
   private const val BYTES_PER_MB = 1024L * 1024L
   
   @Provides
   @Singleton
   fun provideAudioAttributes(): AudioAttributes =
      AudioAttributes.Builder()
         .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
         .setUsage(C.USAGE_MEDIA)
         .build()
   
   @Provides
   @Singleton
   fun provideDatabaseProvider(
      @ApplicationContext context: Context
   ): StandaloneDatabaseProvider = StandaloneDatabaseProvider(context)
   
   @Provides
   @Singleton
   fun provideCache(
      @ApplicationContext context: Context,
      databaseProvider: StandaloneDatabaseProvider,
      sessionRepository: SessionRepository,
      preferencesRepository: UserPreferencesRepository
   ): SimpleCache {
      val cacheSizeMb = runBlocking {
         val userId = sessionRepository.idDeUsuarioActivo.firstOrNull()
         if (userId != null) {
            preferencesRepository.obtenerPreferenciasPorId(userId)?.cacheSizeMb?.toLong()
               ?: DEFAULT_CACHE_SIZE_MB
         } else {
            DEFAULT_CACHE_SIZE_MB
         }
      }
      
      val cacheDir = File(context.cacheDir, "media_cache")
      val cacheSize = cacheSizeMb * BYTES_PER_MB
      val evictor = LeastRecentlyUsedCacheEvictor(cacheSize)
      
      return SimpleCache(cacheDir, evictor, databaseProvider)
   }
   
   @Provides
   @Singleton
   fun provideCacheDataSourceFactory(
      @ApplicationContext context: Context,
      cache: SimpleCache
   ): CacheDataSource.Factory {
      val upstreamFactory = DefaultDataSource.Factory(context)
      
      return CacheDataSource.Factory()
         .setCache(cache)
         .setUpstreamDataSourceFactory(upstreamFactory)
         .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
   }
   
   @Provides
   @Singleton
   fun provideExoPlayer(
      @ApplicationContext context: Context,
      audioAttributes: AudioAttributes,
      cacheDataSourceFactory: CacheDataSource.Factory
   ): ExoPlayer {
      val mediaSourceFactory = DefaultMediaSourceFactory(context)
         .setDataSourceFactory(cacheDataSourceFactory)
      
      return ExoPlayer.Builder(context)
         .setMediaSourceFactory(mediaSourceFactory)
         .setAudioAttributes(audioAttributes, true)
         .setHandleAudioBecomingNoisy(true)
         .setWakeMode(C.WAKE_MODE_LOCAL)
         .build()
   }
   
   @Provides
   @Singleton
   fun providePlayer(exoPlayer: ExoPlayer): Player = exoPlayer
}