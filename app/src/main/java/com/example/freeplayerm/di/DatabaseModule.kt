// en: app/src/main/java/com/example/freeplayerm/di/DatabaseModule.kt
package com.example.freeplayerm.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🔧 DATABASE MODULE - HILT DI v8.0
 *
 * Módulo de Hilt para proveer la base de datos Room Incluye configuración de migraciones, callbacks
 * y fallbacks
 *
 * Características:
 * - Singleton de base de datos con Room
 * - 10 DAOs provistos con inyección de dependencias
 * - Callback para datos iniciales (géneros)
 * - Migraciones configuradas
 * - Fallback destructivo para desarrollo
 *
 * @version 8.0 - Complete DAOs Provider
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

   // ==================== PROVEER BASE DE DATOS ====================

   /** Provee la instancia singleton de la base de datos */
   @Provides
   @Singleton
   fun provideDatabase(
      @ApplicationContext context: Context,
      callback: RoomDatabase.Callback,
   ): AppDatabase {
      return Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
         // ⚠️ PARA DESARROLLO: Permite recrear BD en conflictos
         // ⚠️ PARA PRODUCCIÓN: Cambiar a false e implementar migraciones
         .fallbackToDestructiveMigration(true) // ⬅️ Cambiar a false en producción

         // Para PRODUCCIÓN (descomentar cuando tengas usuarios reales):
         // .fallbackToDestructiveMigration(false)
         // .addMigrations(MIGRATION_6_7, MIGRATION_7_8)

         // Callback para datos iniciales
         .addCallback(callback)
         .build()
   }

   // ==================== PROVEER DAOs PRINCIPALES ====================
   @Provides
   @Singleton
   fun provideUserPreferencesDao(database: AppDatabase): UserPreferencesDao {
      return database.userPreferencesDao()
   }

   /** Provee DAO de usuarios Incluye autenticación, perfil y sesiones */
   @Provides
   @Singleton
   fun provideUsuarioDao(database: AppDatabase): UserDao {
      return database.userDao()
   }

   /** Provee DAO de canciones Incluye CRUD completo y búsquedas */
   @Provides
   @Singleton
   fun provideCancionDao(database: AppDatabase): SongDao {
      return database.songDao()
   }

   /** Provee DAO de artistas Incluye biografías y estadísticas */
   @Provides
   @Singleton
   fun provideArtistaDao(database: AppDatabase): ArtistDao {
      return database.artistDao()
   }

   /** Provee DAO de álbumes Incluye tracks y metadatos */
   @Provides
   @Singleton
   fun provideAlbumDao(database: AppDatabase): AlbumDao {
      return database.albumDao()
   }

   /** Provee DAO de géneros Incluye jerarquías y clasificaciones */
   @Provides
   @Singleton
   fun provideGeneroDao(database: AppDatabase): GenreDao {
      return database.genreDao()
   }

   /** Provee DAO de letras Incluye caché y sincronización */
   @Provides
   @Singleton
   fun provideLetraDao(database: AppDatabase): LyricsDao {
      return database.lyricsDao()
   }

   // ==================== PROVEER DAOs DE LISTAS ====================

   /** Provee DAO de listas de reproducción Incluye playlists colaborativas y públicas */
   @Provides
   @Singleton
   fun provideListaReproduccionDao(database: AppDatabase): PlaylistDao {
      return database.playlistDao()
   }

   /** Provee DAO de favoritos Incluye calificaciones y ordenamiento */
   @Provides
   @Singleton
   fun provideFavoritoDao(database: AppDatabase): FavoriteDao {
      return database.favoriteDao()
   }

   // ==================== PROVEER DAOs DE REPRODUCCIÓN ====================

   /** Provee DAO de historial de reproducción Incluye analytics y estadísticas */
   @Provides
   @Singleton
   fun provideHistorialReproduccionDao(database: AppDatabase): PlaybackHistoryDao {
      return database.playbackHistoryDao()
   }

   /** Provee DAO de cola de reproducción Incluye reordenamiento y gestión */
   @Provides
   @Singleton
   fun provideColaReproduccionDao(database: AppDatabase): PlaybackQueueDao {
      return database.playbackQueueDao()
   }

   // ==================== CALLBACK PARA DATOS INICIALES ====================

   /**
    * Callback para poblar datos iniciales al crear la BD Inserta géneros principales con sus
    * configuraciones
    */
   @Provides
   @Singleton
   fun provideDatabaseCallback(): RoomDatabase.Callback {
      return object : RoomDatabase.Callback() {
         override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            
            // ==================== INSERTAR GÉNEROS POR DEFECTO ====================
            GenreEntity.GENEROS_PRINCIPALES.forEach { nombre ->
               val nombreNormalizado = GenreEntity.normalizar(nombre)
               val color = GenreEntity.obtenerColorSugerido(nombre) ?: "#666666"
               val emoji = GenreEntity.obtenerEmojiSugerido(nombre) ?: "🎵"
               val timestamp = System.currentTimeMillis()
               
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
   }
}
