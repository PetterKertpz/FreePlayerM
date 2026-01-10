package com.example.freeplayerm.ui.features.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.freeplayerm.data.local.entity.UserEntity
import java.util.*

// Colores del tema galáctico
private object ProfileColors {
   val neonPrimario = Color(0xFFD500F9)
   val neonSecundario = Color(0xFF7C4DFF)
   val fondoCard = Color(0xFF1A0B2E).copy(alpha = 0.6f)
   val fondoScreen = Color(0xFF0F0518)
   val textoSecundario = Color.White.copy(alpha = 0.7f)
   val bordeCard = Color.White.copy(alpha = 0.1f)
}

// Vista principal de perfil conectada al ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
   viewModel: ProfileViewModel = hiltViewModel(),
   onNavigateBack: () -> Unit,
   onNavigateToSettings: () -> Unit,
   onNavigateToEditProfile: () -> Unit = {},
) {
   // Observar usuario reactivamente
   val usuario by viewModel.usuario.collectAsState()

   // Registrar última sesión al entrar al perfil
   LaunchedEffect(Unit) { viewModel.registrarInicioSesion() }

   // Si no hay usuario, mostrar pantalla de carga
   if (usuario == null) {
      ProfileLoadingScreen(onNavigateBack = onNavigateBack)
      return
   }

   // Delegar a Content stateless
   ProfileContent(
      usuario = usuario!!,
      onNavigateBack = onNavigateBack,
      onNavigateToSettings = onNavigateToSettings,
      onEditProfile = onNavigateToEditProfile,
      onLogout = { viewModel.cerrarSesion() },
   )
}

// Pantalla de carga mientras se obtiene el usuario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileLoadingScreen(onNavigateBack: () -> Unit) {
   Box(
      modifier =
         Modifier.fillMaxSize()
            .background(
               Brush.verticalGradient(
                  colors =
                     listOf(
                        ProfileColors.fondoScreen,
                        ProfileColors.fondoScreen.copy(alpha = 0.8f),
                        Color.Black,
                     )
               )
            )
   ) {
      TopAppBar(
         title = { Text("Mi Perfil", color = Color.White) },
         navigationIcon = {
            IconButton(onClick = onNavigateBack) {
               Icon(
                  Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Atrás",
                  tint = Color.White,
               )
            }
         },
         colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      )

      CircularProgressIndicator(
         modifier = Modifier.align(Alignment.Center),
         color = ProfileColors.neonPrimario,
      )
   }
}

