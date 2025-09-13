package com.example.freeplayerm.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import com.example.freeplayerm.data.local.AppDatabase
import com.example.freeplayerm.data.local.dao.CancionDao // <-- Importante
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.repository.SessionRepository
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
        )
            // AÑADIDO: Permite a Room destruir y recrear la BD si las migraciones fallan
            // Es útil durante el desarrollo, pero para producción se necesitaría un plan de migración.
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideUsuarioDao(appDatabase: AppDatabase): UsuarioDao {
        return appDatabase.usuarioDao()
    }

    // --- ¡RECETA AÑADIDA AQUÍ! ---
    // Le decimos a Hilt cómo crear un CancionDao.
    @Provides
    @Singleton
    fun provideCancionDao(appDatabase: AppDatabase): CancionDao {
        return appDatabase.cancionDao() // Simplemente lo pedimos a nuestra base de datos.
    }

    @Provides
    @Singleton
    fun provideUsuarioRepository(
        usuarioDao: UsuarioDao,
        sessionRepository: SessionRepository
    ): UsuarioRepository {
        return UsuarioRepositoryImpl(usuarioDao, sessionRepository)
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