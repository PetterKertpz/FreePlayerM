package com.example.freeplayerm.services

import android.util.Log
import com.example.freeplayerm.data.local.entity.relations.SongWithArtist
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio para sincronizar el estado de reproducción de canciones.
 *
 * IMPORTANTE: Debe estar anotado con @Singleton para que Hilt lo inyecte correctamente.
 */
@Singleton
class SongSyncService
@Inject
constructor(
    // Aquí inyectas tus repositorios o DAOs que necesites
    // private val cancionRepository: CancionRepository
) {

    companion object {
        private const val TAG = "SongSyncService"
    }

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, "Error en sincronización: ${throwable.message}", throwable)
    }
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + exceptionHandler)
    private var currentSyncJob: Job? = null

    /** Sincroniza la canción actual con el backend o base de datos local. */
    fun sincronizarCancionAlReproducir(songWithArtist: SongWithArtist) {
        Log.d(TAG, "🔄 Iniciando sincronización: ${songWithArtist.cancion.titulo}")

        // Cancela la sincronización anterior si existe
        currentSyncJob?.cancel()

        currentSyncJob =
            syncScope.launch {
                try {
                    // Aquí implementas tu lógica de sincronización
                    // Ejemplo: actualizar última reproducción, incrementar contador, etc.

                    // Simulación de sincronización
                    delay(100)

                    Log.d(TAG, "✅ Sincronización completada: ${songWithArtist.cancion.titulo}")

                    // Ejemplo: actualizar en base de datos
                    // cancionRepository.actualizarUltimaReproduccion(songWithArtist.cancion.id)

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error en sincronización: ${e.message}", e)
                }
            }
    }

    /** Cancela cualquier sincronización en progreso. */
    fun cancelarSincronizacion() {
        Log.d(TAG, "🛑 Cancelando sincronización")
        currentSyncJob?.cancel()
        currentSyncJob = null
    }

    /**
     * Limpia recursos cuando el servicio ya no se necesita. Llama a esto desde onDestroy() de
     * MusicService.
     */
    fun limpiar() {
        Log.d(TAG, "🧹 Limpiando SongSyncService")
        cancelarSincronizacion()
        syncScope.coroutineContext[Job]?.cancel()
        // No necesitas cancelar syncScope si es Singleton,
        // pero puedes hacerlo si quieres liberar recursos
    }
}
