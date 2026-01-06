package com.pasiflonet.mobile.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.pasiflonet.mobile.util.BlurRectN
import com.pasiflonet.mobile.util.VideoEditPipeline
import com.pasiflonet.mobile.util.WatermarkConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Clean SendWorker (Media3 pipeline).
 * NOTE: The actual "send via TDLib" is routed via a bridge.
 * If your repo has a sender class, we'll hook it next; for now this compiles + exports edited video.
 */
class SendWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "SendWorker"

        const val KEY_CHAT_ID = "chat_id"
        const val KEY_TEXT = "text"
        const val KEY_MEDIA_URI = "media_uri"                 // string Uri
        const val KEY_BLUR_RECTS_JSON = "blur_rects_json"     // json array of {l,t,r,b} normalized 0..1
        const val KEY_WATERMARK_ASSET = "watermark_asset"     // e.g. "watermark.png" in assets/
    }

    override suspend fun doWork(): Result {
        val chatId = inputData.getLong(KEY_CHAT_ID, Long.MIN_VALUE)
        if (chatId == Long.MIN_VALUE) {
            return Result.failure(Data.Builder().putString("error", "Missing chat_id").build())
        }

        val text = inputData.getString(KEY_TEXT) ?: ""
        val mediaUriStr = inputData.getString(KEY_MEDIA_URI)

        val blurRects = parseBlurRects(inputData.getString(KEY_BLUR_RECTS_JSON))
        val watermark = inputData.getString(KEY_WATERMARK_ASSET)?.takeIf { it.isNotBlank() }?.let {
            WatermarkConfig(assetPath = it)
        }

        // 1) If we have media -> export edited media (blur/watermark hook is in pipeline)
        val exportedUri: Uri? = if (!mediaUriStr.isNullOrBlank()) {
            val inUri = Uri.parse(mediaUriStr)
            exportMedia(inUri, blurRects, watermark)
        } else null

        // 2) Send step (bridge) - compile-safe.
        // Replace this with your TDLib sender call once CI is green.
        val sent = TdSendBridge.trySend(applicationContext, chatId, text, exportedUri)
        if (!sent) {
            Log.e(TAG, "Send bridge not implemented / failed. Build is OK; sending will be wired next.")
            return Result.failure(Data.Builder().putString("error", "Send not wired yet").build())
        }

        return Result.success()
    }

    @OptIn(UnstableApi::class)
    private suspend fun exportMedia(
        inputUri: Uri,
        blurRects: List<BlurRectN>,
        watermark: WatermarkConfig?
    ): Uri = suspendCancellableCoroutine { cont ->
        VideoEditPipeline.export(
            context = applicationContext,
            inputUri = inputUri,
            blurRects = blurRects,
            watermark = watermark
        ) { res ->
            if (cont.isActive) cont.resume(res.getOrThrow())
        }
    }

    private fun parseBlurRects(json: String?): List<BlurRectN> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            val out = ArrayList<BlurRectN>(arr.length())
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    BlurRectN(
                        l = o.optDouble("l", 0.0).toFloat(),
                        t = o.optDouble("t", 0.0).toFloat(),
                        r = o.optDouble("r", 0.0).toFloat(),
                        b = o.optDouble("b", 0.0).toFloat()
                    )
                )
            }
            out
        } catch (_: Throwable) {
            emptyList()
        }
    }
}

/**
 * Temporary bridge: compiles always.
 * Next step: wire to your TDLib sending singleton (Client/Repository) after CI turns green.
 */
object TdSendBridge {
    fun trySend(context: Context, chatId: Long, text: String, media: Uri?): Boolean {
        // TODO: Hook to your TDLib sender here (you likely have a TdRepository/TdLibManager in the project).
        // For now: return false so Worker reports failure (no silent "sent").
        return false
    }
}
