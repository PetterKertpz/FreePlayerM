package com.example.freeplayerm.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad para gestionar colaboradores de playlists con permisos granulares.
 *
 * Reemplaza el campo booleano simple "esColaborativa" de ListaReproduccionEntity
 * con un sistema de permisos detallado por usuario.
 *
 * Relaciones:
 * - N:1 con ListaReproduccionEntity (una playlist tiene múltiples colaboradores)
 * - N:1 con UsuarioEntity (un usuario puede colaborar en múltiples playlists)
 *
 * Casos de uso:
 * - Playlists colaborativas entre amigos con diferentes niveles de acceso
 * - Playlists públicas donde cualquiera puede sugerir canciones
 * - Gestión de permisos: quién puede agregar, eliminar, reordenar, editar metadata
 *
 * @property idColaborador ID único del colaborador
 * @property idLista ID de la playlist (foreign key)
 * @property idUsuario ID del usuario colaborador (foreign key)
 * @property rol Rol del colaborador (OWNER, EDITOR, CONTRIBUTOR, VIEWER)
 * @property puedeAgregar Permiso para agregar canciones
 * @property puedeEliminar Permiso para eliminar canciones
 * @property puedeReordenar Permiso para cambiar el orden
 * @property puedeEditarMetadata Permiso para editar título, descripción, portada
 * @property puedeInvitar Permiso para invitar otros colaboradores
 * @property fechaInvitacion Timestamp de cuándo fue invitado
 * @property fechaAceptacion Timestamp de cuándo aceptó la invitación
 * @property estado Estado de la colaboración (PENDING, ACTIVE, REJECTED, REMOVED)
 * @property activo Si la colaboración está activa
 */
