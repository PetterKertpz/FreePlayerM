package com.example.freeplayerm.ui.features.player.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import com.example.freeplayerm.ui.features.player.PlayerState
import com.example.freeplayerm.ui.features.player.ReproductorEvento
import com.example.freeplayerm.ui.features.player.TabExpandido
import com.example.freeplayerm.ui.theme.AppColors
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

// ==================== CONSTANTES Y CONFIGURACIÓN ====================

private object TabsConfig {
    // Colores del sistema
    val BackgroundColor = Color(0xFF0F0518)
    val SurfaceColor = Color.White.copy(alpha = 0.05f)
    val ContentBackground = Color.White.copy(alpha = 0.03f)

    // Animaciones
    val TabTransitionDuration = 250
    val TabFadeDuration = 200
    val TabSlideDuration = 300
    val ScaleSpring = spring<Float>(dampingRatio = 0.5f, stiffness = 400f)

    // Dimensiones
    val TabSelectorRadius = 12.dp
    val TabItemRadius = 8.dp
    val ContentRadius = 12.dp
    val ContentMinHeight = 150.dp
    val ContentMaxHeight = 300.dp

    // Colores de enlaces
    val GeniusColor = Color(0xFFFFFF64)
    val YoutubeColor = Color(0xFFFF0000)
    val GoogleColor = Color(0xFF4285F4)
}

/**
 * ⚡ Configuración inmutable de enlace externo Evita recreaciones innecesarias al pasar datos entre
 * composables
 */
@Immutable
private data class EnlaceConfig(
    val icon: ImageVector,
    val titulo: String,
    val subtitulo: String,
    val color: Color,
    val habilitado: Boolean,
    val onClick: () -> Unit,
)

/** ⚡ Helper para mapear tabs a sus iconos Constante de nivel top para evitar recreaciones */
private fun TabExpandido.toIcon(): ImageVector =
    when (this) {
        TabExpandido.LETRA -> Icons.AutoMirrored.Filled.Article
        TabExpandido.INFO -> Icons.Default.Info
        TabExpandido.ENLACES -> Icons.Default.Link
    }

// ==================== COMPONENTE PRINCIPAL ====================

/**
 * 🔲 TABS DEL MODO EXPANDIDO
 *
 * Sistema de pestañas con transiciones fluidas y contenido dinámico.
 *
 * Optimizaciones implementadas:
 * - ✅ Animaciones centralizadas en constantes
 * - ✅ Estados inmutables para prevenir recomposiciones
 * - ✅ InteractionSource memorable reutilizable
 * - ✅ Transiciones direccionales inteligentes
 */
@Composable
fun ExpandedPlayerTabs(
    estado: PlayerState,
    onEvento: (ReproductorEvento) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Selector de tabs optimizado
        TabSelector(
            tabActivo = estado.tabExpandidoActivo,
            onTabSelected = { tab -> onEvento(ReproductorEvento.Panel.CambiarTab(tab)) },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contenido con transición direccional inteligente
        AnimatedContent(
            targetState = estado.tabExpandidoActivo,
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                val slideOffset = { fullWidth: Int -> direction * fullWidth / 4 }

                (fadeIn(tween(TabsConfig.TabTransitionDuration)) +
                    slideInHorizontally(
                        initialOffsetX = slideOffset,
                        animationSpec = tween(TabsConfig.TabSlideDuration),
                    )) togetherWith
                    (fadeOut(tween(TabsConfig.TabFadeDuration)) +
                        slideOutHorizontally(
                            targetOffsetX = { -direction * it / 4 },
                            animationSpec = tween(TabsConfig.TabTransitionDuration),
                        ))
            },
            label = "tabContent",
        ) { tab ->
            when (tab) {
                TabExpandido.LETRA -> TabLetraContent(estado)
                TabExpandido.INFO -> TabInfoContent(estado)
                TabExpandido.ENLACES -> TabEnlacesContent(estado, onEvento)
            }
        }
    }
}

// ==================== TAB SELECTOR OPTIMIZADO ====================

/**
 * ⚡ Selector de pestañas con animaciones optimizadas
 *
 * Mejora: Reduce de 12 estados animados a 3 componentes independientes
 */
