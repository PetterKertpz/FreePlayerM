// ui/features/biblioteca/components/SeccionEncabezado.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEstado
import com.example.freeplayerm.ui.features.biblioteca.BibliotecaEvento
import com.example.freeplayerm.ui.features.biblioteca.TipoDeCuerpoBiblioteca

// ==========================================
// COLORES TEMÁTICOS
// ==========================================
private object EncabezadoColors {
    val neonPrimario = Color(0xFFD500F9)
    val neonSecundario = Color(0xFF7C4DFF)
    val fondoChipSeleccionado = Color(0xFFD500F9).copy(alpha = 0.2f)
    val fondoChipNormal = Color.White.copy(alpha = 0.05f)
    val bordeSeleccionado = Color(0xFFD500F9)
    val bordeNormal = Color.White.copy(alpha = 0.2f)
}


// ==========================================
// DATOS PARA MENÚ DE NAVEGACIÓN
// ==========================================
private val menusNavegacion = listOf(
    "Canciones" to TipoDeCuerpoBiblioteca.CANCIONES,
    "Listas" to TipoDeCuerpoBiblioteca.LISTAS,
    "Álbumes" to TipoDeCuerpoBiblioteca.ALBUMES,
    "Artistas" to TipoDeCuerpoBiblioteca.ARTISTAS,
    "Géneros" to TipoDeCuerpoBiblioteca.GENEROS,
    "Favoritos" to TipoDeCuerpoBiblioteca.FAVORITOS
)

// ==========================================
// COMPONENTE PRINCIPAL
// ==========================================

/**
 * 🎯 SECCIÓN ENCABEZADO REFACTORIZADA
 *
 * Estructura:
 * - TopAppBar (SIEMPRE VISIBLE): Título "FreePlayer" + Refresh + Avatar
 * - Zona inferior (ALTERNANTE):
 *   - Modo normal: Chips de navegación (Canciones, Listas, Álbumes...)
 *   - Modo selección: Controles de selección (Cerrar, Contador, Seleccionar todo)
 *
 * Las acciones de agregar a listas/favoritos se mueven al FAB flotante.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionEncabezado(
    usuario: UsuarioEntity?,
    cuerpoActual: TipoDeCuerpoBiblioteca,
    escaneoManualEnProgreso: Boolean,
    esModoSeleccion: Boolean,
    cantidadSeleccionada: Int,
    totalCanciones: Int,
    onMenuClick: (TipoDeCuerpoBiblioteca) -> Unit,
    onReescanearClick: () -> Unit,
    onSeleccionarTodo: () -> Unit,
    onDesactivarSeleccion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.8f),
                        Color.Transparent
                    )
                )
            )
            .padding(bottom = 16.dp)
    ) {
        // ═══════════════════════════════════════
        // SECCIÓN SUPERIOR: TopAppBar (SIEMPRE VISIBLE)
        // ═══════════════════════════════════════
        TopAppBar(
            title = {
                Text(
                    text = "FreePlayer",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displaySmall.copy(
                        shadow = Shadow(
                            color = EncabezadoColors.neonPrimario.copy(alpha = 0.6f),
                            blurRadius = 20f
                        )
                    )
                )
            },
            actions = {
                // Botón Refrescar
                IconButton(
                    onClick = onReescanearClick,
                    enabled = !escaneoManualEnProgreso,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    if (escaneoManualEnProgreso) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = EncabezadoColors.neonPrimario,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar biblioteca",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Avatar del usuario
                AsyncImage(
                    model = usuario?.fotoPerfil,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, EncabezadoColors.neonPrimario, CircleShape)
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // ═══════════════════════════════════════
        // SECCIÓN INFERIOR: Chips o Controles de Selección
        // ═══════════════════════════════════════
        AnimatedContent(
            targetState = esModoSeleccion,
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            label = "transicion_menu_seleccion"
        ) { enModoSeleccion ->
            if (enModoSeleccion) {
                // MODO SELECCIÓN: Controles compactos
                BarraSeleccionCompacta(
                    cantidadSeleccionada = cantidadSeleccionada,
                    totalCanciones = totalCanciones,
                    onSeleccionarTodo = onSeleccionarTodo,
                    onCerrarSeleccion = onDesactivarSeleccion
                )
            } else {
                // MODO NORMAL: Chips de navegación
                ChipsNavegacion(
                    cuerpoActual = cuerpoActual,
                    onMenuClick = onMenuClick
                )
            }
        }
    }
}

// ==========================================
// SUB-COMPONENTES
// ==========================================

/**
 * 📍 CHIPS DE NAVEGACIÓN
 * LazyRow horizontal con los menús de la biblioteca
 */
