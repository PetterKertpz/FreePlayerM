package com.example.freeplayerm.di

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.example.freeplayerm.services.MusicService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo que provee ComponentName para conexión con MusicService.
 *
 * NOTA: MediaSession se crea DENTRO de MusicService para sincronizar ciclos de vida.
 */
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    /** Provee el ComponentName para que MediaController se conecte al servicio. */
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMusicServiceComponentName(@ApplicationContext context: Context): ComponentName =
        ComponentName(context, MusicService::class.java)
}
