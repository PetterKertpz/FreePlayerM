package com.example.freeplayerm.ui.features.library.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeplayerm.data.local.entity.UserEntity
import com.example.freeplayerm.ui.features.library.BibliotecaEstado
import com.example.freeplayerm.ui.features.library.BibliotecaEvento
import com.example.freeplayerm.ui.features.library.TipoDeCuerpoBiblioteca

// ==========================================
// 🎨 COLORES TEMÁTICOS GALÁCTICOS
// ==========================================
private object EncabezadoColors {
   val neonPrimario = Color(0xFFD500F9)
   val neonSecundario = Color(0xFF7C4DFF)
   val fondoChipSeleccionado =
      Brush.horizontalGradient(
         colors = listOf(Color(0xFFD500F9).copy(alpha = 0.3f), Color(0xFF7C4DFF).copy(alpha = 0.3f))
      )
   val fondoChipNormal = Color.White.copy(alpha = 0.05f)
   val bordeSeleccionado = Color(0xFFD500F9)
   val bordeNormal = Color.White.copy(alpha = 0.1f)
   val textoNormal = Color.White.copy(alpha = 0.7f)
   val textoSeleccionado = Color.White
}

// ==========================================
// 🧭 DATOS PARA MENÚ DE NAVEGACIÓN
// ==========================================
private val menusNavegacion =
   listOf(
      "Canciones" to TipoDeCuerpoBiblioteca.CANCIONES,
      "Listas" to TipoDeCuerpoBiblioteca.LISTAS,
      "Álbumes" to TipoDeCuerpoBiblioteca.ALBUMES,
      "Artistas" to TipoDeCuerpoBiblioteca.ARTISTAS,
      "Géneros" to TipoDeCuerpoBiblioteca.GENEROS,
      "Favoritos" to TipoDeCuerpoBiblioteca.FAVORITOS,
   )

// ==========================================
// 🧩 COMPONENTE PRINCIPAL
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderSection(
   usuario: UserEntity?,
   cuerpoActual: TipoDeCuerpoBiblioteca,
   escaneoManualEnProgreso: Boolean,
   esModoSeleccion: Boolean,
   cantidadSeleccionada: Int,
   totalCanciones: Int,
   textoBusqueda: String,
   onSearchChange: (String) -> Unit,
   onMenuClick: (TipoDeCuerpoBiblioteca) -> Unit,
   onReescanearClick: () -> Unit,
   onSeleccionarTodo: () -> Unit,
   onDesactivarSeleccion: () -> Unit,
   onAvatarClick: () -> Unit = {},
) {
   // Estado local para mostrar/ocultar barra de búsqueda expandida
   var mostrarBusqueda by remember { mutableStateOf(false) }

   Column(
      modifier =
         Modifier.fillMaxWidth()
            .background(
               Brush.verticalGradient(
                  colors =
                     listOf(
                        Color(0xFF0F0518), // Fondo oscuro profundo
                        Color(0xFF0F0518).copy(alpha = 0.95f),
                        Color.Transparent,
                     )
               )
            )
            .padding(bottom = 16.dp)
            .animateContentSize()
   ) {
      // ═══════════════════════════════════════
      // 1. TOP BAR (Título + Acciones)
      // ═══════════════════════════════════════
      TopAppBar(
         title = {
            if (!mostrarBusqueda) {
               Text(
                  text = "FreePlayer",
                  color = Color.White,
                  fontSize = 28.sp,
                  fontWeight = FontWeight.Bold,
                  style =
                     MaterialTheme.typography.displaySmall.copy(
                        shadow =
                           Shadow(
                              color = EncabezadoColors.neonPrimario.copy(alpha = 0.8f),
                              blurRadius = 25f,
                           )
                     ),
               )
            } else {
               // Barra de búsqueda expandida
               GalacticSearchBar(
                  query = textoBusqueda,
                  onQueryChange = onSearchChange,
                  onClose = {
                     mostrarBusqueda = false
                     onSearchChange("") // Limpiar al cerrar
                  },
               )
            }
         },
         actions = {
            if (!mostrarBusqueda) {
               // 🔍 Botón Buscar
               IconButton(onClick = { mostrarBusqueda = true }) {
                  Icon(
                     imageVector = Icons.Default.Search,
                     contentDescription = "Buscar",
                     tint = Color.White,
                  )
               }

               // 🔄 Botón Refrescar
               IconButton(onClick = onReescanearClick, enabled = !escaneoManualEnProgreso) {
                  if (escaneoManualEnProgreso) {
                     CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = EncabezadoColors.neonPrimario,
                        strokeWidth = 2.dp,
                     )
                  } else {
                     Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refrescar",
                        tint = Color.White,
                     )
                  }
               }

               Spacer(modifier = Modifier.width(8.dp))

               // 👤 Avatar
               Box(
                  modifier =
                     Modifier.padding(end = 16.dp)
                        .size(36.dp)
                        .border(1.dp, EncabezadoColors.neonPrimario, CircleShape)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .clickable { onAvatarClick() } // Nueva acción
               ) {
                  AsyncImage(
                     model = usuario?.fotoPerfil ?: "https://i.pravatar.cc/150?img=11",
                     contentDescription = "Perfil",
                     contentScale = ContentScale.Crop,
                     modifier = Modifier.fillMaxSize(),
                  )
               }
            }
         },
         colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      )

      // ═══════════════════════════════════════
      // 2. CONTROLES INFERIORES (Navegación / Selección)
      // ═══════════════════════════════════════
      AnimatedContent(
         targetState = esModoSeleccion,
         transitionSpec = {
            (slideInVertically { height -> height } + fadeIn()) togetherWith
               (slideOutVertically { height -> -height } + fadeOut())
         },
         label = "MenuTransition",
      ) { enModoSeleccion ->
         if (enModoSeleccion) {
            BarraSeleccionCompacta(
               cantidadSeleccionada = cantidadSeleccionada,
               totalCanciones = totalCanciones,
               onSeleccionarTodo = onSeleccionarTodo,
               onCerrarSeleccion = onDesactivarSeleccion,
            )
         } else {
            ChipsNavegacion(cuerpoActual = cuerpoActual, onMenuClick = onMenuClick)
         }
      }
   }
}

