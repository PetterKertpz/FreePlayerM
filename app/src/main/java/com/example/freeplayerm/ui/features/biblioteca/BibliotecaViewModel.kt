// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/BibliotecaViewModel.kt
package com.example.freeplayerm.ui.features.biblioteca

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.AlbumEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.ArtistaEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.CancionEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.GeneroEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.ListaReproduccionEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Define los diferentes "cuerpos" o secciones que puede mostrar la pantalla Biblioteca.
 * Cada uno representa una vista diferente (lista de canciones, cuadrícula de álbumes, etc.).
 */
enum class TipoDeCuerpoBiblioteca {
    CANCIONES,
    ALBUMES,
    ARTISTAS,
    LISTAS,
    GENEROS,
    FAVORITOS,
    CANCIONES_DE_ALBUM,
    CANCIONES_DE_LISTA,
    CANCIONES_DE_GENERO
}
enum class TipoDeFiltro(val etiqueta: String) {
    NINGUNO("Sin filtro"),
    POR_TITULO("Título"),
    POR_ARTISTA("Artista"),
    MAS_RECIENTE("Más reciente")
}
/**
 * El estado completo de la UI para la pantalla Biblioteca.
 * Ahora contiene el cuerpo actual que se debe mostrar y las listas de datos para cada tipo.
 */
data class BibliotecaEstado(
    val usuarioActual: UsuarioEntity? = null,
    val cuerpoActual: TipoDeCuerpoBiblioteca = TipoDeCuerpoBiblioteca.CANCIONES,
    val canciones: List<CancionEntity> = emptyList(),
    val albumes: List<AlbumEntity> = emptyList(),
    val artistas: List<ArtistaEntity> = emptyList(),
    val generos: List<GeneroEntity> = emptyList(),
    val listas: List<ListaReproduccionEntity> = emptyList(),
    val tituloDelCuerpo: String = "Canciones",
    val textoDeBusqueda: String = "", // <-- NUEVO
    val filtroActual: TipoDeFiltro = TipoDeFiltro.NINGUNO // <-- NUEVO
)

// --- NUEVO: Eventos para la búsqueda y el filtro ---
sealed class BibliotecaEvento {
    data class CambiarCuerpo(val nuevoCuerpo: TipoDeCuerpoBiblioteca) : BibliotecaEvento()
    data class TextoDeBusquedaCambiado(val texto: String) : BibliotecaEvento()
    data class FiltroCambiado(val filtro: TipoDeFiltro) : BibliotecaEvento()
}

@HiltViewModel
class BibliotecaViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val cancionDao: CancionDao // Inyectamos el DAO para acceder a las canciones
    // TODO: Inyectar aquí los DAOs/Repositorios para Álbumes, Artistas, etc.
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(BibliotecaEstado())
    val estadoUi = _estadoUi.asStateFlow()

    // Flow que contiene el texto de búsqueda actual
    private val textoDeBusqueda = MutableStateFlow("")

    init {
        // Al iniciar, cargamos la vista por defecto (Canciones)
        cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES)
        observarCambiosEnCanciones()
    }

    /**
     * Función pública para manejar todos los eventos de la UI.
     */
    fun enEvento(evento: BibliotecaEvento) {
        when (evento) {
            is BibliotecaEvento.CambiarCuerpo -> cambiarCuerpo(evento.nuevoCuerpo)
            is BibliotecaEvento.TextoDeBusquedaCambiado -> {
                textoDeBusqueda.value = evento.texto
                _estadoUi.update { it.copy(textoDeBusqueda = evento.texto) }
            }
            is BibliotecaEvento.FiltroCambiado -> {
                _estadoUi.update { it.copy(filtroActual = evento.filtro) }
                // Aquí podrías relanzar la observación si el filtro requiere una nueva consulta a la BD
            }
        }
    }

    private fun observarCambiosEnCanciones() {
        viewModelScope.launch {
            // --- LÓGICA DE FILTRADO EN TIEMPO REAL ---
            // Usamos 'combine' para que cada vez que la lista original de canciones O el texto de búsqueda cambien,
            // se ejecute este bloque y se genere una nueva lista filtrada.
            cancionDao.obtenerTodasLasCanciones()
                .combine(textoDeBusqueda) { canciones, busqueda ->
                    if (busqueda.isBlank()) {
                        canciones // Si no hay búsqueda, devuelve la lista completa
                    } else {
                        // Filtra la lista en memoria (muy rápido para listas de tamaño razonable)
                        canciones.filter {
                            it.titulo.contains(busqueda, ignoreCase = true)
                            // TODO: Añadir aquí la búsqueda por artista si se desea
                        }
                    }
                }.collect { cancionesFiltradas ->
                    _estadoUi.update { it.copy(canciones = cancionesFiltradas) }
                }
        }
    }

    /**
     * Función pública que la UI llamará para cambiar la sección visible.
     * @param nuevoCuerpo El tipo de cuerpo que se desea mostrar.
     */
    fun cambiarCuerpo(nuevoCuerpo: TipoDeCuerpoBiblioteca) {
        viewModelScope.launch {
            _estadoUi.update { it.copy(cuerpoActual = nuevoCuerpo) }

            // Lógica para cargar los datos correspondientes al nuevo cuerpo
            when (nuevoCuerpo) {
                TipoDeCuerpoBiblioteca.CANCIONES -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Canciones") }
                    cancionDao.obtenerTodasLasCanciones().collectLatest { lista ->
                        _estadoUi.update { it.copy(canciones = lista) }
                    }
                }
                TipoDeCuerpoBiblioteca.ALBUMES -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Álbumes") }
                    // TODO: Cargar lista de álbumes desde el repositorio
                }
                TipoDeCuerpoBiblioteca.ARTISTAS -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Artistas") }
                    // TODO: Cargar lista de artistas desde el repositorio
                }
                TipoDeCuerpoBiblioteca.GENEROS -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Géneros") }
                    // TODO: Cargar lista de géneros desde el repositorio
                }
                // ... añadir casos para las otras secciones (Listas, Favoritos, etc.)
                else -> {
                    // Por defecto, no hacer nada o cargar la lista de canciones
                }
            }
        }
    }

    fun cargarDatosDeUsuario(usuarioId: Int) {
        if (_estadoUi.value.usuarioActual?.id == usuarioId) return

        viewModelScope.launch {
            if (usuarioId != -1) {
                val usuario = usuarioRepository.obtenerUsuarioPorId(usuarioId)
                _estadoUi.update { it.copy(usuarioActual = usuario) }
            }
        }
    }
}