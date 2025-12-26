package com.example.freeplayerm.ui.features.biblioteca.components

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
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.outlined.PlaylistRemove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEstado
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.biblioteca.TipoDeCuerpoBiblioteca
import kotlin.math.absoluteValue

// ==========================================
// COLORES TEMÁTICOS
// ==========================================
private object FabColors {
    val neonPrimario = Color(0xFFD500F9)
    val neonSecundario = Color(0xFF7C4DFF)
    val favoritoRosa = Color(0xFFFF4081)
    val peligroRojo = Color(0xFFFF6B6B)
    val fondoOscuro = Color(0xFF1A0A1F)
    val textoSecundario = Color.White.copy(alpha = 0.8f)
}

// ==========================================
// ESTADOS DEL FAB
// ==========================================

/**
 * Estados posibles del FAB de selección
 */
enum class EstadoFabSeleccion {
    /** Solo muestra el botón "+" */
    COLAPSADO,

    /** Muestra el botón principal + botones secundarios */
    EXPANDIDO
}

// ==========================================
// COMPONENTE PRINCIPAL: FAB EXPANDIBLE
// ==========================================

/**
 * 🎯 BOTÓN FLOTANTE DE SELECCIÓN EXPANDIBLE
 *
 * Comportamiento:
 * - COLAPSADO: Solo botón "+" visible
 * - EXPANDIDO: Botón "×" (cerrar) + botones de acción según contexto
 *
 * Acciones según vista:
 * - Vista Normal: [Agregar a Lista] [Agregar a Favoritos]
 * - Vista Lista: [Agregar a Lista] [Quitar de Lista]
 * - Vista Favoritos: [Agregar a Lista] [Quitar de Favoritos]
 *
 * Gestos:
 * - Tap en "+" → Expande
 * - Tap en "×" → Colapsa
 * - BackHandler → Colapsa si expandido, cancela selección si colapsado
 *
 * @param modifier Modificador externo
 * @param visible Si el FAB debe mostrarse
 * @param estadoFab Estado actual (COLAPSADO/EXPANDIDO)
 * @param esVistaLista Si estamos viendo canciones de una lista
 * @param esVistaFavoritos Si estamos viendo favoritos
 * @param onCambiarEstado Callback para cambiar estado del FAB
 * @param onAgregarALista Callback para agregar a lista de reproducción
 * @param onAgregarAFavoritos Callback para agregar a favoritos
 * @param onQuitarDeLista Callback para quitar de lista (solo vista lista)
 * @param onQuitarDeFavoritos Callback para quitar de favoritos (solo vista favoritos)
 * @param onCancelarSeleccion Callback para salir del modo selección
 */
