package com.example.freeplayerm.ui.features.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.data.local.entity.UserPreferencesEntity

private object SettingsColors {
   val neonPrimario = Color(0xFFD500F9)
   val neonSecundario = Color(0xFF7C4DFF)
   val fondoCard = Color(0xFF1A0B2E).copy(alpha = 0.6f)
   val fondoScreen = Color(0xFF0F0518)
   val textoSecundario = Color.White.copy(alpha = 0.7f)
   val bordeCard = Color.White.copy(alpha = 0.1f)
   val errorColor = Color(0xFFFF1744)
   val successColor = Color(0xFF00E676)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
   preferences: UserPreferencesEntity,
   onNavigateBack: () -> Unit,
   onUpdateTemaOscuro: (Boolean) -> Unit,
   onUpdateAnimaciones: (Boolean) -> Unit,
   onUpdateCalidadAudio: (String) -> Unit,
   onUpdateCrossfade: (Int) -> Unit,
   onUpdateReproduccionAutomatica: (Boolean) -> Unit,
   onUpdateNotificaciones: (Boolean) -> Unit,
   onUpdateSoloWifiStreaming: (Boolean) -> Unit,
   onUpdateNormalizarVolumen: (Boolean) -> Unit,
   onUpdateCacheSize: (Int) -> Unit,
   onRestaurarDefecto: () -> Unit
) {
   val scrollState = rememberScrollState()
   var showResetDialog by remember { mutableStateOf(false) }
   
   Box(
      modifier = Modifier
         .fillMaxSize()
         .background(
            Brush.verticalGradient(
               colors = listOf(
                  SettingsColors.fondoScreen,
                  SettingsColors.fondoScreen.copy(alpha = 0.8f),
                  Color.Black
               )
            )
         )
   ) {
      Column(modifier = Modifier.fillMaxSize()) {
         TopAppBar(
            title = { Text("Configuraciones", color = Color.White) },
            navigationIcon = {
               IconButton(onClick = onNavigateBack) {
                  Icon(
                     Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = "Atrás",
                     tint = Color.White
                  )
               }
            },
            colors = TopAppBarDefaults.topAppBarColors(
               containerColor = Color.Transparent
            )
         )
         
         Column(
            modifier = Modifier
               .fillMaxSize()
               .verticalScroll(scrollState)
               .padding(horizontal = 16.dp)
               .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
         ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sección: Apariencia
            SettingsSection(
               title = "Apariencia",
               icon = Icons.Default.Palette
            ) {
               SwitchSettingItem(
                  title = "Tema Oscuro",
                  description = "Activar modo oscuro en toda la aplicación",
                  checked = preferences.temaOscuro,
                  onCheckedChange = onUpdateTemaOscuro,
                  icon = Icons.Default.DarkMode
               )
               
               SwitchSettingItem(
                  title = "Animaciones",
                  description = "Habilitar transiciones y efectos visuales",
                  checked = preferences.animacionesHabilitadas,
                  onCheckedChange = onUpdateAnimaciones,
                  icon = Icons.Default.Animation
               )
            }
            
            // Sección: Reproducción
            SettingsSection(
               title = "Reproducción",
               icon = Icons.Default.PlayCircle
            ) {
               SwitchSettingItem(
                  title = "Reproducción Automática",
                  description = "Continuar con canciones similares al finalizar",
                  checked = preferences.reproduccionAutomatica,
                  onCheckedChange = onUpdateReproduccionAutomatica,
                  icon = Icons.Default.PlayArrow
               )
               
               SliderSettingItem(
                  title = "Crossfade",
                  description = "Transición suave entre canciones",
                  value = preferences.crossfadeMs.toFloat(),
                  valueRange = 0f..10000f,
                  steps = 19,
                  onValueChange = { onUpdateCrossfade(it.toInt()) },
                  valueLabel = "${preferences.crossfadeMs / 1000}s",
                  icon = Icons.Default.BlurOn
               )
            }
            
            // Sección: Calidad de Audio
            SettingsSection(
               title = "Calidad de Audio",
               icon = Icons.Default.HighQuality
            ) {
               DropdownSettingItem(
                  title = "Calidad Preferida",
                  description = "Calidad de audio para reproducción",
                  selectedValue = preferences.calidadPreferida,
                  options = listOf(
                     UserPreferencesEntity.CALIDAD_BAJA to "Baja (96kbps)",
                     UserPreferencesEntity.CALIDAD_MEDIA to "Media (160kbps)",
                     UserPreferencesEntity.CALIDAD_ALTA to "Alta (320kbps)",
                     UserPreferencesEntity.CALIDAD_LOSSLESS to "Lossless (FLAC)"
                  ),
                  onValueChange = onUpdateCalidadAudio,
                  icon = Icons.Default.MusicNote
               )
               
               SwitchSettingItem(
                  title = "Normalizar Volumen",
                  description = "Igualar el volumen entre canciones",
                  checked = preferences.normalizarVolumen,
                  onCheckedChange = onUpdateNormalizarVolumen,
                  icon = Icons.Default.VolumeUp
               )
            }
            
            // Sección: Red y Datos
            SettingsSection(
               title = "Red y Datos",
               icon = Icons.Default.Wifi
            ) {
               SwitchSettingItem(
                  title = "Solo WiFi para Streaming",
                  description = "Bloquear reproducción en datos móviles",
                  checked = preferences.soloWifiStreaming,
                  onCheckedChange = onUpdateSoloWifiStreaming,
                  icon = Icons.Default.WifiOff
               )
               
               SliderSettingItem(
                  title = "Tamaño de Caché",
                  description = "Espacio reservado para caché de música",
                  value = preferences.cacheSizeMb.toFloat(),
                  valueRange = 100f..2000f,
                  steps = 18,
                  onValueChange = { onUpdateCacheSize(it.toInt()) },
                  valueLabel = "${preferences.cacheSizeMb} MB",
                  icon = Icons.Default.Storage
               )
            }
            
            // Sección: Notificaciones
            SettingsSection(
               title = "Notificaciones",
               icon = Icons.Default.Notifications
            ) {
               SwitchSettingItem(
                  title = "Notificaciones",
                  description = "Recibir notificaciones de la aplicación",
                  checked = preferences.notificacionesHabilitadas,
                  onCheckedChange = onUpdateNotificaciones,
                  icon = Icons.Default.NotificationsActive
               )
            }
            
            // Botón de Restaurar
            RestoreDefaultButton(onClick = { showResetDialog = true })
         }
      }
   }
   
   // Diálogo de confirmación
   if (showResetDialog) {
      AlertDialog(
         onDismissRequest = { showResetDialog = false },
         icon = {
            Icon(
               Icons.Default.RestartAlt,
               contentDescription = null,
               tint = SettingsColors.neonPrimario
            )
         },
         title = { Text("Restaurar Configuración") },
         text = { Text("¿Estás seguro de que deseas restaurar todas las configuraciones a sus valores predeterminados?") },
         confirmButton = {
            TextButton(
               onClick = {
                  onRestaurarDefecto()
                  showResetDialog = false
               }
            ) {
               Text("Restaurar", color = SettingsColors.errorColor)
            }
         },
         dismissButton = {
            TextButton(onClick = { showResetDialog = false }) {
               Text("Cancelar")
            }
         }
      )
   }
}

