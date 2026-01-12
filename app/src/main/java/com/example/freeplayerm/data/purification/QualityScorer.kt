// app/src/main/java/com/example/freeplayerm/data/purification/QualityScorer.kt
package com.example.freeplayerm.data.purification

import com.example.freeplayerm.data.local.entity.ArtistEntity
import com.example.freeplayerm.data.local.entity.SongEntity
import com.example.freeplayerm.data.purification.MetadataPipelineConfig.ScoringWeights

/**
 * 📊 QUALITY SCORER v1.0
 *
 * Calcula el confidence score (0-100) para metadata de canciones
 * Basado en múltiples criterios: validación API, completitud, calidad de artista, etc.
 *
 * El score determina el metadata_status final:
 * - 90-100: EXCELLENT → VERIFIED
 * - 80-89: GOOD → VERIFIED
 * - 70-79: FAIR → PARTIAL_VERIFIED
 * - 60-69: POOR → PARTIAL_VERIFIED
 * - 0-59: BAD → CLEANED_LOCAL (necesita más trabajo)
 *
 * @version 1.0 - Sistema de Purificación de Metadata
 */
object QualityScorer {
   
   // ==================== SCORING PRINCIPAL ====================
   
   /**
    * Calcula el confidence score completo para una canción
    *
    * @param songData Datos de la canción a evaluar
    * @param validationResult Resultado de validación cruzada (si hay)
    * @param artistData Datos del artista asociado (opcional)
    * @return ConfidenceScoreResult con score, nivel y desglose
    */
   fun calculateConfidenceScore(
      songData: SongScoringData,
      validationResult: ValidationResult? = null,
      artistData: ArtistScoringData? = null
   ): ConfidenceScoreResult {
      val breakdown = ScoreBreakdown()
      
      // ==================== CATEGORÍA A: VALIDACIÓN API ====================
      
      // Título
      when {
         validationResult?.titleSimilarity != null && validationResult.titleSimilarity >= 0.8 -> {
            breakdown.titleScore = ScoringWeights.TITLE_VERIFIED
         }
         validationResult?.titleSimilarity != null && validationResult.titleSimilarity >= 0.6 -> {
            breakdown.titleScore = ScoringWeights.TITLE_PARTIAL
         }
         songData.hasGeniusId -> {
            breakdown.titleScore = ScoringWeights.TITLE_PARTIAL // Tiene Genius pero no validamos
         }
      }
      
      // Artista
      when {
         validationResult?.artistSimilarity != null && validationResult.artistSimilarity >= 0.8 -> {
            breakdown.artistScore = ScoringWeights.ARTIST_VERIFIED
         }
         validationResult?.artistSimilarity != null && validationResult.artistSimilarity >= 0.6 -> {
            breakdown.artistScore = ScoringWeights.ARTIST_PARTIAL
         }
         songData.hasGeniusId -> {
            breakdown.artistScore = ScoringWeights.ARTIST_PARTIAL
         }
      }
      
      // Álbum
      when {
         songData.hasAlbumFromApi -> breakdown.albumScore = ScoringWeights.ALBUM_VERIFIED
         songData.hasAlbum -> breakdown.albumScore = ScoringWeights.ALBUM_LOCAL
      }
      
      // ==================== CATEGORÍA B: COMPLETITUD ====================
      
      // Género
      when {
         songData.hasSpecificGenre -> breakdown.genreScore = ScoringWeights.GENRE_SPECIFIC
         songData.hasGenre -> breakdown.genreScore = ScoringWeights.GENRE_GENERIC
      }
      
      // Año
      if (songData.hasValidYear) {
         breakdown.yearScore = ScoringWeights.YEAR_VALID
      }
      
      // Cover Art
      when {
         songData.coverArtResolution >= 1000 -> breakdown.coverArtScore = ScoringWeights.COVER_ART_HD
         songData.coverArtResolution >= 600 -> breakdown.coverArtScore = ScoringWeights.COVER_ART_NORMAL
         songData.coverArtResolution > 0 -> breakdown.coverArtScore = ScoringWeights.COVER_ART_LOW
      }
      
      // Letra
      if (songData.hasLyrics) {
         breakdown.lyricsScore = ScoringWeights.LYRICS_AVAILABLE
      }
      
      // Credits
      when {
         songData.hasFullCredits -> breakdown.creditsScore = ScoringWeights.CREDITS_FULL
         songData.hasPartialCredits -> breakdown.creditsScore = ScoringWeights.CREDITS_PARTIAL
      }
      
      // ==================== CATEGORÍA C: CALIDAD DE ARTISTA ====================
      
      if (artistData != null) {
         if (artistData.isGeniusVerified) {
            breakdown.artistVerifiedScore = ScoringWeights.ARTIST_GENIUS_VERIFIED
         }
         if (artistData.hasImage) {
            breakdown.artistImageScore = ScoringWeights.ARTIST_HAS_IMAGE
         }
      }
      
      // ==================== CATEGORÍA D: ENLACES EXTERNOS ====================
      
      if (songData.hasSpotifyId) breakdown.externalLinksScore += ScoringWeights.HAS_SPOTIFY_ID
      if (songData.hasYoutubeUrl) breakdown.externalLinksScore += ScoringWeights.HAS_YOUTUBE_URL
      if (songData.hasAppleMusicId) breakdown.externalLinksScore += ScoringWeights.HAS_APPLE_MUSIC_ID
      if (songData.hasSoundcloudId) breakdown.externalLinksScore += ScoringWeights.HAS_SOUNDCLOUD_ID
      
      // ==================== CATEGORÍA E: PENALIZACIONES ====================
      
      // Título con baja similitud
      if (validationResult?.titleSimilarity != null && validationResult.titleSimilarity < 0.5) {
         breakdown.penalties += ScoringWeights.PENALTY_TITLE_LOW_SIMILARITY
      }
      
      // Artista con baja similitud
      if (validationResult?.artistSimilarity != null && validationResult.artistSimilarity < 0.4) {
         breakdown.penalties += ScoringWeights.PENALTY_ARTIST_LOW_SIMILARITY
      }
      
      // Álbum desconocido
      if (songData.hasUnknownAlbum) {
         breakdown.penalties += ScoringWeights.PENALTY_ALBUM_UNKNOWN
      }
      
      // Género muy genérico
      if (songData.hasGenericGenre) {
         breakdown.penalties += ScoringWeights.PENALTY_GENRE_GENERIC
      }
      
      // Conflictos no resueltos
      if (validationResult?.hasUnresolvedConflicts == true) {
         breakdown.penalties += ScoringWeights.PENALTY_UNRESOLVED_CONFLICTS
      }
      
      // Metadata junk detectado
      if (songData.hasMetadataJunk) {
         breakdown.penalties += ScoringWeights.PENALTY_METADATA_JUNK
      }
      
      // Contenido dudoso
      if (songData.musicConfidence < 0.7) {
         breakdown.penalties += ScoringWeights.PENALTY_DUBIOUS_CONTENT
      }
      
      // ==================== CALCULAR SCORE FINAL ====================
      
      val baseScore = 50 // Score base
      val totalPositive = breakdown.totalPositive()
      val totalNegative = breakdown.penalties
      
      val rawScore = baseScore + totalPositive + totalNegative
      val finalScore = rawScore.coerceIn(0, 100)
      
      val qualityLevel = when (finalScore) {
         in 90..100 -> QualityLevel.EXCELLENT
         in 80..89 -> QualityLevel.GOOD
         in 70..79 -> QualityLevel.FAIR
         in 60..69 -> QualityLevel.POOR
         else -> QualityLevel.BAD
      }
      
      return ConfidenceScoreResult(
         score = finalScore,
         quality = qualityLevel,
         breakdown = breakdown,
         recommendedStatus = determineRecommendedStatus(finalScore, songData)
      )
   }
   
