package com.example.freeplayerm.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.freeplayerm.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanNotificationHelper @Inject constructor(
   @ApplicationContext private val context: Context
) {
   
   private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
   
   companion object {
      private const val CHANNEL_ID = "music_scan_channel"
      private const val CHANNEL_NAME = "Escaneo de Música"
      private const val CHANNEL_DESCRIPTION = "Notificaciones del progreso de escaneo de música"
      const val SCAN_NOTIFICATION_ID = 1001
   }
   
   init {
      crearCanalDeNotificacion()
   }
   
   private fun crearCanalDeNotificacion() {
      val channel = NotificationChannel(
         CHANNEL_ID,
         CHANNEL_NAME,
         NotificationManager.IMPORTANCE_LOW
      ).apply {
         description = CHANNEL_DESCRIPTION
         setShowBadge(false)
         enableLights(false)
         enableVibration(false)
      }
      notificationManager.createNotificationChannel(channel)
   }
   
   fun mostrarNotificacionInicio() {
      if (!PermissionHelper.hasNotificationPermission(context)) return
      
      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
         .setContentTitle("Escaneando música")
         .setContentText("Preparando escaneo...")
         .setSmallIcon(R.drawable.ic_music_note)
         .setProgress(100, 0, true)
         .setOngoing(true)
         .setPriority(NotificationCompat.PRIORITY_LOW)
         .setCategory(NotificationCompat.CATEGORY_PROGRESS)
         .build()
      
      notificationManager.notify(SCAN_NOTIFICATION_ID, notification)
   }
   
   fun actualizarProgreso(progreso: Int, total: Int, mensaje: String, fase: String) {
      if (!PermissionHelper.hasNotificationPermission(context)) return
      
      val porcentaje = if (total > 0) (progreso * 100) / total else 0
      
      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
         .setContentTitle("Escaneando música")
         .setContentText("$fase: $mensaje")
         .setSmallIcon(R.drawable.ic_music_note)
         .setProgress(100, porcentaje, false)
         .setOngoing(true)
         .setPriority(NotificationCompat.PRIORITY_LOW)
         .setCategory(NotificationCompat.CATEGORY_PROGRESS)
         .setSubText("$porcentaje%")
         .build()
      
      notificationManager.notify(SCAN_NOTIFICATION_ID, notification)
   }
   
   fun mostrarNotificacionCompletada(nuevas: Int, eliminadas: Int, actualizadas: Int, tiempoMs: Long) {
      if (!PermissionHelper.hasNotificationPermission(context)) return
      
      val tiempoSegundos = tiempoMs / 1000
      val mensaje = buildString {
         if (nuevas > 0) append("$nuevas nuevas")
         if (eliminadas > 0) {
            if (isNotEmpty()) append(", ")
            append("$eliminadas eliminadas")
         }
         if (actualizadas > 0) {
            if (isNotEmpty()) append(", ")
            append("$actualizadas actualizadas")
         }
         if (isEmpty()) append("Sin cambios")
      }
      
      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
         .setContentTitle("Escaneo completado")
         .setContentText(mensaje)
         .setSmallIcon(R.drawable.ic_check_circle)
         .setAutoCancel(true)
         .setPriority(NotificationCompat.PRIORITY_DEFAULT)
         .setSubText("${tiempoSegundos}s")
         .build()
      
      notificationManager.notify(SCAN_NOTIFICATION_ID, notification)
   }
   
   fun mostrarNotificacionError(mensaje: String) {
      if (!PermissionHelper.hasNotificationPermission(context)) return
      
      val notification = NotificationCompat.Builder(context, CHANNEL_ID)
         .setContentTitle("Error en escaneo")
         .setContentText(mensaje)
         .setSmallIcon(R.drawable.ic_error)
         .setAutoCancel(true)
         .setPriority(NotificationCompat.PRIORITY_DEFAULT)
         .setCategory(NotificationCompat.CATEGORY_ERROR)
         .build()
      
      notificationManager.notify(SCAN_NOTIFICATION_ID, notification)
   }
   
   fun cancelarNotificacion() {
      notificationManager.cancel(SCAN_NOTIFICATION_ID)
   }
}