package com.example.freeplayerm.utils

import android.content.Context
import com.example.freeplayerm.R


fun Context.optimizedImageRequest(
    data: Any?,
    crossfadeDuration: Int = 300
): coil.request.ImageRequest {
    return coil.request.ImageRequest.Builder(this)
        .data(data)
        .crossfade(crossfadeDuration)
        .memoryCacheKey(data.toString())
        .diskCacheKey(data.toString())
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