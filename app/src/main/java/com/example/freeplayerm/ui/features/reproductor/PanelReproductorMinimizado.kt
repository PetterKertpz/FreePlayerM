// en: app/src/main/java/com/example/freeplayerm/ui/features/reproductor/PanelReproductorMinimizado.kt
package com.example.freeplayerm.ui.features.reproductor

import android.annotation.SuppressLint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeplayerm.R
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.CancionEntity
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelReproductorMinimizado(
    estado: ReproductorEstado, enEvento: (ReproductorEvento) -> Unit
) {
    val cancion = estado.cancionActual ?: return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.1f to Color.Black,   // empieza en negro
                            0.325f to Color.Black,   // mantenemos negro hasta el 60%
                            0.60f to Color(0xFF6A1B9A) // de ahí al final se mezcla al morado
                        )
                    )
                )

                .fillMaxSize()
                .padding(
                    vertical = 5.dp,
                    horizontal = 5.dp
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp)

            ) {
                // --- Vinilo giratorio ---
                ViniloConPortada(
                    urlPortada = cancion.portadaUrl ?: "",
                    estaReproduciendo = estado.estaReproduciendo,
                    size = 130.dp
                )

                Spacer(modifier = Modifier.width(5.dp))

                // --- Información ---
                Column(
                    modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = cancion.titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 35.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Artista ${cancion.idArtista}", // TODO: nombre real del artista desde relación
                        fontSize = 20.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )



                    // --- Controles de reproducción ---
                    ControlesConTiempo(
                        estado = estado,
                        enEvento = enEvento,
                        duracionTotalMs = (cancion.duracionSegundos * 1000).toLong()
                    )

                    // Slider
                    Slider(
                        value = estado.progresoActualMs.toFloat(),
                        onValueChange = { nuevoProgreso ->
                            enEvento(ReproductorEvento.ActualizarProgreso(nuevoProgreso))
                        },
                        valueRange = 0f..(cancion.duracionSegundos * 1000).toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.Gray
                        )
                    )

                }
            }
        }
    }
}

@Composable
private fun ControlesConTiempo(
    estado: ReproductorEstado,
    enEvento: (ReproductorEvento) -> Unit,
    duracionTotalMs: Long
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // --- Contador de tiempo actual ---
        Text(
            text = formatTiempo(estado.progresoActualMs),
            color = Color.White,
            fontSize = 12.sp
        )

        // --- Botones de control (ocupan el espacio central) ---
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            val iconSize = 22.dp
            val buttonSize = 24.dp

            // Aleatorio
            IconButton(onClick = { enEvento(ReproductorEvento.CambiarModoReproduccion) }, modifier = Modifier.size(buttonSize)) {
                Icon(
                    imageVector = IconosReproductor.Aleatorio,
                    contentDescription = "Modo reproducción",
                    tint = if (estado.modoReproduccion == ModoReproduccion.ALEATORIO) AppColors.AcentoRosa else Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
            // Anterior
            IconButton(onClick = { enEvento(ReproductorEvento.CancionAnterior) }, modifier = Modifier.size(buttonSize)) {
                Icon(imageVector = IconosReproductor.Anterior, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(iconSize))
            }
            // Play/Pausa
            IconButton(onClick = { enEvento(ReproductorEvento.ReproducirPausar) }, modifier = Modifier.size(buttonSize + 12.dp)) {
                Icon(
                    imageVector = if (estado.estaReproduciendo) IconosReproductor.Pausa else IconosReproductor.Reproducir,
                    contentDescription = "Play/Pause", tint = Color.White, modifier = Modifier.size(iconSize + 12.dp)
                )
            }
            // Siguiente
            IconButton(onClick = { enEvento(ReproductorEvento.SiguienteCancion) }, modifier = Modifier.size(buttonSize)) {
                Icon(imageVector = IconosReproductor.Siguiente, contentDescription = "Siguiente", tint = Color.White, modifier = Modifier.size(iconSize))
            }
            // Repetir
            IconButton(onClick = { enEvento(ReproductorEvento.CambiarModoRepeticion) }, modifier = Modifier.size(buttonSize)) {
                val (icono, color) = when (estado.modoRepeticion) {
                    ModoRepeticion.NO_REPETIR -> IconosReproductor.RepetirLista to Color.White
                    ModoRepeticion.REPETIR_LISTA -> IconosReproductor.RepetirLista to AppColors.AcentoRosa
                    ModoRepeticion.REPETIR_CANCION -> IconosReproductor.RepetirCancion to AppColors.AcentoRosa
                }
                Icon(imageVector = icono, contentDescription = "Repetir", tint = color, modifier = Modifier.size(iconSize))
            }
        }

        // --- Contador de tiempo total ---
        Text(
            text = formatTiempo(duracionTotalMs),
            color = Color.White,
            fontSize = 12.sp
        )
    }
}

@SuppressLint("DefaultLocale")
private fun formatTiempo(milisegundos: Long): String {
    val minutos = TimeUnit.MILLISECONDS.toMinutes(milisegundos)
    val segundos = TimeUnit.MILLISECONDS.toSeconds(milisegundos) % 60
    return String.format("%02d:%02d", minutos, segundos)
}

@Composable
private fun ViniloConPortada(
    urlPortada: String, estaReproduciendo: Boolean, size: Dp = 60.dp
) {
    val transicionInfinita = rememberInfiniteTransition(label = "vinilo_rotacion")

    val rotacion by transicionInfinita.animateFloat(
        initialValue = 0f,
        targetValue = if (estaReproduciendo) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing)
        ),
        label = "rotacion"
    )

    Box(
        modifier = Modifier.size(size), contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.vinilo_foreground),
            contentDescription = "Disco de Vinilo",
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (estaReproduciendo) rotacion else 0f)
        )
        AsyncImage(
            model = urlPortada,
            contentDescription = "Portada del Álbum",
            modifier = Modifier
                .size(size / 2.5f)
                .clip(CircleShape)
                .background(Color.Red),
            contentScale = ContentScale.Crop
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPanelReproductorEstado() {
    val cancionDemo = CancionEntity(
        idCancion = 1,
        titulo = "Canción de Prueba",
        idArtista = 1,
        idAlbum = 1,
        idGenero = 1,
        duracionSegundos = 240,
        portadaUrl = "",
        origen = "LOCAL",
        archivoPath = null
    )

    val estadoDemo = ReproductorEstado(
        cancionActual = cancionDemo,
        estaReproduciendo = true,
        progresoActualMs = 120_000,
        modoReproduccion = ModoReproduccion.ALEATORIO,
        modoRepeticion = ModoRepeticion.REPETIR_LISTA,
        esFavorita = true
    )

    MaterialTheme {
        PanelReproductorMinimizado(estado = estadoDemo, enEvento = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewPanelReproductorMinimizado() {
    val cancionDePrueba = CancionEntity(
        idCancion = 1,
        titulo = "El Sol no Regresa",
        idArtista = 1,
        idAlbum = 1,
        idGenero = 1,
        duracionSegundos = 227,
        portadaUrl = "",
        origen = "LOCAL",
        archivoPath = null
    )
    FreePlayerMTheme {
        PanelReproductorMinimizado(
            estado = ReproductorEstado(
                cancionActual = cancionDePrueba,
                estaReproduciendo = true,
                progresoActualMs = 75000, // 1 minuto y 15 segundos
                modoReproduccion = ModoReproduccion.ALEATORIO,
                modoRepeticion = ModoRepeticion.REPETIR_CANCION
            ),
            enEvento = {}
        )
    }
}