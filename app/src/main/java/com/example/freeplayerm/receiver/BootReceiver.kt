// en: app/src/main/java/com/example/freeplayerm/receiver/BootReceiver.kt
package com.example.freeplayerm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.freeplayerm.data.scanner.MusicScanWorker

/**
 * 📱 BOOT RECEIVER - Escaneo al Arrancar el Dispositivo
 *
 * Recibe el broadcast de arranque completado y programa un escaneo de música en segundo plano.
 *
 * Requiere permiso RECEIVE_BOOT_COMPLETED en AndroidManifest.xml
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "📱 Dispositivo arrancado - programando escaneo de música")

            try {
                // Programar escaneo con delay para no saturar el arranque
                MusicScanWorker.programarEscaneoPorArranque(context)

                // También asegurar que los escaneos periódicos estén activos
                MusicScanWorker.programarEscaneosPeriodicos(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error programando escaneo post-arranque", e)
            }
        }
    }
}
