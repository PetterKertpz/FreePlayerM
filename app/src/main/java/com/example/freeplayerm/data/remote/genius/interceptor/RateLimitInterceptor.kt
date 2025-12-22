// app/src/main/java/com/example/freeplayerm/data/remote/genius/interceptor/RateLimitInterceptor.kt
package com.example.freeplayerm.data.remote.genius.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * ⏱️ RATE LIMIT INTERCEPTOR
 *
 * Interceptor de OkHttp para manejar rate limiting automáticamente
 * Controla frecuencia de requests para evitar exceder límites de APIs
 *
 * Características:
 * - ✅ Rate limiting configurable por ventana de tiempo
 * - ✅ Thread-safe con ReentrantLock
 * - ✅ Logging de esperas y rechazos
 * - ✅ Múltiples estrategias (esperar, fallar, retry con backoff)
 * - ✅ Rate limiting por dominio/host específico
 */
class RateLimitInterceptor(
    private val maxRequests: Int,
    private val timeWindowMillis: Long,
    private val strategy: RateLimitStrategy = RateLimitStrategy.WAIT,
    private val targetHost: String? = null
) : Interceptor {

    private val requests = mutableListOf<Long>()
    private val lock = ReentrantLock()
    private val tag = "RateLimitInterceptor"

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Si targetHost está definido, solo aplicar rate limit a ese host
        if (targetHost != null && request.url.host != targetHost) {
            return chain.proceed(request)
        }

        return when (strategy) {
            RateLimitStrategy.WAIT -> interceptWithWait(chain)
            RateLimitStrategy.FAIL_FAST -> interceptWithFailFast(chain)
            RateLimitStrategy.RETRY_WITH_BACKOFF -> interceptWithRetry(chain)
        }
    }

    /**
     * Estrategia: Esperar si se excede el límite
     */
    private fun interceptWithWait(chain: Interceptor.Chain): Response {
        lock.withLock {
            cleanupOldRequests()

            while (requests.size >= maxRequests) {
                val oldestRequest = requests.first()
                val waitTime = calculateWaitTime(oldestRequest)

                if (waitTime > 0) {
                    Log.d(tag, "⏳ Rate limit alcanzado. Esperando ${waitTime}ms...")
                    Thread.sleep(waitTime)
                }

                cleanupOldRequests()
            }

            // Registrar nuevo request
            requests.add(System.currentTimeMillis())
        }

        return chain.proceed(chain.request())
    }

    /**
     * Estrategia: Fallar inmediatamente si se excede el límite
     */
    private fun interceptWithFailFast(chain: Interceptor.Chain): Response {
        val canProceed = lock.withLock {
            cleanupOldRequests()

            if (requests.size < maxRequests) {
                requests.add(System.currentTimeMillis())
                true
            } else {
                false
            }
        }

        if (!canProceed) {
            Log.w(tag, "❌ Rate limit excedido. Request rechazado.")
            throw RateLimitExceededException(
                "Rate limit excedido: $maxRequests requests en ${timeWindowMillis}ms"
            )
        }

        return chain.proceed(chain.request())
    }

    /**
     * Estrategia: Reintentar con backoff exponencial
     */
    private fun interceptWithRetry(
        chain: Interceptor.Chain,
        maxRetries: Int = 3
    ): Response {
        var attempt = 0
        var lastException: Exception? = null

        while (attempt < maxRetries) {
            try {
                val canProceed = lock.withLock {
                    cleanupOldRequests()

                    if (requests.size < maxRequests) {
                        requests.add(System.currentTimeMillis())
                        true
                    } else {
                        false
                    }
                }

                if (canProceed) {
                    return chain.proceed(chain.request())
                }

                // Calcular backoff exponencial
                val backoffMillis = calculateExponentialBackoff(attempt)
                Log.d(tag, "⏳ Intento ${attempt + 1}/$maxRetries. Esperando ${backoffMillis}ms...")
                Thread.sleep(backoffMillis)

            } catch (e: Exception) {
                lastException = e
                Log.e(tag, "Error en intento ${attempt + 1}: ${e.message}")
            }

            attempt++
        }

        // Si llegamos aquí, todos los reintentos fallaron
        throw RateLimitExceededException(
            "Rate limit excedido después de $maxRetries intentos",
            lastException
        )
    }

    /**
     * Limpia requests antiguos fuera de la ventana de tiempo
     */
    private fun cleanupOldRequests() {
        val now = System.currentTimeMillis()
        val windowStart = now - timeWindowMillis
        requests.removeAll { it < windowStart }
    }

    /**
     * Calcula tiempo de espera basado en el request más antiguo
     */
    private fun calculateWaitTime(oldestRequest: Long): Long {
        val now = System.currentTimeMillis()
        val timeElapsed = now - oldestRequest
        val remainingTime = timeWindowMillis - timeElapsed
        return maxOf(0L, remainingTime)
    }

    /**
     * Calcula backoff exponencial
     */
    private fun calculateExponentialBackoff(attempt: Int): Long {
        val baseDelay = 1000L // 1 segundo
        val maxDelay = 60000L // 60 segundos
        val delay = baseDelay * (1 shl attempt) // 2^attempt
        return minOf(delay, maxDelay)
    }

    /**
     * Obtiene información del estado actual
     */
    fun getCurrentState(): RateLimitState {
        return lock.withLock {
            cleanupOldRequests()
            RateLimitState(
                currentRequests = requests.size,
                maxRequests = maxRequests,
                remainingRequests = maxRequests - requests.size,
                timeWindowMillis = timeWindowMillis,
                nextAvailableSlot = if (requests.isNotEmpty()) requests.first() + timeWindowMillis else 0
            )
        }
    }

    /**
     * Reinicia el contador de requests
     */
    fun reset() {
        lock.withLock {
            requests.clear()
            Log.d(tag, "🔄 Rate limiter reiniciado")
        }
    }
}

