package com.example.freeplayerm.data.repository

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Copia una imagen desde una URI de contenido a un archivo privado de la app.
     * @return La URI del nuevo archivo copiado, o null si falla.
     */
    suspend fun copyImageToInternalStorage(uri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                // Obtenemos el stream de datos de la imagen original
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null

                // Creamos un archivo de destino en el directorio de archivos privados de la app
                val coversDir = File(context.filesDir, "covers")
                if (!coversDir.exists()) {
                    coversDir.mkdirs()
                }
                val outputFile = File(coversDir, "cover_${System.currentTimeMillis()}.jpg")

                // Copiamos los datos del stream al archivo de salida
                val outputStream = FileOutputStream(outputFile)
                inputStream.copyTo(outputStream)

                // Cerramos los streams
                inputStream.close()
                outputStream.close()

                // Devolvemos la URI del nuevo archivo creado
                Uri.fromFile(outputFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}