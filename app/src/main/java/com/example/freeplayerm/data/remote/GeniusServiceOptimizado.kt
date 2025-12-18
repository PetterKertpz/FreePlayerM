// app/src/main/java/com/example/freeplayerm/data/remote/GeniusServiceOptimizado.kt
package com.example.freeplayerm.data.remote

import android.util.Log
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.remote.dto.GeniusSearchResponse
import com.example.freeplayerm.data.remote.dto.SongResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeniusServiceOptimizado @Inject constructor(
    private val geniusApi: GeniusApiService
) {

    suspend fun buscarCancionOptimizado(
        cancion: CancionEntity,
        artista: ArtistaEntity
    ): ResultadoBusquedaGenius {
        return withContext(Dispatchers.IO) {
            try {
                var resultado: ResultadoBusquedaGenius

                // Estrategia 1: Búsqueda directa
                resultado = busquedaDirecta(cancion, artista)
                if (resultado is ResultadoBusquedaGenius.Exito) {
                    Log.d("GeniusOptimizado", "✅ Éxito con estrategia: ${resultado.estrategia}")
                    return@withContext resultado
                }
                delay(200)

                // Estrategia 2: Búsqueda sin caracteres especiales
                resultado = busquedaSinCaracteresEspeciales(cancion, artista)
                if (resultado is ResultadoBusquedaGenius.Exito) {
                    Log.d("GeniusOptimizado", "✅ Éxito con estrategia: ${resultado.estrategia}")
                    return@withContext resultado
                }
                delay(200)

                // Estrategia 3: Búsqueda por artista primero
                resultado = busquedaPorArtistaPrimero(cancion, artista)
                if (resultado is ResultadoBusquedaGenius.Exito) {
                    Log.d("GeniusOptimizado", "✅ Éxito con estrategia: ${resultado.estrategia}")
                    return@withContext resultado
                }
                delay(200)

                // Estrategia 4: Búsqueda por título solo
                resultado = busquedaPorTituloSolo(cancion)
                if (resultado is ResultadoBusquedaGenius.Exito) {
                    Log.d("GeniusOptimizado", "✅ Éxito con estrategia: ${resultado.estrategia}")
                    return@withContext resultado
                }
                delay(200)

                // Estrategia 5: Búsqueda por artista solo
                resultado = busquedaPorArtistaSolo(artista)
                if (resultado is ResultadoBusquedaGenius.Exito) {
                    Log.d("GeniusOptimizado", "✅ Éxito con estrategia: ${resultado.estrategia}")
                    return@withContext resultado
                }

                ResultadoBusquedaGenius.NoEncontrado

            } catch (e: Exception) {
                ResultadoBusquedaGenius.Error(e.message ?: "Error desconocido")
            }
        }
    }

    private suspend fun busquedaDirecta(cancion: CancionEntity, artista: ArtistaEntity): ResultadoBusquedaGenius {
        val query = "${cancion.titulo} ${artista.nombre}"
        return buscarEnGenius(query, "directa")
    }

    private suspend fun busquedaSinCaracteresEspeciales(cancion: CancionEntity, artista: ArtistaEntity): ResultadoBusquedaGenius {
        val tituloLimpio = limpiarParaBusqueda(cancion.titulo)
        val artistaLimpio = limpiarParaBusqueda(artista.nombre)
        val query = "$tituloLimpio $artistaLimpio"
        return buscarEnGenius(query, "sin_especiales")
    }

    private suspend fun busquedaPorArtistaPrimero(cancion: CancionEntity, artista: ArtistaEntity): ResultadoBusquedaGenius {
        val query = "${artista.nombre} ${cancion.titulo}"
        return buscarEnGenius(query, "artista_primero")
    }

    private suspend fun busquedaPorTituloSolo(cancion: CancionEntity): ResultadoBusquedaGenius {
        return buscarEnGenius(cancion.titulo, "titulo_solo")
    }

    private suspend fun busquedaPorArtistaSolo(artista: ArtistaEntity): ResultadoBusquedaGenius {
        return buscarEnGenius(artista.nombre, "artista_solo")
    }

    private suspend fun buscarEnGenius(query: String, estrategia: String): ResultadoBusquedaGenius {
        return try {
            Log.d("GeniusOptimizado", "Buscando: '$query' (Estrategia: $estrategia)")

            val response: Response<GeniusSearchResponse> = geniusApi.search(query)

            return if (response.isSuccessful && response.body() != null) {
                val resultados = response.body()!!.response?.hits ?: emptyList()

                if (resultados.isNotEmpty()) {
                    val mejorResultado = seleccionarMejorResultado(resultados, query)

                    if (mejorResultado != null) {
                        ResultadoBusquedaGenius.Exito(mejorResultado, estrategia)
                    } else {
                        ResultadoBusquedaGenius.NoEncontrado
                    }
                } else {
                    ResultadoBusquedaGenius.NoEncontrado
                }
            } else {
                ResultadoBusquedaGenius.NoEncontrado
            }
        } catch (e: Exception) {
            ResultadoBusquedaGenius.Error("Error en estrategia $estrategia: ${e.message}")
        }
    }

    private fun seleccionarMejorResultado(resultados: List<com.example.freeplayerm.data.remote.dto.Hit>, queryOriginal: String): SongResult? {
        val resultadosValidos = resultados.mapNotNull { it.result }.filter { it.isValid() }

        if (resultadosValidos.isEmpty()) return null

        return resultadosValidos.maxByOrNull { resultado ->
            var score = 0.0

            // Puntuar por similitud con el título
            score += calcularSimilitud(resultado.title, queryOriginal) * 0.6

            // Bonus por match exacto de artista
            if (resultado.primary_artist?.name?.contains(queryOriginal, ignoreCase = true) == true) {
                score += 0.3
            }

            score
        }
    }

    private fun calcularSimilitud(texto1: String, texto2: String): Double {
        val palabras1 = texto1.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
        val palabras2 = texto2.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()

        if (palabras1.isEmpty() || palabras2.isEmpty()) return 0.0

        val interseccion = palabras1.intersect(palabras2).size
        val union = palabras1.union(palabras2).size

        return interseccion.toDouble() / union.toDouble()
    }

    private fun limpiarParaBusqueda(texto: String): String {
        return texto
            .replace(Regex("""[^\w\s]"""), "") // Eliminar caracteres especiales
            .replace(Regex("""\s+"""), " ")    // Normalizar espacios
            .trim()
    }

    private fun SongResult.isValid(): Boolean {
        return id.isNotBlank() && title.isNotBlank() && url.isNotBlank() && primary_artist != null
    }
}

// Resultados de búsqueda
sealed class ResultadoBusquedaGenius {
    data class Exito(val resultado: SongResult, val estrategia: String) : ResultadoBusquedaGenius()
    object NoEncontrado : ResultadoBusquedaGenius()
    data class Error(val mensaje: String) : ResultadoBusquedaGenius()
}