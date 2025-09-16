// en: app/src/main/java/com/example/freeplayerm/ui/features/biblioteca/components/CuerpoGeneros.kt
package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.GeneroEntity
import com.example.freeplayerm.ui.theme.AppColors

@Composable
fun CuerpoGeneros(
    modifier: Modifier = Modifier,
    generos: List<GeneroEntity>,
    onGeneroClick: (GeneroEntity) -> Unit
) {
    if (generos.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No se encontraron géneros.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 10.dp)
        ) {
            items(generos) { genero ->
                GeneroItem(
                    genero = genero,
                    onClick = { onGeneroClick(genero) }
                )
            }
        }
    }
}

@Composable
private fun GeneroItem(
    genero: GeneroEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 5.dp, vertical = 5.dp)
            .border(
                width = 1.dp,
                color = AppColors.GrisClaro,
                shape = RoundedCornerShape(8.dp)

            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        Text(
            text = genero.nombre,
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp,
            modifier = Modifier.weight(1f),
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis
        )
        Spacer(
            modifier = Modifier
                .width(10.dp)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Ver canciones del género",
            tint = Color.Gray
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewCuerpoGeneros() {
    val listaDePrueba = listOf(
        GeneroEntity(1, "Rock"),
        GeneroEntity(2, "Pop"),
        GeneroEntity(3, "Electrónica"),
        GeneroEntity(4, "Rock"),
        GeneroEntity(5, "Pop"),
        GeneroEntity(6, "Electrónica"),
        GeneroEntity(7, "Rock"),
        GeneroEntity(8, "Pop"),
        GeneroEntity(9, "Electrónica")
    )
    MaterialTheme {
        CuerpoGeneros(generos = listaDePrueba, onGeneroClick = {})
    }
}