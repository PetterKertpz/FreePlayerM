// en: app/src/main/java/com/example/freeplayerm/data/repository/SessionRepository.kt
package com.example.freeplayerm.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Creamos la instancia de DataStore a nivel de archivo para que sea un singleton.
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion_usuario")

@Singleton
class SessionRepository @Inject constructor(
    // Inyectamos el contexto de la aplicación para mayor seguridad.
    @param:ApplicationContext private val context: Context
) {
    // Definimos una llave tipada para guardar el ID del usuario.
    // Usar un objeto companion es una buena práctica para mantener las llaves organizadas.
    private companion object {
        val ID_USUARIO_ACTIVO = intPreferencesKey("id_usuario_activo")
    }

    /**
     * Un Flow público que emite el ID del usuario activo.
     * La UI (especialmente el SplashViewModel) observará este Flow.
     * Si el valor es nulo, significa que no hay sesión activa.
     */
    val idDeUsuarioActivo: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[ID_USUARIO_ACTIVO]
        }

    // En SessionRepository.kt
    suspend fun guardarIdDeUsuario(idDeUsuario: Int) { // <-- Acepta un Int
        context.dataStore.edit { preferences ->
            preferences[ID_USUARIO_ACTIVO] = idDeUsuario // <-- Lo usa directamente
        }
    }

    /**
     * Limpia el ID del usuario de DataStore, cerrando efectivamente la sesión.
     */
    suspend fun cerrarSesion() {
        context.dataStore.edit { preferences ->
            preferences.remove(ID_USUARIO_ACTIVO)
        }
    }
}