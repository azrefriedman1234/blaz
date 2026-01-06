package com.pasiflonet.mobile.util

data class BlurRectN(
    val l: Float, // 0..1
    val t: Float, // 0..1
    val r: Float, // 0..1
    val b: Float, // 0..1
    val radiusPx: Float = 18f
)

data class WatermarkConfig(
    val assetPath: String, // e.g. "watermark.png" in assets/
    val alpha: Float = 0.85f,
    val scale: Float = 0.22f, // relative
    val anchorX: Float = 0.95f, // 0..1 (right)
    val anchorY: Float = 0.95f  // 0..1 (bottom)
)
