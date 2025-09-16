// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/BibliotecaViewModel.kt
package com.example.freeplayerm.ui.features.biblioteca

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.repository.RepositorioDeMusicaLocal
import com.example.freeplayerm.data.repository.UsuarioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer
import javax.inject.Inject


// --- SIN CAMBIOS EN TUS ENUMS Y DATA CLASSES ---
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
    POR_TITULO("Título"),
    POR_ARTISTA("Artista"),
    POR_ALBUM("Álbum"),
    POR_GENERO("Género"),
    MAS_RECIENTE("Más reciente")
}
enum class DireccionDeOrdenamiento {
    ASCENDENTE,
    DESCENDENTE
}

data class BibliotecaEstado(
    val usuarioActual: UsuarioEntity? = null,
    val cuerpoActual: TipoDeCuerpoBiblioteca = TipoDeCuerpoBiblioteca.CANCIONES,
    val canciones: List<CancionConArtista> = emptyList(),
    val albumes: List<AlbumEntity> = emptyList(),
    val artistas: List<ArtistaEntity> = emptyList(),
    val generos: List<GeneroEntity> = emptyList(),
    val listas: List<ListaReproduccionEntity> = emptyList(),
    val tituloDelCuerpo: String = "Canciones",
    val textoDeBusqueda: String = "",
    val criterioDeOrdenamiento: CriterioDeOrdenamiento = CriterioDeOrdenamiento.NINGUNO,
    val direccionDeOrdenamiento: DireccionDeOrdenamiento = DireccionDeOrdenamiento.ASCENDENTE,
    val estaEscaneando: Boolean = false,
    val escaneoManualEnProgreso: Boolean = false,
    val errorDeEscaneo: String? = null

)
sealed class BibliotecaEvento {
    data object ForzarReescaneo : BibliotecaEvento()
    data object PermisoConcedido : BibliotecaEvento()
    data class CambiarCuerpo(val nuevoCuerpo: TipoDeCuerpoBiblioteca) : BibliotecaEvento()
    data class TextoDeBusquedaCambiado(val texto: String) : BibliotecaEvento()
    data class CriterioDeOrdenamientoCambiado(val criterio: CriterioDeOrdenamiento) : BibliotecaEvento()
    data object DireccionDeOrdenamientoCambiada : BibliotecaEvento()
    data class AlbumSeleccionado(val album: AlbumEntity) : BibliotecaEvento()
    data class ArtistaSeleccionado(val artista: ArtistaEntity) : BibliotecaEvento()
    data class GeneroSeleccionado(val genero: GeneroEntity) : BibliotecaEvento()
    data class ListaSeleccionada(val lista: ListaReproduccionEntity) : BibliotecaEvento()
}


