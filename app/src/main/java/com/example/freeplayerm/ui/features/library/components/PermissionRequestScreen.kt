package com.example.freeplayerm.ui.features.library.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.features.auth.components.BotonAnimado
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.shouldShowRationale

// Color Neón local para este componente
private val NeonPurple = Color(0xFFD500F9)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionRequestScreen(estadoPermiso: PermissionState) {

    // ✨ Animación de "Latido" para el icono
    val infiniteTransition = rememberInfiniteTransition(label = "icon_pulse")

    val scale by
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "scale",
        )

    val alpha by
        infiniteTransition.animateFloat(
            initialValue = 0.7f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "alpha",
        )

    // El Box padre ya tiene el fondo, aquí centramos el contenido
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        // 🪟 Tarjeta de Cristal
        LibraryDesignSystem(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // 🎵 Icono Animado
                Box(contentAlignment = Alignment.Center) {
                    // Halo difuso detrás
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier =
                            Modifier.size(100.dp).scale(scale * 1.2f).graphicsLayer {
                                this.alpha = 0.1f
                            }, // Muy tenue
                        tint = NeonPurple,
                    )

                    // Icono principal
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier =
                            Modifier.size(100.dp).scale(scale).graphicsLayer { this.alpha = alpha },
                        tint = NeonPurple,
                    )
                }

                // Título con efecto Glow
                Text(
                    text = "ACCESO REQUERIDO",
                    style =
                        MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            color = Color.White,
                            shadow = Shadow(color = NeonPurple.copy(alpha = 0.8f), blurRadius = 25f),
                        ),
                    textAlign = TextAlign.Center,
                )

                // Descripción Dinámica (Rationale vs Primera vez)
                val textoDescripcion =
                    if (estadoPermiso.status.shouldShowRationale) {
                        "Sin acceso a tu almacenamiento, FreePlayer no puede reproducir tu música local. Por favor, habilita el permiso para continuar."
                    } else {
                        "Para explorar el universo de tu música local, FreePlayer necesita conectar con tus archivos de audio."
                    }

                Text(
                    text = textoDescripcion,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 24.sp,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón Galáctico
                BotonAnimado(
                    onClick = { estadoPermiso.launchPermissionRequest() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.White,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CONCEDER ACCESO",
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
        }
    }
}

// ==================== PREVIEWS & MOCKS ====================

/** 🕵️‍♂️ Mock implementation de PermissionState para Previews. */
@OptIn(ExperimentalPermissionsApi::class)
private class MockPermissionState(
    override val permission: String = "android.permission.READ_MEDIA_AUDIO",
    isGranted: Boolean = false,
    shouldShowRationale: Boolean = false,
) : PermissionState {

    override val status: PermissionStatus =
        if (isGranted) {
            PermissionStatus.Granted
        } else {
            PermissionStatus.Denied(shouldShowRationale)
        }

    override fun launchPermissionRequest() {
        // No-op en preview
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview(name = "1. Permiso - Primera Vez", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun PreviewPermisoPrimeraVez() {
    val fakeState = remember { MockPermissionState(isGranted = false, shouldShowRationale = false) }

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            PermissionRequestScreen(estadoPermiso = fakeState)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview(
    name = "2. Permiso - Rationale (Reintento)",
    showBackground = true,
    backgroundColor = 0xFF121212,
)
@Composable
private fun PreviewPermisoRationale() {
    // Simulamos que el usuario ya dijo que no una vez
    val fakeState = remember { MockPermissionState(isGranted = false, shouldShowRationale = true) }

    FreePlayerMTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0518))) {
            PermissionRequestScreen(estadoPermiso = fakeState)
        }
    }
}
