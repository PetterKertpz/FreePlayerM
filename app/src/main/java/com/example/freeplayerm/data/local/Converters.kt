// en: app/src/main/java/com/example/freeplayerm/data/local/Converters.kt
package com.example.freeplayerm.data.local

import androidx.room.TypeConverter
import java.util.Date

/**
 * 🔄 TYPE CONVERTERS - ROOM DATABASE
 *
 * Clase que define conversores de tipos para Room
 * Permite a Room trabajar con tipos de datos que no soporta nativamente
 *
 * Tipos soportados:
 * - Date <-> Int (timestamp)
 * - List<String> <-> String (JSON o delimitado por comas)
 * - Boolean <-> Int (0 o 1)
 *
 * @version 1.0 - Standard Converters
 */
class Converters {

    // ==================== DATE <-> Int ====================

    /**
     * Convierte un Int (milisegundos desde la época) a un objeto Date
     * Room usará esta función cuando lea datos de la base de datos
     *
     * @param value El valor numérico de la base de datos (timestamp)
     * @return Un objeto Date, o null si el valor de la base de datos es nulo
     */
    @TypeConverter
    fun fromTimestamp(value: Int?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Convierte un objeto Date a un Int (milisegundos)
     * Room usará esta función cuando escriba datos en la base de datos
     *
     * @param date El objeto Date de nuestra entidad
     * @return Un Int que representa la fecha, o null si el objeto Date es nulo
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Int? {
        return date?.time?.toInt()
    }

    // ==================== LIST<STRING> <-> STRING ====================
    // Útil si necesitas almacenar listas de strings (ej: géneros, tags)

    /**
     * Convierte una String delimitada por comas a List<String>
     * Ejemplo: "rock,pop,jazz" -> ["rock", "pop", "jazz"]
     *
     * @param value String de la base de datos
     * @return Lista de strings, o lista vacía si es null
     */
    @TypeConverter
    fun fromStringToList(value: String?): List<String> {
        return value?.split(",")?.map { it.trim() } ?: emptyList()
    }

    /**
     * Convierte una List<String> a String delimitada por comas
     * Ejemplo: ["rock", "pop", "jazz"] -> "rock,pop,jazz"
     *
     * @param list Lista de strings
     * @return String delimitada por comas, o null si la lista está vacía
     */
    @TypeConverter
    fun fromListToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    // ==================== BOOLEAN <-> Int ====================
    // SQLite no tiene tipo BOOLEAN nativo, usa INTEGER (0 o 1)

    /**
     * Convierte Int a Boolean
     * 0 = false, cualquier otro valor = true
     *
     * @param value Valor de la base de datos (0 o 1)
     * @return Boolean correspondiente
     */
    @TypeConverter
    fun fromIntToBoolean(value: Int?): Boolean {
        return value != 0
    }

    /**
     * Convierte Boolean a Int
     * false = 0, true = 1
     *
     * @param boolean Valor booleano
     * @return 0 o 1
     */
    @TypeConverter
    fun fromBooleanToInt(boolean: Boolean?): Int {
        return if (boolean == true) 1 else 0
    }

    /**
     * Convierte JSON String a Map<String, String>
     * Útil para campos dinámicos como redes sociales
     */
    @TypeConverter
    fun fromJsonToStringMap(value: String?): Map<String, String>? {
        if (value.isNullOrBlank()) return null
        return try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
            com.google.gson.Gson().fromJson(value, type)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromStringMapToJson(map: Map<String, String>?): String? {
        return if (map == null) null else com.google.gson.Gson().toJson(map)
    }

    /**
     * Convierte JSON String a List<Int>
     * Útil para listas de IDs (colaboradores, etc.)
     */
    @TypeConverter
    fun fromJsonToIntList(value: String?): List<Int>? {
        if (value.isNullOrBlank()) return null
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<Int>>() {}.type
            com.google.gson.Gson().fromJson(value, type)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromIntListToJson(list: List<Int>?): String? {
        return if (list == null) null else com.google.gson.Gson().toJson(list)
    }

    /**
     * Convierte JSON String a Map<String, Any>
     * Para datos complejos como metadatos
     */
    @TypeConverter
    fun fromJsonToAnyMap(value: String?): Map<String, Any>? {
        if (value.isNullOrBlank()) return null
        return try {
            val type = object : com.google.gson.reflect.TypeToken<Map<String, Any>>() {}.type
            com.google.gson.Gson().fromJson(value, type)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromAnyMapToJson(map: Map<String, Any>?): String? {
        return if (map == null) null else com.google.gson.Gson().toJson(map)
    }

    /**
     * Convierte JSON String a List<String>
     * Alternativa a fromStringToList para casos específicos
     */
    @TypeConverter
    fun fromJsonToStringList(value: String?): List<String>? {
        if (value.isNullOrBlank()) return null
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            com.google.gson.Gson().fromJson(value, type)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromStringListToJson(list: List<String>?): String? {
        return if (list == null) null else com.google.gson.Gson().toJson(list)
    }
}