@Composable
private fun SettingsSection(
   title: String,
   icon: ImageVector,
   content: @Composable ColumnScope.() -> Unit
) {
   var expanded by remember { mutableStateOf(true) }
   val rotationAngle by animateFloatAsState(
      targetValue = if (expanded) 180f else 0f,
      animationSpec = tween(300)
   )
   
   Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
         containerColor = SettingsColors.fondoCard
      ),
      border = androidx.compose.foundation.BorderStroke(1.dp, SettingsColors.bordeCard)
   ) {
      Column {
         Row(
            modifier = Modifier
               .fillMaxWidth()
               .clickable { expanded = !expanded }
               .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
         ) {
            Row(
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
               Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = SettingsColors.neonPrimario,
                  modifier = Modifier.size(24.dp)
               )
               Text(
                  text = title,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
               )
            }
            
            Icon(
               imageVector = Icons.Default.KeyboardArrowDown,
               contentDescription = if (expanded) "Colapsar" else "Expandir",
               tint = SettingsColors.textoSecundario,
               modifier = Modifier.rotate(rotationAngle)
            )
         }
         
         AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
         ) {
            Column(
               modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 20.dp)
                  .padding(bottom = 20.dp),
               verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
               content()
            }
         }
      }
   }
}

@Composable
private fun SwitchSettingItem(
   title: String,
   description: String,
   checked: Boolean,
   onCheckedChange: (Boolean) -> Unit,
   icon: ImageVector
) {
   Row(
      modifier = Modifier
         .fillMaxWidth()
         .clip(RoundedCornerShape(12.dp))
         .clickable { onCheckedChange(!checked) }
         .padding(vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
   ) {
      Icon(
         imageVector = icon,
         contentDescription = null,
         tint = SettingsColors.neonPrimario,
         modifier = Modifier.size(20.dp)
      )
      
      Spacer(modifier = Modifier.width(12.dp))
      
      Column(modifier = Modifier.weight(1f)) {
         Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
         )
         Text(
            text = description,
            fontSize = 12.sp,
            color = SettingsColors.textoSecundario
         )
      }
      
      Switch(
         checked = checked,
         onCheckedChange = onCheckedChange,
         colors = SwitchDefaults.colors(
            checkedThumbColor = SettingsColors.neonPrimario,
            checkedTrackColor = SettingsColors.neonPrimario.copy(alpha = 0.5f)
         )
      )
   }
}

