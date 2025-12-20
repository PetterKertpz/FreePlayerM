// en: app/src/main/java/com/example/freeplayerm/data/local/AppDatabase.kt
package com.example.freeplayerm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.LetraDao
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.local.entity.*

/**
 * 🗄️ APP DATABASE - ROOM DATABASE v6.0
 *
 * Base de datos principal de la aplicación
 * Gestiona todas las entidades y DAOs del sistema
 *
 * Características:
 * - Versión 6 con esquema completo actualizado
 * - TypeConverters para tipos complejos
 * - 9 entidades con relaciones optimizadas
 * - 3 DAOs principales con funcionalidad completa
 * - Soporte para migraciones (configurar en DatabaseModule)
 *
 * @version 6.0 - Production Ready
 */
@Database(
    entities = [
        // Entidades base
        UsuarioEntity::class,
        ArtistaEntity::class,
        AlbumEntity::class,
        GeneroEntity::class,
        CancionEntity::class,

        // Entidades auxiliares
        ListaReproduccionEntity::class,
        DetalleListaReproduccionEntity::class,
        FavoritoEntity::class,
        LetraEntity::class
    ],
    version = 6, // ⚠️ IMPORTANTE: Versión actualizada por cambios de esquema
    exportSchema = true // Cambiar a true para producción y guardar esquemas
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // ==================== DAOs ====================

    /**
     * DAO para operaciones de usuario
     * Incluye autenticación, gestión de perfil y sesiones
     */
    abstract fun usuarioDao(): UsuarioDao

    /**
     * DAO para operaciones de canciones
     * Incluye artistas, álbumes, géneros, favoritos y listas
     */
    abstract fun cancionDao(): CancionDao

    /**
     * DAO para operaciones de letras
     * Incluye búsqueda, caché y sincronización
     */
    abstract fun letraDao(): LetraDao

    companion object {
        const val DATABASE_NAME = "freeplayerm_database"

        /**
         * Notas de versión:
         *
         * v6.0 - Actualización mayor
         * - CancionEntity: Agregados campos veces_reproducida, ultima_reproduccion,
         *   fecha_agregado, numero_pista, anio, url_streaming, calidad_audio, bitrate,
         *   letra_disponible, portada_path, es_favorita_local
         * - LetraEntity: Corregidos nombres de columnas (texto_letra en lugar de letra),
         *   agregados campos fuente, fecha_agregado, idioma, traduccion_disponible,
         *   sincronizada, url_fuente, verificada. Cambiado PrimaryKey a id_letra autoincrementable
         * - UsuarioEntity: Corregidos nombres de columnas (idUsuario en lugar de id,
         *   contrasenia en lugar de contrasenaHash), agregados campos activo, ultima_sesion,
         *   fecha_creacion (Long timestamp), nombre_completo, biografia, fecha_nacimiento,
         *   provider_id, tema_oscuro, notificaciones_habilitadas, reproduccion_automatica,
         *   calidad_preferida, idioma_preferido, estadísticas
         * - DetalleListaReproduccionEntity: Agregado campo orden, fecha_agregado,
         *   agregada_por_usuario, numero_reproducciones_en_lista
         * - ListaReproduccionEntity: Agregados campos es_publica, es_colaborativa,
         *   color_tema, fecha_creacion, fecha_modificacion, estadísticas completas,
         *   categoria, genero_principal, es_favorita, orden_visualizacion
         * - ArtistaEntity: Agregados muchos campos (nombre_real, biografia, fechas,
         *   múltiples imágenes, redes sociales, clasificación, estadísticas)
         * - AlbumEntity: Agregados campos subtitulo, múltiples portadas, fecha_lanzamiento,
         *   tipo, clasificación técnica, estadísticas, enlaces, calificación
         * - GeneroEntity: Agregados campos nombre_normalizado, jerarquía, visualización,
         *   estadísticas, clasificación
         * - FavoritoEntity: Agregados campos fecha_agregado, orden,
         *   veces_reproducida_desde_favoritos, calificacion, notas
         * - EstadisticasModels: Agregado archivo con data classes para estadísticas
         *
         * v5.0 - Versión anterior (esquema base)
         *
         * Migración recomendada:
         * Para migrar de v5 a v6, implementar Migration en DatabaseModule
         * o usar fallbackToDestructiveMigration() para desarrollo
         */
    }
}