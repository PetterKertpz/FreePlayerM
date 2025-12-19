package com.example.freeplayerm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.freeplayerm.ui.features.splash.PantallaDeCarga
import com.example.freeplayerm.ui.features.nav.GrafoDeNavegacion
import com.example.freeplayerm.ui.features.nav.Rutas
import com.example.freeplayerm.ui.features.reproductor.ReproductorViewModel
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Inyectamos el MainViewModel para la lógica de sesión
    private val mainViewModel: MainViewModel by viewModels()

    // --- CAMBIO CLAVE ---
    // Inyectamos el ReproductorViewModel a nivel de Actividad.
    // Su estado persistirá mientras la actividad esté viva.
    private val reproductorViewModel: ReproductorViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FreePlayerMTheme {
                // Lógica para pedir permiso de notificación en Android 13+
                val context = LocalContext.current
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { isGranted ->
                        if (isGranted) {
                            // Permiso concedido, el servicio podrá mostrar notificaciones
                        }
                    }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 1. Obtenemos el NUEVO estado de autenticación
                    val authState by mainViewModel.authState.collectAsStateWithLifecycle()
                    val navController = rememberNavController()

                    // 2. Usamos 'when' para decidir qué pantalla completa mostrar
                    when (val state = authState) {
                        is AuthState.Cargando -> {
                            PantallaDeCarga()
                        }

                        is AuthState.Autenticado -> {
                            GrafoDeNavegacion(
                                navController = navController,
                                rutaDeInicio = Rutas.Biblioteca.crearRuta(state.usuario.id),
                                reproductorViewModel = reproductorViewModel
                            )
                        }

                        is AuthState.NoAutenticado -> {
                            GrafoDeNavegacion(
                                navController = navController,
                                rutaDeInicio = Rutas.Login.ruta,
                                reproductorViewModel = reproductorViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}