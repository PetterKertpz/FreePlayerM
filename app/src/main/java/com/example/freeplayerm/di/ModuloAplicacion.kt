package com.example.freeplayerm.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import com.example.freeplayerm.data.local.AppDatabase
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.repository.UsuarioRepository
import com.example.freeplayerm.data.repository.UsuarioRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModuloAplicacion {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "freeplayer_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideUsuarioDao(appDatabase: AppDatabase): UsuarioDao {
        return appDatabase.usuarioDao()
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(usuarioDao: UsuarioDao): UsuarioRepository {
        return UsuarioRepositoryImpl(usuarioDao)
    }

    // ✅ AÑADIDO: Recetas para la autenticación
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