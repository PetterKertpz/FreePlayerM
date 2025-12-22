// app/src/main/java/com/example/freeplayerm/di/NetworkModule.kt
package com.example.freeplayerm.di

import android.content.Context
import com.example.freeplayerm.BuildConfig
import com.example.freeplayerm.data.remote.genius.api.GeniusApiService
import com.example.freeplayerm.data.remote.genius.interceptor.RateLimitInterceptor
import com.example.freeplayerm.data.remote.genius.interceptor.RateLimitPresets
import com.example.freeplayerm.data.remote.genius.scraper.GeniusScraper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * 🌐 NETWORK MODULE
 *
 * Módulo de inyección de dependencias para configuración de red
 * - Genius API con autenticación
 * - Rate limiting automático
 * - Retry con backoff
 * - Scraper con OkHttpClient dedicado
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.genius.com/"
    private const val CONNECT_TIMEOUT = 15L // segundos
    private const val READ_TIMEOUT = 30L // segundos
    private const val WRITE_TIMEOUT = 30L // segundos
    private const val CACHE_SIZE = 10L * 1024L * 1024L // 10MB Cache

    // ==================== QUALIFIERS ====================

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AuthInterceptor

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RetryInterceptor

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ApiClient

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ScraperClient

    // ==================== INTERCEPTORS ====================

    @Provides
    @Singleton
    @AuthInterceptor
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            // Verificación de seguridad: Asegurar que tenemos el token configurado
            val token = BuildConfig.GENIUS_CLIENT_ACCESS_TOKEN
            if (token.isBlank()) {
                throw SecurityException(
                    "Genius API Client Access Token no está configurado. " +
                            "Verifica que GENIUS_CLIENT_ACCESS_TOKEN esté en tu build.gradle o local.properties"
                )
            }

            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("User-Agent", "FreePlayerM/1.0 (Android)")
                .addHeader("Accept", "application/json")
                .build()

            chain.proceed(request)
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }

            redactHeader("Authorization")
            redactHeader("Cookie")
        }
    }

    /**
     * Rate limiter para Genius API
     * Preset: 10 requests por minuto con estrategia WAIT
     */
    @Provides
    @Singleton
    fun provideRateLimitInterceptor(): RateLimitInterceptor {
        return RateLimitPresets.geniusApi()
    }

    @Provides
    @Singleton
    @RetryInterceptor
    fun provideRetryInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            var response = chain.proceed(request)
            var retryCount = 0
            val maxRetries = 2

            while (!response.isSuccessful && retryCount < maxRetries) {
                retryCount++
                android.util.Log.w("NetworkModule", "Reintento $retryCount para: ${request.url}")

                // Backoff exponencial
                Thread.sleep((100 * retryCount).toLong())

                response.close()
                response = chain.proceed(request)
            }

            // Manejar códigos de error específicos de Genius
            if (!response.isSuccessful) {
                when (response.code) {
                    429 -> throw IOException("Rate limit excedido en Genius API - demasiadas solicitudes")
                    401 -> throw SecurityException("Token de Genius API inválido o expirado")
                    403 -> throw SecurityException("Acceso denegado a Genius API")
                    404 -> throw IOException("Recurso no encontrado en Genius API")
                    in 500..599 -> throw IOException("Error del servidor Genius API: ${response.code}")
                }
            }

            response
        }
    }

    // ==================== OKHTTP CLIENTS ====================

    /**
     * Cliente para Genius API
     * Incluye: Auth, Rate Limiting, Retry, Logging
     */
    @Provides
    @Singleton
    @ApiClient
    fun provideApiOkHttpClient(
        @ApplicationContext context: Context,
        @AuthInterceptor authInterceptor: Interceptor,
        rateLimitInterceptor: RateLimitInterceptor,
        @RetryInterceptor retryInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Orden importante: Auth -> Rate Limit -> Retry -> Logging
            addInterceptor(authInterceptor)
            addInterceptor(rateLimitInterceptor)
            addInterceptor(retryInterceptor)
            addInterceptor(loggingInterceptor)

            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

            cache(Cache(File(context.cacheDir, "genius_api_cache"), CACHE_SIZE))

            retryOnConnectionFailure(true)
            followRedirects(true)
            followSslRedirects(true)
        }.build()
    }

    /**
     * Cliente para Web Scraping
     * Incluye: Rate Limiting más estricto, User-Agent rotation
     */
    @Provides
    @Singleton
    @ScraperClient
    fun provideScraperOkHttpClient(
        @ApplicationContext context: Context,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        // Rate limiting más conservador para scraping
        val scraperRateLimit = RateLimitPresets.scraping()

        return OkHttpClient.Builder().apply {
            addInterceptor(scraperRateLimit)
            addInterceptor(loggingInterceptor)

            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

            cache(Cache(File(context.cacheDir, "genius_scraper_cache"), CACHE_SIZE))

            retryOnConnectionFailure(true)
            followRedirects(true)
            followSslRedirects(true)
        }.build()
    }

    /**
     * Cliente genérico para descargas de imágenes
     * Sin rate limiting (las URLs son directas)
     */
    @Provides
    @Singleton
    fun provideGenericOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

            cache(Cache(File(context.cacheDir, "image_cache"), CACHE_SIZE))

            retryOnConnectionFailure(true)
            followRedirects(true)
        }.build()
    }

    // ==================== MOSHI ====================

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    // ==================== RETROFIT ====================

    @Provides
    @Singleton
    fun provideRetrofit(
        @ApiClient okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideGeniusApiService(retrofit: Retrofit): GeniusApiService {
        return retrofit.create(GeniusApiService::class.java)
    }

    // ==================== SCRAPER ====================

    @Provides
    @Singleton
    fun provideGeniusScraper(
        @ScraperClient okHttpClient: OkHttpClient
    ): GeniusScraper {
        return GeniusScraper(okHttpClient)
    }

    //===================AUTH============================
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

}