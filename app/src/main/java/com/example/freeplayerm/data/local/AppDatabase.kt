// en: app/src/main/java/com/example/freeplayerm/data/local/AppDatabase.kt
package com.example.freeplayerm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.freeplayerm.data.local.dao.*
import com.example.freeplayerm.data.local.entity.*

/**
 * 🗄️ APP DATABASE - ROOM DATABASE v8.0
 *
 * Base de datos principal de la aplicación
 * Gestiona todas las entidades y DAOs del sistema
 *
 * Características:
 * - Versión 8 con esquema completo actualizado
 * - TypeConverters para tipos complejos
 * - 20 entidades con relaciones optimizadas
 * - 10 DAOs principales con funcionalidad completa
 * - Soporte para migraciones (configurar en DatabaseModule)
 *
 * @version 8.0 - Production Ready
 */
@Database(
    entities = [
        // Entidades base principales
        UsuarioEntity::class,
        ArtistaEntity::class,
        AlbumEntity::class,
        GeneroEntity::class,
        CancionEntity::class,

        // Listas y organización
        ListaReproduccionEntity::class,
        DetalleListaReproduccionEntity::class,

        // Favoritos y preferencias
        FavoritoEntity::class,
        LetraEntity::class,

        // Nuevas entidades v7-v8
        CancionArtistaEntity::class,
        HistorialReproduccionEntity::class,
        PreferenciasUsuarioEntity::class,
        EstadoReproduccionEntity::class,
        ColaReproduccionEntity::class,

        // Características avanzadas
        LetraTraduccionEntity::class,
        GeniusAnnotationEntity::class,
        RedesSocialesArtistaEntity::class,
        CreditoAlbumEntity::class,
        GeneroMoodEntity::class,
        ListaColaboradorEntity::class,
    ],
    version = 8, // ⚠️ IMPORTANTE: Versión actualizada por cambios de esquema
    exportSchema = true // Cambiar a true para producción y guardar esquemas
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // ==================== DAOs PRINCIPALES ====================

    /**
     * DAO para operaciones de usuario
     * Incluye autenticación, gestión de perfil y sesiones
     */
    abstract fun usuarioDao(): UsuarioDao

    /**
     * DAO para operaciones de canciones
     * Incluye CRUD completo y búsquedas avanzadas
     */
    abstract fun cancionDao(): CancionDao

    /**
     * DAO para operaciones de artistas
     * Incluye gestión de biografías, imágenes y estadísticas
     */
    abstract fun artistaDao(): ArtistaDao

    /**
     * DAO para operaciones de álbumes
     * Incluye gestión de portadas, tracks y metadatos
     */
    abstract fun albumDao(): AlbumDao

    /**
     * DAO para operaciones de géneros
     * Incluye jerarquías y clasificaciones
     */
    abstract fun generoDao(): GeneroDao

    /**
     * DAO para operaciones de letras
     * Incluye búsqueda, caché y sincronización
     */
    abstract fun letraDao(): LetraDao

    // ==================== DAOs DE LISTAS Y FAVORITOS ====================

    /**
     * DAO para operaciones de playlists
     * Incluye gestión de colaborativas y públicas
     */
    abstract fun listaReproduccionDao(): ListaReproduccionDao

    /**
     * DAO para operaciones de favoritos
     * Incluye calificaciones y ordenamiento personalizado
     */
    abstract fun favoritoDao(): FavoritoDao

    // ==================== DAOs DE REPRODUCCIÓN ====================

    /**
     * DAO para operaciones del historial de reproducción
     * Incluye analytics y estadísticas de escucha
     */
    abstract fun historialReproduccionDao(): HistorialReproduccionDao

    /**
     * DAO para operaciones de la cola de reproducción
     * Incluye reordenamiento y gestión de origen
     */
    abstract fun colaReproduccionDao(): ColaReproduccionDao

    // ==================== CONFIGURACIÓN ====================

    companion object {
        const val DATABASE_NAME = "freeplayerm_database"

        /**
         * Notas de versión:
         *
         * v8.0 - Actualización mayor (Actual)
         * - Agregados todos los DAOs faltantes
         * - Sincronización completa de campos entre Entity y DAO
         * - Optimización de índices y relaciones
         * - Soporte completo para todas las entidades
         *
         * v7.0 - Entidades avanzadas
         * - CancionArtistaEntity: Relaciones múltiples artista-canción
         * - HistorialReproduccionEntity: Tracking completo de reproducciones
         * - PreferenciasUsuarioEntity: Configuración personalizada
         * - EstadoReproduccionEntity: Estado del reproductor
         * - ColaReproduccionEntity: Gestión de cola de reproducción
         * - LetraTraduccionEntity: Traducciones de letras
         * - GeniusAnnotationEntity: Anotaciones de Genius
         * - RedesSocialesArtistaEntity: Enlaces a redes sociales
         * - CreditoAlbumEntity: Créditos de álbumes
         * - GeneroMoodEntity: Estados de ánimo por género
         * - ListaColaboradorEntity: Colaboradores de playlists
         *
         * v6.0 - Actualización de campos
         * - CancionEntity: Agregados campos de reproducción y metadata
         * - LetraEntity: Corregidos nombres de columnas y agregados campos
         * - UsuarioEntity: Corregidos nombres y agregados tokens
         * - DetalleListaReproduccionEntity: Agregado ordenamiento
         * - ListaReproduccionEntity: Agregados campos de colaboración
         * - ArtistaEntity: Agregados biografía y redes sociales
         * - AlbumEntity: Agregados metadatos completos
         * - GeneroEntity: Agregada jerarquía y estadísticas
         * - FavoritoEntity: Agregadas calificaciones y notas
         *
         * v5.0 - Versión base inicial
         * - Esquema básico con entidades principales
         *
         * Migración recomendada:
         * Para migrar entre versiones, implementar Migration en DatabaseModule
         * o usar fallbackToDestructiveMigration() para desarrollo
         */
    }
}