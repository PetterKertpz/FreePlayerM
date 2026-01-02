package com.example.freeplayerm.ui.features.player

import com.example.freeplayerm.data.local.entity.relations.SongWithArtist

/**
 * ⚡ ESTADO DEL REPRODUCTOR - v3.0
 *
 * Sistema de 3 modos del panel:
 * - MINIMIZADO (15%): Durante scroll activo
 * - NORMAL (25-30%): Estado por defecto
 * - EXPANDIDO (100%): Pantalla completa con tabs
 *
 * Mejoras implementadas: ✅ Sistema de 3 modos (MINIMIZADO, NORMAL, EXPANDIDO) ✅ Scroll-awareness
 * para minimización automática ✅ Tabs para modo expandido (Letra, Info, Enlaces) ✅ Metadatos y
 * enlaces externos integrados ✅ Estados de carga unificados ✅ Progreso temporal para scrubbing sin
 * glitches
 *
 * @version 3.0 - Sistema de 3 Modos
 */

// ==================== ENUMS ====================

/** Modos de reproducción de cola */
enum class ModoReproduccion {
    EN_ORDEN,
    ALEATORIO;

    fun toggle(): ModoReproduccion =
        when (this) {
            EN_ORDEN -> ALEATORIO
            ALEATORIO -> EN_ORDEN
        }
}

/** Modos de repetición */
enum class ModoRepeticion {
    NO_REPETIR,
    REPETIR_LISTA,
    REPETIR_CANCION;

    fun siguiente(): ModoRepeticion =
        when (this) {
            NO_REPETIR -> REPETIR_LISTA
            REPETIR_LISTA -> REPETIR_CANCION
            REPETIR_CANCION -> NO_REPETIR
        }
}

/**
 * Modos de visualización del panel reproductor
 * - MINIMIZADO: 15% de pantalla, solo durante scroll activo
 * - NORMAL: 25-30% de pantalla, estado por defecto mientras reproduce
 * - EXPANDIDO: 100% de pantalla, con metadatos y tabs
 */
enum class ModoPanelReproductor {
    MINIMIZADO,
    NORMAL,
    EXPANDIDO;

    val fraccionPantalla: Float
        get() =
            when (this) {
                MINIMIZADO -> 0.15f
                NORMAL -> 0.28f
                EXPANDIDO -> 1f
            }

    val peekHeightDp: Int
        get() =
            when (this) {
                MINIMIZADO -> 72
                NORMAL -> 140
                EXPANDIDO -> Int.MAX_VALUE
            }
}

/** Tabs disponibles en modo expandido */
enum class TabExpandido {
    LETRA,
    INFO,
    ENLACES;

    val titulo: String
        get() =
            when (this) {
                LETRA -> "Letra"
                INFO -> "Info"
                ENLACES -> "Enlaces"
            }
}

// ==================== ESTADO UI ====================

