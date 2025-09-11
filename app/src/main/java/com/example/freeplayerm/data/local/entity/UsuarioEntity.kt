package com.example.freeplayerm.data.local.entity // Asegúrate de que tu nombre de paquete coincida

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
// Todas las propiedades ahora están dentro del constructor primario (...)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_usuario") // Corregido para consistencia
    val id: Int = 0,

    @ColumnInfo(name = "nombre_usuario")
    val nombreUsuario: String,

    @ColumnInfo(name = "correo")
    val correo: String, // Corregido de 'email' a 'correo'

    @ColumnInfo(name = "contrasena_hash")
    val contrasenaHash: String?, // Corregido a nulable para login con Google

    @ColumnInfo(name = "fecha_registro")
    val fechaRegistro: Date,

    @ColumnInfo(name = "foto_perfil_path_local")
    val fotoPerfilPathLocal: String? = null,

    @ColumnInfo(name = "tipo_autenticacion")
    val tipoAutenticacion: String
)