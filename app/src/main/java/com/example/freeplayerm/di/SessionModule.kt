package com.example.freeplayerm.di

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: Player // Hilt sabe cómo proveer el Player gracias a PlayerModule
    ): MediaSession {
        return MediaSession.Builder(context, player).build()
    }
}