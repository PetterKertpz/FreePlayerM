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
 * - Date <-> Long (timestamp en milisegundos)
 * - List<String> <-> String (delimitado por comas)
 * - Boolean <-> Int (0 o 1)
 * - List<Int> <-> String (JSON)
 * - Map<String, Any> <-> String (JSON)
 *
 * @version 1.1 - Fixed Duplicates
 */
class Converters {

    // ==================== DATE <-> Long ====================

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // ==================== LIST<STRING> <-> STRING ====================
    // Usa formato CSV simple (más eficiente que JSON para listas simples)

    @TypeConverter
    fun fromStringToList(value: String?): List<String> {
        return value?.split(",")?.map { it.trim() } ?: emptyList()
    }

    @TypeConverter
    fun fromListToString(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    // ==================== BOOLEAN <-> Int ====================
    // SQLite no tiene tipo BOOLEAN nativo, usa INTEGER (0 o 1)

    @TypeConverter
    fun fromIntToBoolean(value: Int?): Boolean {
        return value != 0
    }

    @TypeConverter
    fun fromBooleanToInt(boolean: Boolean?): Int {
        return if (boolean == true) 1 else 0
    }

    // ==================== LIST<INT> <-> STRING (JSON) ====================
    // Para listas de IDs (colaboradores, etc.)

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

    // ==================== MAP<STRING, ANY> <-> STRING (JSON) ====================
    // Para datos complejos como metadatos

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
}