package com.example.freeplayerm.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthGoogleClient {

    /**
     * Provee el CredentialManager, que es la API principal.
     */
    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    /**
     * Provee nuestro ayudante `GoogleAuthUiClient`, usando el CredentialManager
     * que Hilt ya sabe cómo construir.
     */
    @Provides
    @Singleton
    fun provideGoogleAuthUiClient(
        @ApplicationContext context: Context,
        credentialManager: CredentialManager
    ): GoogleAuthUiClient {
        return GoogleAuthUiClient(
            context = context,
            credentialManager = credentialManager
        )
    }
}