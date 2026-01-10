// en: app/src/main/java/com/example/freeplayerm/data/local/entity/SongEntity.kt
package com.example.freeplayerm.data.local.entity

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
   tableName = "canciones",
   foreignKeys =
      [
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
   indices =
      [
         Index(value = ["archivo_path"], unique = true), // CRÍTICO: Para escaneo O(1)
         Index(value = ["id_artista"]),
         Index(value = ["id_album"]),
         Index(value = ["id_genero"]),
         Index(value = ["titulo"]),
         Index(value = ["veces_reproducida"]),
         Index(value = ["fecha_agregado"]),
         Index(value = ["fecha_modificacion"]),
      ],
)
data class SongEntity(
   @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_cancion") val idCancion: Int = 0,

   // Relaciones
   @ColumnInfo(name = "id_artista") val idArtista: Int?,
   @ColumnInfo(name = "id_album") val idAlbum: Int?,
   @ColumnInfo(name = "id_genero") val idGenero: Int?,

   // Información básica
   @ColumnInfo(name = "titulo") val titulo: String,
   @ColumnInfo(name = "duracion_segundos") val duracionSegundos: Int,
   @ColumnInfo(name = "numero_pista") val numeroPista: Int? = null,
   @ColumnInfo(name = "anio") val anio: Int? = null,

   // Origen y archivo
   @ColumnInfo(name = "origen") val origen: String,
   @ColumnInfo(name = "archivo_path") val archivoPath: String?,
   @ColumnInfo(name = "url_streaming") val urlStreaming: String? = null,

   // Metadatos técnicos (NUEVO)
   @ColumnInfo(name = "tamanio_bytes") val tamanioBytes: Long? = null,
   @ColumnInfo(name = "mime_type") val mimeType: String? = null,
   @ColumnInfo(name = "hash_archivo") val hashArchivo: String? = null,
   @ColumnInfo(name = "bitrate") val bitrate: Int? = null,
   @ColumnInfo(name = "sample_rate") val sampleRate: Int? = null,
   @ColumnInfo(name = "calidad_audio") val calidadAudio: String? = null,

   // Genius API
   @ColumnInfo(name = "genius_id") val geniusId: String? = null,
   @ColumnInfo(name = "genius_url") val geniusUrl: String? = null,
   @ColumnInfo(name = "titulo_completo") val tituloCompleto: String? = null,
   @ColumnInfo(name = "lyrics_state") val lyricsState: String? = null,
   @ColumnInfo(name = "hot") val hot: Boolean = false,
   @ColumnInfo(name = "pageviews") val pageviews: Int? = null,
   @ColumnInfo(name = "idioma") val idioma: String? = null,
   @ColumnInfo(name = "ubicacion_grabacion") val ubicacionGrabacion: String? = null,
   @ColumnInfo(name = "external_ids_json") val externalIdsJson: String? = null,

   // Estadísticas
   @ColumnInfo(name = "veces_reproducida") val vecesReproducida: Int = 0,
   @ColumnInfo(name = "ultima_reproduccion") val ultimaReproduccion: Long? = null,
   @ColumnInfo(name = "fecha_agregado") val fechaAgregado: Long = System.currentTimeMillis(),
   @ColumnInfo(name = "fecha_modificacion") val fechaModificacion: Long? = null,

   // Metadatos adicionales
   @ColumnInfo(name = "letra_disponible") val letraDisponible: Boolean = false,
   @ColumnInfo(name = "portada_path") val portadaPath: String? = null,
) {
   @SuppressLint("DefaultLocale")
   fun duracionFormateada(): String {
      val minutos = duracionSegundos / 60
      val segundos = duracionSegundos % 60
      return String.format("%02d:%02d", minutos, segundos)
   }

   fun esLocal(): Boolean = origen == ORIGEN_LOCAL

   fun esRemota(): Boolean = origen == ORIGEN_REMOTA || origen == ORIGEN_STREAMING

   fun tieneArchivo(): Boolean = !archivoPath.isNullOrBlank()

   fun obtenerPortada(): String? = portadaPath

   companion object {
      const val ORIGEN_LOCAL = "LOCAL"
      const val ORIGEN_REMOTA = "REMOTA"
      const val ORIGEN_STREAMING = "STREAMING"

      const val CALIDAD_LOW = "LOW"
      const val CALIDAD_MEDIUM = "MEDIUM"
      const val CALIDAD_HIGH = "HIGH"
      const val CALIDAD_LOSSLESS = "LOSSLESS"

      fun determinarCalidad(bitrate: Int?): String {
         return when {
            bitrate == null -> CALIDAD_MEDIUM
            bitrate >= 320 -> CALIDAD_LOSSLESS
            bitrate >= 256 -> CALIDAD_HIGH
            bitrate >= 128 -> CALIDAD_MEDIUM
            else -> CALIDAD_LOW
         }
      }
   }
}
