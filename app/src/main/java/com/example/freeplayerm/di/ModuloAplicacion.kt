package com.example.freeplayerm.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.example.freeplayerm.core.auth.GoogleAuthUiClient
import com.example.freeplayerm.data.local.AppDatabase
import com.example.freeplayerm.data.local.dao.CancionDao
import com.example.freeplayerm.data.local.dao.LetraDao
import com.example.freeplayerm.data.local.dao.UsuarioDao
import com.example.freeplayerm.data.remote.GeniusApiService
import com.example.freeplayerm.data.remote.GeniusScraper
import com.example.freeplayerm.data.remote.GeniusServiceOptimizado
import com.example.freeplayerm.data.repository.SessionRepository
import com.example.freeplayerm.data.repository.UsuarioRepository
import com.example.freeplayerm.data.repository.UsuarioRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModuloAplicacion {

    @Provides
    @Singleton
    fun provideGeniusScraper(okHttpClient: OkHttpClient): GeniusScraper {
        return GeniusScraper(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideGeniusServiceOptimizado(apiService: GeniusApiService): GeniusServiceOptimizado {
        return GeniusServiceOptimizado(apiService)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "freeplayer_database"
        )
            .fallbackToDestructiveMigration(true)
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

    @Provides
    @Singleton
    fun provideLetraDao(appDatabase: AppDatabase): LetraDao {
        return appDatabase.letraDao()
    }
}