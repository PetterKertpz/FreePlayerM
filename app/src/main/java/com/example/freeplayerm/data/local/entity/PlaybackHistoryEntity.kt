// en: app/src/main/java/com/example/freeplayerm/data/local/entity/PlaybackHistoryEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 📊 HISTORIAL REPRODUCCION ENTITY - ANALYTICS v1.0
 *
 * Entidad que registra cada reproducción de una canción Permite analytics detallado,
 * recomendaciones y estadísticas
 *
 * Características:
 * - Registro completo de cada play
 * - Timestamp preciso de reproducción
 * - Duración real reproducida (para detectar skips)
 * - Contexto de reproducción (de dónde se reprodujo)
 * - Información del dispositivo
 * - Datos de calidad de audio
 *
 * Casos de uso:
 * - "Tus canciones más escuchadas del mes"
 * - "Reproducidas recientemente"
 * - Recomendaciones basadas en historial
 * - Estadísticas de uso
 * - Detección de patrones de escucha
 *
 * @version 1.0 - Initial Release
 */
@Entity(
    tableName = "historial_reproduccion",
    foreignKeys =
        [
            ForeignKey(
                entity = UserEntity::class,
                parentColumns = ["id_usuario"],
                childColumns = ["id_usuario"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = SongEntity::class,
                parentColumns = ["id_cancion"],
                childColumns = ["id_cancion"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices =
        [
            Index(value = ["id_usuario"]),
            Index(value = ["id_cancion"]),
            Index(value = ["fecha_reproduccion"]),
            Index(
                value = ["id_usuario", "fecha_reproduccion"]
            ), // Para queries de historial por usuario
            Index(value = ["id_cancion", "fecha_reproduccion"]), // Para stats por canción
            Index(value = ["completo"]), // Para filtrar reproducciones completas
            Index(value = ["origen"]), // Para analytics por origen
        ],
)
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id_historial") val idHistorial: Int = 0,
    @ColumnInfo(name = "id_usuario") val idUsuario: Int,
    @ColumnInfo(name = "id_cancion") val idCancion: Int,

    // ==================== INFORMACIÓN DE REPRODUCCIÓN ====================

    @ColumnInfo(name = "fecha_reproduccion")
    val fechaReproduccion: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "duracion_reproducida_ms")
    val duracionReproducidaMs: Int, // Cuánto se escuchó realmente
    @ColumnInfo(name = "duracion_total_cancion_ms")
    val duracionTotalCancionMs: Int, // Duración total de la canción
    @ColumnInfo(name = "porcentaje_reproducido") val porcentajeReproducido: Float = 0f, // 0.0 - 1.0
    @ColumnInfo(name = "completo")
    val completo: Boolean = false, // Si se reprodujo >80% (configurable)

    // ==================== CONTEXTO DE REPRODUCCIÓN ====================

    @ColumnInfo(name = "origen")
    val origen:
        String, // "PLAYLIST", "ALBUM", "ARTISTA", "GENERO", "BUSQUEDA", "ALEATORIO", "FAVORITOS",
                // "RECOMENDACION"
    @ColumnInfo(name = "id_contexto")
    val idContexto: Int? = null, // ID de playlist/album/etc de donde se reprodujo
    @ColumnInfo(name = "nombre_contexto")
    val nombreContexto: String? = null, // Nombre del contexto para facilitar queries
    @ColumnInfo(name = "posicion_en_contexto")
    val posicionEnContexto: Int? = null, // Posición en la lista/álbum

    // ==================== INFORMACIÓN DE REPRODUCCIÓN TÉCNICA ====================

    @ColumnInfo(name = "calidad_reproducida")
    val calidadReproducida: String? = null, // "LOW", "MEDIUM", "HIGH", "LOSSLESS"
    @ColumnInfo(name = "modo_reproduccion")
    val modoReproduccion: String = MODO_NORMAL, // "NORMAL", "SHUFFLE", "REPEAT_ONE", "REPEAT_ALL"
    @ColumnInfo(name = "volumen") val volumen: Float? = null, // 0.0 - 1.0
    @ColumnInfo(name = "con_ecualizador") val conEcualizador: Boolean = false,

    // ==================== INFORMACIÓN DEL DISPOSITIVO ====================

    @ColumnInfo(name = "dispositivo_id")
    val dispositivoId: String? = null, // ID único del dispositivo
    @ColumnInfo(name = "tipo_salida_audio")
    val tipoSalidaAudio: String? = null, // "SPEAKER", "HEADPHONES", "BLUETOOTH", "EXTERNAL"

    // ==================== COMPORTAMIENTO DEL USUARIO ====================

    @ColumnInfo(name = "pausas_durante_reproduccion")
    val pausasDuranteReproduccion: Int = 0, // Cuántas veces pausó
    @ColumnInfo(name = "seeks_realizados")
    val seeksRealizados: Int = 0, // Cuántas veces adelantó/retrocedió
    @ColumnInfo(name = "agregado_a_favoritos_durante")
    val agregadoAFavoritosDurante: Boolean = false, // Si la agregó a favoritos mientras sonaba
    @ColumnInfo(name = "agregado_a_playlist_durante") val agregadoAPlaylistDurante: Boolean = false,

    // ==================== METADATA ADICIONAL ====================

    @ColumnInfo(name = "hora_del_dia") val horaDelDia: Int = 0, // 0-23, útil para detectar patrones
    @ColumnInfo(name = "dia_semana") val diaSemana: Int = 0, // 1-7 (1=Lunes, 7=Domingo)
    @ColumnInfo(name = "ubicacion_geografica")
    val ubicacionGeografica: String? = null, // Opcional: ciudad/país
    @ColumnInfo(name = "sincronizado")
    val sincronizado: Boolean = false, // Si se sincronizó con servidor
    @ColumnInfo(name = "sync_id") val syncId: String? = null, // ID de sincronización cross-device
) {
    /** Calcula el porcentaje reproducido */
    fun calcularPorcentaje(): Float {
        if (duracionTotalCancionMs == 0) return 0f
        return (duracionReproducidaMs.toFloat() / duracionTotalCancionMs.toFloat()).coerceIn(0f, 1f)
    }

    /** Verifica si fue un "skip" (menos del 30% reproducido) */
    fun fueSkip(): Boolean = porcentajeReproducido < 0.3f

    /** Verifica si fue reproducción parcial (30-80%) */
    fun fueParcial(): Boolean = porcentajeReproducido in 0.3f..0.8f

    /** Obtiene el período del día */
    fun obtenerPeriodoDelDia(): String {
        return when (horaDelDia) {
            in 0..5 -> "Madrugada"
            in 6..11 -> "Mañana"
            in 12..17 -> "Tarde"
            in 18..23 -> "Noche"
            else -> "Desconocido"
        }
    }

    /** Obtiene el nombre del día de la semana */
    fun obtenerNombreDiaSemana(): String {
        return when (diaSemana) {
            1 -> "Lunes"
            2 -> "Martes"
            3 -> "Miércoles"
            4 -> "Jueves"
            5 -> "Viernes"
            6 -> "Sábado"
            7 -> "Domingo"
            else -> "Desconocido"
        }
    }

    /**
     * Calcula score de engagement (0-100) Basado en: reproducción completa, pausas, seeks,
     * favoritos
     */
    fun calcularEngagementScore(): Int {
        var score = 0

        // Reproducción completa +40 puntos
        if (completo) score += 40 else score += (porcentajeReproducido * 40).toInt()

        // Pocas pausas +20 puntos
        score +=
            when {
                pausasDuranteReproduccion == 0 -> 20
                pausasDuranteReproduccion <= 2 -> 10
                else -> 0
            }

        // Pocos seeks +20 puntos (seeks excesivos = buscando parte específica o skip)
        score +=
            when {
                seeksRealizados == 0 -> 20
                seeksRealizados <= 2 -> 10
                else -> 0
            }

        // Agregó a favoritos +20 puntos
        if (agregadoAFavoritosDurante) score += 20

        return score.coerceIn(0, 100)
    }

    companion object {
        // Orígenes de reproducción
        const val ORIGEN_PLAYLIST = "PLAYLIST"
        const val ORIGEN_ALBUM = "ALBUM"
        const val ORIGEN_ARTISTA = "ARTISTA"
        const val ORIGEN_GENERO = "GENERO"
        const val ORIGEN_BUSQUEDA = "BUSQUEDA"
        const val ORIGEN_ALEATORIO = "ALEATORIO"
        const val ORIGEN_FAVORITOS = "FAVORITOS"
        const val ORIGEN_RECOMENDACION = "RECOMENDACION"
        const val ORIGEN_RECIENTES = "RECIENTES"
        const val ORIGEN_MAS_REPRODUCIDAS = "MAS_REPRODUCIDAS"
        const val ORIGEN_COMPARTIDO = "COMPARTIDO"

        // Modos de reproducción
        const val MODO_NORMAL = "NORMAL"
        const val MODO_SHUFFLE = "SHUFFLE"
        const val MODO_REPEAT_ONE = "REPEAT_ONE"
        const val MODO_REPEAT_ALL = "REPEAT_ALL"

        // Tipos de salida de audio
        const val SALIDA_SPEAKER = "SPEAKER"
        const val SALIDA_HEADPHONES = "HEADPHONES"
        const val SALIDA_BLUETOOTH = "BLUETOOTH"
        const val SALIDA_EXTERNAL = "EXTERNAL"
        const val SALIDA_CAST = "CAST" // Chromecast, AirPlay, etc.

        // Umbral para considerar reproducción completa
        const val UMBRAL_COMPLETO = 0.8f // 80%

        /** Crea un registro de historial básico */
        fun crear(
            idUsuario: Int,
            idCancion: Int,
            duracionReproducidaMs: Int,
            duracionTotalMs: Int,
            origen: String,
            idContexto: Int? = null,
        ): PlaybackHistoryEntity {
            val porcentaje =
                if (duracionTotalMs > 0) {
                    (duracionReproducidaMs.toFloat() / duracionTotalMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

            val calendario = java.util.Calendar.getInstance()

            return PlaybackHistoryEntity(
                idUsuario = idUsuario,
                idCancion = idCancion,
                duracionReproducidaMs = duracionReproducidaMs,
                duracionTotalCancionMs = duracionTotalMs,
                porcentajeReproducido = porcentaje,
                completo = porcentaje >= UMBRAL_COMPLETO,
                origen = origen,
                idContexto = idContexto,
                horaDelDia = calendario.get(java.util.Calendar.HOUR_OF_DAY),
                diaSemana = calendario.get(java.util.Calendar.DAY_OF_WEEK),
            )
        }

        /** Crea un registro completo con todos los detalles */
        fun crearCompleto(
            idUsuario: Int,
            idCancion: Int,
            duracionReproducidaMs: Int,
            duracionTotalMs: Int,
            origen: String,
            idContexto: Int? = null,
            nombreContexto: String? = null,
            calidadReproducida: String? = null,
            modoReproduccion: String = MODO_NORMAL,
            dispositivoId: String? = null,
            tipoSalidaAudio: String? = null,
        ): PlaybackHistoryEntity {
            val basico =
                crear(
                    idUsuario,
                    idCancion,
                    duracionReproducidaMs,
                    duracionTotalMs,
                    origen,
                    idContexto,
                )

            return basico.copy(
                nombreContexto = nombreContexto,
                calidadReproducida = calidadReproducida,
                modoReproduccion = modoReproduccion,
                dispositivoId = dispositivoId,
                tipoSalidaAudio = tipoSalidaAudio,
            )
        }
    }
}
