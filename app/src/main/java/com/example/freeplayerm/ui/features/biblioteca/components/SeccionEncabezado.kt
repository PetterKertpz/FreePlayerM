package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freeplayerm.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionEncabezado(

) {
    var menus = listOf("Canciones", "Listas", "Álbumes", "Artistas", "Géneros", "Favoritos")
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
                    color = AppColors.Negro,
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic

                )
            },
            actions = {

            },
            colors = TopAppBarColors(
                containerColor = AppColors.Transparente,
                scrolledContainerColor = AppColors.Transparente,
                navigationIconContentColor = AppColors.Transparente,
                titleContentColor = AppColors.Transparente,
                actionIconContentColor = AppColors.Transparente
            ),
            modifier = Modifier
                .fillMaxWidth()
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(
                horizontal = 5.dp,
                vertical = 0.dp
            ),
            modifier = Modifier
                .fillMaxWidth()

        ) {
            items (
                menus
            ) { menu ->
                Button(
                    onClick = {

                    },
                    colors = ButtonColors(
                        containerColor = AppColors.Negro,
                        contentColor = AppColors.RojoFuerte,
                        disabledContainerColor = AppColors.Negro,
                        disabledContentColor = AppColors.RojoFuerte
                    ),
                    contentPadding = PaddingValues(
                        horizontal = 10.dp,
                        vertical = 0.dp
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = AppColors.GrisProfundo
                    ),
                    modifier = Modifier
                        .padding(0.dp)
                ) {
                    Text(
                        text = menu,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true,
    device = "spec:width=1080px,height=2340px,dpi=440"
)
fun Preview() {
    SeccionEncabezado()
}