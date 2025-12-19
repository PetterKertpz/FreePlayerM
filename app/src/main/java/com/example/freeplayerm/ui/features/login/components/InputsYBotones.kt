package com.example.freeplayerm.ui.features.login.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }

    // Estados de foco e interacción
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // 1. Animación de Flotación (Siempre activa, incluso cargando)
    val infiniteTransition = rememberInfiniteTransition(label = "flotacion_input")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    // 2. Animación de Ancho (De llenar ancho a tamaño círculo)
    // Usamos animateFloat para controlar la fracción del ancho o un tamaño fijo
    val anchoFraccion by animateFloatAsState(
        targetValue = if (cargando) 0f else 1f, // 0f significa que usaremos width fijo después
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "ancho"
    )

    // Colores Neón
    val colorBorde = if (esError) Color(0xFFFF5252) else if (isFocused || cargando) Color(0xFFD500F9) else Color(0xFF6A1B9A)
    val colorIcono = if (isFocused) Color(0xFFD500F9) else Color.White.copy(alpha = 0.7f)

    // Contenedor principal que maneja el tamaño y la forma
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .offset(y = offsetY.dp) // Flotación
            .fillMaxWidth(if (cargando) 0.2f else 1f) // Se encoge horizontalmente
            .height(60.dp) // Altura fija para asegurar círculo perfecto
            .clip(RoundedCornerShape(30.dp)) // Redondeo total
            .background(Color.Transparent)
    ) {

        // Transición suave entre el Input y el Loader
        AnimatedContent(
            targetState = cargando,
            label = "contenido_input",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { estaCargando ->
            if (estaCargando) {
                // --- ESTADO 2: CÍRCULO DE CARGA ---
                Box(
                    modifier = Modifier
                        .size(60.dp) // Tamaño cuadrado
                        .border(2.dp, colorBorde, RoundedCornerShape(30.dp)), // Borde Neón
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(30.dp),
                        color = Color(0xFFE040FB), // Color Neón
                        strokeWidth = 3.dp
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
                        val estiloLabel = if (isFocused || valor.isNotEmpty()) {
                            TextStyle(
                                color = Color(0xFFE1BEE7),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                letterSpacing = 2.sp,
                                shadow = Shadow(color = Color(0xFFD500F9), blurRadius = 15f)
                            )
                        } else {
                            TextStyle(
                                color = Color.White.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.sp
                            )
                        }
                        Text(text = etiqueta, style = estiloLabel)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, colorBorde, RoundedCornerShape(30.dp)),
                    shape = RoundedCornerShape(30.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E1E).copy(alpha = 0.6f),
                        unfocusedContainerColor = Color(0xFF121212).copy(alpha = 0.4f),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        errorBorderColor = Color.Transparent,
                        cursorColor = Color(0xFFE040FB),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    visualTransformation = if (esPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                    leadingIcon = if (icono != null) {
                        { Icon(icono, contentDescription = null, tint = colorIcono) }
                    } else null,
                    trailingIcon = if (esPassword) {
                        {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Ver pass",
                                    tint = colorIcono
                                )
                            }
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions.Default
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
    contenido: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val escala by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "escala_boton"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
            }
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6A1B9A),
            contentColor = Color.White,
            disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(25.dp),
        interactionSource = interactionSource,
        content = contenido
    )
}
// -----------------------------------------------------------------------------
// WRAPPER UI/UX: CONTEXTO REAL
// -----------------------------------------------------------------------------
// Este wrapper coloca tu componente sobre el fondo real para validar
// legibilidad y transparencia (Alpha compositing).
@Composable
private fun GalaxyPreviewWrapper(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center // Centramos el login como en la app real
    ) {
        // 1. Capa Fondo
        FondoGalaxiaAnimado()

        // 2. Capa Contenido (Con padding para que no toque los bordes)
        Box(modifier = Modifier.padding(32.dp)) {
            content()
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEW 1: SIMULACIÓN DE PANTALLA COMPLETA (La más importante)
// -----------------------------------------------------------------------------
@Preview(name = "1. Pantalla Login Completa", device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun PreviewLoginFullExperience() {
    GalaxyPreviewWrapper {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título simulado o Logo
            Text(
                "FreePlayer M",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            CampoEntradaLogin(
                valor = "",
                alCambiarValor = {},
                etiqueta = "Usuario",
                icono = Icons.Default.Email
            )

            CampoEntradaLogin(
                valor = "123456",
                alCambiarValor = {},
                etiqueta = "Contraseña",
                icono = Icons.Default.Lock,
                esPassword = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            BotonAnimado(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("INGRESAR AL SISTEMA")
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.Login, contentDescription = null)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// PREVIEW 2: VALIDACIÓN DE ESTADOS CRÍTICOS
// -----------------------------------------------------------------------------
@Preview(name = "2. Error de Validación", group = "Estados")
@Composable
fun PreviewEstadoErrorEnGalaxia() {
    GalaxyPreviewWrapper {
        Column {
            Text(
                "Prueba de contraste: Rojo vs Galaxia",
                color = Color.White.copy(0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CampoEntradaLogin(
                valor = "error@mail",
                alCambiarValor = {},
                etiqueta = "Email Incorrecto",
                icono = Icons.Default.Email,
                esError = true // Validamos que el borde rojo se vea bien sobre el morado
            )
        }
    }
}

@Preview(name = "3. Botón Disabled", group = "Estados")
@Composable
fun PreviewBotonDisabled() {
    GalaxyPreviewWrapper {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Estado Cargando/Disabled", color = Color.White)
            Spacer(Modifier.height(10.dp))
            BotonAnimado(
                onClick = {},
                enabled = false, // Validar si el gris del disabled se pierde en el fondo
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("CONECTANDO...")
            }
        }
    }
}
