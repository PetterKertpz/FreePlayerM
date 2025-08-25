package com.example.freeplayerm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.freeplayerm.ui.features.login.PantallaLogin
import com.example.freeplayerm.ui.features.login.PantallaLogin // <-- Importa nuestra nueva pantalla
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Anotación necesaria para que Hilt pueda inyectar dependencias en esta Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FreePlayerMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Aquí llamamos a nuestra pantalla de Login
                    PantallaLogin()
                }
            }
        }
    }
}