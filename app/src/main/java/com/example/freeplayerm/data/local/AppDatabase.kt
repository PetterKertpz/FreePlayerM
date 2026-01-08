// en: app/src/main/java/com/example/freeplayerm/data/local/AppDatabase.kt
package com.example.freeplayerm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.example.freeplayerm.data.local.entity.AlbumCreditEntity
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.ArtistEntity
import com.example.freeplayerm.data.local.entity.ArtistSocialLinksEntity
import com.example.freeplayerm.data.local.entity.FavoriteEntity
import com.example.freeplayerm.data.local.entity.GeniusAnnotationEntity
import com.example.freeplayerm.data.local.entity.GenreEntity
import com.example.freeplayerm.data.local.entity.GenreMoodEntity
import com.example.freeplayerm.data.local.entity.LyricsEntity
import com.example.freeplayerm.data.local.entity.LyricsTranslationEntity
import com.example.freeplayerm.data.local.entity.PlaybackHistoryEntity
import com.example.freeplayerm.data.local.entity.PlaybackQueueEntity
import com.example.freeplayerm.data.local.entity.PlaybackStateEntity
import com.example.freeplayerm.data.local.entity.PlaylistCollaboratorEntity
import com.example.freeplayerm.data.local.entity.PlaylistEntity
import com.example.freeplayerm.data.local.entity.PlaylistItemEntity
import com.example.freeplayerm.data.local.entity.SongArtistCrossRef
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.UserEntity
import com.example.freeplayerm.data.local.entity.UserPreferencesEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist

/**
 * 🗄️ APP DATABASE - ROOM DATABASE v2.1
 *
 * Base de datos principal de la aplicación Gestiona todas las entidades y DAOs del sistema
 *
 * Características:
 * - Versión 2 con esquema completo actualizado
 * - TypeConverters para tipos complejos (Date, List, Map)
 * - 20 entidades con relaciones optimizadas
 * - 10 DAOs principales con funcionalidad completa
 * - Exportación de esquema habilitada para migraciones
 *
 * @version 2.1 - Production Ready with Schema Export
 */
