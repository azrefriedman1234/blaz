package com.pasiflonet.mobile.td

import android.graphics.BitmapFactory
import androidx.media3.common.util.UnstableApi
import com.pasiflonet.mobile.td.TdLibManager
import org.drinkless.tdlib.TdApi
import java.io.File

@UnstableApi
object TgThumbLoader {

    fun requestThumbFile(fileId: Int, onReady: (String?) -> Unit) {
        // Ask TDLib to download thumbnail (small file)
        TdLibManager.send(TdApi.DownloadFile(fileId, 16, 0, 0, false)) { _ -> }
        TdLibManager.send(TdApi.GetFile(fileId)) { obj ->
            val f = obj as? TdApi.File ?: run { onReady(null); return@send }
            val path = f.local?.path
            if (!path.isNullOrBlank() && f.local.isDownloadingCompleted) {
                onReady(path)
            } else {
                onReady(null)
            }
        }
    }

    fun decodeBitmap(path: String): android.graphics.Bitmap? {
        return try {
            val f = File(path)
            if (!f.exists()) return null
            BitmapFactory.decodeFile(path)
        } catch (_: Throwable) {
            null
        }
    }
}
