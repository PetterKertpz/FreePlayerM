package com.example.freeplayerm.ui.features.auth.components

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.theme.AppColors

// ==========================================
// 6. Campo de Texto TRANSFORMER (Input -> Círculo de Carga)
// ==========================================
@Composable
fun CampoEntradaLogin(
    valor: String,
    alCambiarValor: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector? = null,
    esPassword: Boolean = false,
    esError: Boolean = false,
    cargando: Boolean = false, // <-- NUEVO PARÁMETRO
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // Estados de foco e interacción
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // 1. Animación de Flotación (Siempre activa, incluso cargando)
    val infiniteTransition = rememberInfiniteTransition(label = "flotacion_input")
    val offsetY by
        infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(3000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "offsetY",
        )

    // 2. Animación de Ancho (De llenar ancho a tamaño círculo)
    // Usamos animateFloat para controlar la fracción del ancho o un tamaño fijo
    val anchoFraccion by
        animateFloatAsState(
            targetValue = if (cargando) 0f else 1f, // 0f significa que usaremos width fijo después
            animationSpec = tween(500, easing = FastOutSlowInEasing),
            label = "ancho",
        )

    // Colores Neón
    val colorBorde =
        if (esError) Color(0xFFFF5252)
        else if (isFocused || cargando) Color(0xFFD500F9) else Color(0xFF6A1B9A)
    val colorIcono = if (isFocused) Color(0xFFD500F9) else AppColors.Blanco.copy(alpha = 0.7f)

    // Contenedor principal que maneja el tamaño y la forma
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .offset(y = offsetY.dp) // Flotación
                .fillMaxWidth(if (cargando) 0.2f else 1f) // Se encoge horizontalmente
                .height(60.dp) // Altura fija para asegurar círculo perfecto
                .clip(RoundedCornerShape(30.dp)) // Redondeo total
                .background(AppColors.Transparente),
    ) {

        // Transición suave entre el Input y el Loader
        AnimatedContent(
            targetState = cargando,
            label = "contenido_input",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            },
        ) { estaCargando ->
            if (estaCargando) {
                // --- ESTADO 2: CÍRCULO DE CARGA ---
                Box(
                    modifier =
                        Modifier.size(60.dp) // Tamaño cuadrado
                            .border(2.dp, colorBorde, RoundedCornerShape(30.dp)), // Borde Neón
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = Color(0xFFE040FB), // Color Neón
                        strokeWidth = 3.dp,
                    )
                }
            } else {
                // --- ESTADO 1: CAMPO DE TEXTO ---
                OutlinedTextField(
                    value = valor,
                    onValueChange = alCambiarValor,
                    interactionSource = interactionSource,
                    label = {
                        // Lógica de etiqueta "Neon"
                        val estiloLabel =
                            if (isFocused || valor.isNotEmpty()) {
                                TextStyle(
                                    color = Color(0xFFE1BEE7),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    letterSpacing = 2.sp,
                                    shadow = Shadow(color = Color(0xFFD500F9), blurRadius = 15f),
                                )
                            } else {
                                TextStyle(
                                    color = AppColors.Blanco.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Normal,
                                    letterSpacing = 0.sp,
                                )
                            }
                        Text(text = etiqueta, style = estiloLabel)
                    },
                    singleLine = true,
                    modifier =
                        Modifier.fillMaxSize().border(1.dp, colorBorde, RoundedCornerShape(30.dp)),
                    shape = RoundedCornerShape(30.dp),
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.6f),
                            unfocusedContainerColor = Color(0xFF121212).copy(alpha = 0.4f),
                            focusedBorderColor = AppColors.Transparente,
                            unfocusedBorderColor = AppColors.Transparente,
                            errorBorderColor = AppColors.Transparente,
                            cursorColor = Color(0xFFE040FB),
                            focusedTextColor = AppColors.Blanco,
                            unfocusedTextColor = AppColors.Blanco,
                        ),
                    visualTransformation =
                        if (esPassword && !passwordVisible) PasswordVisualTransformation()
                        else VisualTransformation.None,
                    leadingIcon =
                        if (icono != null) {
                            { Icon(icono, contentDescription = null, tint = colorIcono) }
                        } else null,
                    trailingIcon =
                        if (esPassword) {
                            {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector =
                                            if (passwordVisible) Icons.Default.Visibility
                                            else Icons.Default.VisibilityOff,
                                        contentDescription = "Ver pass",
                                        tint = colorIcono,
                                    )
                                }
                            }
                        } else null,
                    keyboardOptions = KeyboardOptions.Default,
                )
            }
        }
    }
}

