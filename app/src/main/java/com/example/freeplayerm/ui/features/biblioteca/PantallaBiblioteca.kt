// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/PantallaBiblioteca.kt
package com.example.freeplayerm.ui.features.biblioteca

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.PlaylistRemove
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.AlbumEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ArtistaEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.ListaReproduccionEntity
import com.example.freeplayerm.com.example.freeplayerm.ui.features.biblioteca.components.DialogoCrearLista
import com.example.freeplayerm.com.example.freeplayerm.ui.features.biblioteca.components.VentanaListasReproduccion
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.components.BarraDeAccionSeleccion
import com.example.freeplayerm.ui.features.biblioteca.components.BarraDeBusquedaYFiltros
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoAlbumes
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoArtistas
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoCanciones
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoGeneros
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoListas
import com.example.freeplayerm.ui.features.biblioteca.components.EncabezadoFijoLista
import com.example.freeplayerm.ui.features.biblioteca.components.SeccionEncabezado
import com.example.freeplayerm.ui.features.reproductor.PanelReproductorMinimizado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEstado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.features.reproductor.ReproductorViewModel
import com.example.freeplayerm.ui.features.shared.IconoCorazonAnimado
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.Date

/**
 * =================================================================
 * 1. El "Composable Inteligente"
 * =================================================================
 */
@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Biblioteca(
    usuarioId: Int,
    bibliotecaViewModel: BibliotecaViewModel = hiltViewModel(),
    reproductorViewModel: ReproductorViewModel

) {
    val permisoRequerido = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val estadoPermiso = rememberPermissionState(permission = permisoRequerido)

    if (estadoPermiso.status.isGranted) {
        val estadoBiblioteca by bibliotecaViewModel.estadoUi.collectAsStateWithLifecycle()
        val estadoReproductor by reproductorViewModel.estadoUi.collectAsStateWithLifecycle()

        val cuerpoActual = estadoBiblioteca.cuerpoActual
        val lazyListState = if (cuerpoActual == TipoDeCuerpoBiblioteca.CANCIONES) {
            rememberLazyListState()
        } else {
            // Para los demás, recordamos el estado de LISTA.
            remember(cuerpoActual) {
                bibliotecaViewModel.listScrollStates.getOrPut(cuerpoActual) {
                    LazyListState(0, 0)
                }
            }
        }
        // Hacemos lo mismo para los estados de CUADRÍCULA.
        val lazyGridState = remember(cuerpoActual) {
            bibliotecaViewModel.gridScrollStates.getOrPut(cuerpoActual) {
                LazyGridState(0, 0)
            }
        }

        LaunchedEffect(key1 = true) {
            bibliotecaViewModel.enEvento(BibliotecaEvento.PermisoConcedido)
            bibliotecaViewModel.enEvento(BibliotecaEvento.CambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES))
        }

        LaunchedEffect(usuarioId) {
            bibliotecaViewModel.cargarDatosDeUsuario(usuarioId)
        }

        CuerpoBiblioteca(
            estadoBiblioteca = estadoBiblioteca,
            estadoReproductor = estadoReproductor,
            lazyListState = lazyListState,
            lazyGridState = lazyGridState,
            onBibliotecaEvento = bibliotecaViewModel::enEvento,
            onReproductorEvento = reproductorViewModel::enEvento,
            onAlbumClick = { album ->
                bibliotecaViewModel.enEvento(BibliotecaEvento.AlbumSeleccionado(album))
            },
            onArtistaClick = { artista ->
                bibliotecaViewModel.enEvento(BibliotecaEvento.ArtistaSeleccionado(artista))
            },
            onListaClick = { lista ->
                bibliotecaViewModel.enEvento(BibliotecaEvento.ListaSeleccionada(lista))
            },
            onGeneroClick = { genero ->
                bibliotecaViewModel.enEvento(BibliotecaEvento.GeneroSeleccionado(genero))
            }

        )
    } else {
        PantallaSolicitudPermiso(estadoPermiso = estadoPermiso)
    }
}

/**
 * =================================================================
 * 2. El "Composable Tonto"
 * =================================================================
 */
