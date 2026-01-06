package com.pasiflonet.mobile.util

import com.pasiflonet.mobile.td.TgThumbLoader
import android.widget.ImageView
import android.graphics.BitmapFactory
import coil.load

/**
 * Use this from your RecyclerView ViewHolder/Adapter:
 * ThumbBind.bindPreview(imageView, miniThumbBase64, thumbLocalPath)
 *
 * - Shows minithumbnail first (placeholder)
 * - If full thumbnail file exists locally, loads it sharply over the placeholder
 */
object ThumbBind {
    fun bindPreview(
        imageView: ImageView,
        miniThumbBase64: String?,
        thumbLocalPath: String?
    ) {
        // 1) placeholder from minithumbnail (if any)
        val mini = Thumbs.decodeMiniThumb(miniThumbBase64)
        if (mini != null) imageView.setImageBitmap(mini) else imageView.setImageDrawable(null)

        // 2) override with full local thumbnail if already downloaded
        val local = Thumbs.localThumbFile(thumbLocalPath)
        if (local != null) {
            imageView.load(local) {
                crossfade(true)
            }
        }
    }

    /**
     * Placeholder: minithumbnail (base64) -> sharp thumbnail when file is downloaded.
     * Works even if the full media isn't downloaded yet (downloads only the thumb fileId).
     */
    fun bindPreview(
        imageView: ImageView,
        miniThumbBase64: String?,
        thumbLocalPath: String?,
        thumbFileId: Int?
    ) {
        // 1) placeholder from minithumbnail
        val mini = Thumbs.decodeMiniThumb(miniThumbBase64)
        if (mini != null) imageView.setImageBitmap(mini) else imageView.setImageDrawable(null)

        // 2) if we already have a local thumbnail -> show it (sharp)
        val local = Thumbs.localThumbFile(thumbLocalPath)
        if (local != null) {
            val bmp = BitmapFactory.decodeFile(local.absolutePath)
            if (bmp != null) imageView.setImageBitmap(bmp)
            return
        }

        // 3) no local thumb yet -> request download by fileId (if exists)
        if (thumbFileId == null) return

        // prevent RecyclerView reuse bugs: tag the current requested fileId
        imageView.tag = thumbFileId

        TgThumbLoader.requestThumbFile(thumbFileId) { path ->
            if (path.isNullOrBlank()) return@requestThumbFile
            // apply only if still same fileId in this view
            if (imageView.tag != thumbFileId) return@requestThumbFile

            imageView.post {
                if (imageView.tag != thumbFileId) return@post
                val f = Thumbs.localThumbFile(path) ?: return@post
                val bmp = BitmapFactory.decodeFile(f.absolutePath)
                if (bmp != null) imageView.setImageBitmap(bmp)
            }
        }
    }

}
