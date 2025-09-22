package com.example.freeplayerm.di

import android.content.Context
import coil.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            // Habilita una transición suave cuando la imagen se carga.
            .crossfade(true)
            // Optimiza el uso de memoria usando un formato de color más ligero.
            .allowRgb565(true)
            // --- ✅ LA OPTIMIZACIÓN MÁS IMPORTANTE ---
            // Le dice a Coil que haga todo el trabajo pesado (descargar, decodificar)
            // en un hilo de fondo (Dispatchers.IO), manteniendo la UI fluida.
            .dispatcher(Dispatchers.IO)
            .build()
    }
}