// petterkertpz/freeplayerm/FreePlayerM-bd9d6ee82a89fc2c742c9ea4a386bd47da80145d/app/src/main/java/com/example/freeplayerm/data/local/Converters.kt

package com.example.freeplayerm.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Los TypeConverters le enseñan a Room a convertir tipos de datos que no soporta
 * de forma nativa (como Date) a tipos que sí soporta (como Long).
 */
class Converters {
    /**
     * Convierte un timestamp (un número Long que representa milisegundos desde 1970)
     * a un objeto Date. Room usará esto cuando lea datos de la base de datos.
     * @param valor El número Long de la base de datos.
     * @return Un objeto Date, o null si el valor era nulo.
     */
    @TypeConverter
    fun desdeTimestamp(valor: Long?): Date? {
        return valor?.let { Date(it) }
    }

    /**
     * Convierte un objeto Date a un timestamp (Long). Room usará esto
     * cuando escriba datos en la base de datos.
     * @param fecha El objeto Date a convertir.
     * @return Un Long que representa la fecha, o null si la fecha era nula.
     */
    @TypeConverter
    fun fechaATimestamp(fecha: Date?): Long? {
        return fecha?.time
    }
}