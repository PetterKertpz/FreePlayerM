// en: app/src/main/java/com/example/freeplayerm/data/scanner/MusicContentObserver.kt
package com.example.freeplayerm.data.scanner

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import com.example.freeplayerm.data.repository.LocalMusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observer que detecta cambios en MediaStore y dispara escaneos automáticos. Usa debounce para
 * evitar múltiples escaneos cuando se añaden varios archivos.
 */
@Singleton
class MusicContentObserver
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val musicRepository: LocalMusicRepository,
) {
    private val tag = "MusicContentObserver"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var debounceJob: Job? = null
    private var isRegistered = false

    // Estado observable para integración con MusicScannerManager
    sealed class EstadoObserver {
        data object NoRegistrado : EstadoObserver()

        data object Registrado : EstadoObserver()

        data object EsperandoDebounce : EstadoObserver()

        data object Escaneando : EstadoObserver()
    }

    private val _estado = MutableStateFlow<EstadoObserver>(EstadoObserver.NoRegistrado)
    val estado: StateFlow<EstadoObserver> = _estado.asStateFlow()

    // Debounce de 3 segundos para agrupar múltiples cambios
    private val debounceMs = 3000L

    private val contentObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                Log.d(tag, "Cambio detectado en MediaStore")

                // Cancelar job anterior y reiniciar el debounce
                debounceJob?.cancel()
                _estado.value = EstadoObserver.EsperandoDebounce

                debounceJob =
                    scope.launch {
                        delay(debounceMs)
                        Log.d(tag, "Iniciando escaneo automático por cambio en MediaStore")
                        _estado.value = EstadoObserver.Escaneando
                        try {
                            musicRepository.escanearYGuardarMusica()
                        } catch (e: Exception) {
                            Log.e(tag, "Error en escaneo automático", e)
                        } finally {
                            _estado.value = EstadoObserver.Registrado
                        }
                    }
            }
        }

    /**
     * Registra el observer para escuchar cambios en archivos de audio. Llamar desde
     * Application.onCreate() o cuando se concedan permisos.
     */
    fun registrar() {
        if (isRegistered) {
            Log.d(tag, "Observer ya registrado")
            return
        }

        try {
            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true, // notifyForDescendants
                contentObserver,
            )
            isRegistered = true
            _estado.value = EstadoObserver.Registrado
            Log.d(tag, "ContentObserver registrado exitosamente")
        } catch (e: Exception) {
            Log.e(tag, "Error registrando ContentObserver", e)
            _estado.value = EstadoObserver.NoRegistrado
        }
    }

    /** Desregistra el observer. Llamar cuando ya no sea necesario. */
    fun desregistrar() {
        if (!isRegistered) return

        try {
            debounceJob?.cancel()
            context.contentResolver.unregisterContentObserver(contentObserver)
            isRegistered = false
            _estado.value = EstadoObserver.NoRegistrado
            Log.d(tag, "ContentObserver desregistrado")
        } catch (e: Exception) {
            Log.e(tag, "Error desregistrando ContentObserver", e)
        }
    }

    /** Fuerza un escaneo inmediato sin esperar el debounce. */
    fun forzarEscaneoInmediato() {
        debounceJob?.cancel()
        scope.launch { musicRepository.escanearYGuardarMusica() }
    }
}