   /**
    * Calcula score rápido para procesamiento local (sin API)
    * Usado durante el escaneo inicial
    */
   fun calculateLocalScore(songData: SongScoringData): Int {
      var score = 50 // Base
      
      // Título válido
      if (songData.hasValidTitle) score += 5
      
      // Tiene artista
      if (songData.hasArtist) score += 5
      
      // Tiene álbum
      if (songData.hasAlbum) score += 3
      
      // Tiene género
      if (songData.hasGenre) score += 3
      
      // Tiene año
      if (songData.hasValidYear) score += 2
      
      // Tiene cover art
      if (songData.coverArtResolution > 0) score += 2
      
      // Penalizaciones
      if (songData.hasUnknownAlbum) score -= 5
      if (songData.musicConfidence < 0.7) score -= 3
      
      return score.coerceIn(0, 100)
   }
   
   /**
    * Determina el metadata_status recomendado basado en el score
    */
   private fun determineRecommendedStatus(score: Int, songData: SongScoringData): String {
      return when {
         score >= MetadataPipelineConfig.MIN_CONFIDENCE_FOR_VERIFIED -> {
            SongEntity.STATUS_VERIFIED
         }
         score >= MetadataPipelineConfig.MIN_CONFIDENCE_FOR_PARTIAL -> {
            SongEntity.STATUS_PARTIAL_VERIFIED
         }
         songData.hasGeniusId -> {
            SongEntity.STATUS_ENRICHED // Tiene datos API pero score bajo
         }
         else -> {
            SongEntity.STATUS_CLEANED_LOCAL
         }
      }
   }
   