@Composable
private fun ChipsNavegacion(
    cuerpoActual: TipoDeCuerpoBiblioteca,
    onMenuClick: (TipoDeCuerpoBiblioteca) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(menusNavegacion) { (nombreMenu, tipo) ->
            val seleccionado = cuerpoActual == tipo

            Surface(
                onClick = { onMenuClick(tipo) },
                shape = RoundedCornerShape(50),
                color = if (seleccionado)
                    EncabezadoColors.fondoChipSeleccionado
                else
                    EncabezadoColors.fondoChipNormal,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (seleccionado)
                        EncabezadoColors.bordeSeleccionado
                    else
                        EncabezadoColors.bordeNormal
                )
            ) {
                Text(
                    text = nombreMenu,
                    color = if (seleccionado) Color.White else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontSize = 14.sp,
                    fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * 🎯 BARRA DE SELECCIÓN COMPACTA
 *
 * Reemplaza los chips de navegación cuando está en modo selección.
 * Contiene: [Cerrar] [Contador] [Seleccionar Todo]
 */
@Composable
fun BarraSeleccionCompacta(
    cantidadSeleccionada: Int,
    totalCanciones: Int,
    onSeleccionarTodo: () -> Unit,
    onCerrarSeleccion: () -> Unit
) {
    val todoSeleccionado = cantidadSeleccionada == totalCanciones && totalCanciones > 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(50),
        color = Color(0xFF2A0F35).copy(alpha = 0.9f),
        border = BorderStroke(1.dp, EncabezadoColors.neonPrimario.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón: Cerrar selección
            IconButton(onClick = onCerrarSeleccion) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar modo selección",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }

            // Contador central
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "$cantidadSeleccionada",
                    color = EncabezadoColors.neonPrimario,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = " / $totalCanciones seleccionadas",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

            // Botón: Seleccionar todo
            IconButton(onClick = onSeleccionarTodo) {
                Icon(
                    imageVector = if (todoSeleccionado)
                        Icons.Default.CheckBox
                    else
                        Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = if (todoSeleccionado)
                        "Deseleccionar todo" else "Seleccionar todo",
                    tint = if (todoSeleccionado)
                        EncabezadoColors.neonPrimario
                    else
                        Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ==========================================
// VERSIÓN SIMPLIFICADA (RECIBE ESTADO COMPLETO)
// ==========================================

/**
 * 🎯 VERSIÓN QUE RECIBE BibliotecaEstado DIRECTAMENTE
 *
 * Wrapper conveniente para usar en PantallaBiblioteca
 */
@Composable
fun SeccionEncabezadoConEstado(
    estadoBiblioteca: BibliotecaEstado,
    onBibliotecaEvento: (BibliotecaEvento) -> Unit
) {
    SeccionEncabezado(
        usuario = estadoBiblioteca.usuarioActual,
        cuerpoActual = estadoBiblioteca.cuerpoActual,
        escaneoManualEnProgreso = estadoBiblioteca.escaneoManualEnProgreso,
        esModoSeleccion = estadoBiblioteca.esModoSeleccion,
        cantidadSeleccionada = estadoBiblioteca.cancionesSeleccionadas.size,
        totalCanciones = estadoBiblioteca.canciones.size,
        onMenuClick = { onBibliotecaEvento(BibliotecaEvento.CambiarCuerpo(it)) },
        onReescanearClick = { onBibliotecaEvento(BibliotecaEvento.ForzarReescaneo) },
        onSeleccionarTodo = { onBibliotecaEvento(BibliotecaEvento.SeleccionarTodo) },
        onDesactivarSeleccion = { onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccion) }
    )
}

// ==========================================
// PREVIEWS
// ==========================================

// --- DATOS MOCK PARA PREVIEWS ---
private object EncabezadoPreviewMocks {
    val usuarioMock = UsuarioEntity(
        idUsuario = 1,
        nombreUsuario = "Astronauta",
        correo = "astro@freeplayer.com",
        tipoAutenticacion = "LOCAL",
        contraseniaHash = "hash123"
    )
}

@Preview(name = "1. Encabezado - Modo Normal (Canciones)", showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun PreviewEncabezadoNormalCanciones() {
    SeccionEncabezado(
        usuario = EncabezadoPreviewMocks.usuarioMock,
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
        escaneoManualEnProgreso = false,
        esModoSeleccion = false,
        cantidadSeleccionada = 0,
        totalCanciones = 150,
        onMenuClick = {},
        onReescanearClick = {},
        onSeleccionarTodo = {},
        onDesactivarSeleccion = {}
    )
}

@Preview(name = "2. Encabezado - Modo Normal (Álbumes)", showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun PreviewEncabezadoNormalAlbumes() {
    SeccionEncabezado(
        usuario = EncabezadoPreviewMocks.usuarioMock,
        cuerpoActual = TipoDeCuerpoBiblioteca.ALBUMES,
        escaneoManualEnProgreso = false,
        esModoSeleccion = false,
        cantidadSeleccionada = 0,
        totalCanciones = 150,
        onMenuClick = {},
        onReescanearClick = {},
        onSeleccionarTodo = {},
        onDesactivarSeleccion = {}
    )
}

@Preview(name = "3. Encabezado - Escaneando", showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun PreviewEncabezadoEscaneando() {
    SeccionEncabezado(
        usuario = EncabezadoPreviewMocks.usuarioMock,
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
        escaneoManualEnProgreso = true,
        esModoSeleccion = false,
        cantidadSeleccionada = 0,
        totalCanciones = 150,
        onMenuClick = {},
        onReescanearClick = {},
        onSeleccionarTodo = {},
        onDesactivarSeleccion = {}
    )
}

@Preview(name = "4. Encabezado - Modo Selección (5 de 150)", showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun PreviewEncabezadoSeleccionParcial() {
    SeccionEncabezado(
        usuario = EncabezadoPreviewMocks.usuarioMock,
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
        escaneoManualEnProgreso = false,
        esModoSeleccion = true,
        cantidadSeleccionada = 5,
        totalCanciones = 150,
        onMenuClick = {},
        onReescanearClick = {},
        onSeleccionarTodo = {},
        onDesactivarSeleccion = {}
    )
}

@Preview(name = "5. Encabezado - Modo Selección (Todo)", showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun PreviewEncabezadoSeleccionTotal() {
    SeccionEncabezado(
        usuario = EncabezadoPreviewMocks.usuarioMock,
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
        escaneoManualEnProgreso = false,
        esModoSeleccion = true,
        cantidadSeleccionada = 150,
        totalCanciones = 150,
        onMenuClick = {},
        onReescanearClick = {},
        onSeleccionarTodo = {},
        onDesactivarSeleccion = {}
    )
}

@Preview(name = "6. Encabezado - Sin Usuario", showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun PreviewEncabezadoSinUsuario() {
    SeccionEncabezado(
        usuario = null,
        cuerpoActual = TipoDeCuerpoBiblioteca.FAVORITOS,
        escaneoManualEnProgreso = false,
        esModoSeleccion = false,
        cantidadSeleccionada = 0,
        totalCanciones = 0,
        onMenuClick = {},
        onReescanearClick = {},
        onSeleccionarTodo = {},
        onDesactivarSeleccion = {}
    )
}

@Preview(name = "7. Barra Selección Compacta - Aislada", showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun PreviewBarraSeleccionCompacta() {
    BarraSeleccionCompacta(
        cantidadSeleccionada = 23,
        totalCanciones = 150,
        onSeleccionarTodo = {},
        onCerrarSeleccion = {}
    )
}

@Preview(name = "8. Chips Navegación - Aislados", showBackground = true, backgroundColor = 0xFF0D0D0D)
@Composable
private fun PreviewChipsNavegacion() {
    ChipsNavegacion(
        cuerpoActual = TipoDeCuerpoBiblioteca.LISTAS,
        onMenuClick = {}
    )
}