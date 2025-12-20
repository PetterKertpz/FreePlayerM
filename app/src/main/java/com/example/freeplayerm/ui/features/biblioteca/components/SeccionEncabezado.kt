// ui/features/biblioteca/components/SeccionEncabezado.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.ui.features.biblioteca.TipoDeCuerpoBiblioteca

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionEncabezado(
    usuario: UsuarioEntity?,
    cuerpoActual: TipoDeCuerpoBiblioteca,
    escaneoManualEnProgreso: Boolean,
    onMenuClick: (TipoDeCuerpoBiblioteca) -> Unit,
    onReescanearClick: () -> Unit
) {
    val menus = listOf(
        "Canciones" to TipoDeCuerpoBiblioteca.CANCIONES,
        "Listas" to TipoDeCuerpoBiblioteca.LISTAS,
        "Álbumes" to TipoDeCuerpoBiblioteca.ALBUMES,
        "Artistas" to TipoDeCuerpoBiblioteca.ARTISTAS,
        "Géneros" to TipoDeCuerpoBiblioteca.GENEROS,
        "Favoritos" to TipoDeCuerpoBiblioteca.FAVORITOS
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Sombra degradada superior para legibilidad
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
        TopAppBar(
            title = {
                // Texto con efecto Neón (reutilizando estilo del Login)
                Text(
                    text = "FreePlayer",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displaySmall.copy(
                        shadow = Shadow(
                            color = Color(0xFFD500F9).copy(alpha = 0.6f),
                            blurRadius = 20f
                        )
                    )
                )
            },
            actions = {
                // Botón Refrescar (Estilo Glass)
                IconButton(
                    onClick = onReescanearClick,
                    enabled = !escaneoManualEnProgreso,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    if (escaneoManualEnProgreso) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFD500F9),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refrescar",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Avatar
                AsyncImage(
                    model = usuario?.fotoPerfil,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color(0xFFD500F9), CircleShape) // Borde Neón
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        // Chips de Navegación (Estilo Glass/Neón)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(menus) { (nombreMenu, tipo) ->
                val seleccionado = cuerpoActual == tipo

                // Chip personalizado
                Surface(
                    onClick = { onMenuClick(tipo) },
                    shape = RoundedCornerShape(50),
                    color = if (seleccionado) Color(0xFFD500F9).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (seleccionado) Color(0xFFD500F9) else Color.White.copy(alpha = 0.2f)
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
}