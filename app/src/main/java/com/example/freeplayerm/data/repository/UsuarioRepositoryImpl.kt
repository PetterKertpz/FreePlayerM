// Movido al paquete 'repository'
package com.example.freeplayerm.data.repository

// Añadimos los imports necesarios para no usar nombres largos
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.local.entity.UsuarioEntity

// Renombramos la clase a 'UsuarioRepositoryImpl'
class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao
) : UsuarioRepository { // Asegúrate de que implementa 'UsuarioRepository'

    override suspend fun insertarUsuario(usuario: UsuarioEntity) {
        usuarioDao.insertarUsuario(usuario)
    }

    override suspend fun obtenerUsuarioPorCorreo(correo: String): UsuarioEntity? {
        return usuarioDao.obtenerUsuarioPorCorreo(correo)
    }

    override suspend fun actualizarUsuario(usuario: UsuarioEntity) {
        usuarioDao.actualizarUsuario(usuario)
    }

    override suspend fun eliminarUsuario(usuario: UsuarioEntity) {
        usuarioDao.eliminarUsuario(usuario)
    }
}