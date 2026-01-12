// app/src/main/java/com/example/freeplayerm/data/repository/GeniusRepository.kt
package com.example.freeplayerm.data.repository

import android.content.Context
import android.util.Log
import com.example.freeplayerm.data.local.dao.AlbumDao
import com.example.freeplayerm.data.local.dao.ArtistDao
import com.example.freeplayerm.data.local.dao.LyricsDao
import com.example.freeplayerm.data.local.dao.SongDao
import com.example.freeplayerm.data.local.entity.ArtistEntity
import com.example.freeplayerm.data.local.entity.LyricsEntity
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.data.purification.MetadataPipelineConfig
import com.example.freeplayerm.data.purification.QualityScorer
import com.example.freeplayerm.data.remote.genius.api.GeniusApiService
import com.example.freeplayerm.data.remote.genius.dto.AlbumInfo
import com.example.freeplayerm.data.remote.genius.dto.ArtistDetails
import com.example.freeplayerm.data.remote.genius.dto.GeniusSearchResponse
import com.example.freeplayerm.data.remote.genius.dto.SongDetails
import com.example.freeplayerm.data.remote.genius.dto.SongResult
import com.example.freeplayerm.data.remote.genius.dto.getBestCoverArtUrl
import com.example.freeplayerm.data.remote.genius.dto.getBestImageUrl
import com.example.freeplayerm.data.remote.genius.dto.getPlainDescription
import com.example.freeplayerm.data.remote.genius.dto.isVerifiedArtist
import com.example.freeplayerm.data.remote.genius.scraper.GeniusScraper
import com.example.freeplayerm.utils.MusicTitleCleaner
import com.example.freeplayerm.utils.StringSimilarity
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 🎵 GENIUS REPOSITORY v2.0
 *
 * Repositorio para integración con Genius API Mejorado con sistema de purificación de metadata
 *
 * Características:
 * - Búsqueda y matching inteligente
 * - Validación cruzada de resultados
 * - Quality scoring integrado
 * - Actualización de estados del pipeline
 * - Preservación de datos originales
 *
 * @version 2.0 - Sistema de Purificación de Metadata
 */
