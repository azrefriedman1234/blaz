package com.pasiflonet.mobile.util

import android.net.Uri

// Normalized rect (0..1) for region blur
data class BlurRectN(
    val l: Float,
    val t: Float,
    val r: Float,
    val b: Float
)

enum class WatermarkGravity { TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER }

data class WatermarkConfig(
    val assetPath: String,          // e.g. "watermark.png" inside assets/
    val gravity: WatermarkGravity = WatermarkGravity.BOTTOM_RIGHT,
    val marginDp: Float = 12f,
    val scale: Float = 0.18f,        // relative size (approx)
    val alpha: Float = 0.95f
)
