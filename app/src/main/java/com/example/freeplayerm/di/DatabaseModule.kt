// en: app/src/main/java/com/example/freeplayerm/di/DatabaseModule.kt
package com.example.freeplayerm.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.freeplayerm.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🔧 DATABASE MODULE - HILT DI
 *
 * Módulo de Hilt para proveer la base de datos Room
 * Incluye configuración de migraciones y fallbacks
 *
 * @version 2.0 - With Migrations
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provee la instancia singleton de la base de datos
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            // ⚠️ SOLO PARA DESARROLLO: Elimina y recrea la BD en caso de conflicto
            // Para PRODUCCIÓN, implementar migraciones apropiadas
            .fallbackToDestructiveMigration(false)

            // Alternativa para PRODUCCIÓN (comentar fallbackToDestructiveMigration):
            // .addMigrations(MIGRATION_5_6)

            // Permite queries en el thread principal (NO RECOMENDADO para producción)
            // .allowMainThreadQueries()

            .build()
    }

    /**
     * Provee el DAO de Usuario
     */
    @Provides
    @Singleton
    fun provideUsuarioDao(database: AppDatabase) = database.usuarioDao()

    /**
     * Provee el DAO de Canción
     */
    @Provides
    @Singleton
    fun provideCancionDao(database: AppDatabase) = database.cancionDao()

    /**
     * Provee el DAO de Letra
     */
    @Provides
    @Singleton
    fun provideLetraDao(database: AppDatabase) = database.letraDao()

    // ==================== MIGRACIONES ====================

    /**
     * Migración de versión 5 a 6
     *
     * IMPORTANTE: Este es un EJEMPLO de cómo estructurar las migraciones.
     * En PRODUCCIÓN, debes escribir las sentencias SQL específicas para cada
     * cambio de esquema basándote en tus cambios reales.
     *
     * Recomendación: Durante desarrollo, usa fallbackToDestructiveMigration()
     * En producción, implementa migraciones apropiadas para no perder datos.
     */
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // ==================== CANCIONES ====================
            // Agregar nuevas columnas a canciones
            db.execSQL("ALTER TABLE canciones ADD COLUMN veces_reproducida INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE canciones ADD COLUMN ultima_reproduccion INTEGER")
            db.execSQL("ALTER TABLE canciones ADD COLUMN fecha_agregado INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            db.execSQL("ALTER TABLE canciones ADD COLUMN numero_pista INTEGER")
            db.execSQL("ALTER TABLE canciones ADD COLUMN anio INTEGER")
            db.execSQL("ALTER TABLE canciones ADD COLUMN url_streaming TEXT")
            db.execSQL("ALTER TABLE canciones ADD COLUMN calidad_audio TEXT")
            db.execSQL("ALTER TABLE canciones ADD COLUMN bitrate INTEGER")
            db.execSQL("ALTER TABLE canciones ADD COLUMN letra_disponible INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE canciones ADD COLUMN portada_path TEXT")
            db.execSQL("ALTER TABLE canciones ADD COLUMN es_favorita_local INTEGER NOT NULL DEFAULT 0")

            // Crear índices para canciones
            db.execSQL("CREATE INDEX IF NOT EXISTS index_canciones_veces_reproducida ON canciones(veces_reproducida)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_canciones_fecha_agregado ON canciones(fecha_agregado)")

            // ==================== LETRAS ====================
            // Como los cambios en letras son sustanciales, recrear la tabla
            db.execSQL("DROP TABLE IF EXISTS letras")
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS letras (
                    id_letra INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    id_cancion INTEGER NOT NULL,
                    texto_letra TEXT NOT NULL,
                    fuente TEXT NOT NULL DEFAULT 'manual',
                    fecha_agregado INTEGER NOT NULL,
                    idioma TEXT,
                    traduccion_disponible INTEGER NOT NULL DEFAULT 0,
                    sincronizada INTEGER NOT NULL DEFAULT 0,
                    url_fuente TEXT,
                    verificada INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY(id_cancion) REFERENCES canciones(id_cancion) ON DELETE CASCADE
                )
            """)
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_letras_id_cancion ON letras(id_cancion)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_letras_fuente ON letras(fuente)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_letras_fecha_agregado ON letras(fecha_agregado)")

            // ==================== USUARIOS ====================
            // Agregar nuevas columnas a usuarios
            db.execSQL("ALTER TABLE usuarios ADD COLUMN activo INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN ultima_sesion INTEGER")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN nombre_completo TEXT")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN biografia TEXT")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN fecha_nacimiento INTEGER")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN provider_id TEXT")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN tema_oscuro INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN notificaciones_habilitadas INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN reproduccion_automatica INTEGER NOT NULL DEFAULT 1")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN calidad_preferida TEXT NOT NULL DEFAULT 'HIGH'")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN idioma_preferido TEXT NOT NULL DEFAULT 'es'")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN total_reproducciones INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN total_favoritos INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN total_listas INTEGER NOT NULL DEFAULT 0")

            // Crear índices para usuarios
            db.execSQL("CREATE INDEX IF NOT EXISTS index_usuarios_ultima_sesion ON usuarios(ultima_sesion)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_usuarios_activo ON usuarios(activo)")

            // ==================== ARTISTAS ====================
            // Agregar campos a artistas
            db.execSQL("ALTER TABLE artistas ADD COLUMN nombre_normalizado TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN nombre_real TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN ciudad_origen TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN biografia TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN fecha_nacimiento INTEGER")
            db.execSQL("ALTER TABLE artistas ADD COLUMN fecha_inicio_carrera INTEGER")
            db.execSQL("ALTER TABLE artistas ADD COLUMN image_path_local TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN thumbnail_url TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN banner_url TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN genius_url TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN sitio_web TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN instagram TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN twitter TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN facebook TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN youtube TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN spotify_id TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN generos TEXT")
            db.execSQL("ALTER TABLE artistas ADD COLUMN tipo TEXT NOT NULL DEFAULT 'SOLISTA'")
            db.execSQL("ALTER TABLE artistas ADD COLUMN es_verificado INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE artistas ADD COLUMN es_popular INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE artistas ADD COLUMN total_canciones INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE artistas ADD COLUMN total_albumes INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE artistas ADD COLUMN total_reproducciones INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE artistas ADD COLUMN veces_favorito INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE artistas ADD COLUMN fecha_agregado INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            db.execSQL("ALTER TABLE artistas ADD COLUMN ultima_actualizacion INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            db.execSQL("ALTER TABLE artistas ADD COLUMN fuente TEXT NOT NULL DEFAULT 'LOCAL'")

            // Índices para artistas
            db.execSQL("CREATE INDEX IF NOT EXISTS index_artistas_nombre_normalizado ON artistas(nombre_normalizado)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_artistas_es_popular ON artistas(es_popular)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_artistas_fecha_agregado ON artistas(fecha_agregado)")

            // ==================== CONTINUAR CON OTRAS TABLAS... ====================
            // (Álbumes, Géneros, Listas, etc.)
            // Por brevedad, solo muestro algunos ejemplos
            // En producción, debes agregar TODAS las columnas necesarias
        }
    }

    /**
     * ⚠️ MIGRACIÓN DESTRUCTIVA (Solo para desarrollo)
     * Elimina todas las tablas y las recrea
     */
    private val MIGRATION_DESTRUCTIVE = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Eliminar todas las tablas
            db.execSQL("DROP TABLE IF EXISTS detalle_lista_reproduccion")
            db.execSQL("DROP TABLE IF EXISTS favoritos")
            db.execSQL("DROP TABLE IF EXISTS letras")
            db.execSQL("DROP TABLE IF EXISTS listas_reproduccion")
            db.execSQL("DROP TABLE IF EXISTS canciones")
            db.execSQL("DROP TABLE IF EXISTS albumes")
            db.execSQL("DROP TABLE IF EXISTS generos")
            db.execSQL("DROP TABLE IF EXISTS artistas")
            db.execSQL("DROP TABLE IF EXISTS usuarios")

            // Room recreará las tablas automáticamente
        }
    }
}