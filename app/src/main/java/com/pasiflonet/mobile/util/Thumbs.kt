package com.pasiflonet.mobile.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.File

object Thumbs {

    /**
     * TDLib minithumbnail is base64 of raw image bytes.
     * Returns Bitmap or null if decode fails.
     */
    fun decodeMiniThumb(base64Data: String?): Bitmap? {
        if (base64Data.isNullOrBlank()) return null
        return try {
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Throwable) {
            null
        }
    }

    /**
     * Prefer full thumbnail if already downloaded (local path exists), else null.
     */
    fun localThumbFile(path: String?): File? {
        if (path.isNullOrBlank()) return null
        val f = File(path)
        return if (f.exists() && f.length() > 0) f else null
    }
}
