// en: app/src/main/java/com/example/freeplayerm/di/DatabaseModule.kt
package com.example.freeplayerm.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.freeplayerm.data.local.AppDatabase
import com.example.freeplayerm.data.local.dao.*
import com.example.freeplayerm.data.local.entity.GeneroEntity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 🔧 DATABASE MODULE - HILT DI v8.0
 *
 * Módulo de Hilt para proveer la base de datos Room
 * Incluye configuración de migraciones, callbacks y fallbacks
 *
 * Características:
 * - Singleton de base de datos con Room
 * - 10 DAOs provistos con inyección de dependencias
 * - Callback para datos iniciales (géneros)
 * - Migraciones configuradas
 * - Fallback destructivo para desarrollo
 *
 * @version 8.0 - Complete DAOs Provider
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // ==================== PROVEER BASE DE DATOS ====================

    /**
     * Provee la instancia singleton de la base de datos
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        callback: RoomDatabase.Callback
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            // ⚠️ PARA DESARROLLO: Permite recrear BD en conflictos
            // ⚠️ PARA PRODUCCIÓN: Cambiar a false e implementar migraciones
            .fallbackToDestructiveMigration(true) // ⬅️ Cambiar a false en producción

            // Para PRODUCCIÓN (descomentar cuando tengas usuarios reales):
            // .fallbackToDestructiveMigration(false)
            // .addMigrations(MIGRATION_6_7, MIGRATION_7_8)

            // Callback para datos iniciales
            .addCallback(callback)
            .build()
    }

    // ==================== PROVEER DAOs PRINCIPALES ====================

    /**
     * Provee DAO de usuarios
     * Incluye autenticación, perfil y sesiones
     */
    @Provides
    @Singleton
    fun provideUsuarioDao(database: AppDatabase): UsuarioDao {
        return database.usuarioDao()
    }

    /**
     * Provee DAO de canciones
     * Incluye CRUD completo y búsquedas
     */
    @Provides
    @Singleton
    fun provideCancionDao(database: AppDatabase): CancionDao {
        return database.cancionDao()
    }

    /**
     * Provee DAO de artistas
     * Incluye biografías y estadísticas
     */
    @Provides
    @Singleton
    fun provideArtistaDao(database: AppDatabase): ArtistaDao {
        return database.artistaDao()
    }

    /**
     * Provee DAO de álbumes
     * Incluye tracks y metadatos
     */
    @Provides
    @Singleton
    fun provideAlbumDao(database: AppDatabase): AlbumDao {
        return database.albumDao()
    }

    /**
     * Provee DAO de géneros
     * Incluye jerarquías y clasificaciones
     */
    @Provides
    @Singleton
    fun provideGeneroDao(database: AppDatabase): GeneroDao {
        return database.generoDao()
    }

    /**
     * Provee DAO de letras
     * Incluye caché y sincronización
     */
    @Provides
    @Singleton
    fun provideLetraDao(database: AppDatabase): LetraDao {
        return database.letraDao()
    }

    // ==================== PROVEER DAOs DE LISTAS ====================

    /**
     * Provee DAO de listas de reproducción
     * Incluye playlists colaborativas y públicas
     */
    @Provides
    @Singleton
    fun provideListaReproduccionDao(database: AppDatabase): ListaReproduccionDao {
        return database.listaReproduccionDao()
    }

    /**
     * Provee DAO de favoritos
     * Incluye calificaciones y ordenamiento
     */
    @Provides
    @Singleton
    fun provideFavoritoDao(database: AppDatabase): FavoritoDao {
        return database.favoritoDao()
    }

    // ==================== PROVEER DAOs DE REPRODUCCIÓN ====================

    /**
     * Provee DAO de historial de reproducción
     * Incluye analytics y estadísticas
     */
    @Provides
    @Singleton
    fun provideHistorialReproduccionDao(database: AppDatabase): HistorialReproduccionDao {
        return database.historialReproduccionDao()
    }

    /**
     * Provee DAO de cola de reproducción
     * Incluye reordenamiento y gestión
     */
    @Provides
    @Singleton
    fun provideColaReproduccionDao(database: AppDatabase): ColaReproduccionDao {
        return database.colaReproduccionDao()
    }

    // ==================== CALLBACK PARA DATOS INICIALES ====================

    /**
     * Callback para poblar datos iniciales al crear la BD
     * Inserta géneros principales con sus configuraciones
     */
    @Provides
    @Singleton
    fun provideDatabaseCallback(): RoomDatabase.Callback {
        return object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                // ==================== INSERTAR GÉNEROS POR DEFECTO ====================
                GeneroEntity.GENEROS_PRINCIPALES.forEach { nombre ->
                    val nombreNormalizado = GeneroEntity.normalizar(nombre)
                    val color = GeneroEntity.obtenerColorSugerido(nombre) ?: "#666666"
                    val emoji = GeneroEntity.obtenerEmojiSugerido(nombre) ?: "🎵"

                    db.execSQL("""
                        INSERT OR IGNORE INTO generos (
                            nombre, 
                            nombre_normalizado, 
                            color, 
                            emoji, 
                            es_popular,
                            fecha_agregado,
                            ultima_actualizacion
                        ) VALUES (
                            '$nombre', 
                            '$nombreNormalizado', 
                            '$color', 
                            '$emoji', 
                            1,
                            ${System.currentTimeMillis()},
                            ${System.currentTimeMillis()}
                        )
                    """)
                }
            }
        }
    }

    // ==================== MIGRACIONES ====================

    /**
     * Migración de versión 6 a 7
     *
     * Cambios principales:
     * - Agregar 5 nuevas tablas (CancionArtista, Historial, Preferencias, Estado, Cola)
     * - Agregar campos de tokens a usuarios
     * - Crear índices para optimización
     */
    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {

            // ==================== USUARIOS: NUEVOS CAMPOS ====================

            db.execSQL("ALTER TABLE usuarios ADD COLUMN token_sesion TEXT")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN refresh_token TEXT")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN token_expiracion INTEGER")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN salt TEXT")
            db.execSQL("ALTER TABLE usuarios ADD COLUMN ultimo_cambio_contrasena INTEGER")

            // ==================== NUEVA TABLA: CANCION_ARTISTA ====================

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS cancion_artista (
                    id_cancion INTEGER NOT NULL,
                    id_artista INTEGER NOT NULL,
                    tipo_participacion TEXT NOT NULL,
                    orden INTEGER NOT NULL DEFAULT 0,
                    fecha_agregado INTEGER NOT NULL,
                    verificado INTEGER NOT NULL DEFAULT 0,
                    fuente TEXT NOT NULL DEFAULT 'LOCAL',
                    credito_texto TEXT,
                    veces_reproducida_colaboracion INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(id_cancion, id_artista, tipo_participacion),
                    FOREIGN KEY(id_cancion) REFERENCES canciones(id_cancion) ON DELETE CASCADE,
                    FOREIGN KEY(id_artista) REFERENCES artistas(id_artista) ON DELETE CASCADE
                )
            """)

            db.execSQL("CREATE INDEX IF NOT EXISTS index_cancion_artista_id_cancion ON cancion_artista(id_cancion)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_cancion_artista_id_artista ON cancion_artista(id_artista)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_cancion_artista_tipo_participacion ON cancion_artista(tipo_participacion)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_cancion_artista_id_cancion_orden ON cancion_artista(id_cancion, orden)")

            // ==================== NUEVA TABLA: HISTORIAL_REPRODUCCION ====================

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS historial_reproduccion (
                    id_historial INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    id_usuario INTEGER NOT NULL,
                    id_cancion INTEGER NOT NULL,
                    fecha_reproduccion INTEGER NOT NULL,
                    duracion_reproducida_ms INTEGER NOT NULL,
                    duracion_total_cancion_ms INTEGER NOT NULL,
                    porcentaje_reproducido REAL NOT NULL DEFAULT 0.0,
                    completo INTEGER NOT NULL DEFAULT 0,
                    origen TEXT NOT NULL,
                    id_contexto INTEGER,
                    nombre_contexto TEXT,
                    posicion_en_contexto INTEGER,
                    calidad_reproducida TEXT,
                    modo_reproduccion TEXT NOT NULL DEFAULT 'NORMAL',
                    volumen REAL,
                    con_ecualizador INTEGER NOT NULL DEFAULT 0,
                    dispositivo_id TEXT,
                    tipo_salida_audio TEXT,
                    pausas_durante_reproduccion INTEGER NOT NULL DEFAULT 0,
                    seeks_realizados INTEGER NOT NULL DEFAULT 0,
                    agregado_a_favoritos_durante INTEGER NOT NULL DEFAULT 0,
                    agregado_a_playlist_durante INTEGER NOT NULL DEFAULT 0,
                    hora_del_dia INTEGER NOT NULL DEFAULT 0,
                    dia_semana INTEGER NOT NULL DEFAULT 0,
                    ubicacion_geografica TEXT,
                    sincronizado INTEGER NOT NULL DEFAULT 0,
                    sync_id TEXT,
                    FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
                    FOREIGN KEY(id_cancion) REFERENCES canciones(id_cancion) ON DELETE CASCADE
                )
            """)

            db.execSQL("CREATE INDEX IF NOT EXISTS index_historial_reproduccion_id_usuario ON historial_reproduccion(id_usuario)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_historial_reproduccion_id_cancion ON historial_reproduccion(id_cancion)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_historial_reproduccion_fecha_reproduccion ON historial_reproduccion(fecha_reproduccion)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_historial_reproduccion_id_usuario_fecha ON historial_reproduccion(id_usuario, fecha_reproduccion)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_historial_reproduccion_completo ON historial_reproduccion(completo)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_historial_reproduccion_origen ON historial_reproduccion(origen)")

            // ==================== NUEVA TABLA: PREFERENCIAS_USUARIO ====================

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS preferencias_usuario (
                    id_usuario INTEGER PRIMARY KEY NOT NULL,
                    tema_oscuro INTEGER NOT NULL DEFAULT 0,
                    tema_color TEXT,
                    usar_colores_portada INTEGER NOT NULL DEFAULT 1,
                    animaciones_habilitadas INTEGER NOT NULL DEFAULT 1,
                    mostrar_letras_automatico INTEGER NOT NULL DEFAULT 0,
                    reproduccion_automatica INTEGER NOT NULL DEFAULT 1,
                    calidad_preferida TEXT NOT NULL DEFAULT 'HIGH',
                    calidad_streaming TEXT NOT NULL DEFAULT 'MEDIUM',
                    calidad_descarga TEXT NOT NULL DEFAULT 'HIGH',
                    solo_wifi_streaming INTEGER NOT NULL DEFAULT 0,
                    solo_wifi_descarga INTEGER NOT NULL DEFAULT 1,
                    volumen_predeterminado REAL NOT NULL DEFAULT 0.7,
                    crossfade_ms INTEGER NOT NULL DEFAULT 0,
                    gapless_playback INTEGER NOT NULL DEFAULT 1,
                    normalizar_volumen INTEGER NOT NULL DEFAULT 0,
                    ecualizador_preset TEXT,
                    ecualizador_custom_json TEXT,
                    bass_boost INTEGER NOT NULL DEFAULT 0,
                    virtualizer INTEGER NOT NULL DEFAULT 0,
                    notificaciones_habilitadas INTEGER NOT NULL DEFAULT 1,
                    notificar_nuevas_canciones INTEGER NOT NULL DEFAULT 1,
                    notificar_nuevos_albumes INTEGER NOT NULL DEFAULT 1,
                    notificar_recomendaciones INTEGER NOT NULL DEFAULT 1,
                    sonido_notificacion INTEGER NOT NULL DEFAULT 0,
                    vibracion_notificacion INTEGER NOT NULL DEFAULT 0,
                    idioma_preferido TEXT NOT NULL DEFAULT 'es',
                    formato_fecha TEXT NOT NULL DEFAULT 'DD/MM/YYYY',
                    formato_hora TEXT NOT NULL DEFAULT '24H',
                    historial_habilitado INTEGER NOT NULL DEFAULT 1,
                    compartir_estadisticas INTEGER NOT NULL DEFAULT 0,
                    sincronizar_favoritos INTEGER NOT NULL DEFAULT 1,
                    sincronizar_listas INTEGER NOT NULL DEFAULT 1,
                    sincronizar_historial INTEGER NOT NULL DEFAULT 1,
                    backup_automatico INTEGER NOT NULL DEFAULT 1,
                    frecuencia_backup_dias INTEGER NOT NULL DEFAULT 7,
                    iniciar_ultima_cancion INTEGER NOT NULL DEFAULT 0,
                    recordar_posicion_cancion INTEGER NOT NULL DEFAULT 1,
                    auto_descargar_portadas INTEGER NOT NULL DEFAULT 1,
                    auto_descargar_letras INTEGER NOT NULL DEFAULT 1,
                    escaneo_automatico INTEGER NOT NULL DEFAULT 0,
                    incluir_podcasts INTEGER NOT NULL DEFAULT 0,
                    gestos_habilitados INTEGER NOT NULL DEFAULT 1,
                    gesto_deslizar_cambiar_cancion INTEGER NOT NULL DEFAULT 1,
                    gesto_shake_aleatorio INTEGER NOT NULL DEFAULT 0,
                    boton_volumen_cambiar_cancion INTEGER NOT NULL DEFAULT 0,
                    headset_auto_play INTEGER NOT NULL DEFAULT 1,
                    headset_auto_pause INTEGER NOT NULL DEFAULT 1,
                    bluetooth_auto_play INTEGER NOT NULL DEFAULT 1,
                    cache_size_mb INTEGER NOT NULL DEFAULT 500,
                    buffer_size_ms INTEGER NOT NULL DEFAULT 15000,
                    logs_habilitados INTEGER NOT NULL DEFAULT 0,
                    modo_desarrollador INTEGER NOT NULL DEFAULT 0,
                    fecha_creacion INTEGER NOT NULL,
                    ultima_actualizacion INTEGER NOT NULL,
                    version_preferencias INTEGER NOT NULL DEFAULT 1,
                    FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
                )
            """)

            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_preferencias_usuario_id_usuario ON preferencias_usuario(id_usuario)")

            // ==================== NUEVA TABLA: ESTADO_REPRODUCCION ====================

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS estado_reproduccion (
                    id_usuario INTEGER PRIMARY KEY NOT NULL,
                    id_cancion_actual INTEGER,
                    posicion_ms INTEGER NOT NULL DEFAULT 0,
                    duracion_cancion_ms INTEGER NOT NULL DEFAULT 0,
                    esta_reproduciendo INTEGER NOT NULL DEFAULT 0,
                    velocidad_reproduccion REAL NOT NULL DEFAULT 1.0,
                    pitch REAL NOT NULL DEFAULT 1.0,
                    modo_repetir TEXT NOT NULL DEFAULT 'NONE',
                    modo_aleatorio INTEGER NOT NULL DEFAULT 0,
                    modo_aleatorio_inteligente INTEGER NOT NULL DEFAULT 0,
                    volumen REAL NOT NULL DEFAULT 0.7,
                    silenciado INTEGER NOT NULL DEFAULT 0,
                    ecualizador_activo INTEGER NOT NULL DEFAULT 0,
                    ecualizador_preset TEXT,
                    tipo_contexto TEXT,
                    id_contexto INTEGER,
                    nombre_contexto TEXT,
                    portada_contexto TEXT,
                    contexto_json TEXT,
                    indice_cola_actual INTEGER NOT NULL DEFAULT 0,
                    total_canciones_cola INTEGER NOT NULL DEFAULT 0,
                    cola_ids_json TEXT,
                    cola_origen TEXT,
                    cola_shuffle_seed INTEGER,
                    historial_navegacion_json TEXT,
                    puede_ir_anterior INTEGER NOT NULL DEFAULT 0,
                    puede_ir_siguiente INTEGER NOT NULL DEFAULT 0,
                    dispositivo_id TEXT,
                    salida_audio_actual TEXT,
                    ultima_actualizacion INTEGER NOT NULL,
                    version_estado INTEGER NOT NULL DEFAULT 1,
                    sincronizado INTEGER NOT NULL DEFAULT 0,
                    sync_id TEXT,
                    FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
                    FOREIGN KEY(id_cancion_actual) REFERENCES canciones(id_cancion) ON DELETE SET NULL
                )
            """)

            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_estado_reproduccion_id_usuario ON estado_reproduccion(id_usuario)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_estado_reproduccion_id_cancion_actual ON estado_reproduccion(id_cancion_actual)")

            // ==================== NUEVA TABLA: COLA_REPRODUCCION ====================

            db.execSQL("""
                CREATE TABLE IF NOT EXISTS cola_reproduccion (
                    id_cola INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    id_usuario INTEGER NOT NULL,
                    id_cancion INTEGER NOT NULL,
                    orden INTEGER NOT NULL,
                    reproducido INTEGER NOT NULL DEFAULT 0,
                    fecha_reproducido INTEGER,
                    origen TEXT NOT NULL,
                    agregado_desde TEXT,
                    id_contexto_origen INTEGER,
                    fecha_agregado INTEGER NOT NULL,
                    score_sugerencia REAL,
                    razon_sugerencia TEXT,
                    inicio_personalizado_ms INTEGER,
                    fin_personalizado_ms INTEGER,
                    transicion_tipo TEXT NOT NULL DEFAULT 'NORMAL',
                    transicion_duracion_ms INTEGER,
                    dispositivo_id TEXT,
                    sincronizado INTEGER NOT NULL DEFAULT 0,
                    sync_id TEXT,
                    FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
                    FOREIGN KEY(id_cancion) REFERENCES canciones(id_cancion) ON DELETE CASCADE
                )
            """)

            db.execSQL("CREATE INDEX IF NOT EXISTS index_cola_reproduccion_id_usuario ON cola_reproduccion(id_usuario)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_cola_reproduccion_id_cancion ON cola_reproduccion(id_cancion)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_cola_reproduccion_id_usuario_orden ON cola_reproduccion(id_usuario, orden)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_cola_reproduccion_reproducido ON cola_reproduccion(reproducido)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_cola_reproduccion_fecha_agregado ON cola_reproduccion(fecha_agregado)")
        }
    }

    /**
     * Migración de versión 7 a 8
     *
     * Cambios:
     * - Agregar tablas de características avanzadas
     * - Optimización de índices existentes
     */
    private val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Agregar tablas adicionales si es necesario
            // Por ahora solo optimización de índices
            db.execSQL("CREATE INDEX IF NOT EXISTS index_canciones_titulo ON canciones(titulo)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_artistas_nombre ON artistas(nombre)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_albumes_titulo ON albumes(titulo)")
        }
    }
}