// Content stateless (renombrar el ProfileScreen original)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
   usuario: UserEntity,
   onNavigateBack: () -> Unit,
   onNavigateToSettings: () -> Unit,
   onEditProfile: () -> Unit,
   onLogout: () -> Unit,
) {
   val scrollState = rememberScrollState()

   Box(
      modifier =
         Modifier.fillMaxSize()
            .background(
               Brush.verticalGradient(
                  colors =
                     listOf(
                        ProfileColors.fondoScreen,
                        ProfileColors.fondoScreen.copy(alpha = 0.8f),
                        Color.Black,
                     )
               )
            )
   ) {
      Column(modifier = Modifier.fillMaxSize()) {
         // Top Bar
         TopAppBar(
            title = { Text("Mi Perfil", color = Color.White) },
            navigationIcon = {
               IconButton(onClick = onNavigateBack) {
                  Icon(
                     Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = "Atrás",
                     tint = Color.White,
                  )
               }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
         )

         // Contenido scrolleable
         Column(
            modifier =
               Modifier.fillMaxSize()
                  .verticalScroll(scrollState)
                  .padding(horizontal = 16.dp)
                  .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
         ) {
            Spacer(modifier = Modifier.height(24.dp))
            ProfileHeader(usuario = usuario)
            Spacer(modifier = Modifier.height(32.dp))
            StatsSection(usuario = usuario)
            Spacer(modifier = Modifier.height(24.dp))
            AccountInfoSection(usuario = usuario)
            Spacer(modifier = Modifier.height(24.dp))
            QuickActionsSection(
               onNavigateToSettings = onNavigateToSettings,
               onEditProfile = onEditProfile,
               onLogout = onLogout,
            )
         }
      }
   }
}

// Header con avatar grande y nombre
@Composable
private fun ProfileHeader(usuario: UserEntity) {
   Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
      // Avatar con borde neón
      Box(
         modifier =
            Modifier.size(120.dp)
               .border(3.dp, ProfileColors.neonPrimario, CircleShape)
               .padding(4.dp)
               .clip(CircleShape)
               .background(Color.DarkGray)
      ) {
         AsyncImage(
            model =
               usuario.fotoPerfil ?: "https://ui-avatars.com/api/?name=${usuario.nombreUsuario}",
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
         )
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Nombre
      Text(
         text = usuario.nombreParaMostrar(),
         fontSize = 28.sp,
         fontWeight = FontWeight.Bold,
         color = Color.White,
      )

      // Username
      Text(
         text = "@${usuario.nombreUsuario}",
         fontSize = 16.sp,
         color = ProfileColors.textoSecundario,
      )

      // Badge de tipo de cuenta
      Spacer(modifier = Modifier.height(8.dp))
      AccountTypeBadge(tipo = usuario.tipoAutenticacion)
   }
}

// Badge del tipo de autenticación
@Composable
private fun AccountTypeBadge(tipo: String) {
   val (icono, texto, color) =
      when (tipo) {
         UserEntity.TIPO_GOOGLE ->
            Triple(Icons.Default.AccountCircle, "Cuenta Google", ProfileColors.neonSecundario)
         UserEntity.TIPO_LOCAL ->
            Triple(Icons.Default.Person, "Cuenta Local", ProfileColors.neonPrimario)
         else -> Triple(Icons.Default.Shield, tipo, Color.Gray)
      }

   Surface(
      shape = RoundedCornerShape(16.dp),
      color = color.copy(alpha = 0.2f),
      border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
   ) {
      Row(
         modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
         verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
         Icon(
            imageVector = icono,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
         )
         Text(text = texto, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
      }
   }
}

// Sección de estadísticas
@Composable
private fun StatsSection(usuario: UserEntity) {
   Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = ProfileColors.fondoCard),
      border = androidx.compose.foundation.BorderStroke(1.dp, ProfileColors.bordeCard),
   ) {
      Column(modifier = Modifier.padding(20.dp)) {
         Text(
            text = "Estadísticas",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
         )

         Spacer(modifier = Modifier.height(16.dp))

         Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatItem(
               label = "Reproducciones",
               value = usuario.totalReproducciones.toString(),
               icon = Icons.Default.PlayArrow,
            )
            StatItem(
               label = "Favoritos",
               value = usuario.totalFavoritos.toString(),
               icon = Icons.Default.Favorite,
            )
            StatItem(
               label = "Listas",
               value = usuario.totalListas.toString(),
               icon = Icons.Default.List,
            )
         }
      }
   }
}

// Item individual de estadística
@Composable
private fun StatItem(label: String, value: String, icon: ImageVector) {
   Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
   ) {
      Icon(
         imageVector = icon,
         contentDescription = null,
         tint = ProfileColors.neonPrimario,
         modifier = Modifier.size(24.dp),
      )
      Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
      Text(text = label, fontSize = 12.sp, color = ProfileColors.textoSecundario)
   }
}

// Información de la cuenta
@Composable
private fun AccountInfoSection(usuario: UserEntity) {
   Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = ProfileColors.fondoCard),
      border = androidx.compose.foundation.BorderStroke(1.dp, ProfileColors.bordeCard),
   ) {
      Column(modifier = Modifier.padding(20.dp)) {
         Text(
            text = "Información de Cuenta",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
         )

         Spacer(modifier = Modifier.height(16.dp))

         // Correo
         InfoRow(label = "Correo", value = usuario.correo, icon = Icons.Default.Email)

         // Última sesión
         usuario.ultimaSesion?.let { timestamp ->
            InfoRow(
               label = "Última sesión",
               value = formatearFechaRelativa(timestamp),
               icon = Icons.Default.Schedule,
            )
         }

         // Miembro desde
         InfoRow(
            label = "Miembro desde",
            value = formatearFechaCreacion(usuario.fechaCreacion),
            icon = Icons.Default.CalendarToday,
         )
      }
   }
}