/** Estado inmutable del reproductor Contiene toda la información necesaria para renderizar la UI */
data class PlayerState(
    // === Estado de reproducción ===
    val cancionActual: SongWithArtist? = null,
    val estaReproduciendo: Boolean = false,
    val progresoActualMs: Long = 0L,
    val progresoTemporalMs: Long? = null,
    val modoReproduccion: ModoReproduccion = ModoReproduccion.EN_ORDEN,
    val modoRepeticion: ModoRepeticion = ModoRepeticion.NO_REPETIR,
    val esFavorita: Boolean = false,
    val isScrubbing: Boolean = false,

    // === Estado del panel ===
    val modoPanel: ModoPanelReproductor = ModoPanelReproductor.NORMAL,
    val tabExpandidoActivo: TabExpandido = TabExpandido.LETRA,

    // === Metadatos expandidos ===
    val letra: String? = null,
    val infoArtista: String? = null,
    val descripcionAlbum: String? = null,

    // === Enlaces externos ===
    val enlaceGenius: String? = null,
    val enlaceYoutube: String? = null,
    val enlaceGoogle: String? = null,

    // === Estados de carga ===
    val cargandoLetra: Boolean = false,
    val cargandoInfo: Boolean = false,
) {
    // ==================== PROPIEDADES DE REPRODUCCIÓN ====================

    /** Progreso visible en la UI (temporal durante scrubbing, real si no) */
    val progresoVisibleMs: Long
        get() {
            val progreso = progresoTemporalMs ?: progresoActualMs
            return progreso.coerceIn(0L, duracionTotalMs.coerceAtLeast(1L))
        }

    /** Progreso en porcentaje [0.0, 1.0] */
    val progresoPorcentaje: Float
        get() {
            val duracion = duracionTotalMs
            return if (duracion > 0) {
                (progresoVisibleMs.toFloat() / duracion).coerceIn(0f, 1f)
            } else 0f
        }

    /** Duración total en milisegundos */
    val duracionTotalMs: Long
        get() = (cancionActual?.cancion?.duracionSegundos?.toLong() ?: 0L) * 1000L

    /** Tiempo restante en milisegundos */
    val tiempoRestanteMs: Long
        get() = (duracionTotalMs - progresoVisibleMs).coerceAtLeast(0L)

    /** Verifica si hay una canción cargada */
    val tieneCancion: Boolean
        get() = cancionActual != null

    /** Verifica si el reproductor puede reproducir */
    val puedeReproducir: Boolean
        get() = cancionActual != null && cancionActual.cancion.archivoPath != null

    // ==================== PROPIEDADES DE DISPLAY ====================

    /** Nombre del artista formateado */
    val artistaDisplay: String
        get() = cancionActual?.artistaNombre ?: "Artista Desconocido"

    /** Título de la canción formateado */
    val tituloDisplay: String
        get() = cancionActual?.cancion?.titulo ?: "Sin título"

    /** Información del álbum formateada (nombre • año) */
    val albumDisplay: String
        get() =
            buildString {
                    cancionActual?.albumNombre?.let { append(it) }
                    cancionActual?.fechaLanzamiento?.let { fecha ->
                        if (isNotEmpty()) append(" • ")
                        append(fecha.take(4))
                    }
                }
                .ifEmpty { "Álbum desconocido" }

    /** Género de la canción */
    val generoDisplay: String
        get() = cancionActual?.generoNombre ?: "Género desconocido"

    // ==================== PROPIEDADES DEL PANEL ====================

    /** Modo efectivo del panel Considera restricciones como: no minimizar si está en scrubbing */
    val modoPanelEfectivo: ModoPanelReproductor
        get() =
            when {
                // Durante scrubbing, mantener al menos NORMAL
                isScrubbing && modoPanel == ModoPanelReproductor.MINIMIZADO ->
                    ModoPanelReproductor.NORMAL
                else -> modoPanel
            }

    /** Altura en dp del panel según el modo efectivo */
    val alturaPanel: Int
        get() = modoPanelEfectivo.peekHeightDp

    /** Verifica si el panel está en proceso de transición (útil para animaciones) */
    val estaEnTransicion: Boolean
        get() = false // Se puede extender con un campo adicional si se necesita tracking

    /** Verifica si hay enlaces externos disponibles */
    val tieneEnlaces: Boolean
        get() =
            !enlaceGenius.isNullOrBlank() ||
                !enlaceYoutube.isNullOrBlank() ||
                !enlaceGoogle.isNullOrBlank()

    /** Verifica si está cargando algún dato */
    val estaCargando: Boolean
        get() = cargandoLetra || cargandoInfo

    // ==================== HELPERS ====================

    fun esPosicionValida(posicionMs: Long): Boolean {
        return posicionMs in 0..duracionTotalMs
    }

    fun actualizarProgreso(nuevoProgreso: Long): PlayerState {
        return copy(progresoActualMs = nuevoProgreso.coerceIn(0L, duracionTotalMs))
    }
}

// ==================== EVENTOS ====================

/** Eventos del reproductor usando sealed interface para type safety */
sealed interface ReproductorEvento {

    // ==================== EVENTOS DE REPRODUCCIÓN ====================

    sealed interface Reproduccion : ReproductorEvento {
        data class EstablecerColaYReproducir(
            val cola: List<SongWithArtist>,
            val cancionInicial: SongWithArtist,
        ) : Reproduccion

        data object ReproducirPausar : Reproduccion

        data object SiguienteCancion : Reproduccion

        data object CancionAnterior : Reproduccion

        data object Detener : Reproduccion
    }

    // ==================== EVENTOS DE NAVEGACIÓN TEMPORAL ====================

    sealed interface Navegacion : ReproductorEvento {
        data class OnScrub(val positionMs: Long) : Navegacion

        data class OnScrubFinished(val positionMs: Long) : Navegacion
    }

    // ==================== EVENTOS DE CONFIGURACIÓN ====================

    sealed interface Configuracion : ReproductorEvento {
        data object CambiarModoReproduccion : Configuracion

        data object CambiarModoRepeticion : Configuracion

        data object AlternarFavorito : Configuracion
    }

    // ==================== EVENTOS DEL PANEL ====================

    sealed interface Panel : ReproductorEvento {
        data class CambiarModo(val nuevoModo: ModoPanelReproductor) : Panel

        data object Expandir : Panel

        data object Colapsar : Panel

        data class NotificarScroll(val scrollActivo: Boolean) : Panel

        data class CambiarTab(val tab: TabExpandido) : Panel
    }

    // ==================== EVENTOS DE ENLACES EXTERNOS ====================

    sealed interface Enlaces : ReproductorEvento {
        data object AbrirGenius : Enlaces

        data object AbrirYoutube : Enlaces

        data object AbrirGoogle : Enlaces
    }
}

// ==================== HELPERS Y EXTENSIONES ====================

/** Formatea milisegundos a formato MM:SS */
fun Long.formatearTiempo(): String {
    val minutos = this / 60000
    val segundos = (this % 60000) / 1000
    return "%02d:%02d".format(minutos, segundos)
}

/** Formatea milisegundos a formato HH:MM:SS si es necesario */
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