// Botón Animado (Se mantiene igual)
@Composable
fun BotonAnimado(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contenido: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val escala by
        animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "escala_boton")

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = escala
                    scaleY = escala
                }
                .height(50.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6A1B9A),
                contentColor = AppColors.Blanco,
                disabledContainerColor = AppColors.Grays.v4,
            ),
        shape = RoundedCornerShape(25.dp),
        interactionSource = interactionSource,
        content = contenido,
    )
}

// ==========================================
// 1. FAKE DATA (Estados Simulados)
// ==========================================

/**
 * Define todos los escenarios visuales posibles para el Input. Centraliza la data de prueba en un
 * solo lugar.
 */
data class InputUiState(
    val label: String,
    val value: String,
    val isError: Boolean = false,
    val isPassword: Boolean = false,
    val isLoading: Boolean = false,
    val icon: ImageVector? = null,
)

/** Proveedor de estados para el Input. Genera 4 snapshots automáticos en la Preview. */
class InputStateProvider : PreviewParameterProvider<InputUiState> {
    override val values =
        sequenceOf(
            // 1. Estado Normal (Usuario vacío)
            InputUiState(label = "Usuario", value = "", icon = Icons.Default.Person),
            // 2. Estado Password (Oculto)
            InputUiState(
                label = "Contraseña",
                value = "123456",
                isPassword = true,
                icon = Icons.Default.Lock,
            ),
            // 3. Estado Error (Rojo)
            InputUiState(
                label = "Correo Inválido",
                value = "bad_mail",
                isError = true,
                icon = Icons.Default.Warning,
            ),
            // 4. Estado Transformación (Loading Spinner)
            InputUiState(
                label = "Validando...",
                value = "user",
                isLoading = true, // Activa la animación de encogimiento
                icon = null,
            ),
        )
}

/** Proveedor para los estados del Botón (Habilitado/Deshabilitado) */
class ButtonStateProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(true, false)
}

// ==========================================
// 2. PREVIEWS LIMPIAS (Aisladas en contexto Galaxia)
// ==========================================

/**
 * Wrapper oscuro consistente con el tema Galaxia. Usamos el mismo color base (0xFF050010) que en
 * las otras previews.
 */
@Composable
private fun InputComponentWrapper(content: @Composable () -> Unit) {
    Box(modifier = Modifier.wrapContentSize(), contentAlignment = Alignment.Center) {
        GalaxyBackground()
        content()
    }
}

// --- PREVIEW A: Input (4 Estados) ---
@Preview(name = "Input Estados", group = "Componentes Atómicos")
@Composable
fun PreviewInputVariations(@PreviewParameter(InputStateProvider::class) state: InputUiState) {
    InputComponentWrapper {
        CampoEntradaLogin(
            valor = state.value,
            alCambiarValor = {},
            etiqueta = state.label,
            icono = state.icon,
            esPassword = state.isPassword,
            esError = state.isError,
            cargando = state.isLoading,
            modifier = Modifier.width(300.dp), // Ancho fijo para simular móvil
        )
    }
}

// --- PREVIEW B: Botón Principal ---
@Preview(name = "Botón Acción", group = "Componentes Atómicos")
@Composable
fun PreviewButtonVariations(@PreviewParameter(ButtonStateProvider::class) isEnabled: Boolean) {
    InputComponentWrapper {
        BotonAnimado(onClick = {}, enabled = isEnabled, modifier = Modifier.width(280.dp)) {
            Text("INGRESAR")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
        }
    }
}
