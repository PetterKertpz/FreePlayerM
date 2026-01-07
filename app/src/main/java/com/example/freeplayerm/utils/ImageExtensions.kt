package com.example.freeplayerm.utils

import android.content.Context
import com.example.freeplayerm.R


fun Context.mediaStoreImageRequest(
   contentUri: String?,
   crossfadeDuration: Int = 300
): coil.request.ImageRequest {
   return coil.request.ImageRequest.Builder(this)
      .data(contentUri)
      .crossfade(crossfadeDuration)
      .memoryCacheKey(contentUri)
      .diskCacheKey(contentUri)
      // ✅ CRÍTICO: Permite que Coil use ContentResolver
      .allowHardware(true)
      .build()
}

// Extension para placeholders consistentes
fun coil.request.ImageRequest.Builder.withPlaceholder(
    context: Context,
    @androidx.annotation.DrawableRes placeholderId: Int = R.drawable.ic_notification
): coil.request.ImageRequest.Builder {
    return this
        .placeholder(placeholderId)
        .error(placeholderId)
        .fallback(placeholderId)
}