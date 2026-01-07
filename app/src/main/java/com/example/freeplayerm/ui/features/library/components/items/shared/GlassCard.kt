package com.example.freeplayerm.ui.features.library.components.items.shared

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.example.freeplayerm.ui.theme.FreePlayerMTheme

// ==================== COLORES Y ESTILOS LOCALES ====================

private object GlassCardDefaults {
   val NeonPrimary = Color(0xFFD500F9)
   val BackgroundNormal = Color(0xFF1E1E1E).copy(alpha = 0.4f)
   val BackgroundSelected = NeonPrimary.copy(alpha = 0.15f)
   val BorderNormal = Color.White.copy(alpha = 0.1f)
   val BorderSelected = NeonPrimary.copy(alpha = 0.6f)

   // Gradiente sutil para el efecto cristal
   val GlassGradient =
      Brush.horizontalGradient(
         colors = listOf(Color(0xFF2A2A2A).copy(alpha = 0.3f), Color(0xFF121212).copy(alpha = 0.1f))
      )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassCard(
   onClick: () -> Unit,
   modifier: Modifier = Modifier,
   onLongClick: () -> Unit = {},
   isSelected: Boolean = false,
   content: @Composable RowScope.() -> Unit,
) {
   val borderColor = if (isSelected) {
      Color(0xFFD500F9).copy(alpha = 0.7f)
   } else {
      Color.White.copy(alpha = 0.1f)
   }
   
   val backgroundColor = if (isSelected) {
      Color(0xFFD500F9).copy(alpha = 0.15f)
   } else {
      Color.White.copy(alpha = 0.05f)
   }
   
   Row(
      modifier = modifier
         .fillMaxSize()
         .clip(RoundedCornerShape(12.dp))
         .background(backgroundColor)
         .border(1.dp, borderColor, RoundedCornerShape(12.dp))
         .combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick,
         )
         .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      content = content,
   )
}
// ==================== PREVIEWS & MOCKS ====================

/** 📦 Datos de prueba para previsualizar diferentes estados */
data class GlassCardPreviewData(
   val nombreCaso: String,
   val seleccionado: Boolean,
   val titulo: String,
   val subtitulo: String,
)

/** 🎨 Proveedor de datos de ejemplo */
class GlassCardProvider : PreviewParameterProvider<GlassCardPreviewData> {
   override val values =
      sequenceOf(
         // CASO 1: Normal
         GlassCardPreviewData(
            nombreCaso = "Normal",
            seleccionado = false,
            titulo = "Midnight City",
            subtitulo = "M83 • Hurry Up, We're Dreaming",
         ),
         // CASO 2: Seleccionado (Efecto Neon)
         GlassCardPreviewData(
            nombreCaso = "Seleccionado",
            seleccionado = true,
            titulo = "Starboy",
            subtitulo = "The Weeknd ft. Daft Punk",
         ),
         // CASO 3: Texto Largo
         GlassCardPreviewData(
            nombreCaso = "Texto Largo",
            seleccionado = false,
            titulo =
               "Título extremadamente largo para probar el comportamiento del layout en pantallas pequeñas",
            subtitulo = "Descripción también muy extensa que debería cortarse correctamente",
         ),
      )
}

@Preview(
   name = "Variaciones de Tarjeta",
   showBackground = true,
   backgroundColor = 0xFF0F0518, // Fondo oscuro profundo para ver el efecto cristal
   widthDp = 360,
)
@Composable
private fun GlassCardPreview(
   @PreviewParameter(GlassCardProvider::class) data: GlassCardPreviewData
) {
   FreePlayerMTheme(darkTheme = true) {
      Box(modifier = Modifier.padding(16.dp)) {
         GlassCard(onClick = {}, onLongClick = {}, isSelected = data.seleccionado) {
            // CONTENIDO SIMULADO (RowScope)

            // 1. Placeholder de Icono/Portada
            Box(
               modifier =
                  Modifier.size(48.dp)
                     .clip(RoundedCornerShape(8.dp))
                     .background(Color.White.copy(alpha = 0.1f)),
               contentAlignment = Alignment.Center,
            ) {
               if (data.seleccionado) {
                  Icon(
                     imageVector = Icons.Default.CheckCircle,
                     contentDescription = null,
                     tint = GlassCardDefaults.NeonPrimary,
                     modifier = Modifier.size(24.dp),
                  )
               } else {
                  Icon(
                     imageVector = Icons.Default.MusicNote,
                     contentDescription = null,
                     tint = Color.White.copy(alpha = 0.5f),
                     modifier = Modifier.size(24.dp),
                  )
               }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Información de Texto
            Column(modifier = Modifier.weight(1f)) {
               Text(
                  text = data.titulo,
                  color = Color.White,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
               )
               Spacer(modifier = Modifier.height(4.dp))
               Text(
                  text = data.subtitulo,
                  color = Color.White.copy(alpha = 0.7f),
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 1,
               )
            }
         }
      }
   }
}