   // ==================== UTILIDADES ====================
   
   /**
    * Crea SongScoringData desde SongEntity
    */
   fun createScoringData(
      song: SongEntity,
      artistEntity: ArtistEntity? = null,
      coverArtResolution: Int = 0,
      hasLyrics: Boolean = false,
      hasCredits: Boolean = false,
      externalIds: ExternalIds? = null
   ): SongScoringData {
      return SongScoringData(
         hasValidTitle = song.titulo.isNotBlank() && song.titulo.length >= 2,
         hasArtist = song.idArtista != null,
         hasAlbum = song.idAlbum != null,
         hasUnknownAlbum = false, // Necesitaría verificar nombre del álbum
         hasAlbumFromApi = song.geniusId != null && song.idAlbum != null,
         hasGenre = song.idGenero != null,
         hasSpecificGenre = song.idGenero != null, // Necesitaría verificar nombre
         hasGenericGenre = false,
         hasValidYear = song.anio != null && song.anio in 1900..2100,
         hasGeniusId = !song.geniusId.isNullOrBlank(),
         hasLyrics = song.letraDisponible || hasLyrics,
         hasFullCredits = hasCredits,
         hasPartialCredits = false,
         coverArtResolution = coverArtResolution,
         musicConfidence = 1.0, // Ya pasó validación si está aquí
         hasMetadataJunk = false,
         hasSpotifyId = externalIds?.spotifyId != null,
         hasYoutubeUrl = externalIds?.youtubeUrl != null,
         hasAppleMusicId = externalIds?.appleMusicId != null,
         hasSoundcloudId = externalIds?.soundcloudId != null
      )
   }
   
   /**
    * Crea ArtistScoringData desde ArtistEntity
    */
   fun createArtistScoringData(artist: ArtistEntity?): ArtistScoringData? {
      if (artist == null) return null
      
      return ArtistScoringData(
         isGeniusVerified = artist.esVerificado,
         hasImage = artist.tieneImagen(),
         hasGeniusData = !artist.geniusId.isNullOrBlank()
      )
   }
   
   // ==================== DATA CLASSES ====================
   
   /**
    * Datos de canción necesarios para scoring
    */
   data class SongScoringData(
      val hasValidTitle: Boolean = false,
      val hasArtist: Boolean = false,
      val hasAlbum: Boolean = false,
      val hasUnknownAlbum: Boolean = false,
      val hasAlbumFromApi: Boolean = false,
      val hasGenre: Boolean = false,
      val hasSpecificGenre: Boolean = false,
      val hasGenericGenre: Boolean = false,
      val hasValidYear: Boolean = false,
      val hasGeniusId: Boolean = false,
      val hasLyrics: Boolean = false,
      val hasFullCredits: Boolean = false,
      val hasPartialCredits: Boolean = false,
      val coverArtResolution: Int = 0,
      val musicConfidence: Double = 1.0,
      val hasMetadataJunk: Boolean = false,
      val hasSpotifyId: Boolean = false,
      val hasYoutubeUrl: Boolean = false,
      val hasAppleMusicId: Boolean = false,
      val hasSoundcloudId: Boolean = false
   )
   
