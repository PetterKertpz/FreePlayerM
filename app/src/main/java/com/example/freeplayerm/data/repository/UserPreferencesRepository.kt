package com.example.freeplayerm.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.freeplayerm.data.local.dao.UserPreferencesDao
import com.example.freeplayerm.data.local.entity.UserPreferencesEntity
import com.example.freeplayerm.ui.features.library.CriterioDeOrdenamiento
import com.example.freeplayerm.ui.features.library.DireccionDeOrdenamiento
import com.example.freeplayerm.ui.features.library.NivelZoom
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by
preferencesDataStore(name = "user_preferences")

/**
 * ⚙️ USER PREFERENCES REPOSITORY
 *
 * Repositorio híbrido que maneja:
 * 1. Preferencias de UI/ordenamiento (DataStore) - Ligeras y rápidas
 * 2. Preferencias de usuario completas (Room) - Persistentes y relacionadas con UserEntity
 */
@Singleton
class UserPreferencesRepository
@Inject
constructor(
   @ApplicationContext private val context: Context,
   private val userPreferencesDao: UserPreferencesDao
) {
   
   // ==================== DATASTORE (UI PREFERENCES) ====================
   
   private object Keys {
      val SORT_CRITERION = stringPreferencesKey("sort_criterion")
      val SORT_DIRECTION = stringPreferencesKey("sort_direction")
      val ZOOM_LEVEL = stringPreferencesKey("zoom_level")
   }
   
   val userPreferences: Flow<UserPreferences> =
      context.dataStore.data.map { preferences ->
         val sortCriterion =
            CriterioDeOrdenamiento.valueOf(
               preferences[Keys.SORT_CRITERION] ?: CriterioDeOrdenamiento.POR_TITULO.name
            )
         val sortDirection =
            DireccionDeOrdenamiento.valueOf(
               preferences[Keys.SORT_DIRECTION] ?: DireccionDeOrdenamiento.ASCENDENTE.name
            )
         val zoomLevel = NivelZoom.valueOf(preferences[Keys.ZOOM_LEVEL] ?: NivelZoom.NORMAL.name)
         UserPreferences(sortCriterion, sortDirection, zoomLevel)
      }
   
   suspend fun updateSortCriterion(criterion: CriterioDeOrdenamiento) {
      context.dataStore.edit { preferences -> preferences[Keys.SORT_CRITERION] = criterion.name }
   }
   
   suspend fun updateSortDirection(direction: DireccionDeOrdenamiento) {
      context.dataStore.edit { preferences -> preferences[Keys.SORT_DIRECTION] = direction.name }
   }
   
   suspend fun updateZoomLevel(level: NivelZoom) {
      context.dataStore.edit { preferences -> preferences[Keys.ZOOM_LEVEL] = level.name }
   }
   
   // ==================== ROOM (USER PREFERENCES ENTITY) ====================
   
   suspend fun insertarPreferencias(preferencias: UserPreferencesEntity) {
      userPreferencesDao.insertarPreferencias(preferencias)
   }
   
   suspend fun actualizarPreferencias(preferencias: UserPreferencesEntity) {
      val preferenciaActualizada = preferencias.copy(
         ultimaActualizacion = System.currentTimeMillis()
      )
      userPreferencesDao.actualizarPreferencias(preferenciaActualizada)
   }
   
   suspend fun obtenerPreferenciasPorId(idUsuario: Int): UserPreferencesEntity? {
      return userPreferencesDao.obtenerPreferenciasPorId(idUsuario)
   }
   
   fun obtenerPreferenciasPorIdFlow(idUsuario: Int): Flow<UserPreferencesEntity?> {
      return userPreferencesDao.obtenerPreferenciasPorIdFlow(idUsuario)
   }
   
   suspend fun crearPreferenciasPorDefecto(idUsuario: Int): UserPreferencesEntity {
      val preferenciasPorDefecto = UserPreferencesEntity.crearPorDefecto(idUsuario)
      userPreferencesDao.insertarPreferencias(preferenciasPorDefecto)
      return preferenciasPorDefecto
   }
   
   suspend fun obtenerOCrearPreferencias(idUsuario: Int): UserPreferencesEntity {
      return obtenerPreferenciasPorId(idUsuario)
         ?: crearPreferenciasPorDefecto(idUsuario)
   }
   
   suspend fun existenPreferencias(idUsuario: Int): Boolean {
      return userPreferencesDao.existenPreferencias(idUsuario)
   }
   
   suspend fun eliminarPreferencias(idUsuario: Int) {
      userPreferencesDao.eliminarPreferenciasPorUsuario(idUsuario)
   }
}

data class UserPreferences(
   val sortCriterion: CriterioDeOrdenamiento,
   val sortDirection: DireccionDeOrdenamiento,
   val zoomLevel: NivelZoom = NivelZoom.NORMAL,
)

@Deprecated("Usar UserPreferences", ReplaceWith("UserPreferences"))
typealias UserSortPreferences = UserPreferences