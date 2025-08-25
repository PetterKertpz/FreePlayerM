package com.example.freeplayerm.ui.features.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Esta es nuestra pantalla principal. Por ahora, es solo un 'Box'.
 * Un Box es el contenedor más simple, como un lienzo en blanco.
 * Ocupa toda la pantalla (Modifier.fillMaxSize()) y tiene el color de fondo de nuestro tema.
 */
@Composable
fun PantallaLogin() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Aquí dentro, en este espacio, es donde añadiremos nuestros componentes.
        // Por ahora, está vacío.
        Column {
            Text(
                text = "Pantalla de Login",

                )
        }

    }
}

/**
 * Esta es la vista previa para nuestro lienzo en blanco.
 * Nos permite ver la pantalla vacía en la ventana de diseño de Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun VistaPreviaPantallaLogin() {
    PantallaLogin()
}