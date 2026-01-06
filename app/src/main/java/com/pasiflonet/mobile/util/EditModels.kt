package com.pasiflonet.mobile.util

import android.net.Uri

/** Normalized rect 0..1 (relative to video width/height). */
data class RectN(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun clamp(): RectN = RectN(
        left.coerceIn(0f, 1f),
        top.coerceIn(0f, 1f),
        right.coerceIn(0f, 1f),
        bottom.coerceIn(0f, 1f),
    )
}

/** Back-compat name used around the project. */
typealias BlurRectN = RectN

enum class Kind { VIDEO, PHOTO, AUDIO, DOCUMENT, UNKNOWN }

data class WatermarkConfig(
    val assetPath: String = "watermark.png", // assets/
    val alpha: Float = 0.85f,
    val scale: Float = 0.22f, // relative to min(width,height)
    val marginN: Float = 0.03f,
    val position: Position = Position.BOTTOM_RIGHT
) {
    enum class Position { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT }
}

data class MediaInfo(
    val kind: Kind,
    val srcUri: Uri? = null,
    val mime: String? = null,
    val thumbB64: String? = null
)
