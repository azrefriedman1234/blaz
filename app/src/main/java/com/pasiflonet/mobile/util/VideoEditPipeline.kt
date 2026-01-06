package com.pasiflonet.mobile.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

/**
 * Build-safe fallback pipeline:
 * Copies input video to cache as output.
 * (No Media3 / FFmpeg needed for CI compile)
 */
object VideoEditPipeline {

    data class Result(val outFile: File)

    @JvmStatic
    fun editVideoBlocking(
        ctx: Context,
        input: Uri,
        blurRects: List<Any> = emptyList(),
        watermarkText: String? = null
    ): Result {
        val outDir = File(ctx.cacheDir, "pasiflonet_tmp").apply { mkdirs() }
        val outFile = File(outDir, "export_${System.currentTimeMillis()}.mp4")

        ctx.contentResolver.openInputStream(input).use { ins ->
            requireNotNull(ins) { "Cannot open input Uri" }
            FileOutputStream(outFile).use { outs ->
                ins.copyTo(outs)
            }
        }
        return Result(outFile)
    }
}
