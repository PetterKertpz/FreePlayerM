// en: app/src/main/java/com/example/freeplayerm/data/scanner/MusicContentObserver.kt
package com.example.freeplayerm.data.scanner

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicContentObserver @Inject constructor(
   @ApplicationContext private val context: Context,
) {
   companion object {
      private const val TAG = "MusicContentObserver"
      private const val DEBOUNCE_MS = 3000L
      
      private val URIS_A_OBSERVAR = listOf(
         MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
         MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
      )
   }
   
   // Scope con lifecycle controlado
   private var scope: CoroutineScope? = null
   private var debounceJob: Job? = null
   private var isRegistered = false
   
   // Callback para notificar cambios (inyectado por MusicScannerManager)
   private var onChangeCallback: (suspend () -> Unit)? = null
   
   sealed class Estado {
      data object Inactivo : Estado()
      data object Registrado : Estado()
      data object EsperandoDebounce : Estado()
      data object Notificando : Estado()
   }
   
   private val _estado = MutableStateFlow<Estado>(Estado.Inactivo)
   val estado: StateFlow<Estado> = _estado.asStateFlow()
   
   private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
      override fun onChange(selfChange: Boolean, uri: Uri?) {
         super.onChange(selfChange, uri)
         Log.d(TAG, "Cambio detectado: $uri")
         programarNotificacion()
      }
   }
   
   fun setOnChangeListener(callback: suspend () -> Unit) {
      onChangeCallback = callback
   }
   
   fun registrar() {
      if (isRegistered) {
         Log.d(TAG, "Observer ya registrado")
         return
      }
      
      try {
         // Crear nuevo scope
         scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
         
         URIS_A_OBSERVAR.forEach { uri ->
            context.contentResolver.registerContentObserver(
               uri,
               true,
               contentObserver,
            )
         }
         
         isRegistered = true
         _estado.value = Estado.Registrado
         Log.d(TAG, "ContentObserver registrado para ${URIS_A_OBSERVAR.size} URIs")
      } catch (e: Exception) {
         Log.e(TAG, "Error registrando ContentObserver", e)
         _estado.value = Estado.Inactivo
      }
   }
   
   fun desregistrar() {
      if (!isRegistered) return
      
      try {
         debounceJob?.cancel()
         context.contentResolver.unregisterContentObserver(contentObserver)
         isRegistered = false
         _estado.value = Estado.Inactivo
         Log.d(TAG, "ContentObserver desregistrado")
      } catch (e: Exception) {
         Log.e(TAG, "Error desregistrando ContentObserver", e)
      }
   }
   
   // Cleanup completo - llamar desde onDestroy o cuando ya no se necesite
   fun destroy() {
      desregistrar()
      scope?.cancel()
      scope = null
      onChangeCallback = null
      Log.d(TAG, "Observer destruido completamente")
   }
   
   fun forzarNotificacion() {
      debounceJob?.cancel()
      scope?.launch {
         ejecutarCallback()
      }
   }
   
   private fun programarNotificacion() {
      debounceJob?.cancel()
      _estado.value = Estado.EsperandoDebounce
      
      debounceJob = scope?.launch {
         delay(DEBOUNCE_MS)
         ejecutarCallback()
      }
   }
   
   private suspend fun ejecutarCallback() {
      _estado.value = Estado.Notificando
      try {
         onChangeCallback?.invoke()
      } catch (e: Exception) {
         Log.e(TAG, "Error en callback de cambio", e)
      } finally {
         if (isRegistered) {
            _estado.value = Estado.Registrado
         }
      }
   }
}