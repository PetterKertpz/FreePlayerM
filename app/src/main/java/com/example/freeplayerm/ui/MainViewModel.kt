// en: app/src/main/java/com/example/freeplayerm/ui/MainViewModel.kt

package com.example.freeplayerm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UsuarioEntity
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserRepository
import com.example.freeplayerm.data.scanner.MusicScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🎯 MAIN VIEW MODEL - Coordinador Principal de la Aplicación
 *
 * Responsabilidades:
 * - Gestión del estado de autenticación
 * - Inicialización del sistema de escaneo de música
 * - Coordinación entre permisos y funcionalidades
 *
 * @author FreePlayerM
 */

// ==================== ESTADOS ====================

sealed class AuthState {
    data object Cargando : AuthState()
    data class Autenticado(val usuario: UsuarioEntity) : AuthState()
    data object NoAutenticado : AuthState()
}

data class MainUiState(
    val permisosDeAlmacenamientoConcedidos: Boolean = false,
    val sistemaDeEscaneoInicializado: Boolean = false,
    val errorInicial: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val userRepository: UserRepository,
    private val scannerManager: MusicScannerManager
) : ViewModel() {

    companion object {
        private const val TAG = "com.example.freeplayerm.ui.MainViewModel"
    }

    // ==================== UI STATE ====================

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // ==================== AUTH STATE ====================

    /**
     * Flujo reactivo del estado de autenticación.
     *
     * Observa cambios en el ID de sesión almacenado:
     * - null → NoAutenticado
     * - ID válido → Busca usuario en BD → Autenticado o NoAutenticado
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val authState: StateFlow<AuthState> = sessionRepository.idDeUsuarioActivo
        .flatMapLatest { id ->
            when (id) {
                null -> flowOf(AuthState.NoAutenticado)
                else -> userRepository.obtenerUsuarioPorIdFlow(id).map { usuario ->
                    usuario?.let { AuthState.Autenticado(it) } ?: AuthState.NoAutenticado
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Cargando
        )

    // ==================== INICIALIZACIÓN ====================

    init {
        observarEstadoDelScanner()
    }

    /**
     * Observa el estado del sistema de escaneo para reflejarlo en la UI.
     */
    private fun observarEstadoDelScanner() {
        viewModelScope.launch {
            scannerManager.estaInicializado.collect { inicializado ->
                _uiState.value = _uiState.value.copy(
                    sistemaDeEscaneoInicializado = inicializado
                )
            }
        }
    }

    // ==================== API PÚBLICA ====================

    /**
     * Llamar cuando se confirmen los permisos de almacenamiento.
     * Inicializa el sistema completo de escaneo de música.
     */
    fun onPermisosConfirmados() {
        if (_uiState.value.permisosDeAlmacenamientoConcedidos) {
            return // Ya procesado
        }

        _uiState.value = _uiState.value.copy(
            permisosDeAlmacenamientoConcedidos = true
        )

        // Inicializar sistema de escaneo (ContentObserver + WorkManager)
        scannerManager.inicializar()

        // Ejecutar primer escaneo
        scannerManager.escanearAhora()
    }

    /**
     * Llamar cuando se denieguen los permisos de almacenamiento.
     */
    fun onPermisosDenegados() {
        _uiState.value = _uiState.value.copy(
            permisosDeAlmacenamientoConcedidos = false,
            errorInicial = "Se requieren permisos de almacenamiento para escanear música"
        )
    }

    /**
     * Limpia el error mostrado en la UI.
     */
    fun limpiarError() {
        _uiState.value = _uiState.value.copy(errorInicial = null)
    }

    /**
     * Fuerza un escaneo manual de la biblioteca.
     */
    fun forzarEscaneo() {
        if (_uiState.value.permisosDeAlmacenamientoConcedidos) {
            scannerManager.escanearAhora()
        }
    }

    /**
     * Verifica si el sistema de escaneo está listo.
     */
    fun sistemaListo(): Boolean {
        return _uiState.value.permisosDeAlmacenamientoConcedidos &&
                _uiState.value.sistemaDeEscaneoInicializado
    }
}