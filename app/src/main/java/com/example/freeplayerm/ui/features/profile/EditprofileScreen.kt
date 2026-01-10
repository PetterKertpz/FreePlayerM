package com.example.freeplayerm.ui.features.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.freeplayerm.data.local.entity.UserEntity

// Colores consistentes con ProfileScreen
private object EditProfileColors {
   val neonPrimario = Color(0xFFD500F9)
   val neonSecundario = Color(0xFF7C4DFF)
   val fondoCard = Color(0xFF1A0B2E).copy(alpha = 0.6f)
   val fondoScreen = Color(0xFF0F0518)
   val textoSecundario = Color.White.copy(alpha = 0.7f)
   val bordeCard = Color.White.copy(alpha = 0.1f)
   val error = Color(0xFFFF1744)
   val success = Color(0xFF00E676)
}

// Estado del formulario de edición
@Stable
data class EditProfileFormState(
   val nombreCompleto: String = "",
   val biografia: String = "",
   val fotoPerfilUri: String? = null,
   val isLoading: Boolean = false,
   val errorMessage: String? = null,
   val isSaved: Boolean = false,
)

// Validaciones del formulario
private object FormValidation {
   const val MAX_NOMBRE_LENGTH = 50
   const val MAX_BIO_LENGTH = 150
   
   fun validarNombre(nombre: String): String? {
      return when {
         nombre.isBlank() -> "El nombre no puede estar vacío"
         nombre.length < 2 -> "Mínimo 2 caracteres"
         nombre.length > MAX_NOMBRE_LENGTH -> "Máximo $MAX_NOMBRE_LENGTH caracteres"
         else -> null
      }
   }
   
   fun validarBiografia(bio: String): String? {
      return when {
         bio.length > MAX_BIO_LENGTH -> "Máximo $MAX_BIO_LENGTH caracteres"
         else -> null
      }
   }
}

// Pantalla principal conectada al ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
   viewModel: ProfileViewModel = hiltViewModel(),
   onNavigateBack: () -> Unit,
   onSaveSuccess: () -> Unit,
) {
   val usuario by viewModel.usuario.collectAsState()
   
   // Estado del formulario local
   var formState by remember(usuario) {
      mutableStateOf(
         EditProfileFormState(
            nombreCompleto = usuario?.nombreCompleto ?: "",
            biografia = usuario?.biografia ?: "",
            fotoPerfilUri = usuario?.fotoPerfil,
         )
      )
   }
   
   // Efecto para navegación tras guardado exitoso
   LaunchedEffect(formState.isSaved) {
      if (formState.isSaved) {
         onSaveSuccess()
      }
   }
   
   if (usuario == null) {
      EditProfileLoadingScreen(onNavigateBack = onNavigateBack)
      return
   }
   
   EditProfileContent(
      usuario = usuario!!,
      formState = formState,
      onNombreChange = { nuevoNombre ->
         formState = formState.copy(
            nombreCompleto = nuevoNombre,
            errorMessage = null,
         )
      },
      onBiografiaChange = { nuevaBio ->
         formState = formState.copy(
            biografia = nuevaBio,
            errorMessage = null,
         )
      },
      onFotoSelected = { uri ->
         formState = formState.copy(fotoPerfilUri = uri)
      },
      onSave = {
         val errorNombre = FormValidation.validarNombre(formState.nombreCompleto)
         val errorBio = FormValidation.validarBiografia(formState.biografia)
         
         when {
            errorNombre != null -> {
               formState = formState.copy(errorMessage = errorNombre)
            }
            errorBio != null -> {
               formState = formState.copy(errorMessage = errorBio)
            }
            else -> {
               formState = formState.copy(isLoading = true)
               viewModel.actualizarInformacion(
                  nombreCompleto = formState.nombreCompleto.trim(),
                  biografia = formState.biografia.trim().ifBlank { null },
               )
               formState.fotoPerfilUri?.let { uri ->
                  if (uri != usuario!!.fotoPerfil) {
                     viewModel.actualizarFotoPerfil(uri)
                  }
               }
               formState = formState.copy(isLoading = false, isSaved = true)
            }
         }
      },
      onNavigateBack = onNavigateBack,
   )
}

