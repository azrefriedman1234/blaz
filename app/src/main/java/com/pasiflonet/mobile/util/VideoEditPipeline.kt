package com.pasiflonet.mobile.util

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * TEMP COMPILATION FIX:
 * - Provides editVideoBlocking(...) used by DetailsActivity
 * - Does NOT perform real blur/watermark yet (copies input to output).
 * Later we can reintroduce Media3 Transformer / effects safely.
 */
object VideoEditPipeline {

    data class Result(val outFile: File)

    fun editVideoBlocking(
        ctx: Context,
        input: Uri,
        blurRects: List<FloatArray> = emptyList(),
        watermarkText: String? = null
    ): Result {
        // Copy input -> cache file (so build & app flow work)
        val outDir = File(ctx.cacheDir, "pasiflonet_tmp").apply { mkdirs() }
        val out = File(outDir, "edited_${System.currentTimeMillis()}.mp4")

        ctx.contentResolver.openInputStream(input).use { ins ->
            requireNotNull(ins) { "Cannot open input Uri" }
            out.outputStream().use { outs -> ins.copyTo(outs) }
        }

        return Result(out)
    }
}
