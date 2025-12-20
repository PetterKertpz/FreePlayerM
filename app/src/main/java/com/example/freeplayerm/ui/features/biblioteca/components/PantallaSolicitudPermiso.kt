// ui/features/biblioteca/components/PantallaSolicitudPermiso.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.features.inicio.components.BotonAnimado
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PantallaSolicitudPermiso(estadoPermiso: PermissionState) {
    // El Box padre en PantallaBiblioteca ya tiene el FondoGalaxiaAnimado.
    // Aquí centramos el contenido.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Usamos el efecto cristal para que el texto sea legible sobre las estrellas
        ContenedorGlass(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono Neón
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFFD500F9) // Morado Neón
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Título con Glow
                Text(
                    text = "ACCESO REQUERIDO",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color(0xFFD500F9).copy(alpha = 0.5f),
                            blurRadius = 20f
                        )
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Descripción poética pero clara
                Text(
                    text = "Para explorar el universo de tu música local, FreePlayer necesita conectar con tus archivos de audio.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botón reutilizado con animación
                BotonAnimado(
                    onClick = { estadoPermiso.launchPermissionRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("CONCEDER ACCESO", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}