   /**
    * Datos de artista necesarios para scoring
    */
   data class ArtistScoringData(
      val isGeniusVerified: Boolean = false,
      val hasImage: Boolean = false,
      val hasGeniusData: Boolean = false
   )
   
   /**
    * Resultado de validación cruzada
    */
   data class ValidationResult(
      val titleSimilarity: Double? = null,
      val artistSimilarity: Double? = null,
      val albumSimilarity: Double? = null,
      val hasUnresolvedConflicts: Boolean = false,
      val warnings: List<String> = emptyList()
   )
   
   /**
    * IDs externos de la canción
    */
   data class ExternalIds(
      val spotifyId: String? = null,
      val youtubeUrl: String? = null,
      val appleMusicId: String? = null,
      val soundcloudId: String? = null
   )
   
   /**
    * Resultado del cálculo de confidence score
    */
   data class ConfidenceScoreResult(
      val score: Int,
      val quality: QualityLevel,
      val breakdown: ScoreBreakdown,
      val recommendedStatus: String
   ) {
      fun isVerified(): Boolean = score >= MetadataPipelineConfig.MIN_CONFIDENCE_FOR_VERIFIED
      fun isPartiallyVerified(): Boolean = score >= MetadataPipelineConfig.MIN_CONFIDENCE_FOR_PARTIAL
      fun needsEnrichment(): Boolean = score < MetadataPipelineConfig.MIN_CONFIDENCE_FOR_PARTIAL
   }
   
   /**
    * Desglose del score por categoría
    */
   data class ScoreBreakdown(
      // Categoría A: Validación API
      var titleScore: Int = 0,
      var artistScore: Int = 0,
      var albumScore: Int = 0,
      
      // Categoría B: Completitud
      var genreScore: Int = 0,
      var yearScore: Int = 0,
      var coverArtScore: Int = 0,
      var lyricsScore: Int = 0,
      var creditsScore: Int = 0,
      
      // Categoría C: Calidad de artista
      var artistVerifiedScore: Int = 0,
      var artistImageScore: Int = 0,
      
      // Categoría D: Enlaces externos
      var externalLinksScore: Int = 0,
      
      // Categoría E: Penalizaciones
      var penalties: Int = 0
   ) {
      fun totalPositive(): Int {
         return titleScore + artistScore + albumScore +
               genreScore + yearScore + coverArtScore + lyricsScore + creditsScore +
               artistVerifiedScore + artistImageScore +
               externalLinksScore
      }
      
      fun categoryA(): Int = titleScore + artistScore + albumScore
      fun categoryB(): Int = genreScore + yearScore + coverArtScore + lyricsScore + creditsScore
      fun categoryC(): Int = artistVerifiedScore + artistImageScore
      fun categoryD(): Int = externalLinksScore
      
      override fun toString(): String {
         return """
                |ScoreBreakdown:
                |  Categoría A (API): ${categoryA()} pts
                |    - Título: $titleScore
                |    - Artista: $artistScore
                |    - Álbum: $albumScore
                |  Categoría B (Completitud): ${categoryB()} pts
                |    - Género: $genreScore
                |    - Año: $yearScore
                |    - Cover Art: $coverArtScore
                |    - Letras: $lyricsScore
                |    - Credits: $creditsScore
                |  Categoría C (Artista): ${categoryC()} pts
                |    - Verificado: $artistVerifiedScore
                |    - Imagen: $artistImageScore
                |  Categoría D (Links): ${categoryD()} pts
                |  Penalizaciones: $penalties pts
                |  TOTAL: ${totalPositive() + penalties} (+ base 50)
            """.trimMargin()
      }
   }
   
   /**
    * Niveles de calidad de metadata
    */
   enum class QualityLevel(val displayName: String, val emoji: String) {
      EXCELLENT("Excelente", "⭐"),
      GOOD("Buena", "✅"),
      FAIR("Aceptable", "📝"),
      POOR("Pobre", "⚠️"),
      BAD("Mala", "❌");
      
      fun isAcceptable(): Boolean = this in listOf(EXCELLENT, GOOD, FAIR)
   }
}