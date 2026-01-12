// app/src/main/java/com/example/freeplayerm/utils/StringSimilarity.kt
package com.example.freeplayerm.utils

import java.text.Normalizer

/**
 * 📊 STRING SIMILARITY UTIL v2.0
 *
 * Utilidad centralizada para calcular similitud entre cadenas de texto
 * Implementa múltiples algoritmos de comparación con pesos adaptativos
 *
 * Características:
 * - Levenshtein Distance optimizado
 * - Jaccard Similarity (tokenizado)
 * - Soundex (similitud fonética)
 * - Similitud híbrida con pesos adaptativos
 * - Normalización Unicode completa (acentos, diacríticos)
 * - Cache LRU para queries repetidas
 *
 * @version 2.0 - Mejoras para sistema de purificación de metadata
 */
object StringSimilarity {
   
   // ==================== CACHE LRU ====================
   
   private const val CACHE_MAX_SIZE = 500
   private val similarityCache = object : LinkedHashMap<String, Double>(
      CACHE_MAX_SIZE, 0.75f, true
   ) {
      override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Double>?): Boolean {
         return size > CACHE_MAX_SIZE
      }
   }
   
   /**
    * Limpia el cache de similitud
    */
   fun clearCache() {
      synchronized(similarityCache) {
         similarityCache.clear()
      }
   }
   
   // ==================== NORMALIZACIÓN ====================
   
   /**
    * Normaliza texto removiendo acentos y diacríticos usando Unicode NFD
    * "Café" -> "cafe", "Ñoño" -> "nono"
    */
   fun normalizeUnicode(text: String): String {
      return Normalizer.normalize(text, Normalizer.Form.NFD)
         .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
         .lowercase()
   }
   
   /**
    * Limpia y normaliza una cadena para comparación
    * Elimina caracteres especiales, espacios extras, normaliza Unicode
    */
   fun cleanForComparison(str: String): String {
      return normalizeUnicode(str)
         .replace(Regex("[^a-z0-9\\s]"), "")
         .replace(Regex("\\s+"), " ")
         .trim()
   }
   
   /**
    * Limpia cadena removiendo TODO excepto alfanuméricos
    * Útil para comparación estricta
    */
   private fun cleanStringStrict(str: String): String {
      return normalizeUnicode(str)
         .replace(Regex("[^a-z0-9]"), "")
   }
   
   // ==================== ALGORITMOS DE SIMILITUD ====================
   
   /**
    * Calcula la similitud entre dos cadenas usando distancia de Levenshtein
    *
    * @param str1 Primera cadena a comparar
    * @param str2 Segunda cadena a comparar
    * @param useCache Si debe usar cache para resultados (default: true)
    * @return Valor entre 0.0 (completamente diferentes) y 1.0 (idénticas)
    */
   fun calculateSimilarity(
      str1: String,
      str2: String,
      useCache: Boolean = true
   ): Double {
      if (str1.isEmpty() || str2.isEmpty()) return 0.0
      
      val str1Clean = cleanStringStrict(str1)
      val str2Clean = cleanStringStrict(str2)
      
      if (str1Clean.isEmpty() || str2Clean.isEmpty()) return 0.0
      if (str1Clean == str2Clean) return 1.0
      
      // Check cache
      if (useCache) {
         val cacheKey = "${str1Clean}|${str2Clean}"
         val cached = synchronized(similarityCache) { similarityCache[cacheKey] }
         if (cached != null) return cached
      }
      
      val maxLength = maxOf(str1Clean.length, str2Clean.length)
      val distance = levenshteinDistance(str1Clean, str2Clean)
      val similarity = 1.0 - (distance.toDouble() / maxLength)
      
      // Store in cache
      if (useCache) {
         val cacheKey = "${str1Clean}|${str2Clean}"
         synchronized(similarityCache) {
            similarityCache[cacheKey] = similarity
         }
      }
      
      return similarity
   }
   
   /**
    * Calcula similitud de Jaccard basada en tokens
    * Útil para comparar nombres con diferentes ordenamientos
    *
    * Ejemplo: "John Smith" vs "Smith John" = 1.0
    */
   fun calculateJaccardSimilarity(str1: String, str2: String): Double {
      val tokens1 = cleanForComparison(str1)
         .split(Regex("\\s+"))
         .filter { it.isNotBlank() }
         .toSet()
      
      val tokens2 = cleanForComparison(str2)
         .split(Regex("\\s+"))
         .filter { it.isNotBlank() }
         .toSet()
      
      if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0
      if (tokens1 == tokens2) return 1.0
      
      val intersection = tokens1.intersect(tokens2).size
      val union = tokens1.union(tokens2).size
      
      return intersection.toDouble() / union.toDouble()
   }
   
   /**
    * Alias para compatibilidad con código existente
    */
   fun calculateTokenizedSimilarity(str1: String, str2: String): Double {
      return calculateJaccardSimilarity(str1, str2)
   }
   
   /**
    * Calcula similitud fonética usando Soundex
    * Útil para detectar errores tipográficos y variantes
    */
   fun calculatePhoneticSimilarity(str1: String, str2: String): Double {
      val words1 = cleanForComparison(str1).split(Regex("\\s+")).filter { it.isNotBlank() }
      val words2 = cleanForComparison(str2).split(Regex("\\s+")).filter { it.isNotBlank() }
      
      if (words1.isEmpty() || words2.isEmpty()) return 0.0
      
      // Comparar soundex de cada palabra
      val soundex1 = words1.map { soundex(it) }.toSet()
      val soundex2 = words2.map { soundex(it) }.toSet()
      
      val intersection = soundex1.intersect(soundex2).size
      val union = soundex1.union(soundex2).size
      
      return if (union > 0) intersection.toDouble() / union.toDouble() else 0.0
   }
   
   /**
    * Alias para compatibilidad
    */
   fun phonecticSimilarity(str1: String, str2: String): Double {
      return calculatePhoneticSimilarity(str1, str2)
   }
   
   // ==================== SIMILITUD HÍBRIDA ====================
   
   /**
    * Calcula similitud híbrida usando múltiples métricas
    * con PESOS ADAPTATIVOS según características del texto
    *
    * @param str1 Primera cadena
    * @param str2 Segunda cadena
    * @param weights Pesos personalizados (opcional, usa adaptativos si null)
    * @return Similitud combinada entre 0.0 y 1.0
    */
   fun hybridSimilarity(
      str1: String,
      str2: String,
      weights: SimilarityWeights? = null
   ): Double {
      val effectiveWeights = weights ?: calculateAdaptiveWeights(str1, str2)
      
      val levenshtein = calculateSimilarity(str1, str2)
      val jaccard = calculateJaccardSimilarity(str1, str2)
      val phonetic = calculatePhoneticSimilarity(str1, str2)
      
      return (levenshtein * effectiveWeights.levenshteinWeight +
            jaccard * effectiveWeights.jaccardWeight +
            phonetic * effectiveWeights.phoneticWeight)
   }
   
   /**
    * Calcula pesos adaptativos según el número de tokens
    *
    * Lógica:
    * - Pocas palabras (≤3): Priorizar Levenshtein (orden importa)
    * - Muchas palabras (>3): Priorizar Jaccard (tokens importan más)
    * - Palabras cortas: Boost fonético (errores tipográficos comunes)
    */
   fun calculateAdaptiveWeights(str1: String, str2: String): SimilarityWeights {
      val tokens1 = cleanForComparison(str1).split(Regex("\\s+")).filter { it.isNotBlank() }
      val tokens2 = cleanForComparison(str2).split(Regex("\\s+")).filter { it.isNotBlank() }
      
      val avgTokenCount = (tokens1.size + tokens2.size) / 2.0
      val avgWordLength = (tokens1 + tokens2)
         .map { it.length }
         .average()
         .takeIf { !it.isNaN() } ?: 5.0
      
      return when {
         // Cadenas muy cortas (1-2 palabras): Levenshtein domina
         avgTokenCount <= 2 -> SimilarityWeights(
            levenshteinWeight = 0.70,
            jaccardWeight = 0.15,
            phoneticWeight = 0.15
         )
         // Cadenas cortas (3 palabras): Balance
         avgTokenCount <= 3 -> SimilarityWeights(
            levenshteinWeight = 0.50,
            jaccardWeight = 0.30,
            phoneticWeight = 0.20
         )
         // Cadenas medianas (4-5 palabras): Jaccard gana peso
         avgTokenCount <= 5 -> SimilarityWeights(
            levenshteinWeight = 0.35,
            jaccardWeight = 0.45,
            phoneticWeight = 0.20
         )
         // Cadenas largas (>5 palabras): Jaccard domina
         else -> SimilarityWeights(
            levenshteinWeight = 0.25,
            jaccardWeight = 0.55,
            phoneticWeight = 0.20
         )
      }.let { base ->
         // Ajuste adicional: palabras cortas = más errores tipográficos
         if (avgWordLength < 4) {
            base.copy(
               phoneticWeight = base.phoneticWeight + 0.10,
               levenshteinWeight = base.levenshteinWeight - 0.10
            )
         } else {
            base
         }
      }
   }
   
   // ==================== UTILIDADES DE COMPARACIÓN ====================
   
   /**
    * Verifica si dos cadenas son similares según un umbral
    */
   fun areSimilar(
      str1: String,
      str2: String,
      threshold: Double = 0.7,
      useHybrid: Boolean = true
   ): Boolean {
      val similarity = if (useHybrid) {
         hybridSimilarity(str1, str2)
      } else {
         calculateSimilarity(str1, str2)
      }
      return similarity >= threshold
   }
   
   /**
    * Encuentra la cadena más similar de una lista
    *
    * @param target Cadena objetivo
    * @param candidates Lista de candidatos
    * @param minSimilarity Similitud mínima requerida
    * @param useHybrid Usar algoritmo híbrido
    * @return Par de (cadena más similar, similitud) o null
    */
   fun findMostSimilar(
      target: String,
      candidates: List<String>,
      minSimilarity: Double = 0.5,
      useHybrid: Boolean = true
   ): Pair<String, Double>? {
      if (candidates.isEmpty()) return null
      
      return candidates
         .map { candidate ->
            val similarity = if (useHybrid) {
               hybridSimilarity(target, candidate)
            } else {
               calculateSimilarity(target, candidate)
            }
            candidate to similarity
         }
         .filter { (_, similarity) -> similarity >= minSimilarity }
         .maxByOrNull { (_, similarity) -> similarity }
   }
   
   /**
    * Encuentra todos los candidatos que superan el umbral
    * Ordenados por similitud descendente
    */
   fun findAllSimilar(
      target: String,
      candidates: List<String>,
      minSimilarity: Double = 0.5,
      useHybrid: Boolean = true
   ): List<Pair<String, Double>> {
      return candidates
         .map { candidate ->
            val similarity = if (useHybrid) {
               hybridSimilarity(target, candidate)
            } else {
               calculateSimilarity(target, candidate)
            }
            candidate to similarity
         }
         .filter { (_, similarity) -> similarity >= minSimilarity }
         .sortedByDescending { (_, similarity) -> similarity }
   }
   
   // ==================== ALGORITMOS INTERNOS ====================
   
   /**
    * Calcula la distancia de Levenshtein entre dos cadenas
    * Implementación optimizada usando solo 2 filas (O(min(m,n)) espacio)
    */
   private fun levenshteinDistance(str1: String, str2: String): Int {
      val (shorter, longer) = if (str1.length <= str2.length) {
         str1 to str2
      } else {
         str2 to str1
      }
      
      // Optimización: si una cadena está vacía
      if (shorter.isEmpty()) return longer.length
      
      var previousRow = IntArray(shorter.length + 1) { it }
      var currentRow = IntArray(shorter.length + 1)
      
      for (i in 1..longer.length) {
         currentRow[0] = i
         
         for (j in 1..shorter.length) {
            val cost = if (longer[i - 1] == shorter[j - 1]) 0 else 1
            
            currentRow[j] = minOf(
               currentRow[j - 1] + 1,      // Inserción
               previousRow[j] + 1,          // Eliminación
               previousRow[j - 1] + cost    // Sustitución
            )
         }
         
         // Intercambiar filas
         val temp = previousRow
         previousRow = currentRow
         currentRow = temp
      }
      
      return previousRow[shorter.length]
   }
   
   /**
    * Implementación de Soundex para similitud fonética
    * Convierte palabras a representación fonética de 4 caracteres
    */
   private fun soundex(str: String): String {
      if (str.isEmpty()) return "0000"
      
      val s = str.uppercase().filter { it.isLetter() }
      if (s.isEmpty()) return "0000"
      
      val soundexMap = mapOf(
         'B' to '1', 'F' to '1', 'P' to '1', 'V' to '1',
         'C' to '2', 'G' to '2', 'J' to '2', 'K' to '2',
         'Q' to '2', 'S' to '2', 'X' to '2', 'Z' to '2',
         'D' to '3', 'T' to '3',
         'L' to '4',
         'M' to '5', 'N' to '5',
         'R' to '6'
      )
      
      val result = StringBuilder()
      result.append(s[0])
      
      var lastCode = soundexMap[s[0]] ?: '0'
      
      for (i in 1 until s.length) {
         val code = soundexMap[s[i]] ?: '0'
         if (code != '0' && code != lastCode) {
            result.append(code)
            lastCode = code
         }
         if (result.length >= 4) break
      }
      
      while (result.length < 4) {
         result.append('0')
      }
      
      return result.toString()
   }
   
   // ==================== DATA CLASSES ====================
   
   /**
    * Pesos para el algoritmo de similitud híbrida
    */
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
      
      // Alias para compatibilidad
      val tokenizedWeight: Double get() = jaccardWeight
   }
   
   // ==================== FUNCIONES DE COMPATIBILIDAD ====================
   
   /**
    * Mantiene compatibilidad con código existente
    */
   fun cleanStringPreservingSpaces(str: String): String = cleanForComparison(str)
}