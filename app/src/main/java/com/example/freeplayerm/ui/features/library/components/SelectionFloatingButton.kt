package com.example.freeplayerm.ui.features.library.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.features.auth.components.GalaxyBackground
import com.example.freeplayerm.ui.features.library.BibliotecaEstado
import com.example.freeplayerm.ui.features.library.BibliotecaEvento
import com.example.freeplayerm.ui.features.library.TipoDeCuerpoBiblioteca

// ==========================================
// 🎨 COLORES GALÁCTICOS LOCALES
// ==========================================
private object FabColors {
    val NeonPrimary = Color(0xFFD500F9) // Púrpura Vibrante
    val NeonCyan = Color(0xFF00E5FF) // Cyan para acciones positivas
    val NeonDanger = Color(0xFFFF1744) // Rojo brillante para borrar/quitar
    val DarkSurface = Color(0xFF1E1E1E).copy(alpha = 0.95f)
    val TextPrimary = Color.White

    // Gradiente sutil para etiquetas
    val LabelBorder =
        Brush.horizontalGradient(
            colors = listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f))
        )
}

// ==========================================
// ESTADOS DEL FAB
// ==========================================

enum class EstadoFabSeleccion {
    COLAPSADO, // Solo botón "+"
    EXPANDIDO, // Menú completo desplegado
}

// ==========================================
// COMPONENTE PRINCIPAL
// ==========================================

@Composable
fun SelectionFloatingButton(
    modifier: Modifier = Modifier,
    visible: Boolean,
    estadoFab: EstadoFabSeleccion,
    esVistaLista: Boolean,
    esVistaFavoritos: Boolean,
    todasSeleccionadasSonFavoritas: Boolean,
    onCambiarEstado: (EstadoFabSeleccion) -> Unit,
    onAgregarALista: () -> Unit,
    onToggleFavoritos: () -> Unit,
    onQuitarDeLista: () -> Unit,
    onCancelarSeleccion: () -> Unit,
) {
    // Rotación suave del icono principal (+ ↔ ×)
    val rotacionIcono by
        animateFloatAsState(
            targetValue = if (estadoFab == EstadoFabSeleccion.EXPANDIDO) 135f else 0f,
            animationSpec =
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            label = "fab_rotacion",
        )

    // Manejo del botón atrás físico
    BackHandler(enabled = visible) {
        if (estadoFab == EstadoFabSeleccion.EXPANDIDO) {
            onCambiarEstado(EstadoFabSeleccion.COLAPSADO)
        } else {
            onCancelarSeleccion()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp), // Más espacio para respirar
            modifier = Modifier.padding(bottom = 8.dp, end = 8.dp),
        ) {
            // ═══════════════════════════════════════
            // BOTONES SECUNDARIOS (MENÚ)
            // ═══════════════════════════════════════

            // 1. Quitar de Lista (Solo si estamos dentro de una playlist)
            FabItem(
                visible = estadoFab == EstadoFabSeleccion.EXPANDIDO && esVistaLista,
                icono = Icons.Outlined.PlaylistRemove,
                texto = "Quitar de lista",
                colorFondo = FabColors.NeonDanger,
                onClick = {
                    onQuitarDeLista()
                    onCambiarEstado(EstadoFabSeleccion.COLAPSADO)
                },
            )

            // 2. Favoritos (Toggle inteligente)
            FabItem(
                visible = estadoFab == EstadoFabSeleccion.EXPANDIDO,
                icono =
                    if (todasSeleccionadasSonFavoritas) Icons.Default.HeartBroken
                    else Icons.Default.Favorite,
                texto =
                    if (todasSeleccionadasSonFavoritas) "Quitar Favoritos" else "Hacer Favoritas",
                colorFondo =
                    if (todasSeleccionadasSonFavoritas) FabColors.NeonDanger else Color(0xFFFF4081),
                onClick = {
                    onToggleFavoritos()
                    onCambiarEstado(EstadoFabSeleccion.COLAPSADO)
                },
            )

            // 3. Agregar a Lista (Siempre visible excepto si ya estamos añadiendo)
            FabItem(
                visible = estadoFab == EstadoFabSeleccion.EXPANDIDO && !esVistaLista,
                icono = Icons.AutoMirrored.Outlined.PlaylistAdd,
                texto = "Añadir a Playlist",
                colorFondo = FabColors.NeonCyan,
                colorIcono = Color.Black, // Contraste para el Cyan brillante
                onClick = onAgregarALista,
            )

            // ═══════════════════════════════════════
            // FAB PRINCIPAL (Glow Effect)
            // ═══════════════════════════════════════
            FloatingActionButton(
                onClick = {
                    val nuevoEstado =
                        if (estadoFab == EstadoFabSeleccion.COLAPSADO) EstadoFabSeleccion.EXPANDIDO
                        else EstadoFabSeleccion.COLAPSADO
                    onCambiarEstado(nuevoEstado)
                },
                containerColor = FabColors.NeonPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                // Sombra de color para efecto neón
                modifier =
                    Modifier.size(64.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = FabColors.NeonPrimary,
                            ambientColor = FabColors.NeonPrimary,
                        ),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Acciones",
                    modifier = Modifier.size(32.dp).rotate(rotacionIcono),
                )
            }
        }
    }
}

