package com.example.freeplayerm.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object PermissionHelper {
   
   fun getRequiredPermissions(): Array<String> {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
         arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
      } else {
         arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
      }
   }
   
   fun hasStoragePermissions(context: Context): Boolean {
      return getRequiredPermissions().all { permission ->
         ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
      }
   }
   
   fun hasNotificationPermission(context: Context): Boolean {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
         ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
         ) == PackageManager.PERMISSION_GRANTED
      } else {
         true
      }
   }
   
   fun getAllRequiredPermissions(): Array<String> {
      val permissions = mutableListOf<String>()
      permissions.addAll(getRequiredPermissions())
      
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
         permissions.add(Manifest.permission.POST_NOTIFICATIONS)
      }
      
      return permissions.toTypedArray()
   }
}