@Composable
private fun SliderSettingItem(
   title: String,
   description: String,
   value: Float,
   valueRange: ClosedFloatingPointRange<Float>,
   steps: Int,
   onValueChange: (Float) -> Unit,
   valueLabel: String,
   icon: ImageVector
) {
   Column(
      modifier = Modifier
         .fillMaxWidth()
         .padding(vertical = 8.dp)
   ) {
      Row(
         modifier = Modifier.fillMaxWidth(),
         verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.SpaceBetween
      ) {
         Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
         ) {
            Icon(
               imageVector = icon,
               contentDescription = null,
               tint = SettingsColors.neonPrimario,
               modifier = Modifier.size(20.dp)
            )
            Column {
               Text(
                  text = title,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Medium,
                  color = Color.White
               )
               Text(
                  text = description,
                  fontSize = 12.sp,
                  color = SettingsColors.textoSecundario
               )
            }
         }
         
         Text(
            text = valueLabel,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SettingsColors.neonPrimario
         )
      }
      
      Slider(
         value = value,
         onValueChange = onValueChange,
         valueRange = valueRange,
         steps = steps,
         colors = SliderDefaults.colors(
            thumbColor = SettingsColors.neonPrimario,
            activeTrackColor = SettingsColors.neonPrimario,
            inactiveTrackColor = SettingsColors.textoSecundario
         )
      )
   }
}

@Composable
private fun DropdownSettingItem(
   title: String,
   description: String,
   selectedValue: String,
   options: List<Pair<String, String>>,
   onValueChange: (String) -> Unit,
   icon: ImageVector
) {
   var expanded by remember { mutableStateOf(false) }
   
   Column(
      modifier = Modifier
         .fillMaxWidth()
         .padding(vertical = 8.dp)
   ) {
      Row(
         modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { expanded = true }
            .padding(vertical = 12.dp),
         verticalAlignment = Alignment.CenterVertically
      ) {
         Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SettingsColors.neonPrimario,
            modifier = Modifier.size(20.dp)
         )
         
         Spacer(modifier = Modifier.width(12.dp))
         
         Column(modifier = Modifier.weight(1f)) {
            Text(
               text = title,
               fontSize = 14.sp,
               fontWeight = FontWeight.Medium,
               color = Color.White
            )
            Text(
               text = description,
               fontSize = 12.sp,
               color = SettingsColors.textoSecundario
            )
         }
         
         Text(
            text = options.find { it.first == selectedValue }?.second ?: selectedValue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = SettingsColors.neonPrimario
         )
      }
      
      DropdownMenu(
         expanded = expanded,
         onDismissRequest = { expanded = false }
      ) {
         options.forEach { (value, label) ->
            DropdownMenuItem(
               text = { Text(label) },
               onClick = {
                  onValueChange(value)
                  expanded = false
               }
            )
         }
      }
   }
}

@Composable
private fun RestoreDefaultButton(onClick: () -> Unit) {
   Surface(
      modifier = Modifier
         .fillMaxWidth()
         .clip(RoundedCornerShape(16.dp))
         .clickable(onClick = onClick),
      shape = RoundedCornerShape(16.dp),
      color = Color.Transparent,
      border = androidx.compose.foundation.BorderStroke(1.dp, SettingsColors.errorColor)
   ) {
      Box(
         modifier = Modifier
            .background(
               Brush.horizontalGradient(
                  listOf(
                     SettingsColors.errorColor.copy(alpha = 0.2f),
                     Color(0xFFD50000).copy(alpha = 0.2f)
                  )
               )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
      ) {
         Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
         ) {
            Icon(
               imageVector = Icons.Default.RestartAlt,
               contentDescription = null,
               tint = SettingsColors.errorColor,
               modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
               text = "Restaurar Configuración Predeterminada",
               fontSize = 16.sp,
               fontWeight = FontWeight.Medium,
               color = SettingsColors.errorColor
            )
         }
      }
   }
}