// Pantalla de carga
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileLoadingScreen(onNavigateBack: () -> Unit) {
   Box(
      modifier = Modifier
         .fillMaxSize()
         .background(
            Brush.verticalGradient(
               colors = listOf(
                  EditProfileColors.fondoScreen,
                  EditProfileColors.fondoScreen.copy(alpha = 0.8f),
                  Color.Black,
               )
            )
         )
   ) {
      TopAppBar(
         title = { Text("Editar Perfil", color = Color.White) },
         navigationIcon = {
            IconButton(onClick = onNavigateBack) {
               Icon(
                  Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Volver",
                  tint = Color.White,
               )
            }
         },
         colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
      )
      
      CircularProgressIndicator(
         modifier = Modifier.align(Alignment.Center),
         color = EditProfileColors.neonPrimario,
      )
   }
}

// Contenido stateless del formulario
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileContent(
   usuario: UserEntity,
   formState: EditProfileFormState,
   onNombreChange: (String) -> Unit,
   onBiografiaChange: (String) -> Unit,
   onFotoSelected: (String) -> Unit,
   onSave: () -> Unit,
   onNavigateBack: () -> Unit,
) {
   val scrollState = rememberScrollState()
   val focusManager = LocalFocusManager.current
   
   // Detectar cambios para habilitar botón guardar
   val hasChanges = remember(formState, usuario) {
      formState.nombreCompleto != (usuario.nombreCompleto ?: "") ||
            formState.biografia != (usuario.biografia ?: "") ||
            formState.fotoPerfilUri != usuario.fotoPerfil
   }
   
   Box(
      modifier = Modifier
         .fillMaxSize()
         .background(
            Brush.verticalGradient(
               colors = listOf(
                  EditProfileColors.fondoScreen,
                  EditProfileColors.fondoScreen.copy(alpha = 0.8f),
                  Color.Black,
               )
            )
         )
   ) {
      Column(modifier = Modifier.fillMaxSize()) {
         // Top Bar con acción de guardar
         TopAppBar(
            title = { Text("Editar Perfil", color = Color.White) },
            navigationIcon = {
               IconButton(onClick = onNavigateBack) {
                  Icon(
                     Icons.AutoMirrored.Filled.ArrowBack,
                     contentDescription = "Volver",
                     tint = Color.White,
                  )
               }
            },
            actions = {
               // Botón guardar con animación
               SaveButton(
                  enabled = hasChanges && !formState.isLoading,
                  isLoading = formState.isLoading,
                  onClick = {
                     focusManager.clearFocus()
                     onSave()
                  },
               )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
         )
         
         // Contenido scrolleable
         Column(
            modifier = Modifier
               .fillMaxSize()
               .verticalScroll(scrollState)
               .padding(horizontal = 16.dp)
               .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
         ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sección de foto de perfil editable
            EditableProfilePhoto(
               currentPhotoUrl = formState.fotoPerfilUri
                  ?: "https://ui-avatars.com/api/?name=${usuario.nombreUsuario}",
               onPhotoSelected = onFotoSelected,
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Mensaje de error animado
            AnimatedVisibility(
               visible = formState.errorMessage != null,
               enter = fadeIn() + scaleIn(),
               exit = fadeOut() + scaleOut(),
            ) {
               ErrorBanner(message = formState.errorMessage ?: "")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Formulario de edición
            EditFormCard(
               nombreCompleto = formState.nombreCompleto,
               biografia = formState.biografia,
               nombreUsuario = usuario.nombreUsuario,
               correo = usuario.correo,
               onNombreChange = onNombreChange,
               onBiografiaChange = onBiografiaChange,
               onDone = {
                  focusManager.clearFocus()
                  if (hasChanges) onSave()
               },
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Información no editable
            ReadOnlyInfoCard(
               nombreUsuario = usuario.nombreUsuario,
               correo = usuario.correo,
               tipoAutenticacion = usuario.tipoAutenticacion,
            )
         }
      }
   }
}

// Botón de guardar con estados animados
@Composable
private fun SaveButton(
   enabled: Boolean,
   isLoading: Boolean,
   onClick: () -> Unit,
) {
   val scale by animateFloatAsState(
      targetValue = if (enabled) 1f else 0.9f,
      animationSpec = tween(200),
      label = "save_scale",
   )
   
   val backgroundColor by animateColorAsState(
      targetValue = if (enabled) EditProfileColors.neonPrimario else Color.Gray,
      animationSpec = tween(200),
      label = "save_color",
   )
   
   TextButton(
      onClick = onClick,
      enabled = enabled && !isLoading,
      modifier = Modifier.scale(scale),
   ) {
      if (isLoading) {
         CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color.White,
            strokeWidth = 2.dp,
         )
      } else {
         Text(
            text = "Guardar",
            color = backgroundColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
         )
      }
   }
}

// Foto de perfil editable con selector de imagen
@Composable
private fun EditableProfilePhoto(
   currentPhotoUrl: String,
   onPhotoSelected: (String) -> Unit,
) {
   val isInPreview = LocalInspectionMode.current
   
   // Launcher para seleccionar imagen de galería
   val imagePickerLauncher = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.GetContent()
   ) { uri: Uri? ->
      uri?.let { onPhotoSelected(it.toString()) }
   }
   
   // Animación de escala al presionar
   var isPressed by remember { mutableStateOf(false) }
   val photoScale by animateFloatAsState(
      targetValue = if (isPressed) 0.95f else 1f,
      animationSpec = tween(100),
      label = "photo_scale",
   )
   
   Box(
      modifier = Modifier
         .size(140.dp)
         .scale(photoScale)
         .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
         ) {
            isPressed = true
            if (!isInPreview) {
               imagePickerLauncher.launch("image/*")
            }
         },
      contentAlignment = Alignment.Center,
   ) {
      // Foto con borde neón
      Box(
         modifier = Modifier
            .size(120.dp)
            .border(
               width = 3.dp,
               brush = Brush.linearGradient(
                  colors = listOf(
                     EditProfileColors.neonPrimario,
                     EditProfileColors.neonSecundario,
                  )
               ),
               shape = CircleShape,
            )
            .padding(4.dp)
            .clip(CircleShape)
            .background(Color.DarkGray)
      ) {
         AsyncImage(
            model = currentPhotoUrl,
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
         )
      }
      
      // Badge de edición
      Box(
         modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = (-8).dp, y = (-8).dp)
            .size(36.dp)
            .clip(CircleShape)
            .background(
               Brush.linearGradient(
                  colors = listOf(
                     EditProfileColors.neonPrimario,
                     EditProfileColors.neonSecundario,
                  )
               )
            ),
         contentAlignment = Alignment.Center,
      ) {
         Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = "Cambiar foto",
            tint = Color.White,
            modifier = Modifier.size(20.dp),
         )
      }
   }
   
   // Reset del estado presionado
   LaunchedEffect(isPressed) {
      if (isPressed) {
         kotlinx.coroutines.delay(100)
         isPressed = false
      }
   }
}

