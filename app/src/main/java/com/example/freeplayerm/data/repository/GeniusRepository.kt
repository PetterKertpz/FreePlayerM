// app/src/main/java/com/example/freeplayerm/data/repository/GeniusRepository.kt
package com.example.freeplayerm.data.repository

import android.content.Context
import android.util.Log
import com.example.freeplayerm.data.local.dao.AlbumDao
import com.example.freeplayerm.data.local.dao.ArtistaDao
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.LetraDao
import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.LetraEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.remote.genius.api.GeniusApiService
import com.example.freeplayerm.data.remote.genius.dto.*
import com.example.freeplayerm.data.remote.genius.scraper.GeniusScraper
import com.example.freeplayerm.utils.MusicTitleCleanerUtil
import com.example.freeplayerm.utils.StringSimilarityUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🎵 GENIUS REPOSITORY (UNIFIED)
 *
 * Repositorio optimizado para integración con Genius API
 *
 * Flujo:
 * 1. Buscar canción en Genius API (metadata completa)
 * 2. Validar resultado con algoritmo de similitud
 * 3. Scraping solo para letras (única cosa no disponible en API)
 * 4. Guardar metadata en entidades correspondientes
 *
 * Mejoras:
 * - ✅ Rate limiting manejado por interceptor (no manual)
 * - ✅ Uso de DTOs completos (SongDetails)
 * - ✅ Utils extraídos (MusicTitleCleanerUtil, StringSimilarityUtil)
 * - ✅ Separación de responsabilidades
 * - ✅ Manejo robusto de errores
 * - ✅ Logging estructurado
 * - ✅ Sync de metadata completa (álbumes, artistas, stats)
 */