class GeniusRepository
@Inject
constructor(
   private val apiService: GeniusApiService,
   private val songDao: SongDao,
   private val lyricsDao: LyricsDao,
   private val artistDao: ArtistDao,
   private val albumDao: AlbumDao,
   private val geniusScraper: GeniusScraper,
   private val imageRepository: ImageRepository,
   
   
   
   @ApplicationContext private val context: Context,
) {
   private val tag = "GeniusRepository"
   private val gson = Gson()
   
   sealed class GeniusSearchResult {
      data class Success(val metadata: GeniusSongMetadata) : GeniusSearchResult()
      data object NotFound : GeniusSearchResult()
      data class NetworkError(val code: Int, val message: String?) : GeniusSearchResult()
      data class Error(val exception: Exception) : GeniusSearchResult()
   }

   // ==================== API PÚBLICA ====================

   /**
    * Sincroniza letras para canciones sin letra Actualiza estados del pipeline durante el proceso
    */
   suspend fun sincronizarLetrasDeCanciones(usuarioId: Int) {
      withContext(Dispatchers.IO) {
         try {
            Log.d(tag, "🔄 Iniciando sincronización de letras...")

            val cancionesSinLetra =
               songDao
                  .obtenerCancionesSinLetra(usuarioId)
                  .take(MetadataPipelineConfig.BACKGROUND_BATCH_SIZE)

            Log.d(tag, "📊 Encontradas ${cancionesSinLetra.size} canciones sin letra")

            cancionesSinLetra.forEachIndexed { index, cancionConArtista ->
               try {
                  procesarCancionConPipeline(cancionConArtista, index, cancionesSinLetra.size)
               } catch (e: Exception) {
                  Log.e(tag, "Error procesando canción ${index + 1}: ${e.message}", e)
                  // Marcar como fallido si hay error crítico
                  marcarComoFallido(cancionConArtista.cancion, e.message)
               }
            }

            Log.d(tag, "✅ Sincronización completada")
         } catch (e: Exception) {
            Log.e(tag, "Error en sincronización: ${e.message}", e)
         }
      }
   }

   /**
    * Sincroniza canción al reproducir (enriquecimiento on-demand) Solo procesa si la canción
    * necesita enriquecimiento
    */
   suspend fun sincronizarCancionAlReproducir(cancion: SongEntity) {
      if (!MetadataPipelineConfig.ENRICH_ON_PLAY) return

      withContext(Dispatchers.IO) {
         try {
            // Verificar si necesita enriquecimiento
            if (!cancion.necesitaEnriquecimiento()) {
               Log.d(
                  tag,
                  "✅ '${cancion.titulo}' ya está enriquecida (status: ${cancion.metadataStatus})",
               )
               return@withContext
            }

            // Verificar si puede reintentar
            if (!cancion.puedeReintentarEnriquecimiento()) {
               Log.d(tag, "⏩ '${cancion.titulo}' - esperando período de reintento")
               return@withContext
            }

            Log.d(tag, "🎵 Enriqueciendo al reproducir: '${cancion.titulo}'")

            val cancionConArtista =
               songDao.obtenerCancionConArtista(cancion.idCancion).firstOrNull()
            val artista = cancionConArtista?.artistaNombre

            if (artista == null) {
               Log.d(tag, "⏩ '${cancion.titulo}' no tiene artista asociado")
               return@withContext
            }

            // Validar contenido musical
            if (!MusicTitleCleaner.isMusicalContent(cancion.titulo, artista)) {
               Log.d(tag, "⏩ '${cancion.titulo}' no es contenido musical")
               return@withContext
            }

            // Buscar en Genius
            val metadata = buscarCancionEnGenius(cancion.titulo, artista)

            if (metadata != null) {
               guardarMetadataConValidacion(cancion, cancionConArtista, metadata)
               Log.d(tag, "✅ Metadata sincronizada para '${cancion.titulo}'")
            } else {
               // Marcar como no encontrado en API
               marcarComoNoEncontrado(cancion)
               Log.d(tag, "❌ No se encontró '${cancion.titulo}' en Genius")
            }
         } catch (e: Exception) {
            Log.e(tag, "Error sincronizando '${cancion.titulo}': ${e.message}", e)
            incrementarIntentosEnriquecimiento(cancion)
         }
      }
   }

   /**
    * Busca metadata de una canción específica en Genius Incluye validación cruzada de resultados
    */
   suspend fun buscarCancionEnGenius(titulo: String, artista: String?): GeniusSongMetadata? {
      return withContext(Dispatchers.IO) {
         try {
            // 1. Preprocesar búsqueda
            val (tituloLimpio, artistaLimpio) = MusicTitleCleaner.preprocessSearch(titulo, artista)

            // 2. Validar contenido musical
            val musicConfidence =
               MusicTitleCleaner.calculateMusicConfidence(tituloLimpio, artistaLimpio)
            if (musicConfidence < MetadataPipelineConfig.MIN_MUSIC_CONFIDENCE) {
               Log.d(tag, "⏩ Contenido no musical (confidence: ${"%.2f".format(musicConfidence)})")
               return@withContext null
            }

            // 3. Construir y ejecutar búsqueda
            val query = MusicTitleCleaner.buildSearchQuery(tituloLimpio, artistaLimpio)
            Log.d(tag, "🔎 Buscando: '$query'")

            val searchResponse = apiService.search(query)
            if (!searchResponse.isSuccessful || searchResponse.body() == null) {
               Log.w(tag, "❌ Error en búsqueda: ${searchResponse.code()}")
               return@withContext null
            }

            // 4. Seleccionar mejor resultado con validación
            val (resultado, validationResult) =
               seleccionarMejorResultadoConValidacion(
                  searchResponse.body()!!,
                  tituloLimpio,
                  artistaLimpio,
               )

            if (resultado == null) {
               Log.d(tag, "❌ No se encontró resultado válido")
               return@withContext null
            }

            // 5. Obtener detalles completos
            val songDetails = obtenerDetallesCompletos(resultado.id)
            if (songDetails == null) {
               Log.w(tag, "❌ No se pudieron obtener detalles de canción ${resultado.id}")
               return@withContext null
            }

            // 6. Scraping de letras (si está habilitado)
            val letra = geniusScraper.extractLyricsOnly(resultado.url)

            GeniusSongMetadata(
               songResult = resultado,
               songDetails = songDetails,
               lyrics = letra,
               validationResult = validationResult,
               originalTitle = titulo,
               originalArtist = artista,
            )
         } catch (e: Exception) {
            Log.e(tag, "Error buscando canción: ${e.message}", e)
            null
         }
      }
   }

   /** Enriquece un batch de canciones (para procesamiento en background) */
   suspend fun enriquecerBatch(canciones: List<SongWithArtist>): EnrichmentResult {
      return withContext(Dispatchers.IO) {
         var exitosas = 0
         var fallidas = 0
         var saltadas = 0

         canciones.forEachIndexed { index, cancionConArtista ->
            try {
               val cancion = cancionConArtista.cancion
               val artista = cancionConArtista.artistaNombre

               // Verificar si necesita enriquecimiento
               if (!cancion.necesitaEnriquecimiento()) {
                  saltadas++
                  return@forEachIndexed
               }

               if (
                  artista == null || !MusicTitleCleaner.isMusicalContent(cancion.titulo, artista)
               ) {
                  saltadas++
                  return@forEachIndexed
               }

               Log.d(tag, "[${index + 1}/${canciones.size}] Enriqueciendo: '${cancion.titulo}'")

               val metadata = buscarCancionEnGenius(cancion.titulo, artista)

               if (metadata != null) {
                  guardarMetadataConValidacion(cancion, cancionConArtista, metadata)
                  exitosas++
               } else {
                  marcarComoNoEncontrado(cancion)
                  fallidas++
               }
            } catch (e: Exception) {
               Log.e(tag, "Error en batch item $index: ${e.message}", e)
               fallidas++
            }
         }

         EnrichmentResult(exitosas, fallidas, saltadas)
      }
   }

   // ==================== MÉTODOS PRIVADOS - PIPELINE ====================

   private suspend fun procesarCancionConPipeline(
      songWithArtist: SongWithArtist,
      index: Int,
      total: Int,
   ) {
      val cancion = songWithArtist.cancion
      val artista = songWithArtist.artistaNombre

      Log.d(tag, "[$index/$total] 🔍 Procesando: '${cancion.titulo}' - $artista")

      // Verificar estado actual
      if (!cancion.necesitaEnriquecimiento()) {
         Log.d(tag, "[$index/$total] ✅ Ya procesada (status: ${cancion.metadataStatus})")
         return
      }

      // Buscar en Genius
      val metadata = buscarCancionEnGenius(cancion.titulo, artista)

      if (metadata == null) {
         marcarComoNoEncontrado(cancion)
         Log.d(tag, "[$index/$total] ❌ No encontrado en Genius")
         return
      }

      // Guardar con validación y scoring
      guardarMetadataConValidacion(cancion, songWithArtist, metadata)
      Log.d(tag, "[$index/$total] ✅ Metadata guardada")
   }

   private fun seleccionarMejorResultadoConValidacion(
      searchResponse: GeniusSearchResponse,
      titulo: String,
      artista: String?,
   ): Pair<SongResult?, QualityScorer.ValidationResult?> {
      val hits = searchResponse.response?.hits ?: return null to null

      var mejorResultado: SongResult? = null
      var mejorScore = 0.0
      var mejorValidation: QualityScorer.ValidationResult? = null

      for (hit in hits) {
         val resultado = hit.result ?: continue

         // Calcular similitudes
         val similitudTitulo = StringSimilarity.hybridSimilarity(resultado.title, titulo)
         val similitudArtista =
            if (artista != null && resultado.primaryArtist != null) {
               StringSimilarity.hybridSimilarity(resultado.primaryArtist.name, artista)
            } else 0.0

         // Validar umbrales mínimos
         if (similitudTitulo < MetadataPipelineConfig.MIN_TITLE_SIMILARITY) continue
         if (artista != null && similitudArtista < MetadataPipelineConfig.MIN_ARTIST_SIMILARITY)
            continue

         // Verificar contenido musical
         if (!MusicTitleCleaner.isMusicalContent(resultado.title, resultado.primaryArtist?.name))
            continue

         // Calcular score total
         val scoreTotal = (similitudTitulo * 0.7) + (similitudArtista * 0.3)

         // Bonificaciones
         var bonus = 0.0
         // if (resultado.primaryArtist?.isVerified == true) bonus += 0.1
         if (!resultado.songArtImageUrl.isNullOrBlank()) bonus += 0.05

         val scoreFinal = scoreTotal + bonus

         if (scoreFinal > mejorScore) {
            mejorScore = scoreFinal
            mejorResultado = resultado
            mejorValidation =
               QualityScorer.ValidationResult(
                  titleSimilarity = similitudTitulo,
                  artistSimilarity = similitudArtista,
                  hasUnresolvedConflicts = similitudTitulo < 0.6 || similitudArtista < 0.5,
               )
         }
      }

      // Umbral mínimo para aceptar resultado
      if (mejorScore < 0.6) {
         Log.d(
            tag,
            "   ❌ Mejor resultado (${mejorResultado?.title}) no supera umbral: ${"%.2f".format(mejorScore)}",
         )
         return null to null
      }

      Log.d(
         tag,
         "   ✅ Mejor resultado: '${mejorResultado?.title}' (score: ${"%.2f".format(mejorScore)})",
      )
      return mejorResultado to mejorValidation
   }

   private suspend fun guardarMetadataConValidacion(
      cancion: SongEntity,
      songWithArtist: SongWithArtist?,
      metadata: GeniusSongMetadata,
   ) {
      try {
         // 1. Extraer featured artists y version del título original
         val parsedTitle = MusicTitleCleaner.parseTitle(metadata.originalTitle ?: cancion.titulo)
         val featuredArtistsJson =
            if (parsedTitle.featuredArtists.isNotEmpty()) {
               gson.toJson(parsedTitle.featuredArtists)
            } else null

         // 2. Calcular confidence score
         val artistEntity = cancion.idArtista?.let { artistDao.obtenerArtistaPorId(it) }
         val scoringData =
            QualityScorer.createScoringData(
               song = cancion,
               artistEntity = artistEntity,
               hasLyrics = metadata.lyrics != null,
               coverArtResolution =
                  if (metadata.songDetails.getBestCoverArtUrl() != null) 600 else 0,
            )
         val artistScoringData = QualityScorer.createArtistScoringData(artistEntity)

         val scoreResult =
            QualityScorer.calculateConfidenceScore(
               songData = scoringData,
               validationResult = metadata.validationResult,
               artistData = artistScoringData,
            )

         // 3. Preparar raw API data para auditoría
         val rawApiData =
            try {
               gson.toJson(
                  mapOf(
                     "geniusId" to metadata.songResult.id,
                     "title" to metadata.songResult.title,
                     "artist" to metadata.songResult.primaryArtist?.name,
                     "url" to metadata.songResult.url,
                     "fetchedAt" to System.currentTimeMillis(),
                  )
               )
            } catch (e: Exception) {
               null
            }

         // 4. Actualizar canción con todos los datos
         val cancionActualizada =
            cancion.copy(
               // Datos de Genius
               geniusId = metadata.songResult.id,
               geniusUrl = metadata.songResult.url,
               tituloCompleto = metadata.songDetails.fullTitle,
               letraDisponible = metadata.lyrics != null,

               // Datos originales (para auditoría)
               tituloOriginal = cancion.tituloOriginal ?: metadata.originalTitle ?: cancion.titulo,
               artistaOriginal = cancion.artistaOriginal ?: metadata.originalArtist,

               // Featured artists y versión
               featuredArtistsJson = featuredArtistsJson,
               versionType = parsedTitle.version?.name,

               // Sistema de purificación
               metadataStatus = scoreResult.recommendedStatus,
               confidenceScore = scoreResult.score,
               metadataSource = SongEntity.SOURCE_GENIUS,
               rawApiData = rawApiData,
               lastApiSync = System.currentTimeMillis(),
               enrichmentAttempts = cancion.enrichmentAttempts + 1,

               // Fecha de modificación
               fechaModificacion = System.currentTimeMillis(),
            )

         songDao.actualizarCancion(cancionActualizada)
         Log.d(
            tag,
            "   📊 Score: ${scoreResult.score} (${scoreResult.quality.displayName}) → ${scoreResult.recommendedStatus}",
         )

         // 5. Guardar letra si está disponible
         metadata.lyrics?.let { textoLetra ->
            if (textoLetra.isNotBlank()) {
               guardarLetra(cancion.idCancion, textoLetra, metadata.songResult.url)
            }
         }

         // 6. Sincronizar artista con datos de Genius
         metadata.songDetails.primaryArtist.let { geniusArtist ->
            sincronizarArtistaConGenius(cancion.idArtista, geniusArtist)
         }

         // 7. Sincronizar álbum si existe
         metadata.songDetails.album?.let { geniusAlbum ->
            sincronizarAlbum(cancion, geniusAlbum, metadata.songDetails)
         }

         // 8. Descargar portada si es necesario
         metadata.songDetails.getBestCoverArtUrl()?.let { coverUrl ->
            descargarYGuardarPortada(cancionActualizada, coverUrl)
         }
      } catch (e: Exception) {
         Log.e(tag, "Error guardando metadata validada: ${e.message}", e)
         throw e
      }
   }

   private suspend fun guardarLetra(songId: Int, texto: String, urlFuente: String) {
      val letraExistente = lyricsDao.obtenerLetraPorIdCancionSuspending(songId)
      if (letraExistente != null) return

      // Limpiar letra si está configurado
      val letraLimpia =
         if (MetadataPipelineConfig.REMOVE_LYRICS_TAGS) {
            limpiarLetra(texto)
         } else texto

      val letra =
         LyricsEntity(
            idCancion = songId,
            textoLetra = letraLimpia,
            fuente = LyricsEntity.FUENTE_GENIUS,
            urlFuente = urlFuente,
            verificada = true,
         )
      lyricsDao.insertarLetra(letra)
   }

   private fun limpiarLetra(texto: String): String {
      return texto
         // Remover etiquetas de estructura [Verse 1], [Chorus], etc.
         .replace(
            Regex(
               """\[(Intro|Verse|Chorus|Bridge|Outro|Pre-Chorus|Hook|Interlude).*?\]""",
               RegexOption.IGNORE_CASE,
            ),
            "",
         )
         // Remover números de verso solos [1], [2]
         .replace(Regex("""\[\d+\]"""), "")
         // Normalizar saltos de línea (máximo 2 consecutivos)
         .replace(Regex("""\n{3,}"""), "\n\n")
         // Eliminar espacios al inicio/fin de cada línea
         .lines()
         .joinToString("\n") { it.trim() }
         .trim()
   }

   private suspend fun sincronizarArtistaConGenius(
      artistaIdLocal: Int?,
      geniusArtist: com.example.freeplayerm.data.remote.genius.dto.ArtistInfo,
   ) {
      if (artistaIdLocal == null) return

      val artistaEntity = artistDao.obtenerArtistaPorId(artistaIdLocal) ?: return

      // Solo actualizar si no tiene datos de Genius
      if (!artistaEntity.geniusId.isNullOrBlank()) return

      val artistaActualizado =
         artistaEntity.copy(
            geniusId = geniusArtist.id,
            geniusUrl = geniusArtist.url,
            imageUrl = geniusArtist.imageUrl ?: artistaEntity.imageUrl,
            thumbnailUrl = geniusArtist.headerImageThumbnailUrl ?: artistaEntity.thumbnailUrl,
            esVerificado = geniusArtist.isVerified ?: false,
            fuente = ArtistEntity.FUENTE_GENIUS,
            ultimaActualizacion = System.currentTimeMillis(),
         )

      artistDao.actualizarArtista(artistaActualizado)
   }

   // ==================== MÉTODOS DE ESTADO ====================

   private suspend fun marcarComoNoEncontrado(cancion: SongEntity) {
      val actualizada =
         cancion.copy(
            metadataStatus = SongEntity.STATUS_API_NOT_FOUND,
            lastApiSync = System.currentTimeMillis(),
            enrichmentAttempts = cancion.enrichmentAttempts + 1,
         )
      songDao.actualizarCancion(actualizada)
   }

   private suspend fun marcarComoFallido(cancion: SongEntity, mensaje: String?) {
      val actualizada =
         cancion.copy(
            metadataStatus = SongEntity.STATUS_FAILED,
            enrichmentAttempts = cancion.enrichmentAttempts + 1,
            lastApiSync = System.currentTimeMillis(),
         )
      songDao.actualizarCancion(actualizada)
      Log.w(tag, "Canción marcada como FAILED: ${cancion.titulo} - $mensaje")
   }

   private suspend fun incrementarIntentosEnriquecimiento(cancion: SongEntity) {
      val actualizada =
         cancion.copy(
            enrichmentAttempts = cancion.enrichmentAttempts + 1,
            lastApiSync = System.currentTimeMillis(),
         )
      songDao.actualizarCancion(actualizada)
   }

   // ==================== MÉTODOS EXISTENTES (MANTENIDOS) ====================

   private suspend fun obtenerDetallesCompletos(songId: String): SongDetails? {
      return try {
         val response = apiService.getSong(songId, "plain")
         if (response.isSuccessful && response.body() != null) {
            response.body()!!.response.song
         } else {
            Log.w(tag, "Respuesta fallida para canción $songId")
            null
         }
      } catch (e: Exception) {
         Log.e(tag, "Error obteniendo detalles: ${e.message}", e)
         null
      }
   }

   private suspend fun sincronizarAlbum(
      cancion: SongEntity,
      geniusAlbum: AlbumInfo,
      songDetails: SongDetails,
   ) {
      try {
         if (cancion.idAlbum == null) return

         val albumLocal = albumDao.obtenerAlbumPorId(cancion.idAlbum)
         if (albumLocal == null || !albumLocal.geniusId.isNullOrBlank()) return

         val albumActualizado =
            albumLocal.copy(
               geniusId = geniusAlbum.id,
               geniusUrl = geniusAlbum.url,
               portadaUrl = geniusAlbum.coverArtUrl,
               portadaThumbnail = geniusAlbum.coverArtThumbnailUrl,
            )

         albumDao.actualizarAlbum(albumActualizado)
      } catch (e: Exception) {
         Log.e(tag, "Error sincronizando álbum: ${e.message}", e)
      }
   }

   private suspend fun descargarYGuardarPortada(cancion: SongEntity, url: String) {
      try {
         if (!cancion.portadaPath.isNullOrBlank()) return

         val nombreArchivo = "cover_${cancion.idCancion}_${System.currentTimeMillis()}"
         val rutaLocal =
            imageRepository.downloadImage(
               url = url,
               filename = nombreArchivo,
               storageType = ImageRepository.StorageType.CACHE,
            )

         if (rutaLocal != null) {
            songDao.actualizarCancion(cancion.copy(portadaPath = rutaLocal, portadaUrl = url))
            Log.d(tag, "✅ Portada guardada: $rutaLocal")
         }
      } catch (e: Exception) {
         Log.e(tag, "Error guardando portada: ${e.message}", e)
      }
   }

   /** Sincroniza metadata de artista desde Genius */
   suspend fun sincronizarArtista(artistaId: Int): Boolean {
      return withContext(Dispatchers.IO) {
         try {
            val artista = artistDao.obtenerArtistaPorId(artistaId) ?: return@withContext false

            if (!artista.geniusId.isNullOrBlank()) {
               val response = apiService.getArtist(artista.geniusId)
               if (response.isSuccessful && response.body() != null) {
                  val artistDetails = response.body()!!.response.artist
                  actualizarArtistaConDatosGenius(artista, artistDetails)
                  return@withContext true
               }
            }
            false
         } catch (e: Exception) {
            Log.e(tag, "Error sincronizando artista: ${e.message}", e)
            false
         }
      }
   }

   private suspend fun actualizarArtistaConDatosGenius(
      artista: ArtistEntity,
      artistDetails: ArtistDetails,
   ) {
      try {
         val artistaActualizado =
            artista.copy(
               geniusId = artistDetails.id,
               geniusUrl = artistDetails.url,
               biografia = artistDetails.getPlainDescription(),
               imageUrl = artistDetails.getBestImageUrl(),
               instagram = artistDetails.instagramName,
               twitter = artistDetails.twitterName,
               facebook = artistDetails.facebookName,
               esVerificado = artistDetails.isVerifiedArtist(),
               ultimaActualizacion = System.currentTimeMillis(),
            )
         artistDao.actualizarArtista(artistaActualizado)
      } catch (e: Exception) {
         Log.e(tag, "Error actualizando artista: ${e.message}", e)
      }
   }
}

// ==================== DATA CLASSES ====================

/**
 * Encapsula toda la metadata obtenida de Genius Incluye resultado de validación para el pipeline
 */
data class GeniusSongMetadata(
   val songResult: SongResult,
   val songDetails: SongDetails,
   val lyrics: String?,
   val validationResult: QualityScorer.ValidationResult? = null,
   val originalTitle: String? = null,
   val originalArtist: String? = null,
)

/** Resultado de enriquecimiento en batch */
data class EnrichmentResult(val exitosas: Int, val fallidas: Int, val saltadas: Int) {
   val total: Int
      get() = exitosas + fallidas + saltadas

   val tasaExito: Double
      get() = if (total > 0) exitosas.toDouble() / total else 0.0
}