// Banner de error
@Composable
private fun ErrorBanner(message: String) {
   Surface(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      color = EditProfileColors.error.copy(alpha = 0.15f),
      border = androidx.compose.foundation.BorderStroke(
         1.dp,
         EditProfileColors.error.copy(alpha = 0.5f)
      ),
   ) {
      Row(
         modifier = Modifier.padding(12.dp),
         verticalAlignment = Alignment.CenterVertically,
         horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
         Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = EditProfileColors.error,
            modifier = Modifier.size(20.dp),
         )
         Text(
            text = message,
            color = EditProfileColors.error,
            fontSize = 14.sp,
         )
      }
   }
}

// Card con formulario editable
@Composable
private fun EditFormCard(
   nombreCompleto: String,
   biografia: String,
   nombreUsuario: String,
   correo: String,
   onNombreChange: (String) -> Unit,
   onBiografiaChange: (String) -> Unit,
   onDone: () -> Unit,
) {
   val focusManager = LocalFocusManager.current
   
   Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(containerColor = EditProfileColors.fondoCard),
      border = androidx.compose.foundation.BorderStroke(1.dp, EditProfileColors.bordeCard),
   ) {
      Column(modifier = Modifier.padding(20.dp)) {
         Text(
            text = "Información Personal",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
         )
         
         Spacer(modifier = Modifier.height(20.dp))
         
         // Campo: Nombre completo
         GalacticTextField(
            value = nombreCompleto,
            onValueChange = onNombreChange,
            label = "Nombre completo",
            placeholder = "Tu nombre para mostrar",
            icon = Icons.Default.Person,
            maxLength = FormValidation.MAX_NOMBRE_LENGTH,
            keyboardOptions = KeyboardOptions(
               capitalization = KeyboardCapitalization.Words,
               imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
               onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
         )
         
         Spacer(modifier = Modifier.height(16.dp))
         
         // Campo: Biografía
         GalacticTextField(
            value = biografia,
            onValueChange = onBiografiaChange,
            label = "Biografía",
            placeholder = "Cuéntanos sobre ti...",
            icon = Icons.Default.Edit,
            maxLength = FormValidation.MAX_BIO_LENGTH,
            singleLine = false,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
               capitalization = KeyboardCapitalization.Sentences,
               imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
         )
      }
   }
}

