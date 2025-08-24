package com.example.freeplayerm.data.local

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun desdeTimestamp(valor: Long?): Date? {
        return valor?.let { Date(it) }
    }

    @TypeConverter
    fun fechaATimestamp(fecha: Date?): Long? {
        return fecha?.time
    }
}