@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BibliotecaViewModel @Inject constructor(
    private val usuarioRepository: UsuarioRepository,
    private val cancionDao: CancionDao,
    private val repositorioDeMusicaLocal: RepositorioDeMusicaLocal
) : ViewModel() {

    private val _estadoUi = MutableStateFlow(BibliotecaEstado())
    val estadoUi = _estadoUi.asStateFlow()

    // --- CAMBIO #1: SE ELIMINA EL 'textoDeBusqueda' PRIVADO ---
    // Ya tenemos el texto de búsqueda en el estado principal (_estadoUi),
    // usar uno privado aquí era redundante. Simplificamos.

    // --- CAMBIO #2: NUEVO STATEFLOW PARA GESTIONAR LA FUENTE DE CANCIONES ---
    // Este StateFlow es la clave de la solución. Almacenará QUÉ consulta de canciones
    // debe estar activa en cada momento (todas las canciones, las de un artista, favoritos, etc.).
    // Lo inicializamos con un flow vacío.
    private val fuenteDeCancionesActiva =
        MutableStateFlow<Flow<List<CancionConArtista>>>(flowOf(emptyList()))

    // Mantenemos este Job para las cargas que no son de canciones (álbumes, artistas, etc.)
    private var jobDeCargaDeEntidades: Job? = null

    init {
        // --- CAMBIO #3: 'init' AHORA SOLO INICIA EL OBSERVADOR ---
        // Ya no pre-cargamos la vista de canciones aquí. Solo nos aseguramos de que el sistema
        // esté listo para reaccionar a los cambios en la fuente de canciones.
        observarYFiltrarCancionesActuales()
        // La UI ahora es responsable de solicitar la primera vista al componerse.
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun enEvento(evento: BibliotecaEvento) {
        when (evento) {
            is BibliotecaEvento.ForzarReescaneo -> {
                // El escaneo forzado es iniciado por el usuario.
                escanearAlmacenamientoLocal(esIniciadoPorUsuario = true)
            }

            is BibliotecaEvento.CambiarCuerpo -> cambiarCuerpo(evento.nuevoCuerpo)
            is BibliotecaEvento.TextoDeBusquedaCambiado -> {
                // Actualizamos el texto de búsqueda directamente en el estado principal.
                _estadoUi.update { it.copy(textoDeBusqueda = evento.texto) }
            }
            is BibliotecaEvento.CriterioDeOrdenamientoCambiado -> {
                _estadoUi.update { it.copy(criterioDeOrdenamiento = evento.criterio) }
            }
            is BibliotecaEvento.DireccionDeOrdenamientoCambiada -> {
                // Esta lógica invierte la dirección actual.
                val nuevaDireccion = if (_estadoUi.value.direccionDeOrdenamiento == DireccionDeOrdenamiento.ASCENDENTE) {
                    DireccionDeOrdenamiento.DESCENDENTE
                } else {
                    DireccionDeOrdenamiento.ASCENDENTE
                }
                _estadoUi.update { it.copy(direccionDeOrdenamiento = nuevaDireccion) }
            }
            is BibliotecaEvento.AlbumSeleccionado -> cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM, album = evento.album)
            is BibliotecaEvento.ArtistaSeleccionado -> cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA, artista = evento.artista)
            is BibliotecaEvento.GeneroSeleccionado -> cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO, genero = evento.genero)
            is BibliotecaEvento.ListaSeleccionada -> cambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA, lista = evento.lista)
            // El escaneo por permiso concedido es automático y debe ser silencioso.
            is BibliotecaEvento.PermisoConcedido -> { escanearAlmacenamientoLocal(esIniciadoPorUsuario = false)


            }

        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun escanearAlmacenamientoLocal(esIniciadoPorUsuario: Boolean) {
        // Evita iniciar un nuevo escaneo manual si ya hay uno en progreso.
        if (esIniciadoPorUsuario && _estadoUi.value.escaneoManualEnProgreso) return

        viewModelScope.launch {
            // Solo mostramos el indicador de carga si el escaneo fue manual.
            if (esIniciadoPorUsuario) {
                _estadoUi.update { it.copy(escaneoManualEnProgreso = true, errorDeEscaneo = null) }
            }

            try {
                // La lógica de escaneo es la misma para ambos casos.
                repositorioDeMusicaLocal.escanearYGuardarMusica()
            } catch (e: Exception) {
                // Mostramos el error en la UI independientemente de cómo se inició.
                _estadoUi.update { it.copy(errorDeEscaneo = "Error al escanear: ${e.message}") }
            } finally {
                // Al finalizar, solo ocultamos el indicador si fue un escaneo manual.
                if (esIniciadoPorUsuario) {
                    _estadoUi.update { it.copy(escaneoManualEnProgreso = false) }
                }
            }
        }
    }

    // --- CAMBIO #4: EL OBSERVADOR AHORA ES DINÁMICO ---
    // Esta función ahora es mucho más potente.
    private fun observarYFiltrarCancionesActuales() {
        viewModelScope.launch {
            val textoDeBusqueda = _estadoUi.map { it.textoDeBusqueda }.distinctUntilChanged()
            // Ahora observamos los dos nuevos estados de ordenamiento.
            val criterioDeOrdenamiento = _estadoUi.map { it.criterioDeOrdenamiento }.distinctUntilChanged()
            val direccionDeOrdenamiento = _estadoUi.map { it.direccionDeOrdenamiento }.distinctUntilChanged()

            fuenteDeCancionesActiva.flatMapLatest { flowDeCanciones ->
                // El 'combine' ahora incluye los nuevos flows.
                combine(flowDeCanciones, textoDeBusqueda, criterioDeOrdenamiento, direccionDeOrdenamiento) { canciones, busqueda, criterio, direccion ->
                    // 1. La Búsqueda (filtro) se mantiene igual.
                    val cancionesBuscadas = if (busqueda.isBlank()) {
                        canciones
                    } else {
                        // 1. Normalizamos y dividimos la búsqueda en palabras (tokens)
                        val busquedaNormalizada = normalizarTexto(busqueda)
                        val tokensDeBusqueda = busquedaNormalizada.split(" ").filter { it.isNotBlank() }

                        canciones.filter { cancionConArtista ->
                            // 2. Normalizamos el texto de la canción y el artista
                            val textoDeCancionNormalizado = normalizarTexto(
                                "${cancionConArtista.cancion.titulo} ${cancionConArtista.artistaNombre}"
                            )
                            // 3. Verificamos que TODOS los tokens de búsqueda estén presentes
                            tokensDeBusqueda.all { token ->
                                textoDeCancionNormalizado.contains(token)
                            }
                        }
                    }

                    // --- CAMBIO #2: CORRECCIÓN DE NULABILIDAD EN ORDENAMIENTO ---
                    val cancionesOrdenadas = when (criterio) {
                        CriterioDeOrdenamiento.NINGUNO -> cancionesBuscadas
                        CriterioDeOrdenamiento.POR_TITULO -> cancionesBuscadas.sortedBy { it.cancion.titulo }
                        CriterioDeOrdenamiento.POR_ARTISTA -> cancionesBuscadas.sortedBy { it.artistaNombre ?: "" }
                        CriterioDeOrdenamiento.POR_ALBUM -> cancionesBuscadas.sortedBy { it.albumNombre ?: "" }
                        CriterioDeOrdenamiento.POR_GENERO -> cancionesBuscadas.sortedBy { it.generoNombre ?: "" }
                        // Aseguramos que idCancion no sea nulo para ordenar
                        CriterioDeOrdenamiento.MAS_RECIENTE -> cancionesBuscadas.sortedByDescending { it.cancion.idCancion }
                    }

                    if (direccion == DireccionDeOrdenamiento.DESCENDENTE) {
                        cancionesOrdenadas.reversed()
                    } else {
                        cancionesOrdenadas
                    }
                }
            }.collectLatest { cancionesFinales ->
                _estadoUi.update { it.copy(canciones = cancionesFinales) }
            }
        }
    }

    private fun normalizarTexto(texto: String): String {
        val textoSinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return textoSinAcentos.lowercase()
    }

    // --- CAMBIO #5: 'cambiarCuerpo' REFACTORIZADO PARA SER MÁS ROBUSTO ---
    private fun cambiarCuerpo(
        nuevoCuerpo: TipoDeCuerpoBiblioteca,
        album: AlbumEntity? = null,
        artista: ArtistaEntity? = null,
        genero: GeneroEntity? = null,
        lista: ListaReproduccionEntity? = null
    ) {
        // Cancelamos cualquier carga de entidades (álbumes, artistas) que estuviera en curso.
        jobDeCargaDeEntidades?.cancel()

        // Actualizamos el estado de la UI en una sola operación atómica.
        _estadoUi.update {
            it.copy(
                cuerpoActual = nuevoCuerpo,
                textoDeBusqueda = "", // Siempre limpiamos la búsqueda
                // Limpiamos TODAS las listas de datos para evitar mostrar datos viejos.
                canciones = emptyList(),
                albumes = emptyList(),
                artistas = emptyList(),
                generos = emptyList(),
                listas = emptyList()
            )
        }

        // Variable para la nueva fuente de canciones.
        var nuevaFuente: Flow<List<CancionConArtista>>? = null

        // Este job se usará solo para vistas que NO son listas de canciones.
        jobDeCargaDeEntidades = viewModelScope.launch {
            when (nuevoCuerpo) {
                // CASOS QUE MUESTRAN OTRAS ENTIDADES (ÁLBUMES, ARTISTAS, ETC.)
                TipoDeCuerpoBiblioteca.ALBUMES -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Álbumes") }
                    cancionDao.obtenerTodosLosAlbumes().collectLatest { _estadoUi.update { s -> s.copy(albumes = it) } }
                }
                TipoDeCuerpoBiblioteca.ARTISTAS -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Artistas") }
                    cancionDao.obtenerTodosLosArtistas().collectLatest { _estadoUi.update { s -> s.copy(artistas = it) } }
                }
                TipoDeCuerpoBiblioteca.GENEROS -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Géneros") }
                    cancionDao.obtenerTodosLosGeneros().collectLatest { _estadoUi.update { s -> s.copy(generos = it) } }
                }
                TipoDeCuerpoBiblioteca.LISTAS -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Mis Listas") }
                    val usuarioId = _estadoUi.value.usuarioActual?.id ?: -1
                    if (usuarioId != -1) {
                        cancionDao.obtenerListasPorUsuarioId(usuarioId).collectLatest { _estadoUi.update { s -> s.copy(listas = it) } }
                    }
                }

                // CASOS QUE MUESTRAN LISTAS DE CANCIONES
                // Aquí, en lugar de colectar el flow, lo ASIGNAMOS a nuestra variable 'nuevaFuente'.
                TipoDeCuerpoBiblioteca.CANCIONES -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Canciones") }
                    nuevaFuente = cancionDao.obtenerCancionesConArtista()
                }
                TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM -> {
                    album?.let {
                        _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.titulo) }
                        nuevaFuente = cancionDao.obtenerCancionesDeAlbumConArtista(it.idAlbum)
                    }
                }
                TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA -> {
                    artista?.let {
                        _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.nombre) }
                        nuevaFuente = cancionDao.obtenerCancionesDeArtistaConArtista(it.idArtista)
                    }
                }
                TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO -> {
                    genero?.let {
                        _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.nombre) }
                        nuevaFuente = cancionDao.obtenerCancionesDeGeneroConArtista(it.idGenero)
                    }
                }
                TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA -> {
                    lista?.let {
                        _estadoUi.update { estado -> estado.copy(tituloDelCuerpo = it.nombre) }
                        nuevaFuente = cancionDao.obtenerCancionesDeListaConArtista(it.idLista)
                    }
                }
                TipoDeCuerpoBiblioteca.FAVORITOS -> {
                    _estadoUi.update { it.copy(tituloDelCuerpo = "Favoritos") }
                    val usuarioId = _estadoUi.value.usuarioActual?.id ?: -1
                    if (usuarioId != -1) {
                        nuevaFuente = cancionDao.obtenerCancionesFavoritasConArtista(usuarioId)
                    }
                }
            }
        }

        // Si se asignó una nueva fuente de canciones, la emitimos a nuestro observador dinámico.
        // Si no (porque la vista era de álbumes, por ej.), emitimos un flow vacío para limpiar la lista.
        fuenteDeCancionesActiva.value = nuevaFuente ?: flowOf(emptyList())
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