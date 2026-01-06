package com.pasiflonet.mobile.util

import android.content.Context
import android.net.Uri
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import java.io.File

@UnstableApi
object VideoEditPipeline {

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

            // Build Effects list (keep stable/compilable)
            val videoEffects = mutableListOf<Effect>()

            // If your RegionBlurEffect implements Effect (or GlEffect which is an Effect), this will work:
            if (blurRects.isNotEmpty()) {
                videoEffects.add(RegionBlurEffect(blurRects) as Effect)
            }

            // Watermark: add later as Effect (once class is ready)
            // if (watermark != null) videoEffects.add(WatermarkEffect(watermark) as Effect)

            val effects = Effects(
                /* audioProcessors = */ ImmutableList.of(),
                /* videoEffects = */ ImmutableList.copyOf(videoEffects)
            )

            val edited = EditedMediaItem.Builder(mediaItem)
                .setEffects(effects)
                .build()

            val transformer = Transformer.Builder(context).build()

            transformer.addListener(object : Transformer.Listener {
                override fun onCompleted(
                    composition: androidx.media3.transformer.Composition,
                    result: androidx.media3.transformer.ExportResult
                ) {
                    onDone(Result.success(Uri.fromFile(outFile)))
                }

                override fun onError(
                    composition: androidx.media3.transformer.Composition,
                    result: androidx.media3.transformer.ExportResult,
                    exception: Exception
                ) {
                    onDone(Result.failure(exception))
                }
            })

            transformer.start(edited, outFile.absolutePath)
        } catch (t: Throwable) {
            onDone(Result.failure(t))
        }
    }
}
