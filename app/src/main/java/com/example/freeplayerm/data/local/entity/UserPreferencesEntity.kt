// en: app/src/main/java/com/example/freeplayerm/data/local/entity/UserPreferencesEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ⚙️ PREFERENCIAS USUARIO ENTITY - CONFIGURACIÓN v1.0
 *
 * Entidad que almacena las preferencias y configuración de cada usuario Separada de UserEntity para
 * mejor organización y performance
 *
 * Características:
 * - Relación 1:1 con Usuario
 * - Configuración de reproducción
 * - Preferencias de interfaz
 * - Configuración de audio
 * - Preferencias de notificaciones
 * - Opciones de sincronización
 *
 * Ventajas de separar:
 * - UserEntity más ligero para autenticación
 * - Queries más rápidas en login
 * - Actualizar preferencias sin tocar datos de perfil
 * - Facilita implementar "restaurar configuración por defecto"
 *
 * @version 1.0 - Initial Release
 */
@Entity(
    tableName = "preferencias_usuario",
    foreignKeys =
        [
            ForeignKey(
                entity = UserEntity::class,
                parentColumns = ["id_usuario"],
                childColumns = ["id_usuario"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices =
        [
            Index(value = ["id_usuario"], unique = true) // Relación 1:1
        ],
)
data class UserPreferencesEntity(
    @PrimaryKey @ColumnInfo(name = "id_usuario") val idUsuario: Int,

    // ==================== APARIENCIA ====================

    @ColumnInfo(name = "tema_oscuro") val temaOscuro: Boolean = false,
    @ColumnInfo(name = "tema_color") val temaColor: String? = null, // Color primario del tema (hex)
    @ColumnInfo(name = "usar_colores_portada")
    val usarColoresPortada: Boolean = true, // Extraer colores de portadas
    @ColumnInfo(name = "animaciones_habilitadas") val animacionesHabilitadas: Boolean = true,
    @ColumnInfo(name = "mostrar_letras_automatico") val mostrarLetrasAutomatico: Boolean = false,

    // ==================== REPRODUCCIÓN ====================

    @ColumnInfo(name = "reproduccion_automatica")
    val reproduccionAutomatica: Boolean = true, // Continuar con canciones similares
    @ColumnInfo(name = "calidad_preferida")
    val calidadPreferida: String = CALIDAD_ALTA, // "LOW", "MEDIUM", "HIGH", "LOSSLESS"
    @ColumnInfo(name = "calidad_streaming")
    val calidadStreaming: String = CALIDAD_MEDIA, // Calidad cuando usa datos móviles
    @ColumnInfo(name = "calidad_descarga")
    val calidadDescarga: String = CALIDAD_ALTA, // Calidad para descargas offline
    @ColumnInfo(name = "solo_wifi_streaming")
    val soloWifiStreaming: Boolean = false, // Bloquear streaming en datos móviles
    @ColumnInfo(name = "solo_wifi_descarga") val soloWifiDescarga: Boolean = true,

    // ==================== AUDIO ====================

    @ColumnInfo(name = "volumen_predeterminado")
    val volumenPredeterminado: Float = 0.7f, // 0.0 - 1.0
    @ColumnInfo(name = "crossfade_ms")
    val crossfadeMs: Int = 0, // Milisegundos de crossfade (0 = deshabilitado)
    @ColumnInfo(name = "gapless_playback")
    val gaplessPlayback: Boolean = true, // Reproducción sin silencios entre tracks
    @ColumnInfo(name = "normalizar_volumen")
    val normalizarVolumen: Boolean = false, // ReplayGain o similar
    @ColumnInfo(name = "ecualizador_preset")
    val ecualizadorPreset: String? = null, // Preset activo del ecualizador
    @ColumnInfo(name = "ecualizador_custom_json")
    val ecualizadorCustomJson: String? = null, // Configuración custom en JSON
    @ColumnInfo(name = "bass_boost") val bassBoost: Int = 0, // 0-100
    @ColumnInfo(name = "virtualizer") val virtualizer: Int = 0, // 0-100

    // ==================== NOTIFICACIONES ====================

    @ColumnInfo(name = "notificaciones_habilitadas") val notificacionesHabilitadas: Boolean = true,
    @ColumnInfo(name = "notificar_nuevas_canciones")
    val notificarNuevasCanciones: Boolean = true, // En escaneo de biblioteca
    @ColumnInfo(name = "notificar_nuevos_albumes") val notificarNuevosAlbumes: Boolean = true,
    @ColumnInfo(name = "notificar_recomendaciones") val notificarRecomendaciones: Boolean = true,
    @ColumnInfo(name = "sonido_notificacion") val sonidoNotificacion: Boolean = false,
    @ColumnInfo(name = "vibracion_notificacion") val vibracionNotificacion: Boolean = false,

    // ==================== IDIOMA Y REGIÓN ====================

    @ColumnInfo(name = "idioma_preferido") val idiomaPreferido: String = "es", // Código ISO 639-1
    @ColumnInfo(name = "formato_fecha") val formatoFecha: String = "DD/MM/YYYY",
    @ColumnInfo(name = "formato_hora") val formatoHora: String = "24H", // "12H" o "24H"

    // ==================== PRIVACIDAD Y SINCRONIZACIÓN ====================

    @ColumnInfo(name = "historial_habilitado")
    val historialHabilitado: Boolean = true, // Guardar historial de reproducción
    @ColumnInfo(name = "compartir_estadisticas")
    val compartirEstadisticas: Boolean = false, // Compartir con amigos
    @ColumnInfo(name = "sincronizar_favoritos")
    val sincronizarFavoritos: Boolean = true, // Cross-device
    @ColumnInfo(name = "sincronizar_listas") val sincronizarListas: Boolean = true,
    @ColumnInfo(name = "sincronizar_historial") val sincronizarHistorial: Boolean = true,
    @ColumnInfo(name = "backup_automatico") val backupAutomatico: Boolean = true,
    @ColumnInfo(name = "frecuencia_backup_dias")
    val frecuenciaBackupDias: Int = 7, // Cada cuántos días hacer backup

    // ==================== COMPORTAMIENTO DE LA APP ====================

    @ColumnInfo(name = "iniciar_ultima_cancion")
    val iniciarUltimaCancion: Boolean = false, // Continuar donde quedó
    @ColumnInfo(name = "recordar_posicion_cancion")
    val recordarPosicionCancion: Boolean = true, // Recordar posición en cada canción
    @ColumnInfo(name = "auto_descargar_portadas") val autoDescargarPortadas: Boolean = true,
    @ColumnInfo(name = "auto_descargar_letras") val autoDescargarLetras: Boolean = true,
    @ColumnInfo(name = "escaneo_automatico")
    val escaneoAutomatico: Boolean = false, // Escanear al abrir app
    @ColumnInfo(name = "incluir_podcasts")
    val incluirPodcasts: Boolean = false, // En escaneo de biblioteca

    // ==================== CONTROLES Y GESTOS ====================

    @ColumnInfo(name = "gestos_habilitados") val gestosHabilitados: Boolean = true,
    @ColumnInfo(name = "gesto_deslizar_cambiar_cancion")
    val gestoDeslizarCambiarCancion: Boolean = true,
    @ColumnInfo(name = "gesto_shake_aleatorio")
    val gestoShakeAleatorio: Boolean = false, // Agitar para shuffle
    @ColumnInfo(name = "boton_volumen_cambiar_cancion")
    val botonVolumenCambiarCancion: Boolean =
        false, // Usar botones de volumen cuando pantalla apagada
    @ColumnInfo(name = "headset_auto_play")
    val headsetAutoPlay: Boolean = true, // Reproducir al conectar audífonos
    @ColumnInfo(name = "headset_auto_pause")
    val headsetAutoPause: Boolean = true, // Pausar al desconectar
    @ColumnInfo(name = "bluetooth_auto_play") val bluetoothAutoPlay: Boolean = true,

    // ==================== AVANZADO ====================

    @ColumnInfo(name = "cache_size_mb") val cacheSizeMb: Int = 500, // Tamaño máximo de cache en MB
    @ColumnInfo(name = "buffer_size_ms") val bufferSizeMs: Int = 15000, // Buffer de streaming en ms
    @ColumnInfo(name = "logs_habilitados") val logsHabilitados: Boolean = false, // Para debugging
    @ColumnInfo(name = "modo_desarrollador") val modoDesarrollador: Boolean = false,

    // ==================== METADATA ====================

    @ColumnInfo(name = "fecha_creacion") val fechaCreacion: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "ultima_actualizacion")
    val ultimaActualizacion: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "version_preferencias")
    val versionPreferencias: Int = 1, // Para migraciones futuras
) {
    /** Verifica si está en modo ahorro de datos */
    fun esModoAhorroDatos(): Boolean = soloWifiStreaming && calidadStreaming == CALIDAD_BAJA

    /** Obtiene la calidad según conexión */
    fun obtenerCalidadSegunConexion(esWifi: Boolean): String {
        return if (esWifi) calidadPreferida else calidadStreaming
    }

    /** Verifica si debe usar streaming según conexión */
    fun puedeStreamear(esWifi: Boolean): Boolean {
        return esWifi || !soloWifiStreaming
    }

    /** Verifica si debe descargar según conexión */
    fun puedeDescargar(esWifi: Boolean): Boolean {
        return esWifi || !soloWifiDescarga
    }

    /** Verifica si tiene crossfade habilitado */
    fun tieneCrossfade(): Boolean = crossfadeMs > 0

    /** Verifica si tiene ecualizador activo */
    fun tieneEcualizador(): Boolean =
        !ecualizadorPreset.isNullOrBlank() || !ecualizadorCustomJson.isNullOrBlank()

    companion object {
        // Calidades de audio
        const val CALIDAD_BAJA = "LOW" // 96kbps
        const val CALIDAD_MEDIA = "MEDIUM" // 160kbps
        const val CALIDAD_ALTA = "HIGH" // 320kbps
        const val CALIDAD_LOSSLESS = "LOSSLESS" // FLAC

        // Presets de ecualizador
        const val EQ_FLAT = "FLAT"
        const val EQ_ROCK = "ROCK"
        const val EQ_POP = "POP"
        const val EQ_JAZZ = "JAZZ"
        const val EQ_CLASSICAL = "CLASSICAL"
        const val EQ_BASS_BOOST = "BASS_BOOST"
        const val EQ_TREBLE_BOOST = "TREBLE_BOOST"
        const val EQ_CUSTOM = "CUSTOM"

        // Idiomas soportados
        const val IDIOMA_ESPANOL = "es"
        const val IDIOMA_INGLES = "en"
        const val IDIOMA_FRANCES = "fr"
        const val IDIOMA_PORTUGUES = "pt"
        const val IDIOMA_ITALIANO = "it"
        const val IDIOMA_ALEMAN = "de"

        /** Crea preferencias por defecto para un usuario */
        fun crearPorDefecto(idUsuario: Int): UserPreferencesEntity {
            return UserPreferencesEntity(idUsuario = idUsuario)
        }

        /** Crea preferencias optimizadas para ahorro de datos */
        fun crearModoAhorro(idUsuario: Int): UserPreferencesEntity {
            return UserPreferencesEntity(
                idUsuario = idUsuario,
                calidadPreferida = CALIDAD_MEDIA,
                calidadStreaming = CALIDAD_BAJA,
                calidadDescarga = CALIDAD_MEDIA,
                soloWifiStreaming = true,
                soloWifiDescarga = true,
                autoDescargarPortadas = false,
                crossfadeMs = 0,
                cacheSizeMb = 200,
            )
        }

        /** Crea preferencias optimizadas para calidad máxima */
        fun crearModoAudiofilo(idUsuario: Int): UserPreferencesEntity {
            return UserPreferencesEntity(
                idUsuario = idUsuario,
                calidadPreferida = CALIDAD_LOSSLESS,
                calidadStreaming = CALIDAD_ALTA,
                calidadDescarga = CALIDAD_LOSSLESS,
                gaplessPlayback = true,
                normalizarVolumen = true,
                crossfadeMs = 3000,
                cacheSizeMb = 1000,
            )
        }
    }
}
