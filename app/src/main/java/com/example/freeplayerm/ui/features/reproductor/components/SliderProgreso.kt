package com.example.freeplayerm.ui.features.reproductor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.features.reproductor.ReproductorEstado
import com.example.freeplayerm.ui.features.reproductor.ReproductorEvento
import com.example.freeplayerm.ui.features.reproductor.formatearTiempo
import com.example.freeplayerm.ui.theme.AppColors

/**
 * 📊 SLIDER DE PROGRESO - VERSIÓN COMPLETA
 *
 * Para modo expandido con tiempos visibles
 *
 * Características:
 * ✅ Scrubbing sin glitches (usa progresoVisibleMs)
 * ✅ Muestra tiempo actual y total
 * ✅ Feedback visual durante arrastre
 */
@Composable
fun SliderProgreso(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val duracionTotal = estado.duracionTotalMs

        // Variable local para tracking durante arrastre
        var sliderValue by remember(estado.cancionActual) {
            mutableFloatStateOf(estado.progresoVisibleMs.toFloat())
        }

        // Actualizar cuando NO estamos en scrubbing
        LaunchedEffect(estado.progresoVisibleMs, estado.isScrubbing) {
            if (!estado.isScrubbing) {
                sliderValue = estado.progresoVisibleMs.toFloat()
            }
        }

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
                onEvento(ReproductorEvento.Navegacion.OnScrub(newValue.toLong()))
            },
            onValueChangeFinished = {
                onEvento(ReproductorEvento.Navegacion.OnScrubFinished(sliderValue.toLong()))
            },
            valueRange = 0f..duracionTotal.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = AppColors.AcentoRosa,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.height(24.dp)
        )

        // Tiempos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = estado.progresoVisibleMs.formatearTiempo(),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
            Text(
                text = duracionTotal.formatearTiempo(),
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * 📊 SLIDER DE PROGRESO - VERSIÓN COMPACTA
 *
 * Para modo normal, sin tiempos visibles
 */
@Composable
fun SliderProgresoCompacto(
    estado: ReproductorEstado,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier
) {
    val duracionTotal = estado.duracionTotalMs

    var sliderValue by remember(estado.cancionActual) {
        mutableFloatStateOf(estado.progresoVisibleMs.toFloat())
    }

    LaunchedEffect(estado.progresoVisibleMs, estado.isScrubbing) {
        if (!estado.isScrubbing) {
            sliderValue = estado.progresoVisibleMs.toFloat()
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        // Tiempo actual
        Text(
            text = estado.progresoVisibleMs.formatearTiempo(),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.width(40.dp)
        )

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
                onEvento(ReproductorEvento.Navegacion.OnScrub(newValue.toLong()))
            },
            onValueChangeFinished = {
                onEvento(ReproductorEvento.Navegacion.OnScrubFinished(sliderValue.toLong()))
            },
            valueRange = 0f..duracionTotal.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
                thumbColor = AppColors.AcentoRosa,
                activeTrackColor = AppColors.AcentoRosa,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
            ),
            modifier = Modifier
                .weight(1f)
                .height(16.dp)
        )

        // Tiempo total
        Text(
            text = duracionTotal.formatearTiempo(),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            modifier = Modifier.width(40.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
