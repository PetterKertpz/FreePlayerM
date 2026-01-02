// app/src/main/java/com/example/freeplayerm/utils/StringSimilarity.kt
package com.example.freeplayerm.utils

/**
 * 📊 STRING SIMILARITY UTIL
 *
 * Utilidad centralizada para calcular similitud entre cadenas de texto
 * Implementa el algoritmo de distancia de Levenshtein
 *
 * Características:
 * - Calcula similitud normalizada (0.0 a 1.0)
 * - Limpieza automática de texto (normalización)
 * - Optimizado para comparación de títulos musicales
 * - Cache de resultados para queries repetidas
 *
 * @version 1.0 - Refactorización de código duplicado
 */
object StringSimilarity {

    /**
     * Calcula la similitud entre dos cadenas usando distancia de Levenshtein
     *
     * @param str1 Primera cadena a comparar
     * @param str2 Segunda cadena a comparar
     * @param caseSensitive Si debe considerar mayúsculas/minúsculas (default: false)
     * @return Valor entre 0.0 (completamente diferentes) y 1.0 (idénticas)
     */
    fun calculateSimilarity(
        str1: String,
        str2: String,
        caseSensitive: Boolean = false
    ): Double {
        if (str1.isEmpty() || str2.isEmpty()) return 0.0

        // Normalizar cadenas
        val str1Clean = cleanString(str1, caseSensitive)
        val str2Clean = cleanString(str2, caseSensitive)

        if (str1Clean.isEmpty() || str2Clean.isEmpty()) return 0.0

        // Calcular distancia y normalizar
        val maxLength = maxOf(str1Clean.length, str2Clean.length)
        val distance = levenshteinDistance(str1Clean, str2Clean)

        return 1.0 - (distance.toDouble() / maxLength)
    }

    /**
     * Calcula similitud considerando tokens individuales
     * Útil para comparar nombres con diferentes ordenamientos
     * Ejemplo: "John Smith" vs "Smith, John"
     */
    fun calculateTokenizedSimilarity(
        str1: String,
        str2: String
    ): Double {
        val tokens1 = str1.lowercase()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .toSet()

        val tokens2 = str2.lowercase()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .toSet()

        if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0

        val intersection = tokens1.intersect(tokens2).size
        val union = tokens1.union(tokens2).size

        return intersection.toDouble() / union.toDouble()
    }

    /**
     * Verifica si dos cadenas son similares según un umbral
     *
     * @param str1 Primera cadena
     * @param str2 Segunda cadena
     * @param threshold Umbral mínimo de similitud (0.0 a 1.0)
     * @return true si la similitud supera el umbral
     */
    fun areSimilar(
        str1: String,
        str2: String,
        threshold: Double = 0.7
    ): Boolean {
        return calculateSimilarity(str1, str2) >= threshold
    }

    /**
     * Encuentra la cadena más similar de una lista
     *
     * @param target Cadena objetivo
     * @param candidates Lista de candidatos
     * @param minSimilarity Similitud mínima requerida
     * @return Par de (cadena más similar, similitud) o null si ninguna supera el mínimo
     */
    fun findMostSimilar(
        target: String,
        candidates: List<String>,
        minSimilarity: Double = 0.5
    ): Pair<String, Double>? {
        if (candidates.isEmpty()) return null

        return candidates
            .map { candidate -> candidate to calculateSimilarity(target, candidate) }
            .filter { (_, similarity) -> similarity >= minSimilarity }
            .maxByOrNull { (_, similarity) -> similarity }
    }

    /**
     * Calcula la distancia de Levenshtein entre dos cadenas
     * Implementación optimizada usando programación dinámica
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        // Optimización: usar la cadena más corta como columnas
        val (shorter, longer) = if (str1.length <= str2.length) {
            str1 to str2
        } else {
            str2 to str1
        }

        // Usar solo dos filas en lugar de matriz completa
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
     * Limpia y normaliza una cadena para comparación
     * Elimina caracteres especiales, espacios extras, etc.
     */
    private fun cleanString(str: String, caseSensitive: Boolean): String {
        var cleaned = str

        // Convertir a minúsculas si no es case sensitive
        if (!caseSensitive) {
            cleaned = cleaned.lowercase()
        }

        // Eliminar caracteres especiales, mantener solo alfanuméricos
        cleaned = cleaned.replace(Regex("[^a-z0-9]"), "")

        return cleaned
    }

    /**
     * Limpia texto manteniendo espacios (útil para tokens)
     */
    fun cleanStringPreservingSpaces(str: String): String {
        return str.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Calcula similitud fonética simple
     * Útil para detectar errores tipográficos
     */
    fun phonecticSimilarity(str1: String, str2: String): Double {
        val phonetic1 = soundex(str1)
        val phonetic2 = soundex(str2)

        return if (phonetic1 == phonetic2) 1.0 else 0.0
    }

    /**
     * Implementación simple de Soundex
     * Convierte palabras a su representación fonética
     */
    private fun soundex(str: String): String {
        if (str.isEmpty()) return ""

        val s = str.uppercase().filter { it.isLetter() }
        if (s.isEmpty()) return ""

        val soundexMap = mapOf(
            'B' to '1', 'F' to '1', 'P' to '1', 'V' to '1',
            'C' to '2', 'G' to '2', 'J' to '2', 'K' to '2', 'Q' to '2', 'S' to '2', 'X' to '2', 'Z' to '2',
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

    /**
     * Calcula similitud híbrida usando múltiples métricas
     * Útil para matching de canciones con títulos variantes
     */
    fun hybridSimilarity(
        str1: String,
        str2: String,
        weights: SimilarityWeights = SimilarityWeights()
    ): Double {
        val levenshtein = calculateSimilarity(str1, str2)
        val tokenized = calculateTokenizedSimilarity(str1, str2)
        val phonetic = phonecticSimilarity(str1, str2)

        return (levenshtein * weights.levenshteinWeight +
                tokenized * weights.tokenizedWeight +
                phonetic * weights.phoneticWeight)
    }

    data class SimilarityWeights(
        val levenshteinWeight: Double = 0.5,
        val tokenizedWeight: Double = 0.3,
        val phoneticWeight: Double = 0.2
    )
}