// Fila de información
@Composable
private fun InfoRow(label: String, value: String, icon: ImageVector) {
   Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
   ) {
      Icon(
         imageVector = icon,
         contentDescription = null,
         tint = ProfileColors.neonPrimario,
         modifier = Modifier.size(20.dp),
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
         Text(text = label, fontSize = 12.sp, color = ProfileColors.textoSecundario)
         Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
         )
      }
   }
}

// Acciones rápidas
@Composable
private fun QuickActionsSection(
   onNavigateToSettings: () -> Unit,
   onEditProfile: () -> Unit,
   onLogout: () -> Unit,
) {
   Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      ActionButton(
         text = "Configuraciones",
         icon = Icons.Default.Settings,
         onClick = onNavigateToSettings,
         isPrimary = true,
      )

      ActionButton(text = "Editar Perfil", icon = Icons.Default.Edit, onClick = onEditProfile)

      ActionButton(
         text = "Cerrar Sesión",
         icon = Icons.Default.ExitToApp,
         onClick = onLogout,
         isDestructive = true,
      )
   }
}

// Botón de acción
@Composable
private fun ActionButton(
   text: String,
   icon: ImageVector,
   onClick: () -> Unit,
   isPrimary: Boolean = false,
   isDestructive: Boolean = false,
) {
   val backgroundColor =
      when {
         isPrimary ->
            Brush.horizontalGradient(
               listOf(
                  ProfileColors.neonPrimario.copy(alpha = 0.3f),
                  ProfileColors.neonSecundario.copy(alpha = 0.3f),
               )
            )
         isDestructive ->
            Brush.horizontalGradient(
               listOf(Color(0xFFFF1744).copy(alpha = 0.2f), Color(0xFFD50000).copy(alpha = 0.2f))
            )
         else -> Brush.horizontalGradient(listOf(ProfileColors.fondoCard, ProfileColors.fondoCard))
      }

   val borderColor =
      when {
         isPrimary -> ProfileColors.neonPrimario
         isDestructive -> Color(0xFFFF1744)
         else -> ProfileColors.bordeCard
      }

   Surface(
      modifier =
         Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
      shape = RoundedCornerShape(16.dp),
      color = Color.Transparent,
      border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
   ) {
      Box(
         modifier =
            Modifier.background(backgroundColor).padding(horizontal = 20.dp, vertical = 16.dp)
      ) {
         Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
         ) {
            Row(
               verticalAlignment = Alignment.CenterVertically,
               horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
               Icon(
                  imageVector = icon,
                  contentDescription = null,
                  tint = if (isDestructive) Color(0xFFFF1744) else Color.White,
                  modifier = Modifier.size(24.dp),
               )
               Text(
                  text = text,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Medium,
                  color = if (isDestructive) Color(0xFFFF1744) else Color.White,
               )
            }

            Icon(
               imageVector = Icons.Default.ChevronRight,
               contentDescription = null,
               tint = ProfileColors.textoSecundario,
               modifier = Modifier.size(20.dp),
            )
         }
      }
   }
}

// ==========================================
// 🛠️ UTILIDADES DE FORMATEO
// ==========================================

/** Formatea la fecha de creación en formato legible Ejemplo: "15 de Enero, 2024" */
private fun formatearFechaCreacion(timestamp: Long): String {
   return try {
      if (timestamp <= 0) return "Fecha no disponible"

      val calendar =
         Calendar.getInstance().apply {
            timeZone = TimeZone.getDefault()
            timeInMillis = timestamp
         }

      val dia = calendar.get(Calendar.DAY_OF_MONTH)
      val mesIndex = calendar.get(Calendar.MONTH)
      val anio = calendar.get(Calendar.YEAR)

      val meses =
         arrayOf(
            "Enero",
            "Febrero",
            "Marzo",
            "Abril",
            "Mayo",
            "Junio",
            "Julio",
            "Agosto",
            "Septiembre",
            "Octubre",
            "Noviembre",
            "Diciembre",
         )

      "$dia de ${meses[mesIndex]}, $anio"
   } catch (e: Exception) {
      "Fecha inválida"
   }
}

