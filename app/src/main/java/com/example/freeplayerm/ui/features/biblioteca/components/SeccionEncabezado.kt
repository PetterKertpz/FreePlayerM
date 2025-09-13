package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.ui.features.biblioteca.TipoDeCuerpoBiblioteca
import com.example.freeplayerm.ui.theme.AppColors
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionEncabezado(
    usuario: UsuarioEntity?, // <-- CAMBIO CLAVE 1: Recibe el usuario como parámetro
    cuerpoActual: TipoDeCuerpoBiblioteca, // <-- NUEVO: Para saber qué botón resaltar
    onMenuClick: (TipoDeCuerpoBiblioteca) -> Unit // <-- NUEVO: Callback para notificar clics
) {
    val menus = listOf(
        "Canciones" to TipoDeCuerpoBiblioteca.CANCIONES,
        "Listas" to TipoDeCuerpoBiblioteca.LISTAS,
        "Álbumes" to TipoDeCuerpoBiblioteca.ALBUMES,
        "Artistas" to TipoDeCuerpoBiblioteca.ARTISTAS,
        "Géneros" to TipoDeCuerpoBiblioteca.GENEROS,
        "Favoritos" to TipoDeCuerpoBiblioteca.FAVORITOS
    )
    Column (
        modifier = Modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        AppColors.Negro,
                        AppColors.PurpuraProfundo
                    ),
                    start = Offset(0f, Float.POSITIVE_INFINITY),
                    end = Offset(0f, 0f)
                )
            )
            .padding(0.dp)
    ) {
        TopAppBar(
            title = {
                Text(
                    "FreePlayer",
                    color = AppColors.Blanco, // Corregido para que se vea sobre el fondo oscuro
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic
                )
            },
            actions = {
                // <-- CAMBIO CLAVE 2: Usamos el parámetro 'usuario'.
                // La llamada segura '?.' protege contra un valor nulo mientras cargan los datos.
                AsyncImage(
                    model = usuario?.fotoPerfilUrl,
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,// Icono si hay un error
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AppColors.GrisProfundo)
                        .border(
                            width = 1.dp,
                            color = AppColors.Negro,
                            shape = CircleShape
                        )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AppColors.Transparente
            )
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(
                start = 5.dp,
                end = 5.dp,
                bottom = 10.dp // Un poco de espacio inferior
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(menus) { (nombreMenu, tipoDeCuerpo) ->
                val estaSeleccionado = cuerpoActual == tipoDeCuerpo
                Button(
                    onClick = { onMenuClick(tipoDeCuerpo) }, // Notificamos qué menú se seleccionó
                    colors = ButtonDefaults.buttonColors(
                        // Cambia de color si es el menú activo
                        containerColor = if (estaSeleccionado) AppColors.Negro else AppColors.Negro.copy(alpha = 0.5f),
                        contentColor = AppColors.Blanco
                    ),
                    border = BorderStroke(width = 1.dp, color = AppColors.GrisProfundo),
                ) {
                    Text(
                        text = nombreMenu,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSeccionEncabezado() {
    val usuarioDePrueba = UsuarioEntity(
        id = 1,
        nombreUsuario = "Preview User",
        correo = "preview@test.com",
        contrasenaHash = "",
        fechaRegistro = Date(),
        fotoPerfilUrl = "",
        tipoAutenticacion = "LOCAL"
    )
    SeccionEncabezado(
        usuario = usuarioDePrueba,
        cuerpoActual = TipoDeCuerpoBiblioteca.CANCIONES,
        onMenuClick = {}
    )
}