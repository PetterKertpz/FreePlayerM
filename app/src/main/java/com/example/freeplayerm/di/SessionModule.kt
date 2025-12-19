package com.example.freeplayerm.di

import android.content.ComponentName
import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import com.example.freeplayerm.services.MusicService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * ✅ Módulo que provee MediaSession y ComponentName
 * IMPORTANTE: Este módulo es CRÍTICO para que el sistema funcione
 */
@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    /**
     * Provee la MediaSession que usará el MusicService
     */
    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: Player
    ): MediaSession {
        return MediaSession.Builder(context, player)
            .setId("FreePlayerSession")
            .build()
    }

    /**
     * ✅ CLAVE: Provee el ComponentName para que el ViewModel se conecte al servicio
     * Sin esto, el MediaController no sabrá a qué servicio conectarse
     */
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMusicServiceComponentName(
        @ApplicationContext context: Context
    ): ComponentName {
        return ComponentName(context, MusicService::class.java)
    }
}