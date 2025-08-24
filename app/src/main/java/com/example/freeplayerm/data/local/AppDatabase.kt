package com.example.freeplayerm.data.local // Asegúrate de que tu nombre de paquete coincida

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity

/**
 * @Database: Marca esta clase abstracta como la base de datos principal de Room.
 *
 * entities: Una lista de TODAS las clases con la anotación @Entity que formarán parte de esta base de datos.
 *
 * version: El número de versión del esquema. Siempre empezamos en 1.
 *
 * exportSchema = false: Simplifica el proceso de compilación desactivando la exportación del esquema.
 */
@Database(
    entities = [UsuarioEntity::class], // <-- Por ahora, solo tenemos la entidad de usuario.
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class) // <-- ¡Importante! Aquí le decimos a Room que use nuestro traductor.
abstract class AppDatabase : RoomDatabase() {

    /**
     * Declaramos una función abstracta por cada DAO que tengamos.
     * Room generará la implementación de esta función por nosotros.
     * A través de esta función, obtendremos acceso a todas nuestras operaciones CRUD para usuarios.
     */
    abstract fun usuarioDao(): UsuarioDao

    // Cuando creemos más entidades y DAOs, los añadiremos aquí.
    // Ejemplo: abstract fun cancionDao(): CancionDao
}