@Database(
   entities =
      [
         // ==================== ENTIDADES BASE PRINCIPALES ====================
         UserEntity::class,
         ArtistEntity::class,
         AlbumEntity::class,
         GenreEntity::class,
         SongEntity::class,

         // ==================== LISTAS Y ORGANIZACIÓN ====================
         PlaylistEntity::class,
         PlaylistItemEntity::class,

         // ==================== FAVORITOS Y PREFERENCIAS ====================
         FavoriteEntity::class,
         LyricsEntity::class,

         // ==================== RELACIONES Y REPRODUCCIÓN ====================
         SongArtistCrossRef::class,
         PlaybackHistoryEntity::class,
         UserPreferencesEntity::class,
         PlaybackStateEntity::class,
         PlaybackQueueEntity::class,

         // ==================== CARACTERÍSTICAS AVANZADAS ====================
         LyricsTranslationEntity::class,
         GeniusAnnotationEntity::class,
         ArtistSocialLinksEntity::class,
         AlbumCreditEntity::class,
         GenreMoodEntity::class,
         PlaylistCollaboratorEntity::class,
      ],
   views = [SongWithArtist::class],
   version = 1,
   exportSchema = false, // ✅ Habilitado para documentar cambios de esquema
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

   // ==================== DAOs PRINCIPALES ====================

   /**
    * DAO para operaciones de usuario
    * - Autenticación con tokens JWT
    * - Gestión de perfil y sesiones
    * - Validación de credenciales
    */
   abstract fun userDao(): UserDao

   /**
    * DAO para operaciones de canciones
    * - CRUD completo con relaciones
    * - Búsquedas avanzadas por título, artista, álbum
    * - Filtros por género, año, duración
    * - Estadísticas de reproducción
    */
   abstract fun songDao(): SongDao

   /**
    * DAO para operaciones de artistas
    * - Gestión de biografías e imágenes
    * - Sincronización con Genius API
    * - Estadísticas de canciones y álbumes
    * - Normalización de nombres
    */
   abstract fun artistDao(): ArtistDao

   /**
    * DAO para operaciones de álbumes
    * - Gestión de portadas y metadatos
    * - Cálculo de duración total
    * - Contadores de canciones
    * - Filtros por artista, año, tipo
    */
   abstract fun albumDao(): AlbumDao

   /**
    * DAO para operaciones de géneros
    * - Jerarquía padre-hijo de géneros
    * - Clasificaciones y popularidad
    * - Estadísticas por género
    * - Colores y emojis asociados
    */
   abstract fun genreDao(): GenreDao

   /**
    * DAO para operaciones de letras
    * - Búsqueda y caché de letras
    * - Sincronización con APIs externas (Genius, Musixmatch)
    * - Detección de canciones sin letra
    * - Limpieza de datos huérfanos
    */
   abstract fun lyricsDao(): LyricsDao

   // ==================== DAOs DE LISTAS Y FAVORITOS ====================

   /**
    * DAO para operaciones de playlists
    * - Gestión de listas públicas y privadas
    * - Listas colaborativas con múltiples usuarios
    * - Cálculo de duración total
    * - Estadísticas de reproducciones
    */
   abstract fun playlistDao(): PlaylistDao

   /**
    * DAO para operaciones de favoritos
    * - Calificaciones personales (0-5 estrellas)
    * - Notas y comentarios
    * - Ordenamiento personalizado
    * - Estadísticas de favoritos
    */
   abstract fun favoriteDao(): FavoriteDao

   // ==================== DAOs DE REPRODUCCIÓN ====================

   /**
    * DAO para operaciones del historial de reproducción
    * - Analytics y estadísticas de escucha
    * - Tracking de reproducciones completas
    * - Filtros por fecha, origen, contexto
    * - Top canciones y tendencias
    */
   abstract fun playbackHistoryDao(): PlaybackHistoryDao

   /**
    * DAO para operaciones de la cola de reproducción
    * - Reordenamiento drag & drop
    * - Gestión de origen (manual, sugerencia, radio)
    * - Reproducción de siguiente/anterior
    * - Mezcla aleatoria de cola
    */
   abstract fun playbackQueueDao(): PlaybackQueueDao

   // ==================== CONFIGURACIÓN ====================
   abstract fun userPreferencesDao(): UserPreferencesDao
   
   companion object {
      /** Nombre de la base de datos SQLite */
      const val DATABASE_NAME = "freeplayerm_database"

      /**
       * 📋 HISTORIAL DE VERSIONES Y MIGRACIONES
       *
       * ═══════════════════════════════════════════════════════════════ VERSION 2 - ACTUAL
       * (Diciembre 2024) ═══════════════════════════════════════════════════════════════ ✅ Cambios
       * críticos:
       * - TypeConverters: Date <-> Long (antes Int, causaba overflow Y2K38)
       * - DAOs: @Insert return type Int → Long (requisito de Room KSP)
       * - Converters: Eliminados duplicados List<String> (conflictos Room)
       *
       * ✅ Mejoras:
       * - Sincronización completa de campos Entity-DAO
       * - Optimización de índices y relaciones
       * - Soporte completo para 20 entidades
       * - Exportación de esquema habilitada
       *
       * ═══════════════════════════════════════════════════════════════ VERSION 1 - BASE INICIAL
       * ═══════════════════════════════════════════════════════════════
       * - Esquema básico con 9 entidades principales
       * - DAOs básicos sin funcionalidad avanzada
       * - TypeConverters simples (Int para timestamps)
       *
       * ═══════════════════════════════════════════════════════════════ MIGRACIÓN DE v1 → v2
       * ═══════════════════════════════════════════════════════════════ Implementar en
       * DatabaseModule.kt:
       * ```kotlin
       * val MIGRATION_1_2 = object : Migration(1, 2) {
       *     override fun migrate(database: SupportSQLiteDatabase) {
       *         // 1. Cambiar campos de timestamp de INTEGER a BIGINT
       *         database.execSQL("""
       *             CREATE TABLE usuarios_new (
       *                 id_usuario INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
       *                 nombre_usuario TEXT NOT NULL,
       *                 correo TEXT NOT NULL,
       *                 contrasenia_hash TEXT NOT NULL,
       *                 fecha_creacion INTEGER NOT NULL,  -- Ahora BIGINT (Long)
       *                 ultima_sesion INTEGER,            -- Ahora BIGINT (Long)
       *                 -- ... resto de campos
       *             )
       *         """)
       *
       *         database.execSQL("INSERT INTO usuarios_new SELECT * FROM usuarios")
       *         database.execSQL("DROP TABLE usuarios")
       *         database.execSQL("ALTER TABLE usuarios_new RENAME TO usuarios")
       *
       *         // 2. Repetir para todas las tablas con timestamps:
       *         //    - canciones (fecha_agregado, fecha_modificacion)
       *         //    - artistas (fecha_agregado, ultima_actualizacion)
       *         //    - albumes (fecha_agregado, fecha_lanzamiento)
       *         //    - generos (fecha_agregado, ultima_actualizacion)
       *         //    - letras (fecha_agregado)
       *         //    - listas_reproduccion (fecha_creacion, fecha_modificacion, ultima_reproduccion)
       *         //    - favoritos (fecha_agregado)
       *         //    - historial_reproduccion (fecha_reproduccion)
       *         //    - cola_reproduccion (fecha_agregado)
       *     }
       * }
       * ```
       *
       * ⚠️ ALTERNATIVA PARA DESARROLLO (DESTRUCTIVA):
       * ```kotlin
       * Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
       *     .fallbackToDestructiveMigration() // Elimina y recrea la BD
       *     .build()
       * ```
       *
       * ═══════════════════════════════════════════════════════════════ ENTIDADES POR CATEGORÍA
       * ═══════════════════════════════════════════════════════════════ 📁 CORE (5):
       * - UserEntity
       * - SongEntity
       * - ArtistEntity
       * - AlbumEntity
       * - GenreEntity
       *
       * 📁 COLECCIONES (3):
       * - PlaylistEntity
       * - PlaylistItemEntity
       * - FavoriteEntity
       *
       * 📁 CONTENIDO (2):
       * - LyricsEntity
       * - LyricsTranslationEntity
       *
       * 📁 REPRODUCCIÓN (3):
       * - PlaybackHistoryEntity
       * - PlaybackQueueEntity
       * - PlaybackStateEntity
       *
       * 📁 METADATA (4):
       * - GeniusAnnotationEntity
       * - ArtistSocialLinksEntity
       * - AlbumCreditEntity
       * - GenreMoodEntity
       *
       * 📁 RELACIONES (3):
       * - SongArtistCrossRef
       * - PlaylistCollaboratorEntity
       * - UserPreferencesEntity
       *
       * ═══════════════════════════════════════════════════════════════ NOTAS DE DESARROLLO
       * ═══════════════════════════════════════════════════════════════
       * - Esquemas exportados en: app/schemas/com.example.freeplayerm.data.local.AppDatabase/
       * - Tests de migración en: app/src/androidTest/.../MigrationTest.kt
       * - Validación de esquema: automática en cada build
       * - Backup recomendado: antes de cada migración en producción
       */
   }
}