@Composable
fun BotonFlotanteSeleccion(
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
    onCancelarSeleccion: () -> Unit
) {
    // Animación de rotación del icono principal (+ → ×)
    val rotacionIcono by animateFloatAsState(
        targetValue = if (estadoFab == EstadoFabSeleccion.EXPANDIDO) 45f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fab_rotacion"
    )

    // BackHandler para manejar botón atrás
    BackHandler(enabled = visible) {
        when (estadoFab) {
            EstadoFabSeleccion.EXPANDIDO -> onCambiarEstado(EstadoFabSeleccion.COLAPSADO)
            EstadoFabSeleccion.COLAPSADO -> onCancelarSeleccion()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ═══════════════════════════════════════
            // BOTONES SECUNDARIOS (Solo cuando expandido)
            // ═══════════════════════════════════════

            // Opción: Quitar de Lista (solo en vista lista)
            FabSecundarioAnimado(
                visible = estadoFab == EstadoFabSeleccion.EXPANDIDO && esVistaLista,
                icono = Icons.Outlined.PlaylistRemove,
                etiqueta = "Quitar",
                descripcion = "Quitar de lista",
                color = FabColors.peligroRojo,
                onClick = {
                    onQuitarDeLista()
                    onCambiarEstado(EstadoFabSeleccion.COLAPSADO)
                })

            // Opción: Toggle Favoritos (contextual según estado)
            FabSecundarioAnimado(
                visible = estadoFab == EstadoFabSeleccion.EXPANDIDO,
                icono = if (todasSeleccionadasSonFavoritas) Icons.Default.HeartBroken
                else Icons.Default.Favorite,
                etiqueta = if (todasSeleccionadasSonFavoritas) "Quitar" else "Favoritos",
                descripcion = if (todasSeleccionadasSonFavoritas) "Quitar de favoritos"
                else "Agregar a favoritos",
                color = if (todasSeleccionadasSonFavoritas) FabColors.peligroRojo
                else FabColors.favoritoRosa,
                onClick = {
                    onToggleFavoritos()
                    onCambiarEstado(EstadoFabSeleccion.COLAPSADO)
                })

            // Opción: Agregar a Lista (excepto en vista lista)
            FabSecundarioAnimado(
                visible = estadoFab == EstadoFabSeleccion.EXPANDIDO && !esVistaLista,
                icono = Icons.AutoMirrored.Outlined.PlaylistAdd,
                etiqueta = "Lista",
                descripcion = "Agregar a lista",
                color = FabColors.neonSecundario,
                onClick = onAgregarALista // No colapsar, se abrirá diálogo
            )

            // ═══════════════════════════════════════
            // BOTÓN PRINCIPAL
            // ═══════════════════════════════════════
            FloatingActionButton(
                onClick = {
                    val nuevoEstado = when (estadoFab) {
                        EstadoFabSeleccion.COLAPSADO -> EstadoFabSeleccion.EXPANDIDO
                        EstadoFabSeleccion.EXPANDIDO -> EstadoFabSeleccion.COLAPSADO
                    }
                    onCambiarEstado(nuevoEstado)
                },
                containerColor = FabColors.neonPrimario,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp, pressedElevation = 12.dp, hoveredElevation = 10.dp
                ),
                modifier = Modifier.shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = FabColors.neonPrimario.copy(alpha = 0.4f),
                    spotColor = FabColors.neonPrimario.copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (estadoFab == EstadoFabSeleccion.COLAPSADO) "Abrir opciones de selección"
                    else "Cerrar opciones",
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotacionIcono)
                )
            }
        }
    }
}

// ==========================================
// SUB-COMPONENTES
// ==========================================

/**
 * FAB secundario con etiqueta y animación de entrada
 */
@Composable
private fun FabSecundarioAnimado(
    visible: Boolean,
    icono: ImageVector,
    etiqueta: String,
    descripcion: String,
    color: Color,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = visible, enter = scaleIn(
            animationSpec = spring(
                stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy
            )
        ) + fadeIn(), exit = scaleOut() + fadeOut()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Etiqueta de texto
            Text(
                text = etiqueta,
                color = FabColors.textoSecundario,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(
                        color = FabColors.fondoOscuro.copy(alpha = 0.8f), shape = CircleShape
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            // Botón pequeño
            SmallFloatingActionButton(
                onClick = onClick,
                containerColor = color,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp, pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = descripcion,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ==========================================
// VERSIÓN INTEGRADA CON ESTADO DE BIBLIOTECA
// ==========================================

/**
 * 🎯 FAB INTEGRADO CON BibliotecaEstado
 *
 * Versión que maneja internamente el estado del FAB
 * y se conecta directamente con los eventos de la biblioteca.
 */
@Composable
fun FabSeleccionBiblioteca(
    modifier: Modifier = Modifier,
    estadoBiblioteca: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit
) {
    var estadoFab by remember { mutableStateOf(EstadoFabSeleccion.COLAPSADO) }

    val visible =
        estadoBiblioteca.esModoSeleccion && estadoBiblioteca.cancionesSeleccionadas.isNotEmpty()

    val esVistaLista = estadoBiblioteca.cuerpoActual == TipoDeCuerpoBiblioteca.CANCIONES_DE_LISTA
    val esVistaFavoritos = estadoBiblioteca.cuerpoActual == TipoDeCuerpoBiblioteca.FAVORITOS

    // ✅ NUEVO: Calcular si TODAS las seleccionadas son favoritas
    val todasSeleccionadasSonFavoritas = remember(
        estadoBiblioteca.cancionesSeleccionadas, estadoBiblioteca.canciones
    ) {
        if (estadoBiblioteca.cancionesSeleccionadas.isEmpty()) {
            false
        } else {
            estadoBiblioteca.canciones.filter { it.cancion.idCancion in estadoBiblioteca.cancionesSeleccionadas }
                .all { it.esFavorita }
        }
    }

    BotonFlotanteSeleccion(
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
        onCancelarSeleccion = {
            onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccion)
        })
}

// ==========================================
// PREVIEWS
// ==========================================

