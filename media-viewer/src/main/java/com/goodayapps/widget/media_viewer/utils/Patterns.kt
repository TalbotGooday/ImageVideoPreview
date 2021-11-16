package com.goodayapps.widget.media_viewer.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import okio.FileNotFoundException
import java.util.Locale

internal const val WEB_VIDEO_PATTERN =
    "((?:https?|gs):\\/\\/.*\\.(?:mp4|webm))"

fun checkIsVideo(context: Context, url: String?, uri: Uri?): Boolean? {
    val isWebVideo = (url != null && WEB_VIDEO_PATTERN.toRegex().matches(url)) ||
        (uri?.toString() != null && WEB_VIDEO_PATTERN.toRegex().matches(uri.toString()))

    if (isWebVideo) return true
    if (uri == null) return false

    val mimeType = mimeType(context, uri)
    return when {
        "(image/\\w+)".toRegex().matches(mimeType.orEmpty()) -> false
        "(video/\\w+)".toRegex().matches(mimeType.orEmpty()) -> true
        else -> null
    }
}

fun mimeType(ctx: Context, uri: Uri): String? {
    val contentResolver = ctx.contentResolver

    return if (uri.scheme.equals(ContentResolver.SCHEME_CONTENT)) {
        // get (image/jpeg, video/mp4) from ContentResolver if uri scheme is "content://"
        contentResolver.getType(uri)?.toMediaTypeOrNull()
    } else {
        // get (.jpeg, .mp4) from uri "file://example/example.mp4"
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        // turn ".mp4" into "video/mp4"
        MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(fileExtension.lowercase(Locale.US))
            ?.toMediaTypeOrNull()
    }
}

private fun String?.toMediaTypeOrNull(): String? {
    if (this == null) return null

    if ("(\\w+/\\w+)".toRegex().matches(this)) return this

    val videoMap = mapOf(
        "mp4" to "video",
        "mpeg" to "video"
    )

    val imageMap = mapOf(
        "png" to "image",
        "jpg" to "image",
        "jpeg" to "image"
    )

    return videoMap[this] ?: imageMap[this]
}

fun Uri.length(ctx: Context)
    : Long {
    val contentResolver = ctx.contentResolver

    val assetFileDescriptor = try {
        contentResolver.openAssetFileDescriptor(this, "r")
    } catch (e: FileNotFoundException) {
        null
    }
    // uses ParcelFileDescriptor#getStatSize underneath if failed
    val length = assetFileDescriptor?.use { it.length } ?: -1L
    if (length != -1L) {
        return length
    }

    // if "content://" uri scheme, try contentResolver table
    if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
        return contentResolver.query(this, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                // maybe shouldn't trust ContentResolver for size: https://stackoverflow.com/questions/48302972/content-resolver-returns-wrong-size
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex == -1) {
                    return@use -1L
                }
                cursor.moveToFirst()
                return try {
                    cursor.getLong(sizeIndex)
                } catch (_: Throwable) {
                    -1L
                }
            } ?: -1L
    } else {
        return -1L
    }
}
