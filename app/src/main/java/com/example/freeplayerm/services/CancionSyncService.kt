package com.example.freeplayerm.services

import android.util.Log
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
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
class CancionSyncService @Inject constructor(
    // Aquí inyectas tus repositorios o DAOs que necesites
    // private val cancionRepository: CancionRepository
) {
    private val TAG = "CancionSyncService"
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentSyncJob: Job? = null

    /**
     * Sincroniza la canción actual con el backend o base de datos local.
     */
    fun sincronizarCancionAlReproducir(cancionConArtista: CancionConArtista) {
        Log.d(TAG, "🔄 Iniciando sincronización: ${cancionConArtista.cancion.titulo}")

        // Cancela la sincronización anterior si existe
        currentSyncJob?.cancel()

        currentSyncJob = syncScope.launch {
            try {
                // Aquí implementas tu lógica de sincronización
                // Ejemplo: actualizar última reproducción, incrementar contador, etc.

                // Simulación de sincronización
                delay(100)

                Log.d(TAG, "✅ Sincronización completada: ${cancionConArtista.cancion.titulo}")

                // Ejemplo: actualizar en base de datos
                // cancionRepository.actualizarUltimaReproduccion(cancionConArtista.cancion.id)

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en sincronización: ${e.message}", e)
            }
        }
    }

    /**
     * Cancela cualquier sincronización en progreso.
     */
    fun cancelarSincronizacion() {
        Log.d(TAG, "🛑 Cancelando sincronización")
        currentSyncJob?.cancel()
        currentSyncJob = null
    }

    /**
     * Limpia recursos cuando el servicio ya no se necesita.
     * Llama a esto desde onDestroy() de MusicService.
     */
    fun limpiar() {
        Log.d(TAG, "🧹 Limpiando CancionSyncService")
        cancelarSincronizacion()
        // No necesitas cancelar syncScope si es Singleton,
        // pero puedes hacerlo si quieres liberar recursos
    }
}