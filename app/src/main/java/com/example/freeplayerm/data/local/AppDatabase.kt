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

   abstract fun userDao(): UserDao
   
   abstract fun songDao(): SongDao

   abstract fun artistDao(): ArtistDao

   abstract fun albumDao(): AlbumDao

   abstract fun genreDao(): GenreDao

   abstract fun lyricsDao(): LyricsDao

   // ==================== DAOs DE LISTAS Y FAVORITOS ====================

   abstract fun playlistDao(): PlaylistDao
   
   abstract fun favoriteDao(): FavoriteDao

   // ==================== DAOs DE REPRODUCCIÓN ====================

   abstract fun playbackHistoryDao(): PlaybackHistoryDao

   abstract fun playbackQueueDao(): PlaybackQueueDao

   // ==================== CONFIGURACIÓN ====================
   abstract fun userPreferencesDao(): UserPreferencesDao
   
   companion object {
      const val DATABASE_NAME = "freeplayerm_database"
      
   }
}