// TextField personalizado con estilo galáctico
@Composable
private fun GalacticTextField(
   value: String,
   onValueChange: (String) -> Unit,
   label: String,
   placeholder: String,
   icon: ImageVector,
   maxLength: Int,
   singleLine: Boolean = true,
   maxLines: Int = 1,
   keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
   keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
   val interactionSource = remember { MutableInteractionSource() }
   val isFocused by interactionSource.collectIsFocusedAsState()
   
   val borderColor by animateColorAsState(
      targetValue = when {
         isFocused -> EditProfileColors.neonPrimario
         value.isNotEmpty() -> EditProfileColors.neonSecundario.copy(alpha = 0.5f)
         else -> EditProfileColors.bordeCard
      },
      animationSpec = tween(200),
      label = "border_color",
   )
   
   Column {
      OutlinedTextField(
         value = value,
         onValueChange = { if (it.length <= maxLength) onValueChange(it) },
         label = { Text(label) },
         placeholder = { Text(placeholder, color = EditProfileColors.textoSecundario) },
         leadingIcon = {
            Icon(
               imageVector = icon,
               contentDescription = null,
               tint = if (isFocused) EditProfileColors.neonPrimario
               else EditProfileColors.textoSecundario,
            )
         },
         singleLine = singleLine,
         maxLines = maxLines,
         interactionSource = interactionSource,
         keyboardOptions = keyboardOptions,
         keyboardActions = keyboardActions,
         colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            focusedLabelColor = EditProfileColors.neonPrimario,
            unfocusedLabelColor = EditProfileColors.textoSecundario,
            cursorColor = EditProfileColors.neonPrimario,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
         ),
         shape = RoundedCornerShape(12.dp),
         modifier = Modifier.fillMaxWidth(),
      )
      
      // Contador de caracteres
      Row(
         modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, end = 4.dp),
         horizontalArrangement = Arrangement.End,
      ) {
         Text(
            text = "${value.length}/$maxLength",
            fontSize = 12.sp,
            color = if (value.length >= maxLength) EditProfileColors.error
            else EditProfileColors.textoSecundario,
         )
      }
   }
}

// Card con información de solo lectura
@Composable
private fun ReadOnlyInfoCard(
   nombreUsuario: String,
   correo: String,
   tipoAutenticacion: String,
) {
   Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
         containerColor = EditProfileColors.fondoCard.copy(alpha = 0.4f)
      ),
      border = androidx.compose.foundation.BorderStroke(1.dp, EditProfileColors.bordeCard),
   ) {
      Column(modifier = Modifier.padding(20.dp)) {
         Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
         ) {
            Icon(
               imageVector = Icons.Default.Lock,
               contentDescription = null,
               tint = EditProfileColors.textoSecundario,
               modifier = Modifier.size(18.dp),
            )
            Text(
               text = "Información de Cuenta",
               fontSize = 16.sp,
               fontWeight = FontWeight.Medium,
               color = EditProfileColors.textoSecundario,
            )
         }
         
         Text(
            text = "Estos datos no se pueden modificar",
            fontSize = 12.sp,
            color = EditProfileColors.textoSecundario.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp),
         )
         
         Spacer(modifier = Modifier.height(16.dp))
         
         ReadOnlyField(
            label = "Usuario",
            value = "@$nombreUsuario",
            icon = Icons.Default.AlternateEmail,
         )
         
         Spacer(modifier = Modifier.height(12.dp))
         
         ReadOnlyField(
            label = "Correo",
            value = correo,
            icon = Icons.Default.Email,
         )
         
         Spacer(modifier = Modifier.height(12.dp))
         
         ReadOnlyField(
            label = "Tipo de cuenta",
            value = when (tipoAutenticacion) {
               UserEntity.TIPO_GOOGLE -> "Google"
               UserEntity.TIPO_LOCAL -> "Local"
               else -> tipoAutenticacion
            },
            icon = Icons.Default.Shield,
         )
      }
   }
}

