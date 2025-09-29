// en: app/src/main/java/com/example/freeplayerm/ui/features/reproductor/PantallaReproductorExpandido.kt
package com.example.freeplayerm.ui.features.reproductor

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaReproductorExpandido(
    estado: ReproductorEstado,
    onCollapse: () -> Unit,
    enEvento: (ReproductorEvento) -> Unit,
) {
    val cancionConArtista = estado.cancionActual
    val cancionActual = estado.cancionActual

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            AppColors.PurpuraProfundo.copy(alpha = 0.9f),
            Color.Black
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 16.dp)
    ) {
        // --- Barra superior con botón para colapsar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCollapse) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Colapsar Reproductor",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // --- Contenido principal (scrollable) ---
        Column(

            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Sección Superior: Vinilo e Información ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically

            ) {
                ViniloConPortada(
                    cancion = cancionConArtista,
                    anguloRotacion = estado.anguloRotacionVinilo,
                    size = 200.dp,

                )
                Spacer(modifier = Modifier.width(16.dp))

                // --- Información de la Canción (Derecha) ---
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TextoInfo(
                        valor = cancionActual?.cancion?.titulo ?: "Título no disponible",
                        esTitulo = true
                    )
                    TextoInfo(
                        etiqueta = "Artista",
                        valor = cancionActual?.artistaNombre ?: "Desconocido"
                    )
                    TextoInfo(
                        etiqueta = "Género",
                        valor = cancionActual?.generoNombre ?: "Desconocido"
                    )
                    TextoInfo(
                        etiqueta = "Álbum",
                        valor = cancionActual?.albumNombre ?: "Desconocido"
                    )
                    // Nota: El año de lanzamiento no está en el modelo `CancionConArtista`.
                    // Se podría añadir en el futuro. Por ahora, es un valor de ejemplo.
                    TextoInfo(etiqueta = "Lanzamiento", valor = "2023")
                }
            }

            // --- Sección Media: Letra e Información Adicional ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                var tabSeleccionada by remember { mutableIntStateOf(0) }
                val tabs = listOf("Letra", "Acerca de")

                SecondaryTabRow(
                    selectedTabIndex = tabSeleccionada,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = {}, // Sin indicador por ahora

                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = tabSeleccionada == index,
                            onClick = { tabSeleccionada = index },
                            text = { Text(title) },
                            selectedContentColor = AppColors.AcentoRosa,
                            unselectedContentColor = Color.LightGray
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp) // Altura fija para el contenido de la pestaña
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (tabSeleccionada) {
                        0 -> Text(
                            "Letra no disponible.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        1 -> Text(
                            "Información del artista no disponible.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // --- Contenido Fijo en la parte inferior ---
        Column(Modifier.fillMaxWidth()) {
            // --- Barra de Progreso y Tiempos ---
            Column(modifier = Modifier.fillMaxWidth()) {
                var sliderPosition by remember { mutableFloatStateOf(0f) }

                if (cancionConArtista != null) {
                    Slider(
                        value = if (estado.isScrubbing) sliderPosition else estado.progresoActualMs.toFloat(),
                        onValueChange = { nuevoProgreso ->
                            sliderPosition = nuevoProgreso
                            enEvento(ReproductorEvento.OnScrub(nuevoProgreso))
                        },
                        onValueChangeFinished = {
                            enEvento(ReproductorEvento.OnScrubFinished(sliderPosition))
                        },
                        valueRange = 0f..(cancionConArtista.cancion.duracionSegundos * 1000).toFloat(),
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .align(Alignment.CenterHorizontally),
                        thumb = {
                            SliderDefaults.Thumb(
                                interactionSource = MutableInteractionSource(),
                                thumbSize = DpSize(18.dp, 18.dp)
                            )
                        },
                        track = { sliderState: SliderState ->
                            SliderDefaults.Track(
                                modifier = Modifier
                                    .fillMaxHeight(0.01f),
                                sliderState = sliderState,
                                enabled = true,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = Color.Black,
                                    inactiveTrackColor = Color.White
                                ),
                                trackInsideCornerSize = 0.dp,   // opcional, controla bordes internos
                                thumbTrackGapSize = 0.dp        // opcional, separación del thumb
                            )
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Red,
                            disabledThumbColor = Color.DarkGray,
                            activeTrackColor = Color.Black,
                            inactiveTrackColor = Color.White
                        )
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTiempo(estado.progresoActualMs), color = Color.LightGray)
                    Text(
                        text = formatTiempo(cancionActual?.cancion?.duracionSegundos?.times(1000)?.toLong() ?: 0L),
                        color = Color.LightGray
                    )
                }
            }

            // --- Controles de Reproducción ---
            ControlesReproductorExpandido(estado = estado, enEvento = enEvento)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TextoInfo(etiqueta: String? = null, valor: String, esTitulo: Boolean = false) {
    if (esTitulo) {
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    } else {
        Column {
            Text(
                text = etiqueta ?: "",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                text = valor,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ControlesReproductorExpandido(
    estado: ReproductorEstado,
    enEvento: (ReproductorEvento) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val iconSize = 32.dp

        IconButton(onClick = { enEvento(ReproductorEvento.CambiarModoReproduccion) }) {
            Icon(
                imageVector = IconosReproductor.Aleatorio, // ✅ CAMBIO
                contentDescription = "Aleatorio",
                tint = if (estado.modoReproduccion == ModoReproduccion.ALEATORIO) AppColors.AcentoRosa else Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(onClick = { enEvento(ReproductorEvento.CancionAnterior) }) {
            Icon(
                imageVector = IconosReproductor.Anterior, // ✅ CAMBIO
                contentDescription = "Anterior",
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(
            onClick = { enEvento(ReproductorEvento.ReproducirPausar) },
            modifier = Modifier
                .size(80.dp)
                .background(AppColors.AcentoRosa, CircleShape)
        ) {
            Icon(
                imageVector = if (estado.estaReproduciendo) IconosReproductor.Pausa else IconosReproductor.Reproducir, // ✅ CAMBIO
                contentDescription = "Reproducir/Pausar",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        IconButton(onClick = { enEvento(ReproductorEvento.SiguienteCancion) }) {
            Icon(
                imageVector = IconosReproductor.Siguiente, // ✅ CAMBIO
                contentDescription = "Siguiente",
                tint = Color.White,
                modifier = Modifier.size(iconSize)
            )
        }
        IconButton(onClick = { enEvento(ReproductorEvento.CambiarModoRepeticion) }) {
            // ✅ CAMBIO: Lógica adaptada para usar los ImageVector de IconosReproductor
            val (vector, color) = when (estado.modoRepeticion) {
                ModoRepeticion.NO_REPETIR -> IconosReproductor.RepetirLista to Color.White
                ModoRepeticion.REPETIR_LISTA -> IconosReproductor.RepetirLista to AppColors.AcentoRosa
                ModoRepeticion.REPETIR_CANCION -> IconosReproductor.RepetirCancion to AppColors.AcentoRosa
            }

            Icon(
                imageVector = vector,
                contentDescription = "Repetir",
                tint = color,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun formatTiempo(milisegundos: Long): String {
    val minutos = TimeUnit.MILLISECONDS.toMinutes(milisegundos)
    val segundos = TimeUnit.MILLISECONDS.toSeconds(milisegundos) % 60
    return String.format("%02d:%02d", minutos, segundos)
}


// =================================================================
// Previsualización
// =================================================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewPantallaReproductorExpandido() {
    val cancionDePrueba = CancionEntity(
        idCancion = 1,
        idArtista = 1,
        idAlbum = 1,
        idGenero = 1,
        titulo = "Bohemian Rhapsody",
        duracionSegundos = 355,
        origen = "LOCAL",
        archivoPath = ""
    )
    val cancionConArtistaDePrueba = CancionConArtista(
        cancion = cancionDePrueba,
        artistaNombre = "Queen",
        albumNombre = "A Night at the Opera",
        generoNombre = "Rock Progresivo",
        esFavorita = true,
        portadaPath = null // Coil usará el `error` painter
    )

    FreePlayerMTheme {
        PantallaReproductorExpandido(
            estado = ReproductorEstado(
                cancionActual = cancionConArtistaDePrueba,
                estaReproduciendo = true, // Ponlo en `false` para ver el vinilo detenido
                progresoActualMs = 125000L, // 2 minutos y 5 segundos
                modoRepeticion = ModoRepeticion.REPETIR_LISTA,
                modoReproduccion = ModoReproduccion.ALEATORIO
            ),
            onCollapse = {},
            enEvento = {},

        )
    }
}