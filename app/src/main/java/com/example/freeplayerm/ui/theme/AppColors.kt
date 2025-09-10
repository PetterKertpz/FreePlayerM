package com.example.freeplayerm.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 🎨 Paleta de colores corporativa optimizada.
 * - Escalable
 * - Consistente en nombres
 * - Preparada para Light/Dark Theme
 * - Útil tanto para branding como para utilidades generales
 */
object AppColors {

    val Negro = Color(0xFF000000)
    val Blanco = Color(0xFFFFFFFF)
    val Gris = Color(0xFF808080)
    val Transparente = Color(0x00000000)

    // 🔷 Colores principales
    val Primario = Color(0xFF0A3D62)
    val PrimarioClaro = Color(0xFF3C6382)
    val PrimarioOscuro = Color(0xFF082845)

    val Secundario = Color(0xFF10AC84)
    val SecundarioClaro = Color(0xFF1DD1A1)
    val SecundarioOscuro = Color(0xFF0E8C6B)

    // 🌈 Colores de acento
    val AcentoVioleta = Color(0xFFEE82EE)
    val AcentoAzul = Color(0xFF0984E3)
    val AcentoNaranja = Color(0xFFFF9F43)
    val AcentoRosa = Color(0xFFFF6B81)

    // 🚨 Estados
    val Exito = Color(0xFF00B894)
    val ExitoClaro = Color(0xFF55EFC4)
    val ExitoOscuro = Color(0xFF019875)

    val Advertencia = Color(0xFFFBC531)
    val AdvertenciaClaro = Color(0xFFFFEAA7)
    val AdvertenciaOscuro = Color(0xFFD4AC0D)

    val Error = Color(0xFFEE5253)
    val ErrorClaro = Color(0xFFFF7675)
    val ErrorOscuro = Color(0xFFC0392B)

    val Info = Color(0xFF3498DB)
    val InfoClaro = Color(0xFF74B9FF)
    val InfoOscuro = Color(0xFF21618C)

    // ⚪ Escala de grises
    val GrisMuyClaro = Color(0xFFFAFAFA)
    val GrisClaro = Color(0xFFEEEEEE)
    val GrisMedio = Color(0xFF9E9E9E)
    val GrisOscuro = Color(0xFF616161)
    val GrisProfundo = Color(0xFF212121)

    // 🖼️ Fondos y superficies
    val Fondo = Color(0xFFF5F6FA)
    val Superficie = Color(0xFFFFFFFF)
    val Tarjeta = Color(0xFFF0F3F5)
    val Sombra = Color(0x88000000)

    // ✍️ Texto
    val TextoPrincipal = Color(0xFF2D3436)
    val TextoSecundario = Color(0xFF636E72)
    val TextoDeshabilitado = Color(0xFFB2BEC3)
    val TextoEnPrimario = Color(0xFFFFFFFF)

    // 🔴 Rojos
    val RojoClaro = Color(0xFFFFCDD2)
    val RojoMedio = Color(0xFFE57373)
    val RojoFuerte = Color(0xFFF44336)
    val RojoOscuro = Color(0xFFD32F2F)
    val RojoProfundo = Color(0xFFB71C1C)

    // 🟢 Verdes
    val VerdeClaro = Color(0xFFC8E6C9)
    val VerdeMedio = Color(0xFF81C784)
    val VerdeFuerte = Color(0xFF4CAF50)
    val VerdeOscuro = Color(0xFF388E3C)
    val VerdeProfundo = Color(0xFF1B5E20)

    // 🔵 Azules
    val AzulClaro = Color(0xFF90CAF9)
    val AzulMedio = Color(0xFF64B5F6)
    val AzulFuerte = Color(0xFF2196F3)
    val AzulOscuro = Color(0xFF1976D2)
    val AzulProfundo = Color(0xFF0D47A1)

    // 🟡 Amarillos
    val AmarilloClaro = Color(0xFFFFF59D)
    val AmarilloMedio = Color(0xFFFFF176)
    val AmarilloFuerte = Color(0xFFFFEB3B)
    val AmarilloOscuro = Color(0xFFFBC02D)
    val AmarilloProfundo = Color(0xFFF57F17)

    // 🟣 Púrpuras
    val PurpuraClaro = Color(0xFFE1BEE7)
    val PurpuraMedio = Color(0xFFBA68C8)
    val PurpuraFuerte = Color(0xFF9C27B0)
    val PurpuraOscuro = Color(0xFF7B1FA2)
    val PurpuraProfundo = Color(0xFF4A148C)

    // 🟠 Naranjas
    val NaranjaClaro = Color(0xFFFFE0B2)
    val NaranjaMedio = Color(0xFFFFB74D)
    val NaranjaFuerte = Color(0xFFFF9800)
    val NaranjaOscuro = Color(0xFFF57C00)
    val NaranjaProfundo = Color(0xFFE65100)

    // 🌈 Extras
    val Rosa = Color(0xFFFF4081)
    val Cyan = Color(0xFF00BCD4)
    val Turquesa = Color(0xFF1DE9B6)
    val Lima = Color(0xFFCDDC39)
    val Cafe = Color(0xFF795548)

    // 🌙 Paleta oscura
    object Dark {
        val Fondo = Color(0xFF1E272E)
        val Superficie = Color(0xFF2F3640)
        val Tarjeta = Color(0xFF353B48)

        val TextoPrincipal = Color(0xFFF5F6FA)
        val TextoSecundario = Color(0xFFBDC3C7)
        val TextoDeshabilitado = Color(0xFF718093)
    }

    // 📋 Listas útiles (para recorrer dinámicamente en un selector de colores)
    val ListaBasica = listOf(
        Primario, Secundario, AcentoVioleta, AcentoAzul, AcentoNaranja, AcentoRosa,
        Exito, Advertencia, Error, Info,
        RojoFuerte, VerdeFuerte, AzulFuerte, AmarilloFuerte, PurpuraFuerte, NaranjaFuerte,
        Rosa, Cyan, Turquesa, Lima, Cafe
    )

    val ListaGrises = listOf(
        GrisMuyClaro, GrisClaro, GrisMedio, GrisOscuro, GrisProfundo
    )
}