// ==================== ENUMS & DATA CLASSES ====================

/**
 * Estrategias de rate limiting
 */
enum class RateLimitStrategy {
    /** Esperar hasta que haya un slot disponible */
    WAIT,

    /** Fallar inmediatamente si se excede el límite */
    FAIL_FAST,

    /** Reintentar con backoff exponencial */
    RETRY_WITH_BACKOFF
}

/**
 * Estado actual del rate limiter
 */
data class RateLimitState(
    val currentRequests: Int,
    val maxRequests: Int,
    val remainingRequests: Int,
    val timeWindowMillis: Long,
    val nextAvailableSlot: Long
) {
    fun isAtLimit(): Boolean = currentRequests >= maxRequests

    fun getWaitTimeMillis(): Long {
        if (!isAtLimit()) return 0
        val now = System.currentTimeMillis()
        return maxOf(0, nextAvailableSlot - now)
    }

    override fun toString(): String {
        return "RateLimitState(requests: $currentRequests/$maxRequests, " +
                "remaining: $remainingRequests, " +
                "wait: ${getWaitTimeMillis()}ms)"
    }
}

/**
 * Excepción lanzada cuando se excede el rate limit
 */
class RateLimitExceededException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

// ==================== BUILDERS & DSL ====================

/**
 * Builder para configuración avanzada
 */
class RateLimitInterceptorBuilder {
    private var maxRequests: Int = 10
    private var timeWindowMillis: Long = 60_000L
    private var strategy: RateLimitStrategy = RateLimitStrategy.WAIT
    private var targetHost: String? = null

    fun maxRequests(max: Int) = apply { this.maxRequests = max }

    fun timeWindow(time: Long, unit: TimeUnit) = apply {
        this.timeWindowMillis = unit.toMillis(time)
    }

    fun strategy(strategy: RateLimitStrategy) = apply { this.strategy = strategy }

    fun targetHost(host: String) = apply { this.targetHost = host }

    fun build() = RateLimitInterceptor(
        maxRequests = maxRequests,
        timeWindowMillis = timeWindowMillis,
        strategy = strategy,
        targetHost = targetHost
    )
}

/**
 * DSL para crear interceptor
 */
fun rateLimitInterceptor(block: RateLimitInterceptorBuilder.() -> Unit): RateLimitInterceptor {
    return RateLimitInterceptorBuilder().apply(block).build()
}

// ==================== PRESETS ====================

object RateLimitPresets {
    /**
     * Preset para Genius API (10 requests por 60 segundos)
     */
    fun geniusApi() = RateLimitInterceptor(
        maxRequests = 10,
        timeWindowMillis = 60_000L,
        strategy = RateLimitStrategy.WAIT,
        targetHost = "api.genius.com"
    )

    /**
     * Preset conservador (5 requests por minuto)
     */
    fun conservative() = RateLimitInterceptor(
        maxRequests = 5,
        timeWindowMillis = 60_000L,
        strategy = RateLimitStrategy.WAIT
    )

    /**
     * Preset agresivo (20 requests por minuto)
     */
    fun aggressive() = RateLimitInterceptor(
        maxRequests = 20,
        timeWindowMillis = 60_000L,
        strategy = RateLimitStrategy.WAIT
    )

    /**
     * Preset para scraping (1 request por 3 segundos)
     */
    fun scraping() = RateLimitInterceptor(
        maxRequests = 1,
        timeWindowMillis = 3_000L,
        strategy = RateLimitStrategy.WAIT
    )
}