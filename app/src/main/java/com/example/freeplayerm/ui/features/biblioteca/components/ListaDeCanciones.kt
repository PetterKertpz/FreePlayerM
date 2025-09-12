package com.example.freeplayerm.ui.features.biblioteca.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.freeplayerm.ui.theme.AppColors

@Composable
fun ListaDeCanciones(modifier: Modifier = Modifier) {
    Column (
        Modifier
            .fillMaxWidth()
            .background(AppColors.GrisProfundo)



    ) {
        Text(
            "LISTA",
            color = AppColors.Negro,
        )
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewListaDeCanciones() {
    ListaDeCanciones(
    )
}