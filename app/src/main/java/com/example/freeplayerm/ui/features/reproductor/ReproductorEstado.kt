package com.example.freeplayerm.ui.features.reproductor

import com.example.freeplayerm.data.local.entity.relations.CancionConArtista

/**
 * ⚡ ESTADO DEL REPRODUCTOR - OPTIMIZADO v2.0
 *
 * Mejoras implementadas:
 * ✅ Progreso temporal separado para scrubbing sin glitches
 * ✅ Propiedades computadas eficientes
 * ✅ Validaciones robustas
 * ✅ Sin estado muerto (anguloRotacionVinilo movido a UI)
 *
 * @version 2.0 - Production Ready
 */

// ==================== ENUMS ====================

/**
 * Modos de reproducción disponibles
 */
enum class ModoReproduccion {
    EN_ORDEN,
    ALEATORIO;

    fun toggle(): ModoReproduccion = when (this) {
        EN_ORDEN -> ALEATORIO
        ALEATORIO -> EN_ORDEN
    }
}

/**
 * Modos de repetición disponibles
 */
enum class ModoRepeticion {
    NO_REPETIR,
    REPETIR_LISTA,
    REPETIR_CANCION;

    fun siguiente(): ModoRepeticion = when (this) {
        NO_REPETIR -> REPETIR_LISTA
        REPETIR_LISTA -> REPETIR_CANCION
        REPETIR_CANCION -> NO_REPETIR
    }
}

// ==================== ESTADO UI ====================

/**
 * Estado inmutable del reproductor
 * Contiene toda la información necesaria para renderizar la UI
 *
 * @property cancionActual Canción que se está reproduciendo actualmente
 * @property estaReproduciendo True si el audio está sonando
 * @property progresoActualMs Posición real de reproducción en milisegundos
 * @property progresoTemporalMs Posición temporal mientras el usuario arrastra el slider (null si no está arrastrando)
 * @property modoReproduccion Modo de reproducción (en orden o aleatorio)
 * @property modoRepeticion Modo de repetición (no repetir, lista o canción)
 * @property esFavorita True si la canción actual está en favoritos
 * @property isScrubbing True si el usuario está arrastrando el slider de progreso
 */
data class ReproductorEstado(
    val cancionActual: CancionConArtista? = null,
    val estaReproduciendo: Boolean = false,
    val progresoActualMs: Long = 0L,
    val progresoTemporalMs: Long? = null, // Nuevo: progreso mientras se arrastra
    val modoReproduccion: ModoReproduccion = ModoReproduccion.EN_ORDEN,
    val modoRepeticion: ModoRepeticion = ModoRepeticion.NO_REPETIR,
    val esFavorita: Boolean = false,
    val isScrubbing: Boolean = false
) {
    /**
     * Progreso visible en la UI
     * Usa progresoTemporalMs cuando está scrubbing, sino progresoActualMs
     */
    val progresoVisibleMs: Long
        get() = progresoTemporalMs ?: progresoActualMs

    /**
     * Progreso en porcentaje [0.0, 1.0] para la UI
     */
    val progresoPorcentaje: Float
        get() {
            val duracion = duracionTotalMs
            return if (duracion > 0) {
                (progresoVisibleMs.toFloat() / duracion).coerceIn(0f, 1f)
            } else 0f
        }

    /**
     * Duración total en milisegundos
     */
    val duracionTotalMs: Long
        get() = (cancionActual?.cancion?.duracionSegundos?.toLong() ?: 0L) * 1000L

    /**
     * Verifica si hay una canción cargada
     */
    val tieneCancion: Boolean
        get() = cancionActual != null

    /**
     * Verifica si el reproductor está en un estado válido para reproducir
     */
    val puedeReproducir: Boolean
        get() = cancionActual != null && cancionActual.cancion.archivoPath != null

    /**
     * Información del artista o "Desconocido"
     */
    val artistaDisplay: String
        get() = cancionActual?.artistaNombre ?: "Artista Desconocido"

    /**
     * Título de la canción o "Sin título"
     */
    val tituloDisplay: String
        get() = cancionActual?.cancion?.titulo ?: "Sin título"
}

// ==================== EVENTOS ====================

/**
 * Eventos del reproductor usando sealed interface para type safety
 */
sealed interface ReproductorEvento {

    // ==================== EVENTOS DE REPRODUCCIÓN ====================

    sealed interface Reproduccion : ReproductorEvento {
        /**
         * Establece una nueva cola de reproducción y comienza a reproducir
         * @param cola Lista de canciones a reproducir
         * @param cancionInicial Canción con la que comenzar
         */
        data class EstablecerColaYReproducir(
            val cola: List<CancionConArtista>,
            val cancionInicial: CancionConArtista
        ) : Reproduccion

        /**
         * Alterna entre reproducir y pausar
         */
        data object ReproducirPausar : Reproduccion

        /**
         * Salta a la siguiente canción
         */
        data object SiguienteCancion : Reproduccion

        /**
         * Vuelve a la canción anterior o reinicia la actual
         */
        data object CancionAnterior : Reproduccion

        /**
         * Detiene completamente la reproducción y limpia la cola
         */
        data object Detener : Reproduccion
    }

    // ==================== EVENTOS DE NAVEGACIÓN TEMPORAL ====================

    sealed interface Navegacion : ReproductorEvento {
        /**
         * El usuario está arrastrando el slider de progreso
         * @param positionMs Nueva posición en milisegundos
         */
        data class OnScrub(val positionMs: Long) : Navegacion

        /**
         * El usuario soltó el slider de progreso
         * @param positionMs Posición final donde aplicar el seek
         */
        data class OnScrubFinished(val positionMs: Long) : Navegacion
    }

    // ==================== EVENTOS DE CONFIGURACIÓN ====================

    sealed interface Configuracion : ReproductorEvento {
        /**
         * Alterna entre reproducción en orden y aleatoria
         */
        data object CambiarModoReproduccion : Configuracion

        /**
         * Cicla entre los modos de repetición
         */
        data object CambiarModoRepeticion : Configuracion

        /**
         * Agrega o quita la canción actual de favoritos
         */
        data object AlternarFavorito : Configuracion
    }
}

// ==================== HELPERS Y EXTENSIONES ====================

/**
 * Formatea milisegundos a formato MM:SS
 */
fun Long.formatearTiempo(): String {
    val minutos = this / 60000
    val segundos = (this % 60000) / 1000
    return "%02d:%02d".format(minutos, segundos)
}

/**
 * Formatea milisegundos a formato extendido HH:MM:SS si es necesario
 */
fun Long.formatearTiempoExtendido(): String {
    val horas = this / 3600000
    val minutos = (this % 3600000) / 60000
    val segundos = (this % 60000) / 1000

    return if (horas > 0) {
        "%02d:%02d:%02d".format(horas, minutos, segundos)
    } else {
        "%02d:%02d".format(minutos, segundos)
    }
}

/**
 * Extension para facilitar el copiado del estado con progreso validado
 */
fun ReproductorEstado.actualizarProgreso(nuevoProgreso: Long): ReproductorEstado {
    return copy(
        progresoActualMs = nuevoProgreso.coerceIn(0L, duracionTotalMs)
    )
}

/**
 * Extension para validar si una posición de seek es válida
 */
fun ReproductorEstado.esPosicionValida(posicionMs: Long): Boolean {
    return posicionMs in 0..duracionTotalMs
}

/**
 * Extension para obtener el tiempo restante
 */
val ReproductorEstado.tiempoRestanteMs: Long
    get() = (duracionTotalMs - progresoVisibleMs).coerceAtLeast(0L)