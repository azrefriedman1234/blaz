package com.pasiflonet.mobile.util

import android.widget.ImageView
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
}
