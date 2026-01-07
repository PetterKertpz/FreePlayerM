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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.features.library.BibliotecaEstado
import com.example.freeplayerm.ui.features.library.BibliotecaEvento
import com.example.freeplayerm.ui.features.library.TipoDeCuerpoBiblioteca

private object PlaylistFabColors {
   val NeonPrimary = Color(0xFFD500F9)
   val NeonCyan = Color(0xFF00E5FF)
   val NeonDanger = Color(0xFFFF1744)
   val DarkSurface = Color(0xFF1E1E1E).copy(alpha = 0.95f)
   val TextPrimary = Color.White
   val LabelBorder = Brush.horizontalGradient(
      colors = listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.05f))
   )
}

@Composable
fun PlaylistSelectionFab(
   modifier: Modifier = Modifier,
   estadoBiblioteca: BibliotecaEstado,
   onBibliotecaEvento: (BibliotecaEvento) -> Unit,
) {
   var estadoFab by remember { mutableStateOf(EstadoFabSeleccion.COLAPSADO) }
   
   val visible = estadoBiblioteca.esModoSeleccionListas &&
         estadoBiblioteca.listasSeleccionadas.isNotEmpty() &&
         estadoBiblioteca.cuerpoActual == TipoDeCuerpoBiblioteca.LISTAS
   
   val soloUnaSeleccionada = estadoBiblioteca.listasSeleccionadas.size == 1
   
   // Obtener la lista seleccionada si solo hay una
   val listaSeleccionada = if (soloUnaSeleccionada) {
      estadoBiblioteca.listas.find {
         it.idLista == estadoBiblioteca.listasSeleccionadas.first()
      }
   } else null
   
   val rotacionIcono by animateFloatAsState(
      targetValue = if (estadoFab == EstadoFabSeleccion.EXPANDIDO) 135f else 0f,
      animationSpec = spring(
         dampingRatio = Spring.DampingRatioMediumBouncy,
         stiffness = Spring.StiffnessLow,
      ),
      label = "fab_rotacion_playlist",
   )
   
   BackHandler(enabled = visible) {
      if (estadoFab == EstadoFabSeleccion.EXPANDIDO) {
         estadoFab = EstadoFabSeleccion.COLAPSADO
      } else {
         onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccionListas)
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
         verticalArrangement = Arrangement.spacedBy(16.dp),
         modifier = Modifier.padding(bottom = 8.dp, end = 8.dp),
      ) {
         // Eliminar listas
         PlaylistFabItem(
            visible = estadoFab == EstadoFabSeleccion.EXPANDIDO,
            icono = Icons.Default.Delete,
            texto = if (soloUnaSeleccionada) "Eliminar lista"
            else "Eliminar (${estadoBiblioteca.listasSeleccionadas.size})",
            colorFondo = PlaylistFabColors.NeonDanger,
            onClick = {
               onBibliotecaEvento(BibliotecaEvento.EliminarListasSeleccionadas)
               estadoFab = EstadoFabSeleccion.COLAPSADO
            },
         )
         
         // Editar lista (solo si hay una seleccionada)
         PlaylistFabItem(
            visible = estadoFab == EstadoFabSeleccion.EXPANDIDO && soloUnaSeleccionada,
            icono = Icons.Default.Edit,
            texto = "Editar lista",
            colorFondo = PlaylistFabColors.NeonCyan,
            colorIcono = Color.Black,
            onClick = {
               listaSeleccionada?.let {
                  onBibliotecaEvento(BibliotecaEvento.AbrirDialogoEditarListaSeleccionada(it))
               }
               estadoFab = EstadoFabSeleccion.COLAPSADO
            },
         )
         
         // FAB principal
         FloatingActionButton(
            onClick = {
               estadoFab = if (estadoFab == EstadoFabSeleccion.COLAPSADO) {
                  EstadoFabSeleccion.EXPANDIDO
               } else {
                  EstadoFabSeleccion.COLAPSADO
               }
            },
            containerColor = PlaylistFabColors.NeonPrimary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
               .size(64.dp)
               .shadow(
                  elevation = 12.dp,
                  shape = CircleShape,
                  spotColor = PlaylistFabColors.NeonPrimary,
                  ambientColor = PlaylistFabColors.NeonPrimary,
               ),
         ) {
            Icon(
               imageVector = Icons.Default.Add,
               contentDescription = "Acciones de lista",
               modifier = Modifier.size(32.dp).rotate(rotacionIcono),
            )
         }
      }
   }
}

@Composable
private fun PlaylistFabItem(
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
         modifier = Modifier.padding(end = 4.dp),
      ) {
         Box(
            modifier = Modifier
               .clip(RoundedCornerShape(8.dp))
               .background(PlaylistFabColors.DarkSurface)
               .border(1.dp, PlaylistFabColors.LabelBorder, RoundedCornerShape(8.dp))
               .padding(horizontal = 12.dp, vertical = 6.dp)
         ) {
            Text(
               text = texto,
               color = PlaylistFabColors.TextPrimary,
               style = MaterialTheme.typography.labelLarge,
               fontWeight = FontWeight.Medium,
            )
         }
         
         Spacer(modifier = Modifier.width(16.dp))
         
         SmallFloatingActionButton(
            onClick = onClick,
            containerColor = colorFondo,
            contentColor = colorIcono,
            elevation = FloatingActionButtonDefaults.elevation(
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