// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/PantallaBiblioteca.kt
package com.example.freeplayerm.ui.features.biblioteca

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.freeplayerm.com.example.freeplayerm.ui.features.biblioteca.components.VentanaListasReproduccion
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.biblioteca.components.BarraDeBusquedaYFiltros
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoAlbumes
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoArtistas
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoCanciones
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoGeneros
import com.example.freeplayerm.ui.features.biblioteca.components.CuerpoListas
import com.example.freeplayerm.ui.features.biblioteca.components.EncabezadoSeleccionLista
import com.example.freeplayerm.ui.features.biblioteca.components.SeccionEncabezado
import com.example.freeplayerm.ui.features.reproductor.PanelReproductorMinimizado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEstado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.features.reproductor.ReproductorViewModel
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

    // 2. Crea y recuerda el estado del permiso
    val estadoPermiso = rememberPermissionState(permission = permisoRequerido)

    // 3. Decide qué UI mostrar basado en el estado del permiso
    if (estadoPermiso.status.isGranted) {
        // Si el permiso está concedido, muestra la pantalla principal de la biblioteca
        val estadoBiblioteca by bibliotecaViewModel.estadoUi.collectAsStateWithLifecycle()
        val estadoReproductor by reproductorViewModel.estadoUi.collectAsStateWithLifecycle()

        // En el Composable 'Biblioteca'
        // Este se ejecuta solo la primera vez que se obtiene el permiso
        LaunchedEffect(key1 = true) {
            // 1. Inicia el escaneo silencioso al entrar.
            bibliotecaViewModel.enEvento(BibliotecaEvento.PermisoConcedido)
            // 2. Pide que se muestre la vista de canciones por defecto.
            bibliotecaViewModel.enEvento(BibliotecaEvento.CambiarCuerpo(TipoDeCuerpoBiblioteca.CANCIONES))
        }
        // Este se ejecuta cuando el usuarioId cambia
        LaunchedEffect(usuarioId) {
            bibliotecaViewModel.cargarDatosDeUsuario(usuarioId)
        }

        CuerpoBiblioteca(
            estadoBiblioteca = estadoBiblioteca,
            estadoReproductor = estadoReproductor,
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
        // Si el permiso no está concedido, muestra una pantalla para solicitarlo
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
            onCrearLista = { nombre, descripcion ->
                // Si estamos en modo selección, usamos el nuevo evento. Si no, el antiguo.
                if (estadoBiblioteca.esModoSeleccion) {
                    onBibliotecaEvento(BibliotecaEvento.CrearListaYAnadirCancionesSeleccionadas(nombre, descripcion))
                } else {
                    onBibliotecaEvento(BibliotecaEvento.CrearNuevaListaYAnadirCancion(nombre, descripcion))
                }
            },
            onAnadirAListas = { ids ->
                // Si estamos en modo selección, usamos el nuevo evento. Si no, el antiguo.
                if (estadoBiblioteca.esModoSeleccion) {
                    onBibliotecaEvento(BibliotecaEvento.AnadirCancionesSeleccionadasAListas(ids))
                } else {
                    onBibliotecaEvento(BibliotecaEvento.AnadirCancionAListasExistentes(ids))
                }
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
            // El FAB solo será visible si estamos en modo selección
            AnimatedVisibility(visible = estadoBiblioteca.esModoSeleccion) {
                FloatingActionButton(
                    onClick = { onBibliotecaEvento(BibliotecaEvento.AbrirDialogoAnadirSeleccionALista) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir selección a lista")
                }
            }
        },
        bottomBar = {
            // Si hay una canción sonando, componemos el panel.
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
                // Las vistas como 'Álbumes' o 'Artistas' por ahora no tendrán buscador.

            }

            if (mostrarBarraDeBusqueda) {
                BarraDeBusquedaYFiltros(
                    textoDeBusqueda = estadoBiblioteca.textoDeBusqueda,
                    criterioDeOrdenamiento = estadoBiblioteca.criterioDeOrdenamiento,
                    direccionDeOrdenamiento = estadoBiblioteca.direccionDeOrdenamiento,
                    enEvento = onBibliotecaEvento
                )
            }
            if (estadoBiblioteca.esModoSeleccion && estadoBiblioteca.cuerpoActual == TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA) {
                EncabezadoSeleccionLista(
                    lista = estadoBiblioteca.listaActual,
                    cancionesSeleccionadas = estadoBiblioteca.cancionesSeleccionadas.size,
                    totalCanciones = estadoBiblioteca.canciones.size,
                    onSeleccionarTodo = { onBibliotecaEvento(BibliotecaEvento.SeleccionarTodo) },
                    onQuitarSeleccion = { onBibliotecaEvento(BibliotecaEvento.QuitarCancionesSeleccionadasDeLista) },
                    onEliminarLista = { onBibliotecaEvento(BibliotecaEvento.EliminarListaDeReproduccionActual) },
                    onCerrarModoSeleccion = { onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccion) }
                )
            }

            val hayContenido =
                estadoBiblioteca.canciones.isNotEmpty() ||
                        estadoBiblioteca.albumes.isNotEmpty() ||
                        estadoBiblioteca.artistas.isNotEmpty() ||
                        estadoBiblioteca.listas.isNotEmpty()

            // Si hay contenido, mostramos el cuerpo correspondiente.
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    // 1. La prioridad más alta es mostrar que está cargando
                    estadoBiblioteca.estaEscaneando -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    // 2. Si hay un error, lo mostramos
                    estadoBiblioteca.errorDeEscaneo != null -> {
                        TextoDeEstadoVacio(
                            modifier = Modifier.align(Alignment.Center),
                            mensaje = "Ocurrió un error:\n${estadoBiblioteca.errorDeEscaneo}"
                        )
                    }
                    // 3. Si no hay contenido (y ya no está cargando), mostramos el mensaje de vacío
                    !hayContenido -> {
                        TextoDeEstadoVacio(
                            modifier = Modifier.align(Alignment.Center),
                            mensaje = "Tu biblioteca está vacía.\nPulsa el botón de refrescar para buscar música."
                        )
                    }
                    // 4. Si ninguna de las anteriores es cierta, significa que hay contenido
                    else -> {
                        when {
                            // 1. PRIORIDAD MÁXIMA: Si está escaneando, siempre mostramos la carga.
                            estadoBiblioteca.estaEscaneando -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }

                            // 2. SEGUNDA PRIORIDAD: Si hay un error, lo mostramos.
                            false -> {
                                TextoDeEstadoVacio(
                                    modifier = Modifier.align(Alignment.Center),
                                    mensaje = "Ocurrió un error:\n${estadoBiblioteca.errorDeEscaneo}"
                                )
                            }

                            // 3. TERCERA PRIORIDAD: Si no hay contenido (y no está cargando ni hay error), mostramos el mensaje de vacío.
                            !hayContenido -> {
                                TextoDeEstadoVacio(
                                    modifier = Modifier.align(Alignment.Center),
                                    mensaje = "Tu biblioteca está vacía.\nPulsa el botón de refrescar para buscar música."
                                )
                            }

                            // 4. SI NADA DE LO ANTERIOR ES CIERTO: Mostramos el contenido principal.
                            else -> {
                                // Aquí dentro colocamos el 'when' simplificado que vimos antes
                                when (estadoBiblioteca.cuerpoActual) {
                                    TipoDeCuerpoBiblioteca.CANCIONES,
                                    TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA,
                                    TipoDeCuerpoBiblioteca.CANCIONES_DE_ALBUM,
                                    TipoDeCuerpoBiblioteca.CANCIONES_DE_ARTISTA,
                                    TipoDeCuerpoBiblioteca.CANCIONES_DE_GENERO,
                                    TipoDeCuerpoBiblioteca.FAVORITOS -> {
                                        CuerpoCanciones(
                                            estado = estadoBiblioteca,
                                            onBibliotecaEvento = onBibliotecaEvento,
                                            onReproductorEvento = { evento ->
                                                // Comprobamos si el evento es la selección de una canción
                                                if (evento is ReproductorEvento.SeleccionarCancion) {
                                                    // Si lo es, creamos nuestro nuevo evento más completo
                                                    val eventoConCola = ReproductorEvento.EstablecerColaYReproducir(
                                                        cola = estadoBiblioteca.canciones, // Pasamos la lista completa y ordenada
                                                        cancionInicial = evento.cancion
                                                    )
                                                    // Y enviamos este nuevo evento al ViewModel del reproductor
                                                    onReproductorEvento(eventoConCola)
                                                } else {
                                                    // Para otros eventos (play/pause), los pasamos tal cual
                                                    onReproductorEvento(evento)
                                                }
                                            }
                                        )
                                    }

                                    TipoDeCuerpoBiblioteca.LISTAS -> {
                                        CuerpoListas(
                                            listas = estadoBiblioteca.listas,
                                            onListaClick = onListaClick
                                        )
                                    }

                                    TipoDeCuerpoBiblioteca.ALBUMES -> {
                                        CuerpoAlbumes(
                                            albumes = estadoBiblioteca.albumes,
                                            onAlbumClick = onAlbumClick
                                        )
                                    }

                                    TipoDeCuerpoBiblioteca.ARTISTAS -> {
                                        CuerpoArtistas(
                                            artistas = estadoBiblioteca.artistas,
                                            onArtistaClick = onArtistaClick
                                        )
                                    }

                                    TipoDeCuerpoBiblioteca.GENEROS -> {
                                        CuerpoGeneros(
                                            generos = estadoBiblioteca.generos,
                                            onGeneroClick = onGeneroClick
                                        )
                                    }
                                }
                            }
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
            onListaClick = {}
        )
    }
}
