// en: app/src/main/java/com/example/freeplayerm/data/local/AppDatabase.kt
package com.example.freeplayerm.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.AlbumEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.ArtistaEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.CancionEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.DetalleListaReproduccion
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.Favorito
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.GeneroEntity
import com.example.freeplayerm.com.example.freeplayerm.data.local.entity.relations.ListaReproduccionEntity
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity

// --- CAMBIO AQUÍ ---
// Añadimos la anotación @TypeConverters para que Room sepa que debe usar nuestra clase Converters.
@Database(
    entities = [
        UsuarioEntity::class,
        ArtistaEntity::class,
        AlbumEntity::class,
        GeneroEntity::class,
        CancionEntity::class,
        ListaReproduccionEntity::class,
        DetalleListaReproduccion::class,
        Favorito::class
    ],
    version = 2, // ¡IMPORTANTE! Se incrementa la versión por el cambio de esquema
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun cancionDao(): CancionDao // <-- NUEVO DAO AÑADIDO
}