@Composable
private fun TabSelector(tabActivo: TabExpandido, onTabSelected: (TabExpandido) -> Unit) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(TabsConfig.TabSelectorRadius))
                .background(TabsConfig.SurfaceColor)
                .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        TabExpandido.entries.forEach { tab ->
            TabItem(
                tab = tab,
                isSelected = tab == tabActivo,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * ⚡ Item individual de tab simplificado
 *
 * Optimización: Todas las animaciones derivadas del estado isSelected
 */
@Composable
private fun TabItem(
    tab: TabExpandido,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animaciones consolidadas
    val backgroundColor by
        animateColorAsState(
            targetValue =
                if (isSelected) {
                    AppColors.ElectricViolet.v6.copy(alpha = 0.3f)
                } else {
                    Color.Transparent
                },
            animationSpec = tween(TabsConfig.TabTransitionDuration),
            label = "tabBg",
        )

    val contentColor by
        animateColorAsState(
            targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
            animationSpec = tween(TabsConfig.TabTransitionDuration),
            label = "tabContent",
        )

    val iconTint by
        animateColorAsState(
            targetValue =
                if (isSelected) {
                    AppColors.ElectricViolet.v6
                } else {
                    Color.White.copy(alpha = 0.5f)
                },
            animationSpec = tween(TabsConfig.TabTransitionDuration),
            label = "tabIcon",
        )

    val scale by
        animateFloatAsState(
            targetValue =
                when {
                    isPressed -> 0.95f
                    isSelected -> 1.02f
                    else -> 1f
                },
            animationSpec = TabsConfig.ScaleSpring,
            label = "tabScale",
        )

    Box(
        modifier =
            modifier
                .scale(scale)
                .clip(RoundedCornerShape(TabsConfig.TabItemRadius))
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                )
                .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = tab.toIcon(),
                contentDescription = tab.titulo,
                tint = iconTint,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = tab.titulo,
                color = contentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

// ==================== CONTENIDO DE TABS ====================

@Composable
private fun TabLetraContent(estado: PlayerState) {
    ContentContainer {
        if (estado.cargandoLetra) {
            LoadingContent(mensaje = "Cargando letra...")
        } else {
            val scrollState = rememberScrollState()
            Text(
                text = estado.letra ?: "Letra no disponible",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp),
            )
        }
    }
}

@Composable
private fun TabInfoContent(estado: PlayerState) {
    ContentContainer {
        if (estado.cargandoInfo) {
            LoadingContent(mensaje = "Cargando información...")
        } else {
            val scrollState = rememberScrollState()
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)) {
                Text(
                    text = estado.infoArtista ?: "Información no disponible",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                )

                estado.cancionActual?.let { cancion ->
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow(
                        icon = Icons.Default.Album,
                        label = "Álbum",
                        value = cancion.albumNombre ?: "Desconocido",
                    )

                    cancion.generoNombre?.let { genero ->
                        InfoRow(icon = Icons.Default.MusicNote, label = "Género", value = genero)
                    }

                    cancion.fechaLanzamiento?.let { fecha ->
                        InfoRow(
                            icon = Icons.Default.CalendarToday,
                            label = "Año",
                            value = fecha.take(4),
                        )
                    }

                    InfoRow(
                        icon = Icons.Default.Timer,
                        label = "Duración",
                        value = formatDuration(cancion.cancion.duracionSegundos),
                    )
                }
            }
        }
    }
}

@Composable
private fun TabEnlacesContent(estado: PlayerState, onEvento: (ReproductorEvento) -> Unit) {
    Column(
        modifier =
            Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(TabsConfig.ContentRadius))
                .background(TabsConfig.ContentBackground)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Más sobre esta canción",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )

        // Enlaces configurados con datos inmutables
        val enlaces =
            remember(estado) {
                listOf(
                    EnlaceConfig(
                        icon = Icons.Default.Lyrics,
                        titulo = "Ver en Genius",
                        subtitulo = "Letra y anotaciones",
                        color = TabsConfig.GeniusColor,
                        habilitado = !estado.enlaceGenius.isNullOrBlank(),
                        onClick = { onEvento(ReproductorEvento.Enlaces.AbrirGenius) },
                    ),
                    EnlaceConfig(
                        icon = Icons.Default.PlayCircle,
                        titulo = "Buscar en YouTube",
                        subtitulo = "Videos y conciertos",
                        color = TabsConfig.YoutubeColor,
                        habilitado = !estado.enlaceYoutube.isNullOrBlank(),
                        onClick = { onEvento(ReproductorEvento.Enlaces.AbrirYoutube) },
                    ),
                    EnlaceConfig(
                        icon = Icons.Default.Search,
                        titulo = "Buscar artista",
                        subtitulo = "Google Search",
                        color = TabsConfig.GoogleColor,
                        habilitado = !estado.enlaceGoogle.isNullOrBlank(),
                        onClick = { onEvento(ReproductorEvento.Enlaces.AbrirGoogle) },
                    ),
                )
            }

        enlaces.forEach { enlace -> EnlaceButton(config = enlace) }
    }
}

