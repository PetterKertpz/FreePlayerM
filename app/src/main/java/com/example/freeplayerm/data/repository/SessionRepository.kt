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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion_usuario")

@Singleton
class SessionRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private companion object {
        val ID_USUARIO_ACTIVO = intPreferencesKey("id_usuario_activo")
    }

    val idDeUsuarioActivo: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[ID_USUARIO_ACTIVO]
        }

    suspend fun guardarIdDeUsuario(idDeUsuario: Int) {
        context.dataStore.edit { preferences ->
            preferences[ID_USUARIO_ACTIVO] = idDeUsuario
        }
    }

    suspend fun cerrarSesion() {
        context.dataStore.edit { preferences ->
            preferences.remove(ID_USUARIO_ACTIVO)
        }
    }
}