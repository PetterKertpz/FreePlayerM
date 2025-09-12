// en: app/src/main/java/com/example/freeplayerm/data/local/Converters.kt
package com.example.freeplayerm.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * Esta clase le dice a Room cómo convertir tipos de datos complejos que no soporta de forma nativa.
 * En este caso, le enseñamos a convertir un objeto 'Date' a 'Long' y viceversa.
 */
class Converters {

    /**
     * Convierte un Long (milisegundos desde la época) a un objeto Date.
     * Room usará esta función cuando lea datos de la base de datos.
     * @param value El valor numérico de la base de datos.
     * @return Un objeto Date, o null si el valor de la base de datos es nulo.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        // Si el valor no es nulo, crea un nuevo objeto Date con él.
        return value?.let { Date(it) }
    }

    /**
     * Convierte un objeto Date a un Long (milisegundos).
     * Room usará esta función cuando escriba datos en la base de datos.
     * @param date El objeto Date de nuestra entidad.
     * @return Un Long que representa la fecha, o null si el objeto Date es nulo.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        // Devuelve los milisegundos de la fecha.
        return date?.time
    }
}