package com.example.freeplayerm.com.example.freeplayerm.services

import android.content.ContentValues.TAG
import android.util.Log
import com.example.freeplayerm.data.local.entity.relations.CancionConArtista
import com.example.freeplayerm.data.repository.GeniusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancionSyncService @Inject constructor(
    private val geniusRepository: GeniusRepository
) {
    private val tag = "CancionSyncService"
    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null

    /**
     * Inicia la sincronización de una canción cuando se reproduce
     * Retorna inmediatamente, la sincronización corre en segundo plano
     */
    fun sincronizarCancionAlReproducir(cancionConArtista: CancionConArtista) {
        // Cancelar sincronización anterior si existe
        syncJob?.cancel()

        // Iniciar nueva sincronización
        syncJob = scope.launch {
            try {
                val cancionCorregida = geniusRepository.sincronizarCancionAlReproducir(cancionConArtista)
                if (cancionCorregida != null) {
                    Log.d(TAG, " Sincronización exitosa") // Cambié a Log.d para éxito
                } else {
                    Log.w(TAG, " Sincronización falló")
                }
            } catch (e: Exception) {
                Log.e(TAG, " * Error en sincronización: ${e.message}")
            }
        }
    }

    /**
     * Cancela cualquier sincronización en curso
     */
    fun cancelarSincronizacion() {
        syncJob?.cancel()
        syncJob = null
        Log.d(tag, "⏹️ Sincronización cancelada")
    }

    /**
     * Verifica si hay una sincronización en curso
     */
    fun estaSincronizando(): Boolean {
        return syncJob?.isActive == true
    }
}