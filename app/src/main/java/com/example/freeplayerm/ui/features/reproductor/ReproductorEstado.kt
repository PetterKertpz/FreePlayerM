// en: app/src/main/java/com/example/freeplayerm/ui/features/reproductor/ReproductorEstado.kt
package com.example.freeplayerm.ui.features.reproductor

import com.example.freeplayerm.data.local.entity.relations.CancionConArtista

// Representa los posibles modos de reproducción de la lista de canciones.
enum class ModoReproduccion {
    EN_ORDEN,
    ALEATORIO
}

// Representa los posibles modos de repetición.
enum class ModoRepeticion {
    NO_REPETIR,      // Repite la lista una vez y para.
    REPETIR_LISTA,   // Repite la lista indefinidamente.
    REPETIR_CANCION  // Repite la canción actual indefinidamente.
}

// Este data class contiene TODA la información que la UI del reproductor necesita para dibujarse.
// Usamos CancionEntity directamente, ya que contiene todos los datos que necesitamos.
data class ReproductorEstado(
    val cancionActual: CancionConArtista?,
    val estaReproduciendo: Boolean = false,
    val progresoActualMs: Long = 0L,
    val modoReproduccion: ModoReproduccion = ModoReproduccion.EN_ORDEN,
    val modoRepeticion: ModoRepeticion = ModoRepeticion.NO_REPETIR,
    val esFavorita: Boolean = false,
    val isScrubbing: Boolean = false
)

// Define todos los eventos (acciones del usuario) que la UI puede enviar al ViewModel.
sealed class ReproductorEvento {
    data class SeleccionarCancion(val cancion: CancionConArtista) : ReproductorEvento()
    data class EstablecerColaYReproducir(
        val cola: List<CancionConArtista>,
        val cancionInicial: CancionConArtista
    ) : ReproductorEvento()
    object ReproducirPausar : ReproductorEvento()
    object SiguienteCancion : ReproductorEvento()
    object CancionAnterior : ReproductorEvento()
    object CambiarModoReproduccion : ReproductorEvento()
    object CambiarModoRepeticion : ReproductorEvento()
    object AlternarFavorito : ReproductorEvento()
    data class OnScrub(val position: Float) : ReproductorEvento()
    data class OnScrubFinished(val position: Float) : ReproductorEvento()
    object Detener : ReproductorEvento()
}