// Campo de solo lectura
@Composable
private fun ReadOnlyField(
   label: String,
   value: String,
   icon: ImageVector,
) {
   Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
   ) {
      Icon(
         imageVector = icon,
         contentDescription = null,
         tint = EditProfileColors.textoSecundario.copy(alpha = 0.6f),
         modifier = Modifier.size(20.dp),
      )
      
      Spacer(modifier = Modifier.width(12.dp))
      
      Column {
         Text(
            text = label,
            fontSize = 12.sp,
            color = EditProfileColors.textoSecundario.copy(alpha = 0.6f),
         )
         Text(
            text = value,
            fontSize = 14.sp,
            color = EditProfileColors.textoSecundario,
         )
      }
   }
}

// ==========================================
// PREVIEWS
// ==========================================

// Provider para diferentes estados del formulario
class EditProfileStateProvider : PreviewParameterProvider<EditProfileFormState> {
   override val values = sequenceOf(
      // Estado inicial
      EditProfileFormState(
         nombreCompleto = "María González",
         biografia = "Amante de la música 🎵",
         fotoPerfilUri = "https://i.pravatar.cc/300?img=25",
      ),
      // Estado con campos vacíos
      EditProfileFormState(
         nombreCompleto = "",
         biografia = "",
         fotoPerfilUri = null,
      ),
      // Estado con error
      EditProfileFormState(
         nombreCompleto = "M",
         biografia = "",
         errorMessage = "Mínimo 2 caracteres",
      ),
      // Estado cargando
      EditProfileFormState(
         nombreCompleto = "María González",
         biografia = "Nueva biografía actualizada",
         isLoading = true,
      ),
   )
}

// Usuario de prueba para previews
private fun previewUser(
   nombreCompleto: String? = "María González",
   biografia: String? = "Amante de la música 🎵",
   fotoPerfil: String? = "https://i.pravatar.cc/300?img=25",
   tipoAuth: String = UserEntity.TIPO_LOCAL,
) = UserEntity(
   idUsuario = 1,
   nombreUsuario = "musiclover_42",
   correo = "usuario@ejemplo.com",
   contraseniaHash = "",
   nombreCompleto = nombreCompleto,
   biografia = biografia,
   fotoPerfil = fotoPerfil,
   tipoAutenticacion = tipoAuth,
   fechaCreacion = System.currentTimeMillis(),
)

