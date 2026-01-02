// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/LibraryContract.kt
package com.example.freeplayerm.ui.features.library

import com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.data.local.entity.ArtistEntity
import com.example.freeplayerm.data.local.entity.GenreEntity
import com.example.freeplayerm.data.local.entity.PlaylistEntity
import com.example.freeplayerm.data.local.entity.UserEntity
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist

/**
 * Contrato de la feature Biblioteca. Centraliza tipos de vista, criterios de ordenamiento, estado
 * UI y eventos.
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
    CANCIONES_DE_ARTISTA,
}

enum class CriterioDeOrdenamiento(val etiqueta: String) {
    NINGUNO("Por defecto"),
    POR_TITULO("Título (A-Z)"),
    POR_ARTISTA("Artista"),
    POR_ALBUM("Álbum"),
    POR_GENERO("Género"),
    MAS_RECIENTE("Más reciente"),
}

enum class DireccionDeOrdenamiento(val etiqueta: String, val icono: String) {
    ASCENDENTE("Ascendente", "↑"),
    DESCENDENTE("Descendente", "↓"),
}

enum class NivelZoom(val multiplicador: Float) {
    PEQUENO(0.7f), // 70% del tamaño normal
    NORMAL(1.0f), // Tamaño base (actual)
    GRANDE(1.4f); // 140% del tamaño normal

    fun siguiente(): NivelZoom =
        when (this) {
            PEQUENO -> NORMAL
            NORMAL -> GRANDE
            GRANDE -> GRANDE // Ya está en máximo
        }

    fun anterior(): NivelZoom =
        when (this) {
            GRANDE -> NORMAL
            NORMAL -> PEQUENO
            PEQUENO -> PEQUENO // Ya está en mínimo
        }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ESTADO UI
// ═══════════════════════════════════════════════════════════════════════════════

data class BibliotecaEstado(
    // Usuario
    val usuarioActual: UserEntity? = null,

    // Navegación
    val cuerpoActual: TipoDeCuerpoBiblioteca = TipoDeCuerpoBiblioteca.CANCIONES,
    val tituloDelCuerpo: String = "Canciones",
    val listaActual: PlaylistEntity? = null,

    // Zoom de visualización
    val nivelZoom: NivelZoom = NivelZoom.NORMAL,

    // Datos
    val canciones: List<SongWithArtist> = emptyList(),
    val albumes: List<AlbumEntity> = emptyList(),
    val artistas: List<ArtistEntity> = emptyList(),
    val generos: List<GenreEntity> = emptyList(),
    val listas: List<PlaylistEntity> = emptyList(),

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
    val cancionParaAnadirALista: SongWithArtist? = null,
    val mostrandoDialogoEditarLista: Boolean = false,
)

// ═══════════════════════════════════════════════════════════════════════════════
// EVENTOS (Acciones del usuario)
// ═══════════════════════════════════════════════════════════════════════════════

sealed interface BibliotecaEvento {

    // ─── Zoom de visualización ───
    data class CambiarNivelZoom(val nivel: NivelZoom) : BibliotecaEvento

    data object AumentarZoom : BibliotecaEvento

    data object ReducirZoom : BibliotecaEvento

    // ─── Búsqueda y ordenamiento ───
    data object LimpiarBusqueda : BibliotecaEvento

    data class TextoDeBusquedaCambiado(val texto: String) : BibliotecaEvento

    data class CriterioDeOrdenamientoCambiado(val criterio: CriterioDeOrdenamiento) :
        BibliotecaEvento

    data object DireccionDeOrdenamientoCambiada : BibliotecaEvento

    // ─── Escaneo ───
    data object ForzarReescaneo : BibliotecaEvento

    data object PermisoConcedido : BibliotecaEvento

    // ─── Navegación ───
    data class CambiarCuerpo(val nuevoCuerpo: TipoDeCuerpoBiblioteca) : BibliotecaEvento

    data class AlbumSeleccionado(val album: AlbumEntity) : BibliotecaEvento

    data class ArtistaSeleccionado(val artista: ArtistEntity) : BibliotecaEvento

    data class GeneroSeleccionado(val genero: GenreEntity) : BibliotecaEvento

    data class ListaSeleccionada(val lista: PlaylistEntity) : BibliotecaEvento

    data object VolverAListas : BibliotecaEvento

    // ─── Favoritos ───
    data class AlternarFavorito(val songWithArtist: SongWithArtist) : BibliotecaEvento

    data object AnadirSeleccionAFavoritos : BibliotecaEvento

    data object QuitarSeleccionDeFavoritos : BibliotecaEvento

    // ─── Diálogo Playlist ───
    data class AbrirDialogoPlaylist(val cancion: SongWithArtist) : BibliotecaEvento

    data object CerrarDialogoPlaylist : BibliotecaEvento

    data class CrearNuevaListaYAnadirCancion(
        val nombre: String,
        val descripcion: String?,
        val portadaUri: String?,
    ) : BibliotecaEvento

    data class AnadirCancionAListasExistentes(val idListas: List<Int>) : BibliotecaEvento

    // ─── Modo selección ───
    data class ActivarModoSeleccion(val cancion: SongWithArtist) : BibliotecaEvento

    data object DesactivarModoSeleccion : BibliotecaEvento

    data class AlternarSeleccionCancion(val cancionId: Int) : BibliotecaEvento

    data object SeleccionarTodo : BibliotecaEvento

    data object QuitarCancionesSeleccionadasDeLista : BibliotecaEvento

    data object AbrirDialogoAnadirSeleccionALista : BibliotecaEvento

    data class AnadirCancionesSeleccionadasAListas(val idListas: List<Int>) : BibliotecaEvento

    data class CrearListaYAnadirCancionesSeleccionadas(
        val nombre: String,
        val descripcion: String?,
        val portadaUri: String?,
    ) : BibliotecaEvento

    // ─── Edición ───
    data class EditarCancion(val cancion: SongWithArtist) : BibliotecaEvento

    data object EliminarListaDeReproduccionActual : BibliotecaEvento

    data object AbrirDialogoEditarLista : BibliotecaEvento

    data object CerrarDialogoEditarLista : BibliotecaEvento

    data class GuardarCambiosLista(
        val nombre: String,
        val descripcion: String?,
        val portadaUri: String?,
    ) : BibliotecaEvento
}