@Singleton
class GeniusRepository @Inject constructor(
    private val apiService: GeniusApiService,
    private val cancionDao: CancionDao,
    private val letraDao: LetraDao,
    private val artistaDao: ArtistaDao,
    private val albumDao: AlbumDao,
    private val geniusScraper: GeniusScraper,
    private val imageRepository: ImageRepository,
    @ApplicationContext private val context: Context
) {
    private val tag = "GeniusRepository"

    companion object {
        // Umbrales de similitud para validación
        private const val MIN_TITLE_SIMILARITY = 0.4
        private const val MIN_ARTIST_SIMILARITY = 0.3

        // Límites de procesamiento
        private const val MAX_BATCH_SIZE = 50
    }

    // ==================== API PÚBLICA ====================

    /**
     * Sincroniza letras para canciones sin letra
     */
    suspend fun sincronizarLetrasDeCanciones(usuarioId: Int) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "🔄 Iniciando sincronización de letras...")

                val cancionesSinLetra = cancionDao.obtenerCancionesSinLetra(usuarioId)
                    .take(MAX_BATCH_SIZE)

                Log.d(tag, "📊 Encontradas ${cancionesSinLetra.size} canciones sin letra")

                cancionesSinLetra.forEachIndexed { index, cancionConArtista ->
                    try {
                        procesarCancion(cancionConArtista, index, cancionesSinLetra.size)
                    } catch (e: Exception) {
                        Log.e(tag, "Error procesando canción ${index + 1}: ${e.message}", e)
                    }
                }

                Log.d(tag, "✅ Sincronización completada")
            } catch (e: Exception) {
                Log.e(tag, "Error en sincronización: ${e.message}", e)
            }
        }
    }

    /**
     * Sincroniza canción al reproducir (si no tiene letra/portada)
     */
    suspend fun sincronizarCancionAlReproducir(cancion: CancionEntity) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "🎵 Sincronizando canción al reproducir: '${cancion.titulo}'")

                // Verificar si ya tiene letra y portada
                val letraExistente = letraDao.obtenerLetraPorIdCancionSuspending(cancion.idCancion)
                if (letraExistente != null && !cancion.portadaPath.isNullOrEmpty()) {
                    Log.d(tag, "✅ '${cancion.titulo}' ya tiene letra y portada")
                    return@withContext
                }

                // Obtener artista
                val cancionConArtista = cancionDao.obtenerCancionConArtista(cancion.idCancion)
                    .firstOrNull()
                val artista = cancionConArtista?.artistaNombre

                if (artista == null) {
                    Log.d(tag, "⏩ '${cancion.titulo}' no tiene artista asociado")
                    return@withContext
                }

                // Verificar si es contenido musical
                if (!MusicTitleCleanerUtil.isMusicalContent(cancion.titulo, artista)) {
                    Log.d(tag, "⏩ '${cancion.titulo}' no es contenido musical")
                    return@withContext
                }

                // Buscar en Genius
                val metadata = buscarCancionEnGenius(cancion.titulo, artista)

                if (metadata != null) {
                    // Guardar datos solo si faltan
                    if (letraExistente == null) {
                        guardarMetadataCompleta(cancion, cancionConArtista, metadata)
                        Log.d(tag, "✅ Metadata sincronizada para '${cancion.titulo}'")
                    } else if (cancion.portadaPath.isNullOrEmpty()) {
                        // Solo guardar portada
                        guardarSoloPortada(cancion, metadata)
                        Log.d(tag, "✅ Portada sincronizada para '${cancion.titulo}'")
                    }
                } else {
                    Log.d(tag, "❌ No se encontró información en Genius para '${cancion.titulo}'")
                }

            } catch (e: Exception) {
                Log.e(tag, "Error sincronizando canción '${cancion.titulo}': ${e.message}", e)
            }
        }
    }

    /**
     * Busca metadata de una canción específica en Genius
     */
    suspend fun buscarCancionEnGenius(
        titulo: String,
        artista: String?
    ): GeniusSongMetadata? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Preprocesar búsqueda
                val (tituloLimpio, artistaLimpio) = MusicTitleCleanerUtil.preprocessSearch(titulo, artista)

                // 2. Validar que es contenido musical
                if (!MusicTitleCleanerUtil.isMusicalContent(tituloLimpio, artistaLimpio)) {
                    Log.d(tag, "⏩ Contenido no musical detectado: '$titulo'")
                    return@withContext null
                }

                // 3. Buscar en API
                val query = MusicTitleCleanerUtil.buildSearchQuery(tituloLimpio, artistaLimpio)
                Log.d(tag, "🔎 Buscando: '$query'")

                val searchResponse = apiService.search(query)
                if (!searchResponse.isSuccessful || searchResponse.body() == null) {
                    Log.w(tag, "❌ Error en búsqueda: ${searchResponse.code()}")
                    return@withContext null
                }

                // 4. Validar y seleccionar mejor resultado
                val resultado = seleccionarMejorResultado(
                    searchResponse.body()!!,
                    tituloLimpio,
                    artistaLimpio
                )

                if (resultado == null) {
                    Log.d(tag, "❌ No se encontró resultado válido")
                    return@withContext null
                }

                // 5. Obtener detalles completos de la canción
                val songDetails = obtenerDetallesCompletos(resultado.id)

                if (songDetails == null) {
                    Log.w(tag, "❌ No se pudieron obtener detalles de canción ${resultado.id}")
                    return@withContext null
                }

                // 6. Scraping de letras
                val letra = geniusScraper.extractLyricsOnly(resultado.url)

                GeniusSongMetadata(
                    songResult = resultado,
                    songDetails = songDetails,
                    lyrics = letra
                )

            } catch (e: Exception) {
                Log.e(tag, "Error buscando canción: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Busca y guarda letras para múltiples canciones
     */
    suspend fun buscarYGuardarLetras(cancionesConArtista: List<CancionConArtista>) {
        withContext(Dispatchers.IO) {
            Log.d(tag, "🚀 Iniciando búsqueda de letras para ${cancionesConArtista.size} canciones")

            cancionesConArtista.forEachIndexed { index, cancionConArtista ->
                try {
                    val cancion = cancionConArtista.cancion
                    val artista = cancionConArtista.artistaNombre

                    if (artista == null) {
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ⏩ Saltando '${cancion.titulo}' - Sin artista")
                        return@forEachIndexed
                    }

                    if (!MusicTitleCleanerUtil.isMusicalContent(cancion.titulo, artista)) {
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ⏩ Saltando contenido no musical: '${cancion.titulo}'")
                        return@forEachIndexed
                    }

                    val letraExistente = letraDao.obtenerLetraPorIdCancionSuspending(cancion.idCancion)
                    if (letraExistente != null) {
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ✅ '${cancion.titulo}' ya tiene letra guardada")
                        return@forEachIndexed
                    }

                    Log.d(tag, "[$index/${cancionesConArtista.size}] 🔍 Buscando: '${cancion.titulo}' - $artista")

                    val metadata = buscarCancionEnGenius(cancion.titulo, artista)
                    if (metadata != null) {
                        guardarMetadataCompleta(cancion, cancionConArtista, metadata)
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ✅ Letra guardada para '${cancion.titulo}'")
                    } else {
                        Log.d(tag, "[$index/${cancionesConArtista.size}] ❌ No se encontró letra para '${cancion.titulo}'")
                    }

                } catch (e: Exception) {
                    Log.e(tag, "Error procesando canción ${index + 1}: ${e.message}", e)
                }
            }

            Log.d(tag, "✅ Búsqueda de letras completada")
        }
    }

    /**
     * Sincroniza metadata de artista desde Genius
     */
    suspend fun sincronizarArtista(artistaId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val artista = artistaDao.obtenerArtistaPorId(artistaId)
                if (artista == null) {
                    Log.w(tag, "Artista $artistaId no encontrado")
                    return@withContext false
                }

                // Si ya tiene Genius ID, actualizar
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

    // ==================== MÉTODOS PRIVADOS ====================

    private suspend fun procesarCancion(
        cancionConArtista: CancionConArtista,
        index: Int,
        total: Int
    ) {
        val cancion = cancionConArtista.cancion
        val artista = cancionConArtista.artistaNombre

        Log.d(tag, "[$index/$total] 🔍 Procesando: '${cancion.titulo}' - $artista")

        // Verificar si ya tiene letra
        val letraExistente = letraDao.obtenerLetraPorIdCancionSuspending(cancion.idCancion)
        if (letraExistente != null) {
            Log.d(tag, "[$index/$total] ✅ Ya tiene letra")
            return
        }

        // Buscar en Genius
        val metadata = buscarCancionEnGenius(cancion.titulo, artista)
        if (metadata == null) {
            Log.d(tag, "[$index/$total] ❌ No encontrado en Genius")
            return
        }

        // Guardar todos los datos
        guardarMetadataCompleta(cancion, cancionConArtista, metadata)

        Log.d(tag, "[$index/$total] ✅ Metadata guardada")
    }

    private fun seleccionarMejorResultado(
        searchResponse: GeniusSearchResponse,
        titulo: String,
        artista: String?
    ): SongResult? {
        val hits = searchResponse.response?.hits ?: return null

        // Filtrar y ordenar por similitud
        return hits
            .mapNotNull { it.result }
            .filter { esResultadoValido(it, titulo, artista) }
            .sortedByDescending { calcularSimilitudTotal(it, titulo, artista) }
            .firstOrNull()
    }

    private fun esResultadoValido(
        resultado: SongResult,
        titulo: String,
        artista: String?
    ): Boolean {
        if (resultado.title.isBlank()) return false

        // Filtrar palabras clave no deseadas
        val blacklist = listOf(
            "discography", "album", "collection", "calendar", "review",
            "translation", "traducción", "cover", "mix", "remix", "annotated",
            "interview", "unreleased", "demo", "preview", "snippet"
        )

        if (blacklist.any { resultado.title.contains(it, ignoreCase = true) }) {
            Log.d(tag, "   ❌ Resultado rechazado por palabra clave: '${resultado.title}'")
            return false
        }

        // Verificar similitud de título
        val similitudTitulo = StringSimilarityUtil.calculateSimilarity(
            resultado.title,
            titulo
        )

        if (similitudTitulo < MIN_TITLE_SIMILARITY) {
            Log.d(tag, "   ❌ Similitud de título muy baja: ${"%.2f".format(similitudTitulo)}")
            return false
        }

        // Verificar similitud de artista
        if (artista != null && resultado.primaryArtist != null) {
            val similitudArtista = StringSimilarityUtil.calculateSimilarity(
                resultado.primaryArtist.name,
                artista
            )

            if (similitudArtista < MIN_ARTIST_SIMILARITY) {
                Log.d(tag, "   ❌ Similitud de artista muy baja: ${"%.2f".format(similitudArtista)}")
                return false
            }
        }

        Log.d(tag, "   ✅ Resultado válido")
        return true
    }

    private fun calcularSimilitudTotal(
        resultado: SongResult,
        titulo: String,
        artista: String?
    ): Double {
        val similitudTitulo = StringSimilarityUtil.calculateSimilarity(
            resultado.title,
            titulo
        )

        val similitudArtista = if (artista != null && resultado.primaryArtist != null) {
            StringSimilarityUtil.calculateSimilarity(
                resultado.primaryArtist.name,
                artista
            )
        } else {
            0.0
        }

        // Ponderación: 70% título, 30% artista
        return (similitudTitulo * 0.7) + (similitudArtista * 0.3)
    }

    private suspend fun obtenerDetallesCompletos(songId: String): SongDetails? {
        return try {
            val response = apiService.getSong(songId, "plain")

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.response.song
            } else {
                Log.w(tag, "Respuesta fallida o cuerpo nulo para canción $songId")
                null
            }
        } catch (e: Exception) {
            Log.e(tag, "Error obteniendo detalles: ${e.message}", e)
            null
        }
    }

    private suspend fun guardarMetadataCompleta(
        cancion: CancionEntity,
        cancionConArtista: CancionConArtista,
        metadata: GeniusSongMetadata
    ) {
        try {
            // 1. Actualizar canción con IDs de Genius
            val cancionActualizada = cancion.copy(
                geniusId = metadata.songResult.id,
                geniusUrl = metadata.songResult.url,
                letraDisponible = metadata.lyrics != null
            )
            cancionDao.actualizarCancion(cancionActualizada)

            // 2. Guardar letra si está disponible
            metadata.lyrics?.let { textoLetra ->
                if (textoLetra.isNotBlank()) {
                    val letraExistente = letraDao.obtenerLetraPorIdCancionSuspending(cancion.idCancion)
                    if (letraExistente == null) {
                        val letra = LetraEntity(
                            idCancion = cancion.idCancion,
                            textoLetra = textoLetra,
                            fuente = LetraEntity.FUENTE_GENIUS,
                            urlFuente = metadata.songResult.url,
                            verificada = true
                        )
                        letraDao.insertarLetra(letra)
                    }
                }
            }

            // 3. Sincronizar artista
            metadata.songDetails.primaryArtist.let { geniusArtist ->
                val idArtistaLocal = cancion.idArtista

                if (idArtistaLocal != null) {
                    val artistaEntity = artistaDao.obtenerArtistaPorId(idArtistaLocal)

                    if (artistaEntity != null && artistaEntity.geniusId.isNullOrBlank()) {
                        val artistaActualizado = artistaEntity.copy(
                            geniusId = geniusArtist.id,
                            geniusUrl = geniusArtist.url,
                            imageUrl = geniusArtist.imageUrl,
                            thumbnailUrl = geniusArtist.headerImageThumbnailUrl,
                            fuente = ArtistaEntity.FUENTE_GENIUS,
                            ultimaActualizacion = (System.currentTimeMillis() / 1000).toInt()
                        )

                        artistaDao.actualizarArtista(artistaActualizado)
                    }
                }
            }

            // 4. Sincronizar álbum si existe
            metadata.songDetails.album?.let { geniusAlbum ->
                sincronizarAlbum(cancion, geniusAlbum, metadata.songDetails)
            }

            // 5. Descargar portada si es necesario
            metadata.songDetails.getBestCoverArtUrl()?.let { coverUrl ->
                descargarYGuardarPortada(cancion, coverUrl)
            }

        } catch (e: Exception) {
            Log.e(tag, "Error guardando metadata: ${e.message}", e)
        }
    }

    private suspend fun guardarSoloPortada(cancion: CancionEntity, metadata: GeniusSongMetadata) {
        try {
            // Actualizar Genius ID y URL si no los tiene
            if (cancion.geniusId == null) {
                cancionDao.actualizarCancion(
                    cancion.copy(
                        geniusId = metadata.songResult.id,
                        geniusUrl = metadata.songResult.url
                    )
                )
            }

            // Descargar portada
            metadata.songDetails.getBestCoverArtUrl()?.let { urlPortada ->
                val nombreArchivo = "cover_${cancion.idCancion}_${System.currentTimeMillis()}"
                val rutaLocal = imageRepository.downloadImage(
                    url = urlPortada,
                    filename = nombreArchivo,
                    storageType = ImageRepository.StorageType.CACHE
                )

                rutaLocal?.let { ruta ->
                    cancionDao.actualizarCancion(cancion.copy(portadaPath = ruta))
                    Log.d(tag, "✅ Portada guardada: $ruta")
                }
            }

        } catch (e: Exception) {
            Log.e(tag, "Error guardando portada: ${e.message}", e)
        }
    }

    private suspend fun sincronizarAlbum(
        cancion: CancionEntity,
        geniusAlbum: AlbumInfo,
        songDetails: SongDetails
    ) {
        try {
            if (cancion.idAlbum == null) return

            val albumLocal = albumDao.obtenerAlbumPorId(cancion.idAlbum)
            if (albumLocal == null || !albumLocal.geniusId.isNullOrBlank()) return

            val albumActualizado = albumLocal.copy(
                geniusId = geniusAlbum.id,
                geniusUrl = geniusAlbum.url,
                portadaUrl = geniusAlbum.coverArtUrl,
                portadaThumbnail = geniusAlbum.coverArtThumbnailUrl
            )

            albumDao.actualizarAlbum(albumActualizado)

        } catch (e: Exception) {
            Log.e(tag, "Error sincronizando álbum: ${e.message}", e)
        }
    }

    private suspend fun actualizarArtistaConDatosGenius(
        artista: ArtistaEntity,
        artistDetails: ArtistDetails
    ) {
        try {
            val artistaActualizado = artista.copy(
                geniusId = artistDetails.id,
                geniusUrl = artistDetails.url,
                biografia = artistDetails.getPlainDescription(),
                imageUrl = artistDetails.getBestImageUrl(),
                instagram = artistDetails.instagramName,
                twitter = artistDetails.twitterName,
                facebook = artistDetails.facebookName,
                esVerificado = artistDetails.isVerifiedArtist()
            )

            artistaDao.actualizarArtista(artistaActualizado)

        } catch (e: Exception) {
            Log.e(tag, "Error actualizando artista: ${e.message}", e)
        }
    }

    private suspend fun descargarYGuardarPortada(cancion: CancionEntity, url: String) {
        try {
            if (!cancion.portadaPath.isNullOrBlank()) return // Ya tiene portada

            val nombreArchivo = "cover_${cancion.idCancion}_${System.currentTimeMillis()}"
            val rutaLocal = imageRepository.downloadImage(
                url = url,
                filename = nombreArchivo,
                storageType = ImageRepository.StorageType.CACHE
            )

            if (rutaLocal != null) {
                cancionDao.actualizarCancion(cancion.copy(portadaPath = rutaLocal))
                Log.d(tag, "✅ Portada guardada: $rutaLocal")
            }

        } catch (e: Exception) {
            Log.e(tag, "Error guardando portada: ${e.message}", e)
        }
    }

    private suspend fun descargarImagen(url: String, nombreArchivo: String): String? {
        return imageRepository.downloadImage(
            url = url,
            filename = nombreArchivo.substringBeforeLast('.'), // Sin extensión
            storageType = ImageRepository.StorageType.CACHE
        )
    }
}

// ==================== DATA CLASSES ====================

/**
 * Encapsula toda la metadata obtenida de Genius
 */
data class GeniusSongMetadata(
    val songResult: SongResult,
    val songDetails: SongDetails,
    val lyrics: String?
)