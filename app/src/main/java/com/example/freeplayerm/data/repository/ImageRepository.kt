// app/src/main/java/com/example/freeplayerm/data/repository/ImageRepository.kt
package com.example.freeplayerm.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.freeplayerm.di.ImageClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 🖼️ IMAGE REPOSITORY (UNIFIED)
 *
 * Repositorio unificado para manejo de imágenes
 * Soporta tanto descarga desde URLs como copia desde URIs locales
 *
 * Características:
 * - ✅ Descarga desde URLs remotas (covers de Genius, etc.)
 * - ✅ Copia desde URIs locales (MediaStore, galería, etc.)
 * - ✅ Gestión de directorios (cache vs files)
 * - ✅ Limpieza automática de cache antiguo
 * - ✅ Validación de tipos de archivo
 *
 * Estructura de almacenamiento:
 * - `cacheDir/image_cache/` → Imágenes temporales descargadas (covers, artistas)
 * - `filesDir/covers/` → Imágenes permanentes del usuario
 */
@Singleton
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    @ImageClient private val okHttpClient: OkHttpClient
) {
    private val tag = "ImageRepository"

    companion object {
        // Directorios
        private const val CACHE_IMAGES_DIR = "image_cache"
        private const val USER_COVERS_DIR = "covers"

        // Límites
        private const val MAX_CACHE_AGE_DAYS = 30
        private const val MAX_CACHE_SIZE_MB = 50L

        // Tipos de archivo permitidos
        private val ALLOWED_EXTENSIONS = listOf("jpg", "jpeg", "png", "webp")
    }

    /**
     * Tipo de almacenamiento para la imagen
     */
    enum class StorageType {
        /** Cache temporal (se puede borrar automáticamente) */
        CACHE,

        /** Almacenamiento permanente del usuario */
        PERMANENT
    }

    // ==================== API PÚBLICA ====================

    /**
     * Descarga una imagen desde URL y la guarda localmente
     *
     * @param url URL de la imagen a descargar
     * @param filename Nombre del archivo (sin extensión)
     * @param storageType Tipo de almacenamiento (cache o permanente)
     * @return Path absoluto del archivo guardado o null si falla
     */
    suspend fun downloadImage(
        url: String,
        filename: String,
        storageType: StorageType = StorageType.CACHE
    ): String? {
        return withContext(Dispatchers.IO) {
            var response: okhttp3.Response? = null
            try {
                Log.d(tag, "📥 Descargando imagen: $url")

                val request = Request.Builder()
                    .url(url)
                    .build()

                response = okHttpClient.newCall(request).execute()

                if (!response.isSuccessful) {
                    Log.w(tag, "❌ Error al descargar imagen: ${response.code}")
                    return@withContext null
                }

                val targetDir = getDirectory(storageType)
                val extension = detectImageExtension(url, response.headers["Content-Type"])
                val file = File(targetDir, "$filename.$extension")

                response.body.byteStream().use { inputStream ->
                    FileOutputStream(file).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                Log.d(tag, "✅ Imagen guardada: ${file.absolutePath}")
                file.absolutePath

            } catch (e: IOException) {
                Log.e(tag, "❌ Error de IO al descargar imagen: ${e.message}")
                null
            } catch (e: Exception) {
                Log.e(tag, "❌ Error inesperado: ${e.message}", e)
                null
            } finally {
                response?.close()
            }
        }
    }

    /**
     * Copia una imagen desde URI local al almacenamiento interno
     *
     * @param uri URI de la imagen (Content URI, File URI, etc.)
     * @param storageType Tipo de almacenamiento
     * @return URI del archivo copiado o null si falla
     */
    suspend fun copyImageFromUri(
        uri: Uri,
        storageType: StorageType = StorageType.PERMANENT
    ): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(tag, "📋 Copiando imagen desde URI: $uri")

                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    Log.w(tag, "❌ No se pudo abrir InputStream para URI: $uri")
                    return@withContext null
                }

                val targetDir = getDirectory(storageType)
                val extension = getExtensionFromUri(uri) ?: "jpg"
                val filename = "cover_${System.currentTimeMillis()}"
                val outputFile = File(targetDir, "$filename.$extension")

                inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        input.copyTo(output)
                    }
                }

                Log.d(tag, "✅ Imagen copiada: ${outputFile.absolutePath}")
                Uri.fromFile(outputFile)

            } catch (e: Exception) {
                Log.e(tag, "❌ Error copiando imagen: ${e.message}", e)
                null
            }
        }
    }

    /**
     * Elimina una imagen por su path
     *
     * @param imagePath Path absoluto de la imagen
     * @return true si se eliminó exitosamente
     */
    suspend fun deleteImage(imagePath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    val deleted = file.delete()
                    if (deleted) {
                        Log.d(tag, "🗑️ Imagen eliminada: $imagePath")
                    }
                    deleted
                } else {
                    Log.w(tag, "⚠️ Imagen no existe: $imagePath")
                    false
                }
            } catch (e: Exception) {
                Log.e(tag, "❌ Error eliminando imagen: ${e.message}", e)
                false
            }
        }
    }

    /**
     * Limpia el cache de imágenes antiguas
     *
     * @param maxAgeDays Edad máxima en días (default: 30)
     * @return Cantidad de archivos eliminados
     */
    suspend fun cleanOldCache(maxAgeDays: Int = MAX_CACHE_AGE_DAYS): Int {
        return withContext(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, CACHE_IMAGES_DIR)
                if (!cacheDir.exists()) return@withContext 0

                val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
                var deletedCount = 0

                cacheDir.listFiles()?.forEach { file ->
                    if (file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            deletedCount++
                        }
                    }
                }

                Log.d(tag, "🧹 Cache limpiado: $deletedCount archivos eliminados")
                deletedCount

            } catch (e: Exception) {
                Log.e(tag, "❌ Error limpiando cache: ${e.message}", e)
                0
            }
        }
    }

    /**
     * Obtiene el tamaño total del cache en MB
     */
    suspend fun getCacheSize(): Long {
        return withContext(Dispatchers.IO) {
            try {
                val cacheDir = File(context.cacheDir, CACHE_IMAGES_DIR)
                if (!cacheDir.exists()) return@withContext 0L

                val sizeBytes = cacheDir.walkTopDown()
                    .filter { it.isFile }
                    .map { it.length() }
                    .sum()

                sizeBytes / (1024 * 1024) // Convertir a MB

            } catch (e: Exception) {
                Log.e(tag, "❌ Error calculando tamaño de cache: ${e.message}", e)
                0L
            }
        }
    }

    /**
     * Verifica si una imagen existe
     */
    fun imageExists(imagePath: String): Boolean {
        return File(imagePath).exists()
    }

    // ==================== MÉTODOS PRIVADOS ====================

    /**
     * Obtiene el directorio según el tipo de almacenamiento
     */
    private fun getDirectory(storageType: StorageType): File {
        val dir = when (storageType) {
            StorageType.CACHE -> File(context.cacheDir, CACHE_IMAGES_DIR)
            StorageType.PERMANENT -> File(context.filesDir, USER_COVERS_DIR)
        }

        if (!dir.exists()) {
            dir.mkdirs()
        }

        return dir
    }

    /**
     * Detecta la extensión de la imagen desde URL o Content-Type
     */
    private fun detectImageExtension(url: String, contentType: String?): String {
        // Intentar desde Content-Type primero
        contentType?.let {
            when {
                it.contains("jpeg") || it.contains("jpg") -> return "jpg"
                it.contains("png") -> return "png"
                it.contains("webp") -> return "webp"
            }
        }

        // Intentar desde URL
        val extension = url.substringAfterLast('.', "")
            .substringBefore('?') // Remover query params
            .lowercase()

        return if (extension in ALLOWED_EXTENSIONS) extension else "jpg"
    }

    /**
     * Obtiene la extensión desde un URI
     */
    private fun getExtensionFromUri(uri: Uri): String? {
        return try {
            val mimeType = context.contentResolver.getType(uri)
            when {
                mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> "jpg"
                mimeType?.contains("png") == true -> "png"
                mimeType?.contains("webp") == true -> "webp"
                else -> {
                    // Intentar desde el path del URI
                    uri.path?.substringAfterLast('.')?.lowercase()
                        ?.takeIf { it in ALLOWED_EXTENSIONS }
                }
            }
        } catch (e: Exception) {
            Log.w(tag, "⚠️ No se pudo detectar extensión de URI: ${e.message}")
            null
        }
    }
}