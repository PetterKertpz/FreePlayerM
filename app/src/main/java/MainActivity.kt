// en: app/src/main/java/com/example/freeplayerm/MainActivity.kt
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
import com.example.freeplayerm.ui.AuthState
import com.example.freeplayerm.ui.MainViewModel
import com.example.freeplayerm.ui.features.nav.NavigationGraph
import com.example.freeplayerm.ui.features.nav.Routes
import com.example.freeplayerm.ui.features.player.viewmodel.PlayerViewModel
import com.example.freeplayerm.ui.features.splash.PantallaDeCarga
import com.example.freeplayerm.ui.theme.FreePlayerMTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val reproductorViewModel: PlayerViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            FreePlayerMTheme {
                val context = LocalContext.current

                // ==================== PERMISO DE ALMACENAMIENTO ====================
                val permisoAlmacenamiento = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                val launcherAlmacenamiento = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { concedido ->
                    if (concedido) {
                        mainViewModel.onPermisosConfirmados() // ← CRÍTICO
                    } else {
                        mainViewModel.onPermisosDenegados()
                    }
                }

                // ==================== PERMISO DE NOTIFICACIONES ====================
                val launcherNotificaciones = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Solo informativo, no bloquea funcionalidad */ }

                // ==================== SOLICITAR PERMISOS AL INICIAR ====================
                LaunchedEffect(Unit) {
                    // 1. Verificar/solicitar permiso de almacenamiento
                    val tienePermisoAlmacenamiento = ContextCompat.checkSelfPermission(
                        context, permisoAlmacenamiento
                    ) == PackageManager.PERMISSION_GRANTED

                    if (tienePermisoAlmacenamiento) {
                        // Ya tiene permiso, inicializar sistema de escaneo
                        mainViewModel.onPermisosConfirmados()
                    } else {
                        // Solicitar permiso
                        launcherAlmacenamiento.launch(permisoAlmacenamiento)
                    }

                    // 2. Solicitar permiso de notificaciones (Android 13+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val tienePermisoNotificaciones = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!tienePermisoNotificaciones) {
                            launcherNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // ==================== UI ====================
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState by mainViewModel.authState.collectAsStateWithLifecycle()
                    val navController = rememberNavController()

                    when (val state = authState) {
                        is AuthState.Cargando -> {
                            PantallaDeCarga()
                        }

                        is AuthState.Autenticado -> {
                            NavigationGraph(
                                navController = navController,
                                rutaDeInicio = Routes.Biblioteca.crearRuta(state.usuario.idUsuario),
                                playerViewModel = reproductorViewModel
                            )
                        }

                        is AuthState.NoAutenticado -> {
                            NavigationGraph(
                                navController = navController,
                                rutaDeInicio = Routes.Login.ruta,
                                playerViewModel = reproductorViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}