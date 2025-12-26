package com.example.freeplayerm.ui.features.reproductor.components

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.features.reproductor.*
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

/**
 * 🎛️ CONTROLES EXPANDIDOS
 *
 * Controles completos para modo expandido:
 * Shuffle | Anterior | Play/Pause | Siguiente | Repetir
 */
@Composable
fun ControlesExpandidos(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier
) {
    val controles = remember { listOf("shuffle", "prev", "play", "next", "repeat") }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        controles.forEachIndexed { index, control ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(300, delayMillis = 50 * index)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(300, delayMillis = 50 * index))
            ) {
                when (control) {
                    "shuffle" -> BotonShuffle(estado, onEvento)
                    "prev" -> BotonAnterior(onEvento)
                    "play" -> BotonPlayPausePrincipal(estado, onEvento)
                    "next" -> BotonSiguiente(onEvento)
                    "repeat" -> BotonRepetir(estado, onEvento)
                }
            }
        }
    }
}

/**
 * 🎛️ CONTROLES NORMALES (compactos)
 *
 * Controles reducidos para modo normal:
 * Play/Pause | Siguiente
 */
@Composable
fun ControlesNormales(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Play/Pause
        IconButton(
            onClick = { onEvento(ReproductorEvento.Reproduccion.ReproducirPausar) },
            modifier = Modifier
                .size(48.dp)
                .background(AppColors.AcentoRosa.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(
                imageVector = IconosReproductor.obtenerIconoPlayPause(estado.estaReproduciendo),
                contentDescription = if (estado.estaReproduciendo) "Pausar" else "Reproducir",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Botón Siguiente
        IconButton(
            onClick = { onEvento(ReproductorEvento.Reproduccion.SiguienteCancion) }
        ) {
            Icon(
                imageVector = IconosReproductor.Siguiente,
                contentDescription = "Siguiente",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ==================== BOTONES INDIVIDUALES ====================

@Composable
fun BotonShuffle(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit
) {
    val activo = estado.modoReproduccion == ModoReproduccion.ALEATORIO

    IconButton(
        onClick = { onEvento(ReproductorEvento.Configuracion.CambiarModoReproduccion) }
    ) {
        Icon(
            imageVector = IconosReproductor.Aleatorio,
            contentDescription = if (activo) "Desactivar aleatorio" else "Activar aleatorio",
            tint = if (activo) AppColors.AcentoRosa else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun BotonAnterior(onEvento: (ReproductorEvento) -> Unit) {
    IconButton(
        onClick = { onEvento(ReproductorEvento.Reproduccion.CancionAnterior) }
    ) {
        Icon(
            imageVector = IconosReproductor.Anterior,
            contentDescription = "Canción anterior",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun BotonPlayPausePrincipal(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(AppColors.AcentoRosa, Color(0xFFAA00FF))
                )
            )
            .clickable { onEvento(ReproductorEvento.Reproduccion.ReproducirPausar) },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconosReproductor.obtenerIconoPlayPause(estado.estaReproduciendo),
            contentDescription = if (estado.estaReproduciendo) "Pausar" else "Reproducir",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
fun BotonSiguiente(onEvento: (ReproductorEvento) -> Unit) {
    IconButton(
        onClick = { onEvento(ReproductorEvento.Reproduccion.SiguienteCancion) }
    ) {
        Icon(
            imageVector = IconosReproductor.Siguiente,
            contentDescription = "Siguiente canción",
            tint = Color.White,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun BotonRepetir(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit
) {
    val activo = estado.modoRepeticion != ModoRepeticion.NO_REPETIR

    val descripcion = when (estado.modoRepeticion) {
        ModoRepeticion.NO_REPETIR -> "Activar repetición"
        ModoRepeticion.REPETIR_LISTA -> "Repetir lista activado"
        ModoRepeticion.REPETIR_CANCION -> "Repetir canción activado"
    }

    val icono = when (estado.modoRepeticion) {
        ModoRepeticion.REPETIR_CANCION -> IconosReproductor.RepetirCancion
        else -> IconosReproductor.RepetirLista
    }

    IconButton(
        onClick = { onEvento(ReproductorEvento.Configuracion.CambiarModoRepeticion) }
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = if (activo) AppColors.AcentoRosa else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun BotonFavorito(
    esFavorita: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onToggle,
        modifier = modifier
    ) {
        Icon(
            imageVector = IconosReproductor.obtenerIconoFavorito(esFavorita),
            contentDescription = if (esFavorita) "Quitar de favoritos" else "Agregar a favoritos",
            tint = if (esFavorita) Color.Red else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp)
        )
    }
}
// ==================== PREVIEWS ====================

// Helper para crear estado de demo
private fun crearEstadoDemo(
    estaReproduciendo: Boolean = true,
    modoReproduccion: ModoReproduccion = ModoReproduccion.EN_ORDEN,
    modoRepeticion: ModoRepeticion = ModoRepeticion.NO_REPETIR,
    esFavorita: Boolean = false
): ReproductorEstado {
    val cancionDemo = CancionConArtista(
        cancion = CancionEntity(
            idCancion = 1,
            titulo = "Bohemian Rhapsody",
            idArtista = 1,
            idAlbum = 1,
            idGenero = 1,
            duracionSegundos = 355,
            origen = "LOCAL",
            archivoPath = "/music/queen/bohemian.mp3"
        ),
        artistaNombre = "Queen",
        albumNombre = "A Night at the Opera",
        generoNombre = "Rock",
        esFavorita = esFavorita,
        portadaPath = null,
        fechaLanzamiento = "1975"
    )

    return ReproductorEstado(
        cancionActual = cancionDemo,
        estaReproduciendo = estaReproduciendo,
        progresoActualMs = 125000L,
        modoReproduccion = modoReproduccion,
        modoRepeticion = modoRepeticion,
        esFavorita = esFavorita
    )
}

@Composable
private fun PreviewContainer(
    content: @Composable () -> Unit
) {
    FreePlayerMTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E0B24))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

// ==================== CONTROLES EXPANDIDOS PREVIEWS ====================

@Preview(name = "Controles Expandidos - Reproduciendo", showBackground = true)
@Composable
private fun PreviewControlesExpandidosReproduciendo() {
    PreviewContainer {
        ControlesExpandidos(
            estado = crearEstadoDemo(estaReproduciendo = true),
            onEvento = {}
        )
    }
}

@Preview(name = "Controles Expandidos - Pausado", showBackground = true)
@Composable
private fun PreviewControlesExpandidosPausado() {
    PreviewContainer {
        ControlesExpandidos(
            estado = crearEstadoDemo(estaReproduciendo = false),
            onEvento = {}
        )
    }
}

@Preview(name = "Controles Expandidos - Aleatorio Activo", showBackground = true)
@Composable
private fun PreviewControlesExpandidosAleatorio() {
    PreviewContainer {
        ControlesExpandidos(
            estado = crearEstadoDemo(
                estaReproduciendo = true,
                modoReproduccion = ModoReproduccion.ALEATORIO
            ),
            onEvento = {}
        )
    }
}

@Preview(name = "Controles Expandidos - Repetir Lista", showBackground = true)
@Composable
private fun PreviewControlesExpandidosRepetirLista() {
    PreviewContainer {
        ControlesExpandidos(
            estado = crearEstadoDemo(
                estaReproduciendo = true,
                modoRepeticion = ModoRepeticion.REPETIR_LISTA
            ),
            onEvento = {}
        )
    }
}

@Preview(name = "Controles Expandidos - Repetir Canción", showBackground = true)
@Composable
private fun PreviewControlesExpandidosRepetirCancion() {
    PreviewContainer {
        ControlesExpandidos(
            estado = crearEstadoDemo(
                estaReproduciendo = true,
                modoRepeticion = ModoRepeticion.REPETIR_CANCION
            ),
            onEvento = {}
        )
    }
}

@Preview(name = "Controles Expandidos - Todo Activo", showBackground = true)
@Composable
private fun PreviewControlesExpandidosTodoActivo() {
    PreviewContainer {
        ControlesExpandidos(
            estado = crearEstadoDemo(
                estaReproduciendo = true,
                modoReproduccion = ModoReproduccion.ALEATORIO,
                modoRepeticion = ModoRepeticion.REPETIR_CANCION
            ),
            onEvento = {}
        )
    }
}

// ==================== CONTROLES NORMALES PREVIEWS ====================

@Preview(name = "Controles Normales - Reproduciendo", showBackground = true)
@Composable
private fun PreviewControlesNormalesReproduciendo() {
    PreviewContainer {
        ControlesNormales(
            estado = crearEstadoDemo(estaReproduciendo = true),
            onEvento = {}
        )
    }
}

@Preview(name = "Controles Normales - Pausado", showBackground = true)
@Composable
private fun PreviewControlesNormalesPausado() {
    PreviewContainer {
        ControlesNormales(
            estado = crearEstadoDemo(estaReproduciendo = false),
            onEvento = {}
        )
    }
}

// ==================== BOTONES INDIVIDUALES PREVIEWS ====================

@Preview(name = "Botón Play/Pause Grande - Reproduciendo", showBackground = true)
@Composable
private fun PreviewBotonPlayPauseReproduciendo() {
    PreviewContainer {
        BotonPlayPausePrincipal(
            estado = crearEstadoDemo(estaReproduciendo = true),
            onEvento = {}
        )
    }
}

@Preview(name = "Botón Play/Pause Grande - Pausado", showBackground = true)
@Composable
private fun PreviewBotonPlayPausePausado() {
    PreviewContainer {
        BotonPlayPausePrincipal(
            estado = crearEstadoDemo(estaReproduciendo = false),
            onEvento = {}
        )
    }
}

@Preview(name = "Botón Favorito - Activo", showBackground = true)
@Composable
private fun PreviewBotonFavoritoActivo() {
    PreviewContainer {
        BotonFavorito(
            esFavorita = true,
            onToggle = {}
        )
    }
}

@Preview(name = "Botón Favorito - Inactivo", showBackground = true)
@Composable
private fun PreviewBotonFavoritoInactivo() {
    PreviewContainer {
        BotonFavorito(
            esFavorita = false,
            onToggle = {}
        )
    }
}

@Preview(name = "Botones Navegación", showBackground = true)
@Composable
private fun PreviewBotonesNavegacion() {
    PreviewContainer {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BotonAnterior(onEvento = {})
            BotonSiguiente(onEvento = {})
        }
    }
}

@Preview(name = "Botón Shuffle - Estados", showBackground = true)
@Composable
private fun PreviewBotonShuffleEstados() {
    PreviewContainer {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BotonShuffle(
                    estado = crearEstadoDemo(modoReproduccion = ModoReproduccion.EN_ORDEN),
                    onEvento = {}
                )
                androidx.compose.material3.Text(
                    "Off",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BotonShuffle(
                    estado = crearEstadoDemo(modoReproduccion = ModoReproduccion.ALEATORIO),
                    onEvento = {}
                )
                androidx.compose.material3.Text(
                    "On",
                    color = AppColors.AcentoRosa,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Preview(name = "Botón Repetir - Estados", showBackground = true)
@Composable
private fun PreviewBotonRepetirEstados() {
    PreviewContainer {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BotonRepetir(
                    estado = crearEstadoDemo(modoRepeticion = ModoRepeticion.NO_REPETIR),
                    onEvento = {}
                )
                androidx.compose.material3.Text(
                    "Off",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BotonRepetir(
                    estado = crearEstadoDemo(modoRepeticion = ModoRepeticion.REPETIR_LISTA),
                    onEvento = {}
                )
                androidx.compose.material3.Text(
                    "Lista",
                    color = AppColors.AcentoRosa,
                    fontSize = 10.sp
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                BotonRepetir(
                    estado = crearEstadoDemo(modoRepeticion = ModoRepeticion.REPETIR_CANCION),
                    onEvento = {}
                )
                androidx.compose.material3.Text(
                    "Canción",
                    color = AppColors.AcentoRosa,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ==================== DARK MODE PREVIEWS ====================

@Preview(
    name = "Controles Expandidos - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewControlesExpandidosDark() {
    PreviewContainer {
        ControlesExpandidos(
            estado = crearEstadoDemo(
                estaReproduciendo = true,
                modoReproduccion = ModoReproduccion.ALEATORIO,
                modoRepeticion = ModoRepeticion.REPETIR_LISTA
            ),
            onEvento = {}
        )
    }
}

@Preview(
    name = "Controles Normales - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun PreviewControlesNormalesDark() {
    PreviewContainer {
        ControlesNormales(
            estado = crearEstadoDemo(estaReproduciendo = true),
            onEvento = {}
        )
    }
}

// ==================== COMPARISON PREVIEWS ====================

@Preview(
    name = "Comparación - Todos los Estados",
    showBackground = true,
    widthDp = 400
)
@Composable
private fun PreviewComparacionTodosEstados() {
    FreePlayerMTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E0B24))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Título
            androidx.compose.material3.Text(
                "CONTROLES DEL REPRODUCTOR",
                color = Color.White,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 14.sp
            )

            // Controles Expandidos - Pausado
            Column {
                androidx.compose.material3.Text(
                    "Expandidos (Pausado)",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                ControlesExpandidos(
                    estado = crearEstadoDemo(estaReproduciendo = false),
                    onEvento = {}
                )
            }

            // Controles Expandidos - Reproduciendo con modos
            Column {
                androidx.compose.material3.Text(
                    "Expandidos (Reproduciendo + Aleatorio + Repetir)",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                ControlesExpandidos(
                    estado = crearEstadoDemo(
                        estaReproduciendo = true,
                        modoReproduccion = ModoReproduccion.ALEATORIO,
                        modoRepeticion = ModoRepeticion.REPETIR_CANCION
                    ),
                    onEvento = {}
                )
            }

            androidx.compose.material3.HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f)
            )

            // Controles Normales
            Column {
                androidx.compose.material3.Text(
                    "Normales (Compactos)",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ControlesNormales(
                            estado = crearEstadoDemo(estaReproduciendo = false),
                            onEvento = {}
                        )
                        androidx.compose.material3.Text(
                            "Pausado",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ControlesNormales(
                            estado = crearEstadoDemo(estaReproduciendo = true),
                            onEvento = {}
                        )
                        androidx.compose.material3.Text(
                            "Reproduciendo",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}