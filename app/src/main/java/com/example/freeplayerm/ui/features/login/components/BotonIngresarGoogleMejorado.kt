package com.example.freeplayerm.ui.features.login.components
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.R

enum class TemaBotonGoogle {
    Claro,
    Oscuro,
    Neutro
}

@Composable
fun BotonIngresarGoogleMejorado(
    modifier: Modifier = Modifier,
    texto: String = "Acceder con Google", // Puedes cambiarlo a "Continuar con Google", etc.
    cargando: Boolean = false,
    tema: TemaBotonGoogle = TemaBotonGoogle.Oscuro,
    onClick: () -> Unit
) {
    // Definimos los colores basados en los lineamientos del documento [cite: 95]
    val (colorFondo, colorTexto, colorBorde) = when (tema) {
        TemaBotonGoogle.Claro -> Triple(Color.White, Color(0xFF1F1F1F), Color(0xFF747775))
        TemaBotonGoogle.Oscuro -> Triple(Color(0xFF131314), Color(0xFFE3E3E3), Color(0xFF8E918F))
        TemaBotonGoogle.Neutro -> Triple(Color(0xFFF2F2F2), Color(0xFF1F1F1F), Color.Transparent) // Sin borde [cite: 95]
    }

    val familiaFuenteRoboto = FontFamily(Font(R.font.roboto_medium, FontWeight.Medium))

    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp), // Forma de píldora [cite: 46]
        colors = ButtonDefaults.buttonColors(
            containerColor = colorFondo,
            contentColor = colorTexto
        ),
        border = if (tema != TemaBotonGoogle.Neutro) BorderStroke(1.dp, colorBorde) else null,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp), // Espaciado lateral [cite: 115, 129]
        modifier = modifier
            .height(40.dp), // Altura especificada para Android [cite: 110]

    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = colorTexto
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Logo de Google",
                    modifier = Modifier.size(20.dp), // Tamaño visualmente correcto
                    tint = Color.Unspecified,


                    )
                Text(
                    text = texto,
                    fontFamily = familiaFuenteRoboto, // Fuente Roboto Medium
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp, // Tamaño de fuente especificado [cite: 95]
                    color = colorTexto
                )
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun VistaPreviaBotonCargando() {
    BotonIngresarGoogleMejorado(
        tema = TemaBotonGoogle.Oscuro,
        cargando = true, // Así puedes previsualizar el estado de carga
        onClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun VistaPreviaBotonNormal() {
    BotonIngresarGoogleMejorado(
        tema = TemaBotonGoogle.Oscuro,
        cargando = false,
        onClick = {}
    )
}