@Entity(
    tableName = "lista_colaborador",
    foreignKeys = [
        ForeignKey(
            entity = ListaReproduccionEntity::class,
            parentColumns = ["id_lista"],
            childColumns = ["id_lista"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id_usuario"],
            childColumns = ["id_usuario"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_lista"]),
        Index(value = ["id_usuario"]),
        Index(value = ["id_lista", "id_usuario"], unique = true), // Un usuario por playlist
        Index(value = ["rol"]),
        Index(value = ["estado"])
    ]
)
data class ListaColaboradorEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_colaborador")
    val idColaborador: Int = 0,

    // ==================== RELACIONES ====================

    @ColumnInfo(name = "id_lista")
    val idLista: Int,

    @ColumnInfo(name = "id_usuario")
    val idUsuario: Int,

    // ==================== ROL Y PERMISOS ====================

    /**
     * Rol del colaborador en la playlist
     * Valores: OWNER, EDITOR, CONTRIBUTOR, VIEWER
     *
     * - OWNER: Creador, todos los permisos incluyendo eliminar playlist
     * - EDITOR: Puede hacer todo excepto eliminar la playlist o cambiar owner
     * - CONTRIBUTOR: Puede agregar y reordenar, no eliminar
     * - VIEWER: Solo puede ver y reproducir
     */
    @ColumnInfo(name = "rol")
    val rol: String,

    // ==================== PERMISOS GRANULARES ====================

    /**
     * Puede agregar nuevas canciones a la playlist
     */
    @ColumnInfo(name = "puede_agregar")
    val puedeAgregar: Boolean = false,

    /**
     * Puede eliminar canciones de la playlist
     */
    @ColumnInfo(name = "puede_eliminar")
    val puedeEliminar: Boolean = false,

    /**
     * Puede reordenar canciones existentes
     */
    @ColumnInfo(name = "puede_reordenar")
    val puedeReordenar: Boolean = false,

    /**
     * Puede editar título, descripción y portada
     */
    @ColumnInfo(name = "puede_editar_metadata")
    val puedeEditarMetadata: Boolean = false,

    /**
     * Puede invitar a otros colaboradores
     */
    @ColumnInfo(name = "puede_invitar")
    val puedeInvitar: Boolean = false,

    /**
     * Puede remover otros colaboradores (excepto owner)
     */
    @ColumnInfo(name = "puede_remover_colaboradores")
    val puedeRemoverColaboradores: Boolean = false,

    /**
     * Puede cambiar la visibilidad (pública/privada)
     */
    @ColumnInfo(name = "puede_cambiar_visibilidad")
    val puedeCambiarVisibilidad: Boolean = false,

    // ==================== INVITACIÓN Y ESTADO ====================

    /**
     * ID del usuario que invitó a este colaborador
     */
    @ColumnInfo(name = "invitado_por")
    val invitadoPor: Int? = null,

    @ColumnInfo(name = "fecha_invitacion")
    val fechaInvitacion: Long = System.currentTimeMillis(),

    /**
     * Fecha en que aceptó la invitación (null si aún no acepta)
     */
    @ColumnInfo(name = "fecha_aceptacion")
    val fechaAceptacion: Int? = null,

    /**
     * Estado de la colaboración
     * Valores: PENDING, ACTIVE, REJECTED, REMOVED
     */
    @ColumnInfo(name = "estado")
    val estado: String = "PENDING",

    // ==================== ESTADÍSTICAS ====================

    /**
     * Cantidad de canciones agregadas por este colaborador
     */
    @ColumnInfo(name = "canciones_agregadas")
    val cancionesAgregadas: Int = 0,

    /**
     * Cantidad de cambios realizados (ediciones, reorden, etc.)
     */
    @ColumnInfo(name = "cambios_realizados")
    val cambiosRealizados: Int = 0,

    /**
     * Última actividad en la playlist
     */
    @ColumnInfo(name = "ultima_actividad")
    val ultimaActividad: Int? = null,

    // ==================== METADATOS ====================

    /**
     * Mensaje de invitación personalizado
     */
    @ColumnInfo(name = "mensaje_invitacion")
    val mensajeInvitacion: String? = null,

    /**
     * Notas sobre este colaborador (para el owner)
     */
    @ColumnInfo(name = "notas")
    val notas: String? = null,

    @ColumnInfo(name = "activo")
    val activo: Boolean = true

) {
    companion object {
        /**
         * Roles de colaborador disponibles
         */
        object Rol {
            const val OWNER = "OWNER"
            const val EDITOR = "EDITOR"
            const val CONTRIBUTOR = "CONTRIBUTOR"
            const val VIEWER = "VIEWER"

            /**
             * Obtiene el nombre para mostrar del rol
             */
            fun obtenerNombreDisplay(rol: String): String {
                return when (rol) {
                    OWNER -> "Propietario"
                    EDITOR -> "Editor"
                    CONTRIBUTOR -> "Colaborador"
                    VIEWER -> "Espectador"
                    else -> rol.replaceFirstChar { it.uppercase() }
                }
            }

            /**
             * Obtiene descripción del rol
             */
            fun obtenerDescripcion(rol: String): String {
                return when (rol) {
                    OWNER -> "Control total sobre la playlist"
                    EDITOR -> "Puede editar todo excepto eliminar la playlist"
                    CONTRIBUTOR -> "Puede agregar y reordenar canciones"
                    VIEWER -> "Solo puede ver y reproducir"
                    else -> ""
                }
            }

            /**
             * Obtiene permisos predeterminados para un rol
             */
            fun obtenerPermisosDefault(rol: String): PermisosColaborador {
                return when (rol) {
                    OWNER -> PermisosColaborador(
                        puedeAgregar = true,
                        puedeEliminar = true,
                        puedeReordenar = true,
                        puedeEditarMetadata = true,
                        puedeInvitar = true,
                        puedeRemoverColaboradores = true,
                        puedeCambiarVisibilidad = true
                    )
                    EDITOR -> PermisosColaborador(
                        puedeAgregar = true,
                        puedeEliminar = true,
                        puedeReordenar = true,
                        puedeEditarMetadata = true,
                        puedeInvitar = true,
                        puedeRemoverColaboradores = false,
                        puedeCambiarVisibilidad = false
                    )
                    CONTRIBUTOR -> PermisosColaborador(
                        puedeAgregar = true,
                        puedeEliminar = false,
                        puedeReordenar = true,
                        puedeEditarMetadata = false,
                        puedeInvitar = false,
                        puedeRemoverColaboradores = false,
                        puedeCambiarVisibilidad = false
                    )
                    VIEWER -> PermisosColaborador(
                        puedeAgregar = false,
                        puedeEliminar = false,
                        puedeReordenar = false,
                        puedeEditarMetadata = false,
                        puedeInvitar = false,
                        puedeRemoverColaboradores = false,
                        puedeCambiarVisibilidad = false
                    )
                    else -> PermisosColaborador()
                }
            }
        }

        /**
         * Estados de colaboración
         */
        object Estado {
            const val PENDING = "PENDING" // Invitación pendiente
            const val ACTIVE = "ACTIVE" // Colaboración activa
            const val REJECTED = "REJECTED" // Invitación rechazada
            const val REMOVED = "REMOVED" // Removido por el owner
        }

        /**
         * Data class auxiliar para permisos
         */
        data class PermisosColaborador(
            val puedeAgregar: Boolean = false,
            val puedeEliminar: Boolean = false,
            val puedeReordenar: Boolean = false,
            val puedeEditarMetadata: Boolean = false,
            val puedeInvitar: Boolean = false,
            val puedeRemoverColaboradores: Boolean = false,
            val puedeCambiarVisibilidad: Boolean = false
        )

        /**
         * Crea un colaborador owner (creador de la playlist)
         */
        fun crearOwner(idLista: Int, idUsuario: Int): ListaColaboradorEntity {
            val permisos = Rol.obtenerPermisosDefault(Rol.OWNER)
            return ListaColaboradorEntity(
                idLista = idLista,
                idUsuario = idUsuario,
                rol = Rol.OWNER,
                puedeAgregar = permisos.puedeAgregar,
                puedeEliminar = permisos.puedeEliminar,
                puedeReordenar = permisos.puedeReordenar,
                puedeEditarMetadata = permisos.puedeEditarMetadata,
                puedeInvitar = permisos.puedeInvitar,
                puedeRemoverColaboradores = permisos.puedeRemoverColaboradores,
                puedeCambiarVisibilidad = permisos.puedeCambiarVisibilidad,
                estado = Estado.ACTIVE,
                fechaAceptacion = System.currentTimeMillis().toInt()
            )
        }

        /**
         * Crea una invitación de colaborador
         */
        fun crearInvitacion(
            idLista: Int,
            idUsuario: Int,
            invitadoPor: Int,
            rol: String = Rol.CONTRIBUTOR,
            mensaje: String? = null
        ): ListaColaboradorEntity {
            val permisos = Rol.obtenerPermisosDefault(rol)
            return ListaColaboradorEntity(
                idLista = idLista,
                idUsuario = idUsuario,
                rol = rol,
                invitadoPor = invitadoPor,
                puedeAgregar = permisos.puedeAgregar,
                puedeEliminar = permisos.puedeEliminar,
                puedeReordenar = permisos.puedeReordenar,
                puedeEditarMetadata = permisos.puedeEditarMetadata,
                puedeInvitar = permisos.puedeInvitar,
                puedeRemoverColaboradores = permisos.puedeRemoverColaboradores,
                puedeCambiarVisibilidad = permisos.puedeCambiarVisibilidad,
                estado = Estado.PENDING,
                mensajeInvitacion = mensaje
            )
        }
    }

    /**
     * Indica si es el propietario de la playlist
     */
    fun esOwner(): Boolean {
        return rol == Rol.OWNER
    }

    /**
     * Indica si la colaboración está activa
     */
    fun estaActiva(): Boolean {
        return estado == Estado.ACTIVE && activo
    }

    /**
     * Indica si la invitación está pendiente
     */
    fun estaPendiente(): Boolean {
        return estado == Estado.PENDING
    }

    /**
     * Indica si fue rechazado o removido
     */
    fun estaInactivo(): Boolean {
        return estado in listOf(Estado.REJECTED, Estado.REMOVED) || !activo
    }

    /**
     * Obtiene el nombre para mostrar del rol
     */
    fun obtenerNombreRol(): String {
        return Rol.obtenerNombreDisplay(rol)
    }

    /**
     * Obtiene descripción del rol
     */
    fun obtenerDescripcionRol(): String {
        return Rol.obtenerDescripcion(rol)
    }

    /**
     * Indica si tiene algún permiso de edición
     */
    fun tienePermisosEdicion(): Boolean {
        return puedeAgregar || puedeEliminar || puedeReordenar || puedeEditarMetadata
    }

    /**
     * Indica si puede realizar una acción específica
     */
    fun puedeRealizarAccion(accion: String): Boolean {
        return when (accion.uppercase()) {
            "AGREGAR" -> puedeAgregar
            "ELIMINAR" -> puedeEliminar
            "REORDENAR" -> puedeReordenar
            "EDITAR_METADATA" -> puedeEditarMetadata
            "INVITAR" -> puedeInvitar
            "REMOVER_COLABORADORES" -> puedeRemoverColaboradores
            "CAMBIAR_VISIBILIDAD" -> puedeCambiarVisibilidad
            else -> false
        } && estaActiva()
    }

    /**
     * Copia aceptando la invitación
     */
    fun aceptarInvitacion(): ListaColaboradorEntity {
        return copy(
            estado = Estado.ACTIVE,
            fechaAceptacion = System.currentTimeMillis().toInt()
        )
    }

    /**
     * Copia rechazando la invitación
     */
    fun rechazarInvitacion(): ListaColaboradorEntity {
        return copy(
            estado = Estado.REJECTED
        )
    }

    /**
     * Copia removiendo al colaborador
     */
    fun remover(): ListaColaboradorEntity {
        return copy(
            estado = Estado.REMOVED,
            activo = false
        )
    }

    /**
     * Copia incrementando contador de cambios
     */
    fun registrarCambio(): ListaColaboradorEntity {
        return copy(
            cambiosRealizados = cambiosRealizados + 1,
            ultimaActividad = System.currentTimeMillis().toInt()
        )
    }

    /**
     * Copia incrementando contador de canciones agregadas
     */
    fun registrarCancionAgregada(): ListaColaboradorEntity {
        return copy(
            cancionesAgregadas = cancionesAgregadas + 1,
            cambiosRealizados = cambiosRealizados + 1,
            ultimaActividad = System.currentTimeMillis().toInt()
        )
    }

    /**
     * Copia cambiando el rol
     */
    fun cambiarRol(nuevoRol: String): ListaColaboradorEntity {
        if (esOwner()) {
            throw IllegalStateException("No se puede cambiar el rol del owner")
        }

        val permisos = Rol.obtenerPermisosDefault(nuevoRol)
        return copy(
            rol = nuevoRol,
            puedeAgregar = permisos.puedeAgregar,
            puedeEliminar = permisos.puedeEliminar,
            puedeReordenar = permisos.puedeReordenar,
            puedeEditarMetadata = permisos.puedeEditarMetadata,
            puedeInvitar = permisos.puedeInvitar,
            puedeRemoverColaboradores = permisos.puedeRemoverColaboradores,
            puedeCambiarVisibilidad = permisos.puedeCambiarVisibilidad
        )
    }

    /**
     * Obtiene un resumen de actividad
     */
    fun obtenerResumenActividad(): String {
        return buildString {
            append("$cancionesAgregadas canciones agregadas")
            if (cambiosRealizados > cancionesAgregadas) {
                append(", ${cambiosRealizados - cancionesAgregadas} cambios adicionales")
            }
        }
    }
}