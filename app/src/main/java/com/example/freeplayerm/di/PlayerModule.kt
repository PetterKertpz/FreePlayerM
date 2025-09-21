package com.example.freeplayerm.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlayerModule {

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ): Player {

        // --- ✅ SOLUCIÓN ROBUSTA PARA LA VERSIÓN 1.8.0 ---

        // 1. Creamos un gestor de búfer (LoadControl) con valores aumentados.
        //    Esto es clave para evitar los clics y la interferencia.
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                /* minBufferMs = */ 60 * 1000,      // 1 minuto de búfer mínimo
                /* maxBufferMs = */ 120 * 1000,     // 2 minutos de búfer máximo
                /* bufferForPlaybackMs = */ 5 * 1000, // 5 segundos deben estar listos para empezar
                /* bufferForPlaybackAfterRebufferMs = */ 10 * 1000 // 10 segundos después de un re-buffer
            )
            .build()

        // 2. Construimos el ExoPlayer pasándole nuestra configuración de búfer.
        //    No intentamos modificar el 'offloading' para máxima compatibilidad.
        return ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: Player
    ): MediaSession {
        return MediaSession.Builder(context, player).build()
    }
}