// ==========================================
// SUB-COMPONENTES
// ==========================================

@Composable
private fun FabItem(
    visible: Boolean,
    icono: ImageVector,
    texto: String,
    colorFondo: Color,
    colorIcono: Color = Color.White,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it / 2 } + fadeIn() + scaleIn(),
        exit = slideOutVertically { it / 2 } + fadeOut() + scaleOut(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.padding(end = 4.dp), // Alineación visual fina
        ) {
            // Etiqueta "Glass"
            Box(
                modifier =
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(FabColors.DarkSurface)
                        .border(1.dp, FabColors.LabelBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = texto,
                    color = FabColors.TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Mini FAB
            SmallFloatingActionButton(
                onClick = onClick,
                containerColor = colorFondo,
                contentColor = colorIcono,
                elevation =
                    FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp,
                    ),
                shape = CircleShape,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

// ==========================================
// LOGICA DE INTEGRACIÓN
// ==========================================

@Composable
fun FabSeleccionBiblioteca(
    modifier: Modifier = Modifier,
    estadoBiblioteca: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit,
) {
    var estadoFab by remember { mutableStateOf(EstadoFabSeleccion.COLAPSADO) }

    // Mostrar solo si hay selección activa
    val visible =
        estadoBiblioteca.esModoSeleccion && estadoBiblioteca.cancionesSeleccionadas.isNotEmpty()

    val esVistaLista = estadoBiblioteca.cuerpoActual == TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA
    val esVistaFavoritos = estadoBiblioteca.cuerpoActual == TipoDeCuerpoBiblioteca.FAVORITOS

    // Calcular estado contextual de favoritos
    val todasSeleccionadasSonFavoritas =
        remember(estadoBiblioteca.cancionesSeleccionadas, estadoBiblioteca.canciones) {
            if (estadoBiblioteca.cancionesSeleccionadas.isEmpty()) false
            else {
                val idsSeleccionados = estadoBiblioteca.cancionesSeleccionadas
                // Filtramos solo las que están seleccionadas
                val itemsSeleccionados =
                    estadoBiblioteca.canciones.filter { it.cancion.idCancion in idsSeleccionados }
                // Verificamos si TODAS son favoritas
                itemsSeleccionados.isNotEmpty() && itemsSeleccionados.all { it.esFavorita }
            }
        }

    SelectionFloatingButton(
        modifier = modifier,
        visible = visible,
        estadoFab = estadoFab,
        esVistaLista = esVistaLista,
        esVistaFavoritos = esVistaFavoritos,
        todasSeleccionadasSonFavoritas = todasSeleccionadasSonFavoritas,
        onCambiarEstado = { estadoFab = it },
        onAgregarALista = {
            onBibliotecaEvento(BibliotecaEvento.AbrirDialogoAnadirSeleccionALista)
        },
        onToggleFavoritos = {
            if (todasSeleccionadasSonFavoritas) {
                onBibliotecaEvento(BibliotecaEvento.QuitarSeleccionDeFavoritos)
            } else {
                onBibliotecaEvento(BibliotecaEvento.AnadirSeleccionAFavoritos)
            }
        },
        onQuitarDeLista = {
            onBibliotecaEvento(BibliotecaEvento.QuitarCancionesSeleccionadasDeLista)
        },
        onCancelarSeleccion = { onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccion) },
    )
}

// ==========================================
// PREVIEWS
// ==========================================

@Preview(name = "1. FAB Expandido (Contexto Normal)")
@Composable
private fun PreviewFabExpandido() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        GalaxyBackground()
        SelectionFloatingButton(
            visible = true,
            estadoFab = EstadoFabSeleccion.EXPANDIDO,
            esVistaLista = false,
            esVistaFavoritos = false,
            todasSeleccionadasSonFavoritas = false,
            onCambiarEstado = {},
            onAgregarALista = {},
            onToggleFavoritos = {},
            onQuitarDeLista = {},
            onCancelarSeleccion = {},
        )
    }
}

@Preview(name = "2. FAB Colapsado")
@Composable
private fun PreviewFabColapsado() {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
        GalaxyBackground()
        SelectionFloatingButton(
            visible = true,
            estadoFab = EstadoFabSeleccion.COLAPSADO,
            esVistaLista = false,
            esVistaFavoritos = false,
            todasSeleccionadasSonFavoritas = true,
            onCambiarEstado = {},
            onAgregarALista = {},
            onToggleFavoritos = {},
            onQuitarDeLista = {},
            onCancelarSeleccion = {},
        )
    }
}
