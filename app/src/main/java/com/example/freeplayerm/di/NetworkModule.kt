// en: app/src/main/java/com/example/freeplayerm/di/NetworkModule.kt
package com.example.freeplayerm.di

import android.content.Context
import com.example.freeplayerm.BuildConfig
import com.example.freeplayerm.data.remote.GeniusApiService
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
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.genius.com/"
    private const val RATE_LIMIT_TIMEOUT = 30L // segundos
    private const val CONNECT_TIMEOUT = 15L // segundos
    private const val READ_TIMEOUT = 30L // segundos
    private const val WRITE_TIMEOUT = 30L // segundos
    private const val CACHE_SIZE = 10L * 1024L * 1024L // 10MB Cache

    // Qualifiers para diferenciar los interceptores
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AuthInterceptor

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RateLimitingInterceptor

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class RetryInterceptor

    @Provides
    @Singleton
    @AuthInterceptor
    fun provideAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            // VERIFICACIÓN DE SEGURIDAD: Asegurar que tenemos el token configurado
            val token = BuildConfig.GENIUS_CLIENT_ACCESS_TOKEN
            if (token.isBlank()) {
                throw SecurityException("Genius API Client Access Token no está configurado. " +
                        "Verifica que GENIUS_CLIENT_ACCESS_TOKEN esté en tu build.gradle o local.properties")
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
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.NONE
            }

            redactHeader("Authorization")
            redactHeader("Cookie")
        }
    }

    @Provides
    @Singleton
    @RateLimitingInterceptor
    fun provideRateLimitingInterceptor(): Interceptor {
        return Interceptor { chain ->
            try {
                // Pequeño delay para evitar rate limiting agresivo
                Thread.sleep(100)
                chain.proceed(chain.request())
            } catch (e: SocketTimeoutException) {
                throw IOException("Timeout de Genius API - posible rate limiting o problemas de conexión", e)
            } catch (e: Exception) {
                throw IOException("Error de red al conectar con Genius API: ${e.message}", e)
            }
        }
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

                // Backoff exponencial simple
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

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        @AuthInterceptor authInterceptor: Interceptor,
        @RateLimitingInterceptor rateLimitingInterceptor: Interceptor,
        @RetryInterceptor retryInterceptor: Interceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Interceptores en orden de ejecución
            addInterceptor(authInterceptor)
            addInterceptor(rateLimitingInterceptor)
            addInterceptor(retryInterceptor)
            addInterceptor(loggingInterceptor)

            // Timeouts robustos
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)

            // Configuración de cache
            cache(Cache(File(context.cacheDir, "genius_http_cache"), CACHE_SIZE))

            // Configuraciones adicionales para robustez
            retryOnConnectionFailure(true)
            followRedirects(true)
            followSslRedirects(true)

        }.build()
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
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
}