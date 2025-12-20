package com.example.freeplayerm.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.freeplayerm.ui.features.biblioteca.CriterioDeOrdenamiento
import com.example.freeplayerm.ui.features.biblioteca.DireccionDeOrdenamiento
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SORT_CRITERION = stringPreferencesKey("sort_criterion")
        val SORT_DIRECTION = stringPreferencesKey("sort_direction")
    }

    val userPreferences: Flow<UserSortPreferences> = context.dataStore.data
        .map { preferences ->
            val sortCriterion = CriterioDeOrdenamiento.valueOf(
                preferences[Keys.SORT_CRITERION] ?: CriterioDeOrdenamiento.NINGUNO.name
            )
            val sortDirection = DireccionDeOrdenamiento.valueOf(
                preferences[Keys.SORT_DIRECTION] ?: DireccionDeOrdenamiento.ASCENDENTE.name
            )
            UserSortPreferences(sortCriterion, sortDirection)
        }

    suspend fun updateSortCriterion(criterion: CriterioDeOrdenamiento) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SORT_CRITERION] = criterion.name
        }
    }

    suspend fun updateSortDirection(direction: DireccionDeOrdenamiento) {
        context.dataStore.edit { preferences ->
            preferences[Keys.SORT_DIRECTION] = direction.name
        }
    }
}

data class UserSortPreferences(
    val sortCriterion: CriterioDeOrdenamiento,
    val sortDirection: DireccionDeOrdenamiento
)