// ==========================================
// 🛠️ SUB-COMPONENTES
// ==========================================

@Composable
private fun GalacticSearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
   val focusRequester = remember { FocusRequester() }
   val focusManager = LocalFocusManager.current

   LaunchedEffect(Unit) { focusRequester.requestFocus() }

   Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
      BasicTextField(
         value = query,
         onValueChange = onQueryChange,
         modifier = Modifier.weight(1f).focusRequester(focusRequester),
         textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
         cursorBrush = SolidColor(EncabezadoColors.neonPrimario),
         singleLine = true,
         keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
         keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
         decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
               if (query.isEmpty()) {
                  Text("Buscar...", color = Color.White.copy(alpha = 0.5f), fontSize = 18.sp)
               }
               innerTextField()
            }
         },
      )

      IconButton(onClick = onClose) {
         Icon(Icons.Default.Close, contentDescription = "Cerrar búsqueda", tint = Color.White)
      }
   }
}

@Composable
private fun ChipsNavegacion(
   cuerpoActual: TipoDeCuerpoBiblioteca,
   onMenuClick: (TipoDeCuerpoBiblioteca) -> Unit,
) {
   LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(horizontal = 16.dp),
   ) {
      items(menusNavegacion) { (nombreMenu, tipo) ->
         val seleccionado = cuerpoActual == tipo

         Box(
            modifier =
               Modifier.clip(RoundedCornerShape(50))
                  .background(
                     if (seleccionado) EncabezadoColors.fondoChipSeleccionado
                     else SolidColor(EncabezadoColors.fondoChipNormal)
                  )
                  .border(
                     width = 1.dp,
                     color =
                        if (seleccionado) EncabezadoColors.bordeSeleccionado
                        else EncabezadoColors.bordeNormal,
                     shape = RoundedCornerShape(50),
                  )
                  .clickable { onMenuClick(tipo) }
                  .padding(horizontal = 16.dp, vertical = 8.dp)
         ) {
            Text(
               text = nombreMenu,
               color =
                  if (seleccionado) EncabezadoColors.textoSeleccionado
                  else EncabezadoColors.textoNormal,
               fontSize = 14.sp,
               fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal,
               style =
                  if (seleccionado)
                     MaterialTheme.typography.bodyMedium.copy(
                        shadow = Shadow(EncabezadoColors.neonPrimario, blurRadius = 10f)
                     )
                  else MaterialTheme.typography.bodyMedium,
            )
         }
      }
   }
}

