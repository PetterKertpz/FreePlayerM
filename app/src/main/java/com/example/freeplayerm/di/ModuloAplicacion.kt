package com.example.freeplayerm.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.remote.genius.scraper.GeniusScraper
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UserRepository
import com.example.freeplayerm.data.repository.UserRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Named
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModuloAplicacion {

    @Provides
    @Singleton
    fun provideGeniusScraper(
        @ScraperClient okHttpClient: OkHttpClient  // ✅ Cambiado de @Named("genius") a @ScraperClient
    ): GeniusScraper {
        return GeniusScraper(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(
        usuarioDao: UsuarioDao,
        sessionRepository: SessionRepository,
        firebaseAuth: FirebaseAuth
    ): UserRepository {
        return UserRepositoryImpl(usuarioDao, sessionRepository, firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager {
        return CredentialManager.create(context)
    }

    @Provides
    @Singleton
    fun provideGoogleAuthUiClient(
        @ApplicationContext context: Context,
        credentialManager: CredentialManager
    ): GoogleAuthUiClient {
        return GoogleAuthUiClient(context, credentialManager)
    }
}