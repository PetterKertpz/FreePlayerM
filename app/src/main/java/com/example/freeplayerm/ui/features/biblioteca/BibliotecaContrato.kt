// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/BibliotecaContrato.kt
package com.example.freeplayerm.ui.features.biblioteca

import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista

/**
 * Contrato de la feature Biblioteca.
 * Centraliza tipos de vista, criterios de ordenamiento, estado UI y eventos.
 */

// ═══════════════════════════════════════════════════════════════════════════════
// ENUMS
// ═══════════════════════════════════════════════════════════════════════════════

enum class TipoDeCuerpoBiblioteca {
    CANCIONES,
    LISTAS,
    CANCIONES_DE_LISTA,
    ALBUMES,
    CANCIONES_DE_ALBUM,
    ARTISTAS,
    GENEROS,
    FAVORITOS,
    CANCIONES_DE_GENERO,
    CANCIONES_DE_ARTISTA
}

enum class CriterioDeOrdenamiento(val etiqueta: String) {
    NINGUNO("Por defecto"),
    POR_TITULO("Título (A-Z)"),
    POR_ARTISTA("Artista"),
    POR_ALBUM("Álbum"),
    POR_GENERO("Género"),
    MAS_RECIENTE("Más reciente")
}

enum class DireccionDeOrdenamiento(val etiqueta: String, val icono: String) {
    ASCENDENTE("Ascendente", "↑"),
    DESCENDENTE("Descendente", "↓")
}

// ═══════════════════════════════════════════════════════════════════════════════
// ESTADO UI
// ═══════════════════════════════════════════════════════════════════════════════

data class BibliotecaEstado(
    // Usuario
    val usuarioActual: UsuarioEntity? = null,

    // Navegación
    val cuerpoActual: TipoDeCuerpoBiblioteca = TipoDeCuerpoBiblioteca.CANCIONES,
    val tituloDelCuerpo: String = "Canciones",
    val listaActual: ListaReproduccionEntity? = null,

    // Datos
    val canciones: List<CancionConArtista> = emptyList(),
    val albumes: List<AlbumEntity> = emptyList(),
    val artistas: List<ArtistaEntity> = emptyList(),
    val generos: List<GeneroEntity> = emptyList(),
    val listas: List<ListaReproduccionEntity> = emptyList(),

    // Búsqueda y ordenamiento
    val textoDeBusqueda: String = "",
    val criterioDeOrdenamiento: CriterioDeOrdenamiento = CriterioDeOrdenamiento.POR_TITULO,
    val direccionDeOrdenamiento: DireccionDeOrdenamiento = DireccionDeOrdenamiento.ASCENDENTE,

    // Estado de escaneo
    val estaEscaneando: Boolean = false,
    val escaneoManualEnProgreso: Boolean = false,
    val errorDeEscaneo: String? = null,
    val progresoEscaneo: Pair<Int, Int>? = null,
    val mensajeEscaneo: String? = null,

    // Modo selección
    val esModoSeleccion: Boolean = false,
    val cancionesSeleccionadas: Set<Int> = emptySet(),

    // Diálogos
    val mostrarDialogoPlaylist: Boolean = false,
    val cancionParaAnadirALista: CancionConArtista? = null,
    val mostrandoDialogoEditarLista: Boolean = false
)

// ═══════════════════════════════════════════════════════════════════════════════
// EVENTOS (Acciones del usuario)
// ═══════════════════════════════════════════════════════════════════════════════

sealed interface BibliotecaEvento {

    // ─── Búsqueda y ordenamiento ───
    data object LimpiarBusqueda : BibliotecaEvento
    data class TextoDeBusquedaCambiado(val texto: String) : BibliotecaEvento
    data class CriterioDeOrdenamientoCambiado(val criterio: CriterioDeOrdenamiento) : BibliotecaEvento
    data object DireccionDeOrdenamientoCambiada : BibliotecaEvento

    // ─── Escaneo ───
    data object ForzarReescaneo : BibliotecaEvento
    data object PermisoConcedido : BibliotecaEvento

    // ─── Navegación ───
    data class CambiarCuerpo(val nuevoCuerpo: TipoDeCuerpoBiblioteca) : BibliotecaEvento
    data class AlbumSeleccionado(val album: AlbumEntity) : BibliotecaEvento
    data class ArtistaSeleccionado(val artista: ArtistaEntity) : BibliotecaEvento
    data class GeneroSeleccionado(val genero: GeneroEntity) : BibliotecaEvento
    data class ListaSeleccionada(val lista: ListaReproduccionEntity) : BibliotecaEvento
    data object VolverAListas : BibliotecaEvento

    // ─── Favoritos ───
    data class AlternarFavorito(val cancionConArtista: CancionConArtista) : BibliotecaEvento
    data object AnadirSeleccionAFavoritos : BibliotecaEvento
    data object QuitarSeleccionDeFavoritos : BibliotecaEvento

    // ─── Diálogo Playlist ───
    data class AbrirDialogoPlaylist(val cancion: CancionConArtista) : BibliotecaEvento
    data object CerrarDialogoPlaylist : BibliotecaEvento
    data class CrearNuevaListaYAnadirCancion(
        val nombre: String,
        val descripcion: String?,
        val portadaUri: String?
    ) : BibliotecaEvento
    data class AnadirCancionAListasExistentes(val idListas: List<Int>) : BibliotecaEvento

    // ─── Modo selección ───
    data class ActivarModoSeleccion(val cancion: CancionConArtista) : BibliotecaEvento
    data object DesactivarModoSeleccion : BibliotecaEvento
    data class AlternarSeleccionCancion(val cancionId: Int) : BibliotecaEvento
    data object SeleccionarTodo : BibliotecaEvento
    data object QuitarCancionesSeleccionadasDeLista : BibliotecaEvento
    data object AbrirDialogoAnadirSeleccionALista : BibliotecaEvento
    data class AnadirCancionesSeleccionadasAListas(val idListas: List<Int>) : BibliotecaEvento
    data class CrearListaYAnadirCancionesSeleccionadas(
        val nombre: String,
        val descripcion: String?,
        val portadaUri: String?
    ) : BibliotecaEvento

    // ─── Edición ───
    data class EditarCancion(val cancion: CancionConArtista) : BibliotecaEvento
    data object EliminarListaDeReproduccionActual : BibliotecaEvento
    data object AbrirDialogoEditarLista : BibliotecaEvento
    data object CerrarDialogoEditarLista : BibliotecaEvento
    data class GuardarCambiosLista(
        val nombre: String,
        val descripcion: String?,
        val portadaUri: String?
    ) : BibliotecaEvento
}