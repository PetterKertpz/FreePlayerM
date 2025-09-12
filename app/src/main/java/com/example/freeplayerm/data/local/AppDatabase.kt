// en: app/src/main/java/com/example/freeplayerm/data/local/AppDatabase.kt
package com.example.freeplayerm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity

// --- CAMBIO AQUÍ ---
// Añadimos la anotación @TypeConverters para que Room sepa que debe usar nuestra clase Converters.
@Database(entities = [UsuarioEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // ¡AÑADIR ESTA LÍNEA!
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
}