@Composable
fun CuerpoBiblioteca(
    estadoBiblioteca: BibliotecaEstado,
    estadoReproductor: ReproductorEstado,
    lazyListState: LazyListState,
    lazyGridState: LazyGridState,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
    onReproductorEvento: (ReproductorEvento) -> Unit,
    onAlbumClick: (AlbumEntity) -> Unit,
    onArtistaClick: (ArtistaEntity) -> Unit,
    onGeneroClick: (GeneroEntity) -> Unit,
    onListaClick: (ListaReproduccionEntity) -> Unit
) {
    if (estadoBiblioteca.mostrarDialogoPlaylist) {
        VentanaListasReproduccion(
            listasExistentes = estadoBiblioteca.listas,
            onDismiss = { onBibliotecaEvento(BibliotecaEvento.CerrarDialogoPlaylist) },
            onCrearLista = { nombre, descripcion, portadaUri ->
                if (estadoBiblioteca.esModoSeleccion) {
                    onBibliotecaEvento(BibliotecaEvento.CrearListaYAnadirCancionesSeleccionadas(nombre, descripcion, portadaUri))
                } else {
                    onBibliotecaEvento(BibliotecaEvento.CrearNuevaListaYAnadirCancion(nombre, descripcion, portadaUri))
                }
            },
            onAnadirAListas = { ids ->
                if (estadoBiblioteca.esModoSeleccion) {
                    onBibliotecaEvento(BibliotecaEvento.AnadirCancionesSeleccionadasAListas(ids))
                } else {
                    onBibliotecaEvento(BibliotecaEvento.AnadirCancionAListasExistentes(ids))
                }
            }
        )
    }

    if (estadoBiblioteca.mostrandoDialogoEditarLista) {
        DialogoCrearLista(
            listaAEditar = estadoBiblioteca.listaActual,
            onDismiss = { onBibliotecaEvento(BibliotecaEvento.CerrarDialogoEditarLista) },
            onCrear = { nombre, descripcion, portadaUri ->
                onBibliotecaEvento(BibliotecaEvento.GuardarCambiosLista(nombre, descripcion, portadaUri))
            }
        )
    }

    Scaffold(
        topBar = {
            SeccionEncabezado(
                usuario = estadoBiblioteca.usuarioActual,
                cuerpoActual = estadoBiblioteca.cuerpoActual,
                onMenuClick = { nuevoCuerpo ->
                    onBibliotecaEvento(BibliotecaEvento.CambiarCuerpo(nuevoCuerpo))
                },
                escaneoManualEnProgreso = estadoBiblioteca.escaneoManualEnProgreso,
                onReescanearClick = {
                    onBibliotecaEvento(BibliotecaEvento.ForzarReescaneo)
                }
            )
        },
        floatingActionButton = {
            val mostrarFab = estadoBiblioteca.esModoSeleccion && estadoBiblioteca.cancionesSeleccionadas.isNotEmpty()

            AnimatedVisibility(visible = mostrarFab) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Decidimos qué botones mostrar según la vista actual
                    when (estadoBiblioteca.cuerpoActual) {

                        TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA -> {
                            // --- Botones para la vista DENTRO de una lista ---

                            // FAB para Añadir a Favoritos
                            FloatingActionButton(
                                onClick = { onBibliotecaEvento(BibliotecaEvento.AnadirSeleccionAFavoritos) },
                                modifier = Modifier.border(BorderStroke(1.dp, AppColors.Negro), CircleShape),
                                shape = CircleShape,
                                containerColor = AppColors.PurpuraProfundo,
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Añadir selección a favoritos")
                            }
                            // FAB para Quitar de la Lista
                            FloatingActionButton(
                                onClick = { onBibliotecaEvento(BibliotecaEvento.QuitarCancionesSeleccionadasDeLista) },
                                modifier = Modifier.border(BorderStroke(1.dp, AppColors.Negro), CircleShape),
                                shape = CircleShape,
                                containerColor = AppColors.PurpuraProfundo,
                            ) {
                                Icon(Icons.Default.PlaylistRemove, contentDescription = "Quitar selección de la lista")
                            }

                        }
                        TipoDeCuerpoBiblioteca.FAVORITOS -> {
                            FloatingActionButton(
                                onClick = { onBibliotecaEvento(BibliotecaEvento.QuitarSeleccionDeFavoritos) },
                                // --- ✅ ESTILO APLICADO ---
                                modifier = Modifier.border(BorderStroke(1.dp, AppColors.Negro), CircleShape),
                                shape = CircleShape,
                                containerColor = AppColors.PurpuraProfundo,

                            ) {
                                Icon(Icons.Default.HeartBroken, contentDescription = "Quitar selección de favoritos")
                            }
                        }

                        else -> {
                            // --- Botones para TODAS las demás vistas de canciones ---

                            // FAB para Añadir a Favoritos
                            FloatingActionButton(
                                onClick = { onBibliotecaEvento(BibliotecaEvento.AnadirSeleccionAFavoritos) },
                                modifier = Modifier.border(BorderStroke(1.dp, AppColors.Negro), CircleShape),
                                shape = CircleShape,
                                containerColor = AppColors.PurpuraProfundo,
                            ) {
                                IconoCorazonAnimado(
                                    esFavorito = false
                                )
                            }

                            // FAB para Añadir a otra Lista de Reproducción
                            FloatingActionButton(
                                onClick = { onBibliotecaEvento(BibliotecaEvento.AbrirDialogoAnadirSeleccionALista) },
                                modifier = Modifier.border(BorderStroke(1.dp, AppColors.Negro), CircleShape),
                                shape = CircleShape,
                                containerColor = AppColors.PurpuraProfundo,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Añadir selección a otra lista")
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (estadoReproductor.cancionActual != null) {
                PanelReproductorMinimizado(
                    estado = estadoReproductor,
                    enEvento = onReproductorEvento
                )
            }
        }
    ) { paddingInterno ->
        Column(modifier = Modifier.padding(paddingInterno).fillMaxSize()) {

            val mostrarBarraDeBusqueda = when (estadoBiblioteca.cuerpoActual) {
                TipoDeCuerpoBiblioteca.CANCIONES,
                TipoDeCuerpoBiblioteca.LISTAS,
                TipoDeCuerpoBiblioteca.ALBUMES,
                TipoDeCuerpoBiblioteca.ARTISTAS,
                TipoDeCuerpoBiblioteca.GENEROS,
                TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA,
                TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM,
                TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA,
                TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO,
                TipoDeCuerpoBiblioteca.FAVORITOS -> true
            }
            if (mostrarBarraDeBusqueda) {
                BarraDeBusquedaYFiltros(
                    textoDeBusqueda = estadoBiblioteca.textoDeBusqueda,
                    criterioDeOrdenamiento = estadoBiblioteca.criterioDeOrdenamiento,
                    direccionDeOrdenamiento = estadoBiblioteca.direccionDeOrdenamiento,
                    enEvento = onBibliotecaEvento
                )
            }

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                val hayContenido = estadoBiblioteca.canciones.isNotEmpty() ||
                        estadoBiblioteca.albumes.isNotEmpty() ||
                        estadoBiblioteca.artistas.isNotEmpty() ||
                        estadoBiblioteca.generos.isNotEmpty() ||
                        estadoBiblioteca.listas.isNotEmpty()

                when {
                    estadoBiblioteca.estaEscaneando -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    estadoBiblioteca.errorDeEscaneo != null -> {
                        TextoDeEstadoVacio(
                            modifier = Modifier.align(Alignment.Center),
                            mensaje = "Ocurrió un error:\n${estadoBiblioteca.errorDeEscaneo}"
                        )
                    }
                    !hayContenido -> {
                        TextoDeEstadoVacio(
                            modifier = Modifier.align(Alignment.Center),
                            mensaje = "Tu biblioteca está vacía.\nPulsa el botón de refrescar para buscar música."
                        )
                    }
                    else -> {
                        when (estadoBiblioteca.cuerpoActual) {

                            TipoDeCuerpoBiblioteca.CANCIONES,
                            TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM,
                            TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA,
                            TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO,
                            TipoDeCuerpoBiblioteca.FAVORITOS -> {
                                Column(Modifier.fillMaxSize()) {
                                    AnimatedVisibility(visible = estadoBiblioteca.esModoSeleccion) {
                                        BarraDeAccionSeleccion(
                                            cancionesSeleccionadas = estadoBiblioteca.cancionesSeleccionadas.size,
                                            totalCanciones = estadoBiblioteca.canciones.size,
                                            onSeleccionarTodo = { onBibliotecaEvento(BibliotecaEvento.SeleccionarTodo) },
                                            onCerrarModoSeleccion = { onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccion) }
                                        )
                                    }
                                    CuerpoCanciones(
                                        canciones = estadoBiblioteca.canciones,
                                        estado = estadoBiblioteca,
                                        lazyListState = lazyListState,
                                        onBibliotecaEvento = onBibliotecaEvento,
                                        onReproductorEvento = { evento ->
                                            if (evento is ReproductorEvento.SeleccionarCancion) {
                                                val eventoConCola =
                                                    ReproductorEvento.EstablecerColaYReproducir(
                                                        cola = estadoBiblioteca.canciones,
                                                        cancionInicial = evento.cancion
                                                    )
                                                onReproductorEvento(eventoConCola)
                                            } else {
                                                onReproductorEvento(evento)
                                            }
                                        },
                                    )
                                }
                            }

                            TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA -> {
                                Column(Modifier.fillMaxSize()) {
                                    EncabezadoFijoLista(
                                        lista = estadoBiblioteca.listaActual,
                                        onVolverClick = { onBibliotecaEvento(BibliotecaEvento.VolverAListas) },
                                        onEliminarListaClick = { onBibliotecaEvento(BibliotecaEvento.EliminarListaDeReproduccionActual) },
                                        onEditarListaClick = { onBibliotecaEvento(BibliotecaEvento.AbrirDialogoEditarLista) }
                                    )
                                    AnimatedVisibility(visible = estadoBiblioteca.esModoSeleccion) {
                                        BarraDeAccionSeleccion(
                                            cancionesSeleccionadas = estadoBiblioteca.cancionesSeleccionadas.size,
                                            totalCanciones = estadoBiblioteca.canciones.size,
                                            onSeleccionarTodo = { onBibliotecaEvento(BibliotecaEvento.SeleccionarTodo) },
                                            onCerrarModoSeleccion = { onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccion) }
                                        )
                                    }
                                    CuerpoCanciones(
                                        estado = estadoBiblioteca,
                                        lazyListState = lazyListState,
                                        onBibliotecaEvento = onBibliotecaEvento,
                                        onReproductorEvento = { evento ->
                                            if (evento is ReproductorEvento.SeleccionarCancion) {
                                                val eventoConCola =
                                                    ReproductorEvento.EstablecerColaYReproducir(
                                                        cola = estadoBiblioteca.canciones,
                                                        cancionInicial = evento.cancion
                                                    )
                                                onReproductorEvento(eventoConCola)
                                            } else {
                                                onReproductorEvento(evento)
                                            }
                                        },
                                        canciones = estadoBiblioteca.canciones
                                    )
                                }
                            }

                            TipoDeCuerpoBiblioteca.LISTAS -> CuerpoListas(
                                listas = estadoBiblioteca.listas,
                                lazyListState = lazyListState,
                                onListaClick = onListaClick
                            )
                            TipoDeCuerpoBiblioteca.ALBUMES -> CuerpoAlbumes(
                                albumes = estadoBiblioteca.albumes,
                                lazyGridState = lazyGridState,
                                onAlbumClick = onAlbumClick
                            )
                            TipoDeCuerpoBiblioteca.ARTISTAS -> CuerpoArtistas(
                                artistas = estadoBiblioteca.artistas,
                                lazyGridState = lazyGridState,
                                onArtistaClick = onArtistaClick
                            )

                            TipoDeCuerpoBiblioteca.GENEROS -> CuerpoGeneros(
                                generos = estadoBiblioteca.generos,
                                lazyGridState = lazyGridState,
                                onGeneroClick = onGeneroClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextoDeEstadoVacio(
    modifier: Modifier = Modifier,
    mensaje: String
) {
    Text(
        text = mensaje,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(32.dp) // Un padding generoso para que no se pegue a los bordes
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PantallaSolicitudPermiso(estadoPermiso: PermissionState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permiso Requerido",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Para poder escanear y mostrar tu música local, FreePlayer necesita permiso para acceder a tus archivos de audio.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { estadoPermiso.launchPermissionRequest() }) {
            Text("Conceder Permiso")
        }
    }
}

/**
 * =================================================================
 * 3. Previsualización
 * =================================================================
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewBiblioteca() {
    // Datos de prueba para el usuario
    val usuarioDePrueba =
        UsuarioEntity(1, "Preview User", "a@a.com", "", Date(), "", "LOCAL")

    // --- PASO 1: Crear una CancionEntity de prueba ---
    // Esta es la base de datos de la canción, con sus IDs y propiedades.
    val cancionEntityDePrueba = CancionEntity(
        idCancion = 1,
        idArtista = 1,
        idAlbum = 1,
        idGenero = 1,
        titulo = "El Sol no Regresa",
        duracionSegundos = 227,
        origen = "LOCAL",
        archivoPath = ""
    )

    // --- PASO 2: Construir el CancionConArtista usando la entidad anterior ---
    // Este es el objeto "enriquecido" que usa la UI, con los nombres ya resueltos.
    val cancionConArtistaDePrueba =
        CancionConArtista(
            cancion = cancionEntityDePrueba,
            artistaNombre = "La Quinta Estación", // Reemplaza TODO()
            albumNombre = "Flores de Alquiler", // Reemplaza TODO()
            generoNombre = "Pop Rock",
            esFavorita = false,
            portadaPath = null            // Reemplaza TODO()
        )

    // Datos de prueba para la lista de álbumes
    val albumesDePrueba = listOf(
        AlbumEntity(1, 1, "Viaje de Lujo", 2023, ""),
        AlbumEntity(2, 2, "Noches de Verano", 2022, "")
    )

    FreePlayerMTheme {
        CuerpoBiblioteca(
            estadoBiblioteca = BibliotecaEstado(
                usuarioActual = usuarioDePrueba,
                cuerpoActual = TipoDeCuerpoBiblioteca.ALBUMES,
                albumes = albumesDePrueba
            ),
            // --- PASO 3: Usar el objeto CancionConArtista en el estado del reproductor ---
            estadoReproductor = ReproductorEstado(
                cancionActual = cancionConArtistaDePrueba,
                estaReproduciendo = true
            ),
            onBibliotecaEvento = {},
            onReproductorEvento = {},
            onAlbumClick = {},
            onArtistaClick = {},
            onGeneroClick = {},
            onListaClick = {},
            lazyListState = LazyListState(),
            lazyGridState = LazyGridState()
        )
    }
}
