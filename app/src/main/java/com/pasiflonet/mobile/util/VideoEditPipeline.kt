package com.pasiflonet.mobile.util

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Transformer
import java.io.File

@UnstableApi
object VideoEditPipeline {

    /**
     * Exports edited video to cache and returns output Uri via callback.
     * blurRects: normalized rects 0..1
     * watermark: optional watermark config (assets)
     */
    fun export(
        context: Context,
        inputUri: Uri,
        blurRects: List<BlurRectN>,
        watermark: WatermarkConfig?,
        onDone: (Result<Uri>) -> Unit
    ) {
        try {
            val outFile = File(context.cacheDir, "pasiflonet_export_${System.currentTimeMillis()}.mp4")
            if (outFile.exists()) outFile.delete()

            val mediaItem = MediaItem.fromUri(inputUri)

            // Effects:
            // 1) Region blur (custom effect placeholder)
            val videoEffects = mutableListOf<Any>()
            if (blurRects.isNotEmpty()) {
                videoEffects.add(RegionBlurEffect(blurRects))
            }

            // 2) Watermark overlay:
            // We'll wire a real overlay after build is green and we confirm exact Media3 overlay classes in your version.
            // For now, we keep the pipeline stable and compile.

            val edited = EditedMediaItem.Builder(mediaItem)
                // TODO: setEffects(...) once we confirm exact signature/classes for your Media3 version
                .build()

            val transformer = Transformer.Builder(context)
                .build()

            transformer.addListener(object : Transformer.Listener {
                override fun onCompleted(composition: androidx.media3.transformer.Composition, result: androidx.media3.transformer.ExportResult) {
                    onDone(Result.success(Uri.fromFile(outFile)))
                }
                override fun onError(composition: androidx.media3.transformer.Composition, result: androidx.media3.transformer.ExportResult, exception: Exception) {
                    onDone(Result.failure(exception))
                }
            })

            transformer.start(edited, outFile.absolutePath)
        } catch (t: Throwable) {
            onDone(Result.failure(t))
        }
    }
}