@Composable
fun BarraSeleccionCompacta(
   cantidadSeleccionada: Int,
   totalCanciones: Int,
   onSeleccionarTodo: () -> Unit,
   onCerrarSeleccion: () -> Unit,
) {
   val todoSeleccionado = cantidadSeleccionada == totalCanciones && totalCanciones > 0

   Surface(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
      shape = RoundedCornerShape(50),
      color = Color(0xFF2A0F35).copy(alpha = 0.9f),
      border = BorderStroke(1.dp, EncabezadoColors.neonPrimario.copy(alpha = 0.5f)),
      shadowElevation = 8.dp,
   ) {
      Row(
         modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
         verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.SpaceBetween,
      ) {
         IconButton(onClick = onCerrarSeleccion) {
            Icon(
               imageVector = Icons.Default.Close,
               contentDescription = "Cerrar",
               tint = Color.White.copy(alpha = 0.7f),
            )
         }

         Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
               text = "$cantidadSeleccionada",
               color = EncabezadoColors.neonPrimario,
               fontWeight = FontWeight.Bold,
               fontSize = 18.sp,
            )
            Text(text = " seleccionadas", color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
         }

         IconButton(onClick = onSeleccionarTodo) {
            Icon(
               imageVector =
                  if (todoSeleccionado) Icons.Default.CheckBox
                  else Icons.Default.CheckBoxOutlineBlank,
               contentDescription = "Seleccionar todo",
               tint =
                  if (todoSeleccionado) EncabezadoColors.neonPrimario
                  else Color.White.copy(alpha = 0.7f),
            )
         }
      }
   }
}

/** Wrapper para usar con el ViewModel State */
@Composable
fun SeccionEncabezadoConEstado(
   estadoBiblioteca: BibliotecaEstado,
   onBibliotecaEvento: (BibliotecaEvento) -> Unit,
) {
   HeaderSection(
      usuario = estadoBiblioteca.usuarioActual,
      cuerpoActual = estadoBiblioteca.cuerpoActual,
      escaneoManualEnProgreso = estadoBiblioteca.escaneoManualEnProgreso,
      esModoSeleccion = estadoBiblioteca.esModoSeleccion,
      cantidadSeleccionada = estadoBiblioteca.cancionesSeleccionadas.size,
      totalCanciones = estadoBiblioteca.canciones.size,
      textoBusqueda = estadoBiblioteca.textoDeBusqueda,
      onSearchChange = { onBibliotecaEvento(BibliotecaEvento.TextoDeBusquedaCambiado(it)) },
      onMenuClick = { onBibliotecaEvento(BibliotecaEvento.CambiarCuerpo(it)) },
      onReescanearClick = { onBibliotecaEvento(BibliotecaEvento.ForzarReescaneo) },
      onSeleccionarTodo = { onBibliotecaEvento(BibliotecaEvento.SeleccionarTodo) },
      onDesactivarSeleccion = { onBibliotecaEvento(BibliotecaEvento.DesactivarModoSeleccion) },
      onAvatarClick = { onBibliotecaEvento(BibliotecaEvento.AbrirPerfil) },
   )
}

// ==========================================
// 📸 PREVIEWS
// ==========================================

@Preview(name = "1. Encabezado - Normal", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewEncabezadoNormal() {
   HeaderSection(
      usuario =
         UserEntity(
            idUsuario = 1,
            nombreUsuario = "User",
            correo = "a@a.com",
            tipoAutenticacion = "LOCAL",
            contraseniaHash = "",
         ),
      cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
      escaneoManualEnProgreso = false,
      esModoSeleccion = false,
      cantidadSeleccionada = 0,
      totalCanciones = 100,
      textoBusqueda = "",
      onSearchChange = {},
      onMenuClick = {},
      onReescanearClick = {},
      onSeleccionarTodo = {},
      onDesactivarSeleccion = {},
   )
}

@Preview(name = "2. Encabezado - Selección", showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun PreviewEncabezadoSeleccion() {
   HeaderSection(
      usuario = null,
      cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
      escaneoManualEnProgreso = false,
      esModoSeleccion = true,
      cantidadSeleccionada = 15,
      totalCanciones = 100,
      textoBusqueda = "",
      onSearchChange = {},
      onMenuClick = {},
      onReescanearClick = {},
      onSeleccionarTodo = {},
      onDesactivarSeleccion = {},
   )
}