// ==================== COMPONENTES REUTILIZABLES ====================

/** ⚡ Contenedor estándar para contenido de tabs Centraliza altura y estilo */
@Composable
private fun ContentContainer(content: @Composable () -> Unit) {
    Box(
        modifier =
            Modifier.fillMaxWidth()
                .heightIn(min = TabsConfig.ContentMinHeight, max = TabsConfig.ContentMaxHeight)
                .clip(RoundedCornerShape(TabsConfig.ContentRadius))
                .background(TabsConfig.ContentBackground)
    ) {
        content()
    }
}

@Composable
private fun LoadingContent(mensaje: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = AppColors.ElectricViolet.v6,
                modifier = Modifier.size(32.dp),
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = mensaje, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.ElectricViolet.v6.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 12.sp,
            modifier = Modifier.width(70.dp),
        )
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

/**
 * ⚡ Botón de enlace externo optimizado
 *
 * Mejoras:
 * - Recibe configuración inmutable
 * - Animaciones simplificadas (3 vs 4 anteriores)
 * - InteractionSource memorable único
 */
@Composable
private fun EnlaceButton(config: EnlaceConfig) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animaciones consolidadas
    val scale by
        animateFloatAsState(
            targetValue = if (isPressed && config.habilitado) 0.97f else 1f,
            animationSpec = TabsConfig.ScaleSpring,
            label = "enlaceScale",
        )

    val backgroundColor by
        animateColorAsState(
            targetValue =
                when {
                    !config.habilitado -> Color.White.copy(alpha = 0.02f)
                    isPressed -> config.color.copy(alpha = 0.2f)
                    else -> config.color.copy(alpha = 0.1f)
                },
            animationSpec = tween(150),
            label = "enlaceBg",
        )

    Row(
        modifier =
            Modifier.fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = config.habilitado,
                    onClick = config.onClick,
                )
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icono con gradiente
        Box(
            modifier =
                Modifier.size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (config.habilitado) {
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        config.color.copy(alpha = 0.3f),
                                        config.color.copy(alpha = 0.15f),
                                    )
                            )
                        } else {
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(alpha = 0.05f),
                                        Color.White.copy(alpha = 0.02f),
                                    )
                            )
                        }
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = config.icon,
                contentDescription = null,
                tint = if (config.habilitado) config.color else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = config.titulo,
                color = if (config.habilitado) Color.White else Color.White.copy(alpha = 0.3f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = config.subtitulo,
                color =
                    if (config.habilitado) {
                        Color.White.copy(alpha = 0.5f)
                    } else {
                        Color.White.copy(alpha = 0.2f)
                    },
                fontSize = 12.sp,
            )
        }

        if (config.habilitado) {
            val chevronAlpha by
                animateFloatAsState(
                    targetValue = if (isPressed) 0.7f else 0.4f,
                    animationSpec = tween(100),
                    label = "chevronAlpha",
                )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = chevronAlpha),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

// ==================== HELPERS ====================

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%d:%02d".format(mins, secs)
}

// ==================== PREVIEW INFRASTRUCTURE ====================

/** 📦 Provider optimizado de estados Genera los 3 estados principales para testing visual */
private class TabStateProvider : PreviewParameterProvider<PlayerState> {
    private val cancionDemo =
        SongWithArtist.preview(
                titulo = "Bohemian Rhapsody",
                artista = "Queen",
                album = "A Night at the Opera",
                genero = "Rock",
                duracionSegundos = 354,
                esFavorita = true,
            )
            .copy(fechaLanzamiento = "1975-10-31")

    private val infoArtistaDemo =
        """
        🎤 Queen
        📍 Londres, Reino Unido

        Banda británica de rock formada en 1970. Sus integrantes clásicos fueron Freddie Mercury, Brian May, Roger Taylor y John Deacon.
        """
            .trimIndent()