@Preview(
   name = "Editar Perfil - Light",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewEditProfileLight() {
   EditProfileContent(
      usuario = previewUser(),
      formState = EditProfileFormState(
         nombreCompleto = "María González",
         biografia = "Amante de la música 🎵",
         fotoPerfilUri = "https://i.pravatar.cc/300?img=25",
      ),
      onNombreChange = {},
      onBiografiaChange = {},
      onFotoSelected = {},
      onSave = {},
      onNavigateBack = {},
   )
}

@Preview(
   name = "Editar Perfil - Dark Mode",
   showBackground = true,
   backgroundColor = 0xFF000000,
   uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun PreviewEditProfileDark() {
   EditProfileContent(
      usuario = previewUser(),
      formState = EditProfileFormState(
         nombreCompleto = "María González",
         biografia = "Amante de la música 🎵",
         fotoPerfilUri = "https://i.pravatar.cc/300?img=25",
      ),
      onNombreChange = {},
      onBiografiaChange = {},
      onFotoSelected = {},
      onSave = {},
      onNavigateBack = {},
   )
}

@Preview(
   name = "Editar Perfil - Estado Error",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewEditProfileError() {
   EditProfileContent(
      usuario = previewUser(nombreCompleto = "M"),
      formState = EditProfileFormState(
         nombreCompleto = "M",
         biografia = "",
         errorMessage = "Mínimo 2 caracteres",
      ),
      onNombreChange = {},
      onBiografiaChange = {},
      onFotoSelected = {},
      onSave = {},
      onNavigateBack = {},
   )
}

@Preview(
   name = "Editar Perfil - Cargando",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewEditProfileLoading() {
   EditProfileContent(
      usuario = previewUser(),
      formState = EditProfileFormState(
         nombreCompleto = "María González",
         biografia = "Nueva bio actualizada",
         isLoading = true,
      ),
      onNombreChange = {},
      onBiografiaChange = {},
      onFotoSelected = {},
      onSave = {},
      onNavigateBack = {},
   )
}

@Preview(
   name = "Editar Perfil - Usuario Google",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewEditProfileGoogle() {
   EditProfileContent(
      usuario = previewUser(
         nombreCompleto = "Carlos Ramírez",
         biografia = "Developer & Music Lover",
         tipoAuth = UserEntity.TIPO_GOOGLE,
      ),
      formState = EditProfileFormState(
         nombreCompleto = "Carlos Ramírez",
         biografia = "Developer & Music Lover",
         fotoPerfilUri = "https://i.pravatar.cc/300?img=12",
      ),
      onNombreChange = {},
      onBiografiaChange = {},
      onFotoSelected = {},
      onSave = {},
      onNavigateBack = {},
   )
}

@Preview(
   name = "Editar Perfil - Usuario Nuevo (Empty)",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewEditProfileEmpty() {
   EditProfileContent(
      usuario = previewUser(
         nombreCompleto = null,
         biografia = null,
         fotoPerfil = null,
      ),
      formState = EditProfileFormState(
         nombreCompleto = "",
         biografia = "",
         fotoPerfilUri = null,
      ),
      onNombreChange = {},
      onBiografiaChange = {},
      onFotoSelected = {},
      onSave = {},
      onNavigateBack = {},
   )
}

@Preview(
   name = "Editar Perfil - Loading Screen",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewEditProfileLoadingScreen() {
   EditProfileLoadingScreen(onNavigateBack = {})
}

@Preview(
   name = "TextField - Estados",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewGalacticTextField() {
   Column(
      modifier = Modifier
         .fillMaxWidth()
         .background(Color(0xFF0F0518))
         .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
   ) {
      GalacticTextField(
         value = "",
         onValueChange = {},
         label = "Campo vacío",
         placeholder = "Placeholder...",
         icon = Icons.Default.Person,
         maxLength = 50,
      )
      
      GalacticTextField(
         value = "Con contenido",
         onValueChange = {},
         label = "Campo lleno",
         placeholder = "Placeholder...",
         icon = Icons.Default.Edit,
         maxLength = 50,
      )
      
      GalacticTextField(
         value = "Texto que alcanza el límite máximo permitido!!",
         onValueChange = {},
         label = "Límite alcanzado",
         placeholder = "Placeholder...",
         icon = Icons.Default.Warning,
         maxLength = 45,
      )
   }
}

@Preview(
   name = "Foto Editable - Componente",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewEditablePhoto() {
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .background(Color(0xFF0F0518))
         .padding(32.dp),
      contentAlignment = Alignment.Center,
   ) {
      EditableProfilePhoto(
         currentPhotoUrl = "https://i.pravatar.cc/300?img=25",
         onPhotoSelected = {},
      )
   }
}

@Preview(
   name = "Error Banner - Componente",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewErrorBanner() {
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .background(Color(0xFF0F0518))
         .padding(16.dp),
   ) {
      ErrorBanner(message = "El nombre debe tener al menos 2 caracteres")
   }
}

@Preview(
   name = "Info Card Solo Lectura",
   showBackground = true,
   backgroundColor = 0xFF0F0518,
)
@Composable
private fun PreviewReadOnlyInfoCard() {
   Box(
      modifier = Modifier
         .fillMaxWidth()
         .background(Color(0xFF0F0518))
         .padding(16.dp),
   ) {
      ReadOnlyInfoCard(
         nombreUsuario = "musiclover_42",
         correo = "usuario@ejemplo.com",
         tipoAutenticacion = UserEntity.TIPO_LOCAL,
      )
   }
}