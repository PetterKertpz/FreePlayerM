// app/src/main/java/com/example/freeplayerm/data/repository/SessionRepository.kt
package com.example.freeplayerm.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion_usuario")

/**
 * 👤 SESSION REPOSITORY
 *
 * Gestiona la sesión del usuario actual usando DataStore
 * Almacena el ID del usuario activo de forma persistente
 */
@Singleton
class SessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private companion object {
        val ID_USUARIO_ACTIVO = intPreferencesKey("id_usuario_activo")
    }

    /**
     * Flow que emite el ID del usuario activo
     * Emite null si no hay sesión activa
     */
    val idDeUsuarioActivo: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[ID_USUARIO_ACTIVO]
        }

    /**
     * Guarda el ID del usuario activo
     *
     * @param idDeUsuario ID del usuario que inicia sesión
     */
    suspend fun guardarIdDeUsuario(idDeUsuario: Int) {
        context.dataStore.edit { preferences ->
            preferences[ID_USUARIO_ACTIVO] = idDeUsuario
        }
    }
    suspend fun guardarToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("session_token")] = token
        }
    }

    val tokenActual: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey("session_token")]
        }

    suspend fun validarYRenovarToken(): Boolean {
        val token = tokenActual.first() ?: return false
        // Validar con backend o Room
        return true // Implementar lógica real
    }
    /**
     * Cierra la sesión actual
     * Elimina el ID del usuario del almacenamiento
     */
    suspend fun cerrarSesion() {
        context.dataStore.edit { preferences ->
            preferences.remove(ID_USUARIO_ACTIVO)
        }
    }

    /**
     * Verifica si hay una sesión activa
     *
     * @return true si hay un usuario logueado
     */
    suspend fun haySesionActiva(): Boolean {
        return context.dataStore.data
            .map { preferences -> preferences[ID_USUARIO_ACTIVO] != null }
            .first()
    }
}