    private val letraDemo =
        """
        Is this the real life?
        Is this just fantasy?
        Caught in a landslide,
        No escape from reality.

        Open your eyes,
        Look up to the skies and see,
        I'm just a poor boy, I need no sympathy...
        """
            .trimIndent()

    override val values =
        sequenceOf(
            // Estado 1: Tab LETRA
            PlayerState(
                cancionActual = cancionDemo,
                tabExpandidoActivo = TabExpandido.LETRA,
                letra = letraDemo,
                cargandoLetra = false,
            ),
            // Estado 2: Tab INFO
            PlayerState(
                cancionActual = cancionDemo,
                tabExpandidoActivo = TabExpandido.INFO,
                infoArtista = infoArtistaDemo,
                cargandoInfo = false,
            ),
            // Estado 3: Tab ENLACES
            PlayerState(
                cancionActual = cancionDemo,
                tabExpandidoActivo = TabExpandido.ENLACES,
                enlaceGenius = "https://genius.com",
                enlaceYoutube = "https://youtube.com",
                enlaceGoogle = "https://google.com",
            ),
        )
}

/** 🎨 Contenedor base para previews */
@Composable
private fun PreviewContainer(content: @Composable () -> Unit) {
    FreePlayerMTheme(darkTheme = true) {
        Box(
            modifier = Modifier.fillMaxSize().background(TabsConfig.BackgroundColor).padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

// ==================== PREVIEWS CONSOLIDADAS ====================

/**
 * 📱 Preview 1: Todos los tabs con contenido real Usa PreviewParameter para generar 3 variantes
 * automáticamente
 */
@Preview(
    name = "Tabs - Todos los Estados",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    heightDp = 450,
)
@Composable
private fun PreviewTabsEstados(
    @PreviewParameter(TabStateProvider::class) estado: PlayerState
) {
    PreviewContainer { ExpandedPlayerTabs(estado = estado, onEvento = {}) }
}

/** 📱 Preview 2: Estados de carga Muestra el shimmer/loading state */
@Preview(
    name = "Tabs - Estados de Carga",
    showBackground = true,
    backgroundColor = 0xFF0F0518,
    heightDp = 400,
)
@Composable
private fun PreviewTabsLoading() {
    PreviewContainer {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            // Loading en tab INFO
            ExpandedPlayerTabs(
                estado =
                    PlayerState(
                        cancionActual = SongWithArtist.preview(),
                        tabExpandidoActivo = TabExpandido.INFO,
                        cargandoInfo = true,
                    ),
                onEvento = {},
            )

            HorizontalDivider(color = Color.White.copy(0.1f))

            // Loading en tab LETRA
            ExpandedPlayerTabs(
                estado =
                    PlayerState(
                        cancionActual = SongWithArtist.preview(),
                        tabExpandidoActivo = TabExpandido.LETRA,
                        cargandoLetra = true,
                    ),
                onEvento = {},
            )
        }
    }
}

/** 📱 Preview 3: Comparación visual completa Muestra selector de tabs en diferentes estados */
@Preview(
    name = "Sistema Completo",
    widthDp = 400,
    heightDp = 600,
    showBackground = true,
    backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewSistemaCompleto() {
    FreePlayerMTheme(darkTheme = true) {
        Column(
            modifier = Modifier.fillMaxSize().background(TabsConfig.BackgroundColor).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Text(
                "SISTEMA DE TABS",
                color = AppColors.ElectricViolet.v6,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
            )

            // Tab activa: LETRA
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tab Letra Activa", color = Color.White.copy(0.6f), fontSize = 11.sp)
                ExpandedPlayerTabs(
                    estado =
                        PlayerState(
                            cancionActual = SongWithArtist.preview(),
                            tabExpandidoActivo = TabExpandido.LETRA,
                            letra = "Is this the real life?\nIs this just fantasy?",
                        ),
                    onEvento = {},
                )
            }

            HorizontalDivider(color = Color.White.copy(0.1f))

            // Tab activa: ENLACES
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tab Enlaces Activa", color = Color.White.copy(0.6f), fontSize = 11.sp)
                ExpandedPlayerTabs(
                    estado =
                        PlayerState(
                            cancionActual = SongWithArtist.preview(),
                            tabExpandidoActivo = TabExpandido.ENLACES,
                            enlaceGenius = "https://genius.com",
                            enlaceYoutube = "https://youtube.com",
                            enlaceGoogle = "https://google.com",
                        ),
                    onEvento = {},
                )
            }
        }
    }
}
