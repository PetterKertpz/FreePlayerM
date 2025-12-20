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
    suspend fun copyImageToInternalStorage(uri: Uri): Uri? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null

                val coversDir = File(context.filesDir, "covers")
                if (!coversDir.exists()) {
                    coversDir.mkdirs()
                }
                val outputFile = File(coversDir, "cover_${System.currentTimeMillis()}.jpg")

                val outputStream = FileOutputStream(outputFile)
                inputStream.copyTo(outputStream)

                inputStream.close()
                outputStream.close()

                Uri.fromFile(outputFile)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}