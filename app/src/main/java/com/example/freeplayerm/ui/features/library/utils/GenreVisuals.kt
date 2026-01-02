package com.example.freeplayerm.ui.features.library.utils

import androidx.annotation.DrawableRes
import com.example.freeplayerm.R

object GenreVisuals {

    // Un mapa que asocia el nombre de un género (en minúsculas y sin acentos)
    // con el ID de su imagen en los recursos drawable.
    private val genreImageMap =
        mapOf(
            "clásica" to R.mipmap.clasica,
            "jazz" to R.mipmap.jazz,
            "blues" to R.mipmap.blues,
            "rock" to R.mipmap.rock,
            "metal" to R.mipmap.metal,
            "pop" to R.mipmap.pop,
            "hip hop" to R.mipmap.hiphop,
            "rap" to R.mipmap.rap, // Se reutiliza la imagen de Hip Hop
            "electrónica" to R.mipmap.electronica,
            "reggae" to R.mipmap.reggae,
            "salsa" to R.mipmap.salsa,
            "reguetón" to R.mipmap.reggaeton,
            "cumbia" to R.mipmap.cumbia,
            "bachata" to R.mipmap.bachata,
            // Puedes añadir más géneros y sus respectivos drawables aquí
        )

    // Un recurso por defecto si no encontramos el género en nuestro mapa
    @DrawableRes private val defaultImage = R.mipmap.defaultgenero

    /**
     * Devuelve el ID del recurso drawable para un nombre de género dado. Normaliza el nombre para
     * que no importen mayúsculas o acentos.
     */
    @DrawableRes
    fun getImageForGenre(genreName: String): Int {
        val normalizedName =
            genreName
                .lowercase()
                .replace("é", "e") // Normalizamos caracteres comunes
                .trim()

        return genreImageMap[normalizedName] ?: defaultImage
    }
}
