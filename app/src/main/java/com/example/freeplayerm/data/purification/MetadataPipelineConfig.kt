// app/src/main/java/com/example/freeplayerm/data/purification/MetadataPipelineConfig.kt
package com.example.freeplayerm.data.purification

/**
 * ⚙️ METADATA PIPELINE CONFIG v1.0
 *
 * Configuración centralizada para el Sistema de Purificación de Metadata
 * Todos los parámetros ajustables del pipeline en un solo lugar
 *
 * Características:
 * - Umbrales de similitud configurables
 * - Pesos para scoring de calidad
 * - Límites de rate limiting
 * - Modos de operación predefinidos
 *
 * @version 1.0 - Sistema de Purificación de Metadata
 */
object MetadataPipelineConfig {
   
   // ==================== PRE-PROCESAMIENTO ====================
   
   /** Longitud mínima de título válido */
   var MIN_TITLE_LENGTH = 1
   
   /** Longitud máxima de título válido */
   var MAX_TITLE_LENGTH = 200
   
   /** Confianza mínima para considerar contenido musical */
   var MIN_MUSIC_CONFIDENCE = 0.6
   
   // ==================== SIMILITUD ====================
   
   /** Similitud mínima de título para considerar match válido */
   var MIN_TITLE_SIMILARITY = 0.4
   
   /** Similitud mínima de artista para considerar match válido */
   var MIN_ARTIST_SIMILARITY = 0.3
   
   /** Similitud para considerar título "verificado" (alta confianza) */
   var VERIFIED_TITLE_SIMILARITY = 0.8
   
   /** Similitud para considerar artista "verificado" */
   var VERIFIED_ARTIST_SIMILARITY = 0.8
   
   /** Pesos para similitud híbrida */
   var SIMILARITY_WEIGHTS = SimilarityWeights(
      levenshteinWeight = 0.50,
      jaccardWeight = 0.30,
      phoneticWeight = 0.20
   )
   
   // ==================== GENIUS API ====================
   
   /** Requests por minuto (Genius limit) */
   var GENIUS_RATE_LIMIT = 10
   
   /** Timeout para requests en milisegundos */
   var GENIUS_TIMEOUT_MS = 10_000L
   
   /** Máximo de reintentos por request */
   var GENIUS_MAX_RETRIES = 3
   
   /** Base para backoff exponencial en ms */
   var GENIUS_BACKOFF_BASE_MS = 1_000L
   
   /** Máximo delay de backoff en ms */
   var GENIUS_BACKOFF_MAX_MS = 60_000L
   
   // ==================== SCRAPING ====================
   
   /** Timeout para scraping de letras en ms */
   var SCRAPING_TIMEOUT_MS = 15_000L
   
   /** User agents para rotación en scraping */
   var SCRAPING_USER_AGENTS = listOf(
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
      "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36",
      "Mozilla/5.0 (Linux; Android 10; SM-G981B) AppleWebKit/537.36"
   )
   
   // ==================== ENRIQUECIMIENTO ====================
   
   /** Habilitar enriquecimiento automático en background */
   var ENABLE_AUTO_ENRICHMENT = true
   
   /** Habilitar enriquecimiento al reproducir canción */
   var ENRICH_ON_PLAY = true
   
   /** Habilitar enriquecimiento en background con WorkManager */
   var ENRICH_IN_BACKGROUND = true
   
   /** Tamaño de batch para enriquecimiento background */
   var BACKGROUND_BATCH_SIZE = 50
   
   /** Intervalo entre ejecuciones de background en horas */
   var BACKGROUND_INTERVAL_HOURS = 6
   
   /** Máximo de intentos de enriquecimiento por canción */
   var MAX_ENRICHMENT_ATTEMPTS = 3
   
   /** Días a esperar antes de reintentar después de API_NOT_FOUND */
   var RETRY_NOT_FOUND_DAYS = 7
   
   // ==================== VALIDACIÓN ====================
   
   /** Resolución mínima aceptable para cover art */
   var MIN_COVER_ART_RESOLUTION = 300
   
