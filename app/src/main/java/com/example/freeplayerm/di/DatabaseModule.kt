// en: app/src/main/java/com/example/freeplayerm/di/DatabaseModule.kt
package com.example.freeplayerm.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.freeplayerm.data.local.AppDatabase
import com.example.freeplayerm.data.local.dao.AlbumDao
import com.example.freeplayerm.data.local.dao.ArtistDao
import com.example.freeplayerm.data.local.dao.FavoriteDao
import com.example.freeplayerm.data.local.dao.GenreDao
import com.example.freeplayerm.data.local.dao.LyricsDao
import com.example.freeplayerm.data.local.dao.PlaybackHistoryDao
import com.example.freeplayerm.data.local.dao.PlaybackQueueDao
import com.example.freeplayerm.data.local.dao.PlaylistDao
import com.example.freeplayerm.data.local.dao.SongDao
import com.example.freeplayerm.data.local.dao.UserDao
import com.example.freeplayerm.data.local.dao.UserPreferencesDao
import com.example.freeplayerm.data.local.entity.GenreEntity
import com.example.freeplayerm.utils.ScanNotificationHelper
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
   fun provideDatabase(
      @ApplicationContext context: Context,
      callback: RoomDatabase.Callback,
   ): AppDatabase {
      return Room.databaseBuilder(
         context,
         AppDatabase::class.java,
         AppDatabase.DATABASE_NAME
      )
         .fallbackToDestructiveMigration(true)
         .addCallback(callback)
         .build()
   }
   
   // DAOs principales
   @Provides
   @Singleton
   fun provideUserPreferencesDao(database: AppDatabase): UserPreferencesDao =
      database.userPreferencesDao()
   
   @Provides
   @Singleton
   fun provideUserDao(database: AppDatabase): UserDao =
      database.userDao()
   
   @Provides
   @Singleton
   fun provideSongDao(database: AppDatabase): SongDao =
      database.songDao()
   
   @Provides
   @Singleton
   fun provideArtistDao(database: AppDatabase): ArtistDao =
      database.artistDao()
   
   @Provides
   @Singleton
   fun provideAlbumDao(database: AppDatabase): AlbumDao =
      database.albumDao()
   
   @Provides
   @Singleton
   fun provideGenreDao(database: AppDatabase): GenreDao =
      database.genreDao()
   
   @Provides
   @Singleton
   fun provideLyricsDao(database: AppDatabase): LyricsDao =
      database.lyricsDao()
   
   // DAOs de listas y favoritos
   @Provides
   @Singleton
   fun providePlaylistDao(database: AppDatabase): PlaylistDao =
      database.playlistDao()
   
   @Provides
   @Singleton
   fun provideFavoriteDao(database: AppDatabase): FavoriteDao =
      database.favoriteDao()
   
   // DAOs de reproduccion
   @Provides
   @Singleton
   fun providePlaybackHistoryDao(database: AppDatabase): PlaybackHistoryDao =
      database.playbackHistoryDao()
   
   @Provides
   @Singleton
   fun providePlaybackQueueDao(database: AppDatabase): PlaybackQueueDao =
      database.playbackQueueDao()
   
   // Callback para datos iniciales
   @Provides
   @Singleton
   fun provideDatabaseCallback(): RoomDatabase.Callback {
      return object : RoomDatabase.Callback() {
         override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            insertarGenerosIniciales(db)
         }
      }
   }
   
   @Provides
   @Singleton
   fun provideScanNotificationHelper(
      @ApplicationContext context: Context
   ): ScanNotificationHelper {
      return ScanNotificationHelper(context)
   }
   
   private fun insertarGenerosIniciales(db: SupportSQLiteDatabase) {
      val timestamp = System.currentTimeMillis()
      
      GenreEntity.GENEROS_PRINCIPALES.forEach { nombre ->
         val nombreNormalizado = GenreEntity.normalizar(nombre)
         val color = GenreEntity.obtenerColorSugerido(nombre) ?: "#666666"
         val emoji = GenreEntity.obtenerEmojiSugerido(nombre) ?: "music"
         
         db.execSQL(
            """
                INSERT OR IGNORE INTO generos (
                    nombre,
                    nombre_normalizado,
                    color,
                    emoji,
                    es_popular,
                    fecha_agregado,
                    ultima_actualizacion
                ) VALUES (?, ?, ?, ?, 1, ?, ?)
                """,
            arrayOf<Any>(nombre, nombreNormalizado, color, emoji, timestamp, timestamp),
         )
      }
   }
}