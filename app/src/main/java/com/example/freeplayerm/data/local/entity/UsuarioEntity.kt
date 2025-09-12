// en: app/src/main/java/com/example/freeplayerm/data/local/entity/UsuarioEntity.kt
package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["nombre_usuario"], unique = true),
        Index(value = ["correo"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario")
    val id: Int = 0,

    @ColumnInfo(name = "nombre_usuario")
    val nombreUsuario: String,

    @ColumnInfo(name = "correo")
    val correo: String,

    @ColumnInfo(name = "contrasena_hash")
    val contrasenaHash: String?,

    // --- CAMBIO AQUÍ ---
    // Eliminamos el 'defaultValue'. Room ahora usará el TypeConverter para manejar el objeto Date.
    // La fecha se asigna de forma segura en UsuarioRepositoryImpl cuando se crea un nuevo usuario.
    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: Date,

    @ColumnInfo(name = "foto_perfil_url")
    val fotoPerfilUrl: String? = null,

    @ColumnInfo(name = "tipo_autenticacion")
    val tipoAutenticacion: String
)