   /** Resolución preferida para cover art */
   var PREFERRED_COVER_ART_RESOLUTION = 1000
   
   /** Remover etiquetas de estructura en letras ([Verse], [Chorus], etc.) */
   var REMOVE_LYRICS_TAGS = true
   
   /** Remover metadata junk (comments de tiendas, etc.) */
   var REMOVE_METADATA_JUNK = true
   
   // ==================== SCORING ====================
   
   /** Confianza mínima para estado VERIFIED */
   var MIN_CONFIDENCE_FOR_VERIFIED = 80
   
   /** Confianza mínima para estado PARTIAL_VERIFIED */
   var MIN_CONFIDENCE_FOR_PARTIAL = 60
   
   /** Habilitar cálculo de quality scoring */
   var ENABLE_QUALITY_SCORING = true
   
   // ==================== PERFORMANCE ====================
   
   /** Tamaño de batch para escaneo */
   var SCAN_BATCH_SIZE = 50
   
   /** Habilitar procesamiento paralelo en pre-procesamiento */
   var PARALLEL_PREPROCESSING = true
   
   /** Usar caché de artistas/álbumes/géneros */
   var USE_CACHE = true
   
   /** Tamaño máximo de cache */
   var CACHE_MAX_SIZE = 500
   
   // ==================== PERSISTENCIA ====================
   
   /** Habilitar snapshots para rollback */
   var ENABLE_SNAPSHOTS = true
   
   /** Días de retención de snapshots */
   var SNAPSHOT_RETENTION_DAYS = 7
   
   /** Usar transacciones para operaciones de BD */
   var USE_TRANSACTIONS = true
   
   // ==================== SCORING WEIGHTS ====================
   
   /**
    * Pesos para cálculo de confidence score
    * Total debe sumar aproximadamente 100 puntos máximo
    */
   object ScoringWeights {
      // Categoría A: Validación con API (max 40 puntos)
      const val TITLE_VERIFIED = 15          // Título verificado (similitud >= 0.8)
      const val TITLE_PARTIAL = 10           // Título parcialmente verificado (0.6-0.79)
      const val ARTIST_VERIFIED = 15         // Artista verificado (similitud >= 0.8)
      const val ARTIST_PARTIAL = 10          // Artista parcialmente verificado
      const val ALBUM_VERIFIED = 10          // Álbum encontrado en API
      const val ALBUM_LOCAL = 5              // Álbum solo local
      
      // Categoría B: Completitud de datos (max 30 puntos)
      const val GENRE_SPECIFIC = 5           // Género específico (no genérico)
      const val GENRE_GENERIC = 3            // Género genérico pero válido
      const val YEAR_VALID = 5               // Año presente y válido
      const val COVER_ART_HD = 8             // Carátula HD (>= 1000x1000)
      const val COVER_ART_NORMAL = 5         // Carátula normal (>= 600x600)
      const val COVER_ART_LOW = 2            // Carátula baja resolución
      const val LYRICS_AVAILABLE = 7         // Letra disponible
      const val CREDITS_FULL = 5             // Credits completos
      const val CREDITS_PARTIAL = 3          // Credits parciales
      
      // Categoría C: Calidad de artista (max 15 puntos)
      const val ARTIST_GENIUS_VERIFIED = 8   // Artista verificado en Genius
      const val ARTIST_HAS_IMAGE = 7         // Artista tiene imagen
      
      // Categoría D: Enlaces externos (max 10 puntos)
      const val HAS_SPOTIFY_ID = 3
      const val HAS_YOUTUBE_URL = 3
      const val HAS_APPLE_MUSIC_ID = 2
      const val HAS_SOUNDCLOUD_ID = 2
      
      // Categoría E: Penalizaciones
      const val PENALTY_TITLE_LOW_SIMILARITY = -15
      const val PENALTY_ARTIST_LOW_SIMILARITY = -15
      const val PENALTY_ALBUM_UNKNOWN = -10
      const val PENALTY_GENRE_GENERIC = -5
      const val PENALTY_UNRESOLVED_CONFLICTS = -5
      const val PENALTY_METADATA_JUNK = -3
      const val PENALTY_DUBIOUS_CONTENT = -2
   }
   
