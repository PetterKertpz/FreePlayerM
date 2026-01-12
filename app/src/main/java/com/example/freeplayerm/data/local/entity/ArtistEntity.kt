// en: app/src/main/java/com/example/freeplayerm/data/local/entity/ArtistEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.Normalizer

/**
 * 🎤 ARTISTA ENTITY v2.1
 *
 * Entidad que representa un artista musical
 * Incluye información biográfica, imágenes y metadata
 *
 * Características:
 * - Índice único en nombre para evitar duplicados
 * - Integración con Genius API
 * - Soporte para múltiples imágenes
 * - Verificación de artistas populares
 * - Estadísticas de reproducción
 * - Normalización Unicode correcta
 *
 * @version 2.1 - Sistema de Purificación de Metadata
 */
@Entity(
   tableName = "artistas",
   indices = [
      Index(value = ["nombre"], unique = true),
      Index(value = ["nombre_normalizado"]),  // Para búsquedas sin acentos
      Index(value = ["es_popular"]),
      Index(value = ["fecha_agregado"]),
      Index(value = ["genius_id"]),           // NUEVO: Para lookup rápido por Genius ID
      Index(value = ["fuente"]),              // NUEVO: Para filtrar por fuente
   ],
)
data class ArtistEntity(
   @PrimaryKey(autoGenerate = true)
   @ColumnInfo(name = "id_artista")
   val idArtista: Int = 0,
   
   // ==================== INFORMACIÓN BÁSICA ====================
   
   @ColumnInfo(name = "nombre")
   val nombre: String,
   
   /**
    * Nombre normalizado para búsquedas (lowercase, sin acentos)
    * Se calcula automáticamente usando Unicode NFD
    */
   @ColumnInfo(name = "nombre_normalizado")
   val nombreNormalizado: String = normalizar(nombre),
   
   /**
    * Nombre real del artista (si es diferente del artístico)
    * Ejemplo: "Aubrey Drake Graham" para "Drake"
    */
   @ColumnInfo(name = "nombre_real")
   val nombreReal: String? = null,
   
   /**
    * Aliases o nombres alternativos del artista
    * JSON array: ["Drizzy", "Champagne Papi"]
    */
   @ColumnInfo(name = "aliases_json")
   val aliasesJson: String? = null,
   
   // ==================== INFORMACIÓN GEOGRÁFICA ====================
   
   @ColumnInfo(name = "pais_origen")
   val paisOrigen: String? = null,
   
   @ColumnInfo(name = "ciudad_origen")
   val ciudadOrigen: String? = null,
   
   // ==================== BIOGRAFÍA ====================
   
   @ColumnInfo(name = "descripcion")
   val descripcion: String? = null,
   
   @ColumnInfo(name = "biografia")
   val biografia: String? = null,
   
   @ColumnInfo(name = "fecha_nacimiento")
   val fechaNacimiento: Long? = null,
   
   @ColumnInfo(name = "fecha_inicio_carrera")
   val fechaInicioCarrera: Long? = null,
   
   // ==================== IMÁGENES ====================
   
   @ColumnInfo(name = "image_url")
   val imageUrl: String? = null,
   
   @ColumnInfo(name = "image_path_local")
   val imagePathLocal: String? = null,
   
   @ColumnInfo(name = "header_image_url")
   val headerImageUrl: String? = null,
   
   @ColumnInfo(name = "thumbnail_url")
   val thumbnailUrl: String? = null,
   
   @ColumnInfo(name = "banner_url")
   val bannerUrl: String? = null,
   
   // ==================== GENIUS API ====================
   
   @ColumnInfo(name = "genius_id")
   val geniusId: String? = null,
   
   @ColumnInfo(name = "genius_url")
   val geniusUrl: String? = null,
   
   /**
    * IQ score del artista en Genius (si disponible)
    */
   @ColumnInfo(name = "genius_iq")
   val geniusIq: Int? = null,
   
   // ==================== REDES SOCIALES Y WEB ====================
   
   @ColumnInfo(name = "sitio_web")
   val sitioWeb: String? = null,
   
   @Deprecated("Usar ArtistSocialLinksEntity", ReplaceWith("ArtistSocialLinksEntity"))
   @ColumnInfo(name = "instagram")
   val instagram: String? = null,
   
   @Deprecated("Usar ArtistSocialLinksEntity", ReplaceWith("ArtistSocialLinksEntity"))
   @ColumnInfo(name = "twitter")
   val twitter: String? = null,
   
   @Deprecated("Usar ArtistSocialLinksEntity", ReplaceWith("ArtistSocialLinksEntity"))
   @ColumnInfo(name = "facebook")
   val facebook: String? = null,
   
   @Deprecated("Usar ArtistSocialLinksEntity", ReplaceWith("ArtistSocialLinksEntity"))
   @ColumnInfo(name = "youtube")
   val youtube: String? = null,
   
   @ColumnInfo(name = "spotify_id")
   val spotifyId: String? = null,
   
   // ==================== CLASIFICACIÓN ====================
   
   /**
    * Lista de géneros separados por coma
    * Ejemplo: "Hip Hop, Rap, R&B"
    */
   @ColumnInfo(name = "generos")
   val generos: String? = null,
   
   /**
    * Tipo de artista
    * @see TIPO_SOLISTA, TIPO_BANDA, etc.
    */
   @ColumnInfo(name = "tipo")
   val tipo: String = TIPO_SOLISTA,
   
   /**
    * Si está verificado oficialmente (checkmark azul en Genius)
    */
   @ColumnInfo(name = "es_verificado")
   val esVerificado: Boolean = false,
   
   /**
    * Si es un artista popular (para destacar en UI)
    */
   @ColumnInfo(name = "es_popular")
   val esPopular: Boolean = false,
   
   // ==================== ESTADÍSTICAS ====================
   
   @ColumnInfo(name = "total_canciones")
   val totalCanciones: Int = 0,
   
   @ColumnInfo(name = "total_albumes")
   val totalAlbumes: Int = 0,
   
   @ColumnInfo(name = "total_reproducciones")
   val totalReproducciones: Int = 0,
   
   @ColumnInfo(name = "veces_favorito")
   val vecesFavorito: Int = 0,
   
   // ==================== METADATA DEL SISTEMA ====================
   
   @ColumnInfo(name = "fecha_agregado")
   val fechaAgregado: Long = System.currentTimeMillis(),
   
   @ColumnInfo(name = "ultima_actualizacion")
   val ultimaActualizacion: Long = System.currentTimeMillis(),
   
   /**
    * Fuente principal de la información
    * @see FUENTE_LOCAL, FUENTE_GENIUS, etc.
    */
   @ColumnInfo(name = "fuente")
   val fuente: String = FUENTE_LOCAL,
   
   /**
    * Score de confianza de la metadata del artista (0-100)
    * Calculado basado en completitud de información
    */
   @ColumnInfo(name = "confidence_score")
   val confidenceScore: Int = 0,
) {
   // ==================== FUNCIONES DE UTILIDAD ====================
   
   /**
    * Obtiene la imagen a usar (prioriza local, luego URL)
    */
   fun obtenerImagen(): String? = imagePathLocal ?: imageUrl
   
   /**
    * Verifica si tiene imagen disponible
    */
   fun tieneImagen(): Boolean = !imagePathLocal.isNullOrBlank() || !imageUrl.isNullOrBlank()
   
   /**
    * Obtiene los géneros como lista
    */
   fun obtenerGeneros(): List<String> {
      return generos?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
   }
   
   /**
    * Obtiene los aliases como lista
    */
   fun obtenerAliases(): List<String> {
      if (aliasesJson.isNullOrBlank()) return emptyList()
      return try {
         // Simple JSON array parsing
         aliasesJson
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
      } catch (e: Exception) {
         emptyList()
      }
   }
   
   /**
    * Verifica si es una banda/grupo
    */
   fun esBanda(): Boolean = tipo == TIPO_BANDA || tipo == TIPO_GRUPO
   
   /**
    * Verifica si es solista
    */
   fun esSolista(): Boolean = tipo == TIPO_SOLISTA
   
   /**
    * Verifica si tiene información de Genius
    */
   fun tieneGeniusData(): Boolean = !geniusId.isNullOrBlank()
   
   /**
    * Verifica si la información es confiable
    */
   fun tieneMetadataConfiable(): Boolean = confidenceScore >= 70
   
   /**
    * Calcula un score de completitud basado en campos presentes
    */
   fun calcularCompletitud(): Int {
      var score = 0
      
      // Campos básicos (40 puntos)
      if (nombre.isNotBlank()) score += 10
      if (!nombreReal.isNullOrBlank()) score += 5
      if (!paisOrigen.isNullOrBlank()) score += 5
      if (!ciudadOrigen.isNullOrBlank()) score += 5
      if (!generos.isNullOrBlank()) score += 5
      if (tipo != TIPO_SOLISTA) score += 5 // Tiene tipo específico
      if (!descripcion.isNullOrBlank()) score += 5
      
      // Imágenes (20 puntos)
      if (tieneImagen()) score += 15
      if (!headerImageUrl.isNullOrBlank()) score += 5
      
      // Genius data (25 puntos)
      if (tieneGeniusData()) score += 15
      if (esVerificado) score += 10
      
      // Biografía y extras (15 puntos)
      if (!biografia.isNullOrBlank()) score += 10
      if (!sitioWeb.isNullOrBlank()) score += 5
      
      return score.coerceIn(0, 100)
   }
   
   companion object {
      // ==================== TIPOS DE ARTISTA ====================
      const val TIPO_SOLISTA = "SOLISTA"
      const val TIPO_BANDA = "BANDA"
      const val TIPO_DUO = "DUO"
      const val TIPO_GRUPO = "GRUPO"
      const val TIPO_VARIOS = "VARIOS_ARTISTAS"
      const val TIPO_DESCONOCIDO = "DESCONOCIDO"
      
      // ==================== FUENTES ====================
      const val FUENTE_LOCAL = "LOCAL"
      const val FUENTE_GENIUS = "GENIUS"
      const val FUENTE_SPOTIFY = "SPOTIFY"
      const val FUENTE_LASTFM = "LASTFM"
      const val FUENTE_MANUAL = "MANUAL"
      const val FUENTE_CONSOLIDATED = "CONSOLIDATED"
      
      /**
       * Normaliza un nombre de artista para búsquedas
       * Usa Unicode NFD para remover acentos correctamente
       *
       * Ejemplo: "Café Tacvba" -> "cafe tacvba"
       */
      fun normalizar(nombre: String): String {
         return Normalizer.normalize(nombre, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .lowercase()
            .trim()
      }
      
      /**
       * Compara dos nombres de artista de forma normalizada
       */
      fun nombresCoinciden(nombre1: String, nombre2: String): Boolean {
         return normalizar(nombre1) == normalizar(nombre2)
      }
      
      /**
       * Artistas especiales que no deben ser modificados
       */
      val ARTISTAS_ESPECIALES = setOf(
         "Various Artists",
         "Varios Artistas",
         "Unknown Artist",
         "Artista Desconocido",
         "Soundtrack",
         "Original Soundtrack",
         "OST"
      )
      
      /**
       * Verifica si es un artista especial/genérico
       */
      fun esArtistaEspecial(nombre: String): Boolean {
         val normalizado = normalizar(nombre)
         return ARTISTAS_ESPECIALES.any { normalizar(it) == normalizado }
      }
   }
}