/**
 * Formatea la última sesión de manera relativa Ejemplos:
 * - "Hace 5 minutos"
 * - "Hace 2 horas"
 * - "Ayer a las 14:30"
 * - "15 Ene, 14:30"
 */
@SuppressLint("DefaultLocale")
private fun formatearFechaRelativa(timestamp: Long): String {
   return try {
      if (timestamp <= 0) return "Nunca"

      val ahora = System.currentTimeMillis()
      val diferencia = ahora - timestamp

      if (diferencia < 0) return "Ahora mismo"

      val segundos = diferencia / 1000
      val minutos = segundos / 60
      val horas = minutos / 60
      val dias = horas / 24

      when {
         segundos < 60 -> "Ahora mismo"
         minutos < 60 -> {
            val min = minutos.toInt()
            "Hace $min ${if (min == 1) "minuto" else "minutos"}"
         }
         horas < 24 -> {
            val hrs = horas.toInt()
            "Hace $hrs ${if (hrs == 1) "hora" else "horas"}"
         }
         dias < 2 -> {
            val calendar =
               Calendar.getInstance().apply {
                  timeZone = TimeZone.getDefault()
                  timeInMillis = timestamp
               }
            val hora = calendar.get(Calendar.HOUR_OF_DAY)
            val minuto = calendar.get(Calendar.MINUTE)
            "Ayer a las ${String.format("%02d:%02d", hora, minuto)}"
         }
         dias < 7 -> {
            val calendar =
               Calendar.getInstance().apply {
                  timeZone = TimeZone.getDefault()
                  timeInMillis = timestamp
               }
            val diasSemana =
               arrayOf("", "Domingo", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")
            val diaSemana = diasSemana[calendar.get(Calendar.DAY_OF_WEEK)]
            val hora = calendar.get(Calendar.HOUR_OF_DAY)
            val minuto = calendar.get(Calendar.MINUTE)
            "$diaSemana a las ${String.format("%02d:%02d", hora, minuto)}"
         }
         else -> {
            val calendar =
               Calendar.getInstance().apply {
                  timeZone = TimeZone.getDefault()
                  timeInMillis = timestamp
               }
            val mesesCortos =
               arrayOf(
                  "Ene",
                  "Feb",
                  "Mar",
                  "Abr",
                  "May",
                  "Jun",
                  "Jul",
                  "Ago",
                  "Sep",
                  "Oct",
                  "Nov",
                  "Dic",
               )
            val dia = calendar.get(Calendar.DAY_OF_MONTH)
            val mes = mesesCortos[calendar.get(Calendar.MONTH)]
            val hora = calendar.get(Calendar.HOUR_OF_DAY)
            val minuto = calendar.get(Calendar.MINUTE)
            "$dia $mes, ${String.format("%02d:%02d", hora, minuto)}"
         }
      }
   } catch (e: Exception) {
      "Fecha inválida"
   }
}

// ==========================================
// 📸 PREVIEWS ACTUALIZADAS
// ==========================================

@Preview(
   name = "Perfil - Usuario Local",
   showBackground = true,
   backgroundColor = 0xFF000000,
   uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewProfileLocal() {
   val usuario =
      UserEntity(
         idUsuario = 1,
         nombreUsuario = "musiclover_42",
         correo = "usuario@ejemplo.com",
         contraseniaHash = "",
         nombreCompleto = "María González",
         fotoPerfil = "https://i.pravatar.cc/300?img=25",
         tipoAutenticacion = UserEntity.TIPO_LOCAL,
         fechaCreacion = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
         ultimaSesion = System.currentTimeMillis() - (2L * 60 * 60 * 1000),
         totalReproducciones = 1247,
         totalFavoritos = 89,
         totalListas = 12,
      )

   ProfileContent(
      usuario = usuario,
      onNavigateBack = {},
      onNavigateToSettings = {},
      onEditProfile = {},
      onLogout = {},
   )
}

@Preview(
   name = "Perfil - Usuario Google",
   showBackground = true,
   backgroundColor = 0xFF000000,
   uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewProfileGoogle() {
   val usuario =
      UserEntity(
         idUsuario = 2,
         nombreUsuario = "carlos.dev",
         correo = "carlos.developer@gmail.com",
         contraseniaHash = "",
         nombreCompleto = "Carlos Ramírez",
         fotoPerfil = "https://i.pravatar.cc/300?img=12",
         tipoAutenticacion = UserEntity.TIPO_GOOGLE,
         providerId = "google_123456",
         fechaCreacion = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000),
         ultimaSesion = System.currentTimeMillis(),
         totalReproducciones = 5432,
         totalFavoritos = 256,
         totalListas = 34,
      )

   ProfileContent(
      usuario = usuario,
      onNavigateBack = {},
      onNavigateToSettings = {},
      onEditProfile = {},
      onLogout = {},
   )
}

@Composable
private fun PreviewProfileNuevo() {
   val usuario =
      UserEntity(
         idUsuario = 3,
         nombreUsuario = "newuser",
         correo = "nuevo@ejemplo.com",
         contraseniaHash = "",
         fotoPerfil = null,
         tipoAutenticacion = UserEntity.TIPO_LOCAL,
         fechaCreacion = System.currentTimeMillis(),
         ultimaSesion = System.currentTimeMillis(),
         totalReproducciones = 0,
         totalFavoritos = 0,
         totalListas = 0,
      )

   ProfileContent(
      usuario = usuario,
      onNavigateBack = {},
      onNavigateToSettings = {},
      onEditProfile = {},
      onLogout = {},
   )
}

@Preview(name = "Stats Section - High Numbers", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewStatsHighNumbers() {
   Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F0518)).padding(16.dp)) {
      StatsSection(
         usuario =
            UserEntity(
               idUsuario = 1,
               nombreUsuario = "test",
               correo = "test@test.com",
               contraseniaHash = "",
               tipoAutenticacion = UserEntity.TIPO_LOCAL,
               totalReproducciones = 99999,
               totalFavoritos = 9999,
               totalListas = 999,
            )
      )
   }
}

@Preview(name = "Quick Actions - Componente", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewQuickActions() {
   Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F0518)).padding(16.dp)) {
      QuickActionsSection(onNavigateToSettings = {}, onEditProfile = {}, onLogout = {})
   }
}

@Preview(name = "Debug - Fecha HOY", showBackground = true, backgroundColor = 0xFF0F0518)
@Composable
private fun PreviewDebugFechaHoy() {
   val ahoraExacto = System.currentTimeMillis()

   // Calcular fecha esperada manualmente
   val cal =
      Calendar.getInstance().apply {
         timeZone = TimeZone.getDefault()
         timeInMillis = ahoraExacto
      }
   val diaEsperado = cal.get(Calendar.DAY_OF_MONTH)
   val mesEsperado = cal.get(Calendar.MONTH) + 1
   val anioEsperado = cal.get(Calendar.YEAR)

   val usuario =
      UserEntity(
         idUsuario = 1,
         nombreUsuario = "debug",
         correo = "debug@test.com",
         contraseniaHash = "",
         tipoAutenticacion = UserEntity.TIPO_LOCAL,
         fechaCreacion = ahoraExacto,
         ultimaSesion = ahoraExacto,
      )

   Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F0518)).padding(16.dp)) {
      Text(text = "Timestamp: $ahoraExacto", color = Color.White, fontSize = 10.sp)
      Text(
         text = "Esperado: $diaEsperado/$mesEsperado/$anioEsperado",
         color = Color.Green,
         fontSize = 12.sp,
         fontWeight = FontWeight.Bold,
      )
      Spacer(modifier = Modifier.height(8.dp))
      AccountInfoSection(usuario = usuario)
   }
}