   // ==================== MODOS DE OPERACIÓN ====================
   
   /**
    * Modos predefinidos de operación del pipeline
    */
   enum class ProcessingMode {
      /**
       * Modo rápido: Solo limpieza local, sin API
       * Tiempo estimado: ~20 segundos para 5000 canciones
       */
      FAST_LOCAL_ONLY,
      
      /**
       * Modo balanceado: API para nuevas, local para existentes
       * Tiempo estimado: ~30 minutos para 500 nuevas
       */
      BALANCED,
      
      /**
       * Modo completo: API para todas las canciones
       * Tiempo estimado: ~3 horas para 5000 canciones
       */
      FULL_ENRICHMENT,
      
      /**
       * Modo conservador: Máxima validación, mínima modificación
       * Prioriza precisión sobre velocidad
       */
      CONSERVATIVE
   }
   
   /**
    * Aplica configuración según el modo seleccionado
    */
   fun applyMode(mode: ProcessingMode) {
      when (mode) {
         ProcessingMode.FAST_LOCAL_ONLY -> {
            ENABLE_AUTO_ENRICHMENT = false
            ENRICH_ON_PLAY = false
            ENRICH_IN_BACKGROUND = false
            MIN_CONFIDENCE_FOR_VERIFIED = 60
         }
         ProcessingMode.BALANCED -> {
            ENABLE_AUTO_ENRICHMENT = true
            ENRICH_ON_PLAY = true
            ENRICH_IN_BACKGROUND = true
            BACKGROUND_BATCH_SIZE = 50
            GENIUS_RATE_LIMIT = 10
         }
         ProcessingMode.FULL_ENRICHMENT -> {
            ENABLE_AUTO_ENRICHMENT = true
            ENRICH_ON_PLAY = true
            ENRICH_IN_BACKGROUND = true
            BACKGROUND_BATCH_SIZE = 100
            GENIUS_RATE_LIMIT = 15  // Más agresivo
         }
         ProcessingMode.CONSERVATIVE -> {
            MIN_TITLE_SIMILARITY = 0.7  // Más estricto
            MIN_ARTIST_SIMILARITY = 0.6
            MIN_CONFIDENCE_FOR_VERIFIED = 90
            ENABLE_SNAPSHOTS = true
            MAX_ENRICHMENT_ATTEMPTS = 5
         }
      }
   }
   
   /**
    * Restaura configuración por defecto
    */
   fun resetToDefaults() {
      MIN_TITLE_LENGTH = 1
      MAX_TITLE_LENGTH = 200
      MIN_MUSIC_CONFIDENCE = 0.6
      MIN_TITLE_SIMILARITY = 0.4
      MIN_ARTIST_SIMILARITY = 0.3
      VERIFIED_TITLE_SIMILARITY = 0.8
      VERIFIED_ARTIST_SIMILARITY = 0.8
      GENIUS_RATE_LIMIT = 10
      GENIUS_TIMEOUT_MS = 10_000L
      GENIUS_MAX_RETRIES = 3
      ENABLE_AUTO_ENRICHMENT = true
      ENRICH_ON_PLAY = true
      ENRICH_IN_BACKGROUND = true
      BACKGROUND_BATCH_SIZE = 50
      MIN_CONFIDENCE_FOR_VERIFIED = 80
      MIN_CONFIDENCE_FOR_PARTIAL = 60
      SCAN_BATCH_SIZE = 50
      CACHE_MAX_SIZE = 500
   }
   
   // ==================== DATA CLASSES ====================
   
   data class SimilarityWeights(
      val levenshteinWeight: Double = 0.50,
      val jaccardWeight: Double = 0.30,
      val phoneticWeight: Double = 0.20
   ) {
      init {
         require(levenshteinWeight + jaccardWeight + phoneticWeight in 0.99..1.01) {
            "Los pesos deben sumar 1.0"
         }
      }
   }
}