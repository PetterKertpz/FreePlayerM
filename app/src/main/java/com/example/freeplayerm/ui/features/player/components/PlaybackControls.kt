package com.example.freeplayerm.ui.features.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.ModoRepeticion
import com.example.freeplayerm.ui.features.player.ModoReproduccion
import com.example.freeplayerm.com.example.freeplayerm.ui.features.player.components.PlayerIcons
import com.example.freeplayerm.ui.features.player.PlayerState
import com.example.freeplayerm.ui.features.player.ReproductorEvento
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

// ==================== CONSTANTES DE ANIMACIÓN ====================

private object PlaybackAnimationSpec {
    val FADE_DURATION = 300
    val SCALE_DURATION = 300
    val STAGGER_DELAY = 50
    val BUTTON_SPRING = spring<Float>(dampingRatio = 0.5f, stiffness = 400f)
    val FAVORITE_SPRING = spring<Float>(dampingRatio = 0.3f, stiffness = 500f)
}

// ⚡ Optimización: Lista constante movida fuera del composable
private val CONTROL_SEQUENCE = listOf("shuffle", "prev", "play", "next", "repeat")

// ==================== COMPONENTES PRINCIPALES ====================

/**
 * 🎛️ CONTROLES EXPANDIDOS
 *
 * Controles completos para modo expandido con animaciones secuenciales. Optimizado con lista
 * constante y lambdas estables.
 */
@Composable
fun PlaybackControls(
    estado: PlayerState,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CONTROL_SEQUENCE.forEachIndexed { index, control ->
            AnimatedVisibility(
                visible = true,
                enter =
                    fadeIn(
                        tween(
                            durationMillis = PlaybackAnimationSpec.FADE_DURATION,
                            delayMillis = PlaybackAnimationSpec.STAGGER_DELAY * index,
                        )
                    ) +
                        scaleIn(
                            initialScale = 0.8f,
                            animationSpec =
                                tween(
                                    durationMillis = PlaybackAnimationSpec.SCALE_DURATION,
                                    delayMillis = PlaybackAnimationSpec.STAGGER_DELAY * index,
                                ),
                        ),
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
 * Versión reducida para modo normal: Anterior | Play/Pause | Siguiente
 */
@Composable
fun ControlesNormales(
    estado: PlayerState,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),  // ANTES: 8.dp
    ) {
        // Botón Anterior
        IconButton(
            onClick = { onEvento(ReproductorEvento.Reproduccion.CancionAnterior) },
            modifier = Modifier.size(36.dp)  // 👈 AÑADIR ESTO
        ) {
            Icon(
                imageVector = PlayerIcons.Anterior,
                contentDescription = "Canción anterior",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),  // ANTES: 28.dp
            )
        }

        // Botón Play/Pause
        IconButton(
            onClick = { onEvento(ReproductorEvento.Reproduccion.ReproducirPausar) },
            modifier = Modifier
                .size(36.dp)  // ANTES: 48.dp
                .background(AppColors.ElectricViolet.v6.copy(alpha = 0.2f), CircleShape),
        ) {
            Icon(
                imageVector = PlayerIcons.obtenerIconoPlayPause(estado.estaReproduciendo),
                contentDescription = if (estado.estaReproduciendo) "Pausar" else "Reproducir",
                tint = Color.White,
                modifier = Modifier.size(20.dp),  // ANTES: 28.dp
            )
        }

        // Botón Siguiente
        IconButton(
            onClick = { onEvento(ReproductorEvento.Reproduccion.SiguienteCancion) },
            modifier = Modifier.size(36.dp)  // 👈 AÑADIR ESTO
        ) {
            Icon(
                imageVector = PlayerIcons.Siguiente,
                contentDescription = "Siguiente canción",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),  // ANTES: 28.dp
            )
        }
    }
}

// ==================== BOTONES INDIVIDUALES ====================

@Composable
fun BotonShuffle(estado: PlayerState, onEvento: (ReproductorEvento) -> Unit) {
    val activo = estado.modoReproduccion == ModoReproduccion.ALEATORIO

    IconButton(onClick = { onEvento(ReproductorEvento.Configuracion.CambiarModoReproduccion) }) {
        Icon(
            imageVector = PlayerIcons.Aleatorio,
            contentDescription = if (activo) "Desactivar aleatorio" else "Activar aleatorio",
            tint = if (activo) AppColors.ElectricViolet.v6 else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
fun BotonAnterior(onEvento: (ReproductorEvento) -> Unit) {
    IconButton(onClick = { onEvento(ReproductorEvento.Reproduccion.CancionAnterior) }) {
        Icon(
            imageVector = PlayerIcons.Anterior,
            contentDescription = "Canción anterior",
            tint = Color.White,
            modifier = Modifier.size(40.dp),
        )
    }
}

/**
 * ⚡ Botón principal con animación táctil mejorada
 *
 * Optimizaciones:
 * - InteractionSource memorable para evitar recreaciones
 * - Animaciones suaves con spring physics
 * - Crossfade fluido entre estados
 */
@Composable
fun BotonPlayPausePrincipal(estado: PlayerState, onEvento: (ReproductorEvento) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animación de escala al presionar
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed) 0.9f else 1f,
            animationSpec = PlaybackAnimationSpec.BUTTON_SPRING,
            label = "buttonScale",
        )

    Box(
        modifier =
            Modifier.size(72.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(AppColors.ElectricViolet.v6, Color(0xFFAA00FF))
                    )
                )
                .clickable(interactionSource = interactionSource, indication = null) {
                    onEvento(ReproductorEvento.Reproduccion.ReproducirPausar)
                },
        contentAlignment = Alignment.Center,
    ) {
        // Transición suave entre iconos
        Crossfade(
            targetState = estado.estaReproduciendo,
            animationSpec = tween(200),
            label = "playPauseIcon",
        ) { isPlaying ->
            Icon(
                imageVector = PlayerIcons.obtenerIconoPlayPause(isPlaying),
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
fun BotonSiguiente(onEvento: (ReproductorEvento) -> Unit) {
    IconButton(onClick = { onEvento(ReproductorEvento.Reproduccion.SiguienteCancion) }) {
        Icon(
            imageVector = PlayerIcons.Siguiente,
            contentDescription = "Siguiente canción",
            tint = Color.White,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Composable
fun BotonRepetir(estado: PlayerState, onEvento: (ReproductorEvento) -> Unit) {
    val activo = estado.modoRepeticion != ModoRepeticion.NO_REPETIR

    val descripcion =
        when (estado.modoRepeticion) {
            ModoRepeticion.NO_REPETIR -> "Activar repetición"
            ModoRepeticion.REPETIR_LISTA -> "Repetir lista activado"
            ModoRepeticion.REPETIR_CANCION -> "Repetir canción activado"
        }

    val icono =
        when (estado.modoRepeticion) {
            ModoRepeticion.REPETIR_CANCION -> PlayerIcons.RepetirCancion
            else -> PlayerIcons.RepetirLista
        }

    IconButton(onClick = { onEvento(ReproductorEvento.Configuracion.CambiarModoRepeticion) }) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = if (activo) AppColors.ElectricViolet.v6 else Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp),
        )
    }
}

/**
 * ⚡ Botón favorito optimizado
 *
 * Mejoras:
 * - Animación simplificada sin trigger manual
 * - Transiciones de color suaves
 * - Efecto "pop" natural con spring physics
 */
@Composable
fun BotonFavorito(esFavorita: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    // Animación de escala simplificada
    val scale by
        animateFloatAsState(
            targetValue = if (esFavorita) 1.1f else 1f,
            animationSpec = PlaybackAnimationSpec.FAVORITE_SPRING,
            label = "heartScale",
        )

    // Color animado
    val tintColor by
        animateColorAsState(
            targetValue = if (esFavorita) Color.Red else Color.White.copy(alpha = 0.6f),
            animationSpec = tween(300),
            label = "heartColor",
        )

    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = PlayerIcons.obtenerIconoFavorito(esFavorita),
            contentDescription = if (esFavorita) "Quitar de favoritos" else "Agregar a favoritos",
            tint = tintColor,
            modifier = Modifier.size(28.dp).scale(scale),
        )
    }
}

// ==================== PREVIEW INFRASTRUCTURE ====================

/**
 * 📦 Provider para estados de reproducción Genera todas las variantes necesarias para testing
 * visual completo
 */
private class PlayerStateProvider : PreviewParameterProvider<PlayerState> {
    override val values =
        sequenceOf(
            // Estado básico: Reproduciendo
            PlayerState(
                cancionActual =
                    SongWithArtist.preview(
                        titulo = "Bohemian Rhapsody",
                        artista = "Queen",
                        album = "A Night at the Opera",
                    ),
                estaReproduciendo = true,
                progresoActualMs = 125000L,
                modoReproduccion = ModoReproduccion.EN_ORDEN,
                modoRepeticion = ModoRepeticion.NO_REPETIR,
                esFavorita = false,
            ),
            // Estado: Pausado
            PlayerState(
                cancionActual =
                    SongWithArtist.preview(titulo = "Stairway to Heaven", artista = "Led Zeppelin"),
                estaReproduciendo = false,
                progresoActualMs = 45000L,
                modoReproduccion = ModoReproduccion.EN_ORDEN,
                modoRepeticion = ModoRepeticion.NO_REPETIR,
                esFavorita = false,
            ),
            // Estado: Modos activos
            PlayerState(
                cancionActual =
                    SongWithArtist.preview(
                        titulo = "Hotel California",
                        artista = "Eagles",
                        esFavorita = true,
                    ),
                estaReproduciendo = true,
                progresoActualMs = 200000L,
                modoReproduccion = ModoReproduccion.ALEATORIO,
                modoRepeticion = ModoRepeticion.REPETIR_CANCION,
                esFavorita = true,
            ),
        )
}

/** 🎨 Contenedor base para previews Fondo oficial con padding consistente */
@Composable
private fun PreviewContainer(content: @Composable () -> Unit) {
    FreePlayerMTheme(darkTheme = true) {
        Box(
            modifier = Modifier.fillMaxWidth().background(Color(0xFF0F0518)).padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

// ==================== PREVIEWS CONSOLIDADAS ====================

/**
 * 📱 Preview 1: Controles Expandidos - Todos los estados Muestra los 3 estados principales del
 * reproductor
 */
@Preview(name = "Controles Expandidos", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewPlaybackControlsStates(
    @PreviewParameter(PlayerStateProvider::class) estado: PlayerState
) {
    PreviewContainer { PlaybackControls(estado = estado, onEvento = {}) }
}

/** 📱 Preview 2: Controles Normales (Compactos) Versión mini player con estados principales */
@Preview(name = "Controles Compactos", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewControlesNormales(
    @PreviewParameter(PlayerStateProvider::class) estado: PlayerState
) {
    PreviewContainer { ControlesNormales(estado = estado, onEvento = {}) }
}

/**
 * 📱 Preview 3: Comparación Visual Completa Muestra todos los componentes juntos para verificación
 * rápida
 */
@Preview(
    name = "Sistema Completo",
    widthDp = 400,
    heightDp = 700,
    showBackground = true,
    backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewSistemaCompleto() {
    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            // Header
            androidx.compose.material3.Text(
                "SISTEMA DE CONTROLES",
                color = AppColors.ElectricViolet.v6,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
            )

            // Sección: Controles Expandidos
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.Text(
                    "Modo Expandido - Activo",
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp,
                )
                PlaybackControls(
                    estado =
                        PlayerState(
                            cancionActual = SongWithArtist.preview(),
                            estaReproduciendo = true,
                            modoReproduccion = ModoReproduccion.ALEATORIO,
                            modoRepeticion = ModoRepeticion.REPETIR_CANCION,
                        ),
                    onEvento = {},
                )
            }

            androidx.compose.material3.HorizontalDivider(
                color = Color.White.copy(0.1f),
                thickness = 1.dp,
            )

            // Sección: Controles Compactos
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.Text(
                    "Modo Compacto",
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp,
                )
                ControlesNormales(
                    estado =
                        PlayerState(
                            cancionActual = SongWithArtist.preview(),
                            estaReproduciendo = false,
                        ),
                    onEvento = {},
                )
            }

            androidx.compose.material3.HorizontalDivider(
                color = Color.White.copy(0.1f),
                thickness = 1.dp,
            )

            // Sección: Botones de Estado
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                androidx.compose.material3.Text(
                    "Estados de Botones",
                    color = Color.White.copy(0.6f),
                    fontSize = 11.sp,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Play/Pause grande
                    BotonPlayPausePrincipal(
                        estado =
                            PlayerState(
                                cancionActual = SongWithArtist.preview(),
                                estaReproduciendo = true,
                            ),
                        onEvento = {},
                    )

                    // Favorito activo/inactivo
                    BotonFavorito(esFavorita = false, onToggle = {})
                    BotonFavorito(esFavorita = true, onToggle = {})
                }
            }
        }
    }
}
