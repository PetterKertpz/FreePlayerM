// en: app/src/main/java/com/example/freeplayerm/data/local/entity/SongEntity.kt
package com.example.freeplayerm.data.local.entity

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 🎵 SONG ENTITY v2.1
 *
 * Entidad que representa una canción en la base de datos
 * Optimizada para el Sistema de Purificación de Metadata
 *
 * Características:
 * - Sistema de estados para tracking del pipeline de purificación
 * - Confidence score para calidad de metadata
 * - Soporte para featured artists y versiones
 * - Preservación de datos originales para auditoría
 * - Índices optimizados para queries frecuentes
 *
 * @version 2.1 - Sistema de Purificación de Metadata
 */
@Entity(
   tableName = "canciones",
   foreignKeys = [
      ForeignKey(
         entity = ArtistEntity::class,
         parentColumns = ["id_artista"],
         childColumns = ["id_artista"],
         onDelete = ForeignKey.SET_NULL,
      ),
      ForeignKey(
         entity = AlbumEntity::class,
         parentColumns = ["id_album"],
         childColumns = ["id_album"],
         onDelete = ForeignKey.SET_NULL,
      ),
      ForeignKey(
         entity = GenreEntity::class,
         parentColumns = ["id_genero"],
         childColumns = ["id_genero"],
         onDelete = ForeignKey.SET_NULL,
      ),
   ],
   indices = [
      Index(value = ["archivo_path"], unique = true),  // CRÍTICO: Escaneo O(1)
      Index(value = ["id_artista"]),
      Index(value = ["id_album"]),
      Index(value = ["id_genero"]),
      Index(value = ["titulo"]),
      Index(value = ["veces_reproducida"]),
      Index(value = ["fecha_agregado"]),
      Index(value = ["fecha_modificacion"]),
      Index(value = ["metadata_status"]),              // NUEVO: Para queries por estado
      Index(value = ["confidence_score"]),             // NUEVO: Para ordenar por calidad
      Index(value = ["letra_disponible"]),             // NUEVO: Para filtrar canciones sin letra
   ],
)
data class SongEntity(
   @PrimaryKey(autoGenerate = true)
   @ColumnInfo(name = "id_cancion")
   val idCancion: Int = 0,
   
   // ==================== RELACIONES ====================
   
   @ColumnInfo(name = "id_artista")
   val idArtista: Int?,
   
   @ColumnInfo(name = "id_album")
   val idAlbum: Int?,
   
   @ColumnInfo(name = "id_genero")
   val idGenero: Int?,
   
   // ==================== INFORMACIÓN BÁSICA ====================
   
   @ColumnInfo(name = "titulo")
   val titulo: String,
   
   @ColumnInfo(name = "duracion_segundos")
   val duracionSegundos: Int,
   
   @ColumnInfo(name = "numero_pista")
   val numeroPista: Int? = null,
   
   @ColumnInfo(name = "anio")
   val anio: Int? = null,
   
   // ==================== DATOS ORIGINALES (AUDITORÍA) ====================
   
   /**
    * Título original tal como viene del archivo/MediaStore
    * Se preserva para auditoría y posible rollback
    */
   @ColumnInfo(name = "titulo_original")
   val tituloOriginal: String? = null,
   
   /**
    * Artista original tal como viene del archivo/MediaStore
    */
   @ColumnInfo(name = "artista_original")
   val artistaOriginal: String? = null,
   
   // ==================== FEATURED ARTISTS Y VERSIÓN ====================
   
   /**
    * Lista de artistas colaboradores en formato JSON
    * Ejemplo: ["Bad Bunny", "J Balvin"]
    */
   @ColumnInfo(name = "featured_artists_json")
   val featuredArtistsJson: String? = null,
   
   /**
    * Tipo de versión de la canción
    * Valores: REMIX, COVER, ACOUSTIC, LIVE, INSTRUMENTAL, KARAOKE, DEMO, RADIO_EDIT, EXTENDED, etc.
    */
   @ColumnInfo(name = "version_type")
   val versionType: String? = null,
   
   // ==================== ORIGEN Y ARCHIVO ====================
   
   @ColumnInfo(name = "origen")
   val origen: String,
   
   @ColumnInfo(name = "archivo_path")
   val archivoPath: String?,
   
   @ColumnInfo(name = "url_streaming")
   val urlStreaming: String? = null,
   
   // ==================== METADATOS TÉCNICOS ====================
   
   @ColumnInfo(name = "tamanio_bytes")
   val tamanioBytes: Long? = null,
   
   @ColumnInfo(name = "mime_type")
   val mimeType: String? = null,
   
   @ColumnInfo(name = "hash_archivo")
   val hashArchivo: String? = null,
   
   @ColumnInfo(name = "bitrate")
   val bitrate: Int? = null,
   
   @ColumnInfo(name = "sample_rate")
   val sampleRate: Int? = null,
   
   @ColumnInfo(name = "calidad_audio")
   val calidadAudio: String? = null,
   
   // ==================== GENIUS API ====================
   
   @ColumnInfo(name = "genius_id")
   val geniusId: String? = null,
   
   @ColumnInfo(name = "genius_url")
   val geniusUrl: String? = null,
   
   @ColumnInfo(name = "titulo_completo")
   val tituloCompleto: String? = null,
   
   @ColumnInfo(name = "lyrics_state")
   val lyricsState: String? = null,
   
   @ColumnInfo(name = "hot")
   val hot: Boolean = false,
   
   @ColumnInfo(name = "pageviews")
   val pageviews: Int? = null,
   
   @ColumnInfo(name = "idioma")
   val idioma: String? = null,
   
   @ColumnInfo(name = "ubicacion_grabacion")
   val ubicacionGrabacion: String? = null,
   
   @ColumnInfo(name = "external_ids_json")
   val externalIdsJson: String? = null,
   
   // ==================== ESTADÍSTICAS ====================
   
   @ColumnInfo(name = "veces_reproducida")
   val vecesReproducida: Int = 0,
   
   @ColumnInfo(name = "ultima_reproduccion")
   val ultimaReproduccion: Long? = null,
   
   @ColumnInfo(name = "fecha_agregado")
   val fechaAgregado: Long = System.currentTimeMillis(),
   
   @ColumnInfo(name = "fecha_modificacion")
   val fechaModificacion: Long? = null,
   
   // ==================== SISTEMA DE PURIFICACIÓN ====================
   
   /**
    * Si la letra está disponible
    */
   @ColumnInfo(name = "letra_disponible")
   val letraDisponible: Boolean = false,
   
   /**
    * Estado actual en el pipeline de purificación
    * @see MetadataStatus
    */
   @ColumnInfo(name = "metadata_status")
   val metadataStatus: String = STATUS_DIRTY,
   
   /**
    * Score de confianza de la metadata (0-100)
    * - 90-100: EXCELLENT (metadata totalmente verificada)
    * - 80-89: GOOD (metadata confiable)
    * - 70-79: FAIR (metadata aceptable)
    * - 60-69: POOR (metadata parcial)
    * - 0-59: BAD (metadata no confiable)
    */
   @ColumnInfo(name = "confidence_score")
   val confidenceScore: Int = 0,
   
   /**
    * Fuente principal de la metadata
    * @see MetadataSource
    */
   @ColumnInfo(name = "metadata_source")
   val metadataSource: String = SOURCE_FILE,
   
   /**
    * JSON con datos crudos de la API para auditoría
    */
   @ColumnInfo(name = "raw_api_data")
   val rawApiData: String? = null,
   
   /**
    * Timestamp de la última sincronización con API
    */
   @ColumnInfo(name = "last_api_sync")
   val lastApiSync: Long? = null,
   
   /**
    * Número de intentos fallidos de enriquecimiento
    */
   @ColumnInfo(name = "enrichment_attempts")
   val enrichmentAttempts: Int = 0,
   
   // ==================== PORTADA ====================
   
   @ColumnInfo(name = "portada_path")
   val portadaPath: String? = null,
   
   @ColumnInfo(name = "portada_url")
   val portadaUrl: String? = null,
) {
   // ==================== FUNCIONES DE UTILIDAD ====================
   
   @SuppressLint("DefaultLocale")
   fun duracionFormateada(): String {
      val minutos = duracionSegundos / 60
      val segundos = duracionSegundos % 60
      return String.format("%02d:%02d", minutos, segundos)
   }
   
   fun esLocal(): Boolean = origen == ORIGEN_LOCAL
   
   fun esRemota(): Boolean = origen == ORIGEN_REMOTA || origen == ORIGEN_STREAMING
   
   fun tieneArchivo(): Boolean = !archivoPath.isNullOrBlank()
   
   fun obtenerPortada(): String? = portadaPath ?: portadaUrl
   
   fun tienePortada(): Boolean = !portadaPath.isNullOrBlank() || !portadaUrl.isNullOrBlank()
   
   /**
    * Verifica si la canción necesita enriquecimiento
    */
   fun necesitaEnriquecimiento(): Boolean {
      return metadataStatus in listOf(STATUS_DIRTY, STATUS_CLEANED_LOCAL) &&
            enrichmentAttempts < MAX_ENRICHMENT_ATTEMPTS
   }
   
   /**
    * Verifica si la metadata es confiable (score >= 80)
    */
   fun tieneMetadataConfiable(): Boolean = confidenceScore >= 80
   
   /**
    * Verifica si está verificada completamente
    */
   fun estaVerificada(): Boolean = metadataStatus == STATUS_VERIFIED
   
   /**
    * Obtiene el nivel de calidad basado en el score
    */
   fun obtenerNivelCalidad(): QualityLevel {
      return when (confidenceScore) {
         in 90..100 -> QualityLevel.EXCELLENT
         in 80..89 -> QualityLevel.GOOD
         in 70..79 -> QualityLevel.FAIR
         in 60..69 -> QualityLevel.POOR
         else -> QualityLevel.BAD
      }
   }
   
   /**
    * Verifica si puede reintentar enriquecimiento después de fallo
    */
   fun puedeReintentarEnriquecimiento(): Boolean {
      if (metadataStatus != STATUS_API_NOT_FOUND) return true
      
      // Permitir reintento después de 7 días
      val lastSync = lastApiSync ?: return true
      val daysSinceLastSync = (System.currentTimeMillis() - lastSync) / (1000 * 60 * 60 * 24)
      return daysSinceLastSync >= 7
   }
   
   // ==================== ENUMS ====================
   
   enum class QualityLevel {
      EXCELLENT,  // 90-100
      GOOD,       // 80-89
      FAIR,       // 70-79
      POOR,       // 60-69
      BAD         // 0-59
   }
   
   companion object {
      // ==================== CONSTANTES DE ORIGEN ====================
      const val ORIGEN_LOCAL = "LOCAL"
      const val ORIGEN_REMOTA = "REMOTA"
      const val ORIGEN_STREAMING = "STREAMING"
      
      // ==================== CONSTANTES DE CALIDAD DE AUDIO ====================
      const val CALIDAD_LOW = "LOW"
      const val CALIDAD_MEDIUM = "MEDIUM"
      const val CALIDAD_HIGH = "HIGH"
      const val CALIDAD_LOSSLESS = "LOSSLESS"
      
      // ==================== ESTADOS DE METADATA ====================
      /**
       * Estado inicial - datos crudos sin procesar
       */
      const val STATUS_DIRTY = "DIRTY"
      
      /**
       * Limpieza local completada (sin API)
       */
      const val STATUS_CLEANED_LOCAL = "CLEANED_LOCAL"
      
      /**
       * Datos de API agregados, pendiente validación
       */
      const val STATUS_ENRICHED = "ENRICHED"
      
      /**
       * Datos de API limpiados y validados
       */
      const val STATUS_REFINED = "REFINED"
      
      /**
       * Completamente verificado (score >= 80)
       */
      const val STATUS_VERIFIED = "VERIFIED"
      
      /**
       * Parcialmente verificado (score 60-79)
       */
      const val STATUS_PARTIAL_VERIFIED = "PARTIAL_VERIFIED"
      
      /**
       * No encontrado en API, mantener datos locales
       */
      const val STATUS_API_NOT_FOUND = "API_NOT_FOUND"
      
      /**
       * Error crítico, requiere revisión manual
       */
      const val STATUS_FAILED = "FAILED"
      
      // ==================== FUENTES DE METADATA ====================
      const val SOURCE_FILE = "FILE_TAGS"
      const val SOURCE_GENIUS = "GENIUS_API"
      const val SOURCE_SCRAPER = "WEB_SCRAPER"
      const val SOURCE_MANUAL = "USER_EDIT"
      const val SOURCE_CONSOLIDATED = "CONSOLIDATED"
      
      // ==================== TIPOS DE VERSIÓN ====================
      const val VERSION_REMIX = "REMIX"
      const val VERSION_COVER = "COVER"
      const val VERSION_ACOUSTIC = "ACOUSTIC"
      const val VERSION_LIVE = "LIVE"
      const val VERSION_INSTRUMENTAL = "INSTRUMENTAL"
      const val VERSION_KARAOKE = "KARAOKE"
      const val VERSION_DEMO = "DEMO"
      const val VERSION_RADIO_EDIT = "RADIO_EDIT"
      const val VERSION_EXTENDED = "EXTENDED"
      const val VERSION_SLOWED_REVERB = "SLOWED_REVERB"
      const val VERSION_SPED_UP = "SPED_UP"
      
      // ==================== CONFIGURACIÓN ====================
      const val MAX_ENRICHMENT_ATTEMPTS = 3
      const val MIN_CONFIDENCE_FOR_VERIFIED = 80
      const val MIN_CONFIDENCE_FOR_PARTIAL = 60
      
      /**
       * Determina la calidad de audio basada en bitrate
       */
      fun determinarCalidad(bitrate: Int?): String {
         return when {
            bitrate == null -> CALIDAD_MEDIUM
            bitrate >= 320 -> CALIDAD_LOSSLESS
            bitrate >= 256 -> CALIDAD_HIGH
            bitrate >= 128 -> CALIDAD_MEDIUM
            else -> CALIDAD_LOW
         }
      }
      
      /**
       * Lista de todos los estados válidos
       */
      val ALL_STATUSES = listOf(
         STATUS_DIRTY,
         STATUS_CLEANED_LOCAL,
         STATUS_ENRICHED,
         STATUS_REFINED,
         STATUS_VERIFIED,
         STATUS_PARTIAL_VERIFIED,
         STATUS_API_NOT_FOUND,
         STATUS_FAILED
      )
      
      /**
       * Estados que permiten enriquecimiento
       */
      val ENRICHABLE_STATUSES = listOf(
         STATUS_DIRTY,
         STATUS_CLEANED_LOCAL
      )
      
      /**
       * Estados considerados "completos"
       */
      val COMPLETE_STATUSES = listOf(
         STATUS_VERIFIED,
         STATUS_PARTIAL_VERIFIED
      )
   }
}