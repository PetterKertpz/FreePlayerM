package com.example.freeplayerm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freeplayerm.data.local.entity.UserEntity
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserPreferencesRepository
import com.example.freeplayerm.data.repository.UserRepository
import com.example.freeplayerm.data.scanner.MusicScannerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 🎯 MAIN VIEW MODEL - Coordinador Principal de la Aplicación
 *
 * Responsabilidades:
 * - Gestión del estado de autenticación
 * - Inicialización del sistema de escaneo de música
 * - Verificación y creación de preferencias de usuario
 * - Coordinación entre permisos y funcionalidades
 *
 * @author FreePlayerM
 */

// ==================== ESTADOS ====================

sealed class AuthState {
   data object Cargando : AuthState()
   data class Autenticado(val usuario: UserEntity) : AuthState()
   data object NoAutenticado : AuthState()
}

data class MainUiState(
   val permisosDeAlmacenamientoConcedidos: Boolean = false,
   val sistemaDeEscaneoInicializado: Boolean = false,
   val preferenciasInicializadas: Boolean = false,
   val errorInicial: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
   private val sessionRepository: SessionRepository,
   private val userRepository: UserRepository,
   private val userPreferencesRepository: UserPreferencesRepository,
   private val scannerManager: MusicScannerManager
) : ViewModel() {
   
   companion object {
      private const val TAG = "MainViewModel"
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
    * - Verifica y crea preferencias si no existen
    */
   @OptIn(ExperimentalCoroutinesApi::class)
   val authState: StateFlow<AuthState> = sessionRepository.idDeUsuarioActivo
      .flatMapLatest { id ->
         when (id) {
            null -> {
               android.util.Log.d(TAG, "No hay sesión activa")
               flowOf(AuthState.NoAutenticado)
            }
            else -> {
               userRepository.obtenerUsuarioPorIdFlow(id).map { usuario ->
                  if (usuario != null) {
                     android.util.Log.d(TAG, "Usuario autenticado: ID=$id, correo=${usuario.correo}")
                     
                     // Verificar y crear preferencias en segundo plano
                     verificarYCrearPreferencias(id)
                     
                     AuthState.Autenticado(usuario)
                  } else {
                     android.util.Log.w(TAG, "Usuario con ID=$id no encontrado en BD")
                     AuthState.NoAutenticado
                  }
               }
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
      android.util.Log.d(TAG, "MainViewModel inicializado")
      observarEstadoDelScanner()
   }
   
   /**
    * Observa el estado del sistema de escaneo para reflejarlo en la UI.
    */
   private fun observarEstadoDelScanner() {
      viewModelScope.launch {
         scannerManager.estaInicializado.collect { inicializado ->
            android.util.Log.d(TAG, "Estado del scanner: inicializado=$inicializado")
            _uiState.value = _uiState.value.copy(
               sistemaDeEscaneoInicializado = inicializado
            )
         }
      }
   }
   
   /**
    * Verifica si existen preferencias para el usuario y las crea si no existen.
    * Esta función se ejecuta de manera asíncrona para no bloquear el flujo de autenticación.
    *
    * @param userId ID del usuario a verificar
    */
   private fun verificarYCrearPreferencias(userId: Int) {
      viewModelScope.launch {
         try {
            val existenPrefs = userPreferencesRepository.existenPreferencias(userId)
            
            if (!existenPrefs) {
               android.util.Log.d(TAG, "Creando preferencias por defecto para usuario ID=$userId")
               userPreferencesRepository.crearPreferenciasPorDefecto(userId)
               android.util.Log.d(TAG, "Preferencias creadas exitosamente para usuario ID=$userId")
            } else {
               android.util.Log.d(TAG, "Usuario ID=$userId ya tiene preferencias")
            }
            
            _uiState.value = _uiState.value.copy(
               preferenciasInicializadas = true
            )
         } catch (e: Exception) {
            android.util.Log.e(TAG, "Error al verificar/crear preferencias: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
               errorInicial = "Error al inicializar preferencias: ${e.message}"
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
         android.util.Log.d(TAG, "Permisos ya confirmados previamente")
         return // Ya procesado
      }
      
      android.util.Log.d(TAG, "Permisos de almacenamiento confirmados")
      
      _uiState.value = _uiState.value.copy(
         permisosDeAlmacenamientoConcedidos = true
      )
      
      // Inicializar sistema de escaneo (ContentObserver + WorkManager)
      scannerManager.inicializar()
      
      // Ejecutar primer escaneo
      scannerManager.escanearAhora()
      
      android.util.Log.d(TAG, "Sistema de escaneo inicializado y primer escaneo ejecutado")
   }
   
   /**
    * Llamar cuando se denieguen los permisos de almacenamiento.
    */
   fun onPermisosDenegados() {
      android.util.Log.w(TAG, "Permisos de almacenamiento denegados")
      
      _uiState.value = _uiState.value.copy(
         permisosDeAlmacenamientoConcedidos = false,
         errorInicial = "Se requieren permisos de almacenamiento para escanear música"
      )
   }
   
   /**
    * Limpia el error mostrado en la UI.
    */
   fun limpiarError() {
      android.util.Log.d(TAG, "Limpiando mensaje de error")
      _uiState.value = _uiState.value.copy(errorInicial = null)
   }
   
   /**
    * Fuerza un escaneo manual de la biblioteca.
    */
   fun forzarEscaneo() {
      if (_uiState.value.permisosDeAlmacenamientoConcedidos) {
         android.util.Log.d(TAG, "Forzando escaneo manual de biblioteca")
         scannerManager.escanearAhora()
      } else {
         android.util.Log.w(TAG, "Intento de escaneo sin permisos de almacenamiento")
         _uiState.value = _uiState.value.copy(
            errorInicial = "Se requieren permisos de almacenamiento para escanear música"
         )
      }
   }
   
   /**
    * Verifica si el sistema de escaneo está listo.
    *
    * @return true si hay permisos y el sistema está inicializado
    */
   fun sistemaListo(): Boolean {
      val listo = _uiState.value.permisosDeAlmacenamientoConcedidos &&
            _uiState.value.sistemaDeEscaneoInicializado
      
      android.util.Log.d(TAG, "Estado del sistema: listo=$listo")
      return listo
   }
   
   /**
    * Verifica si las preferencias del usuario están inicializadas.
    *
    * @return true si las preferencias están listas
    */
   fun preferenciasListas(): Boolean {
      return _uiState.value.preferenciasInicializadas
   }
   
   /**
    * Reinicializa las preferencias del usuario actual a sus valores por defecto.
    * Útil para casos de corrupción de datos o reset solicitado por el usuario.
    */
   fun reinicializarPreferencias() {
      viewModelScope.launch {
         try {
            val userId = sessionRepository.idDeUsuarioActivo.first()
            if (userId != null) {
               android.util.Log.d(TAG, "Reinicializando preferencias para usuario ID=$userId")
               userPreferencesRepository.crearPreferenciasPorDefecto(userId)
               android.util.Log.d(TAG, "Preferencias reinicializadas exitosamente")
            }
         } catch (e: Exception) {
            android.util.Log.e(TAG, "Error al reinicializar preferencias: ${e.message}", e)
            _uiState.value = _uiState.value.copy(
               errorInicial = "Error al reinicializar preferencias: ${e.message}"
            )
         }
      }
   }
   
   // ==================== LIFECYCLE ====================
   
   override fun onCleared() {
      super.onCleared()
      android.util.Log.d(TAG, "MainViewModel destruido")
   }
}