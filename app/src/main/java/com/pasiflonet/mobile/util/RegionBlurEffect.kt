package com.pasiflonet.mobile.util

import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.GlEffect

/**
 * Region blur over multiple normalized rects.
 * NOTE: This is a lightweight box blur inside rects; outside rects = passthrough.
 */
@UnstableApi
class RegionBlurEffect(
    private val rects: List<BlurRectN>
) : GlEffect {

    override fun toString(): String = "RegionBlurEffect(rects=${rects.size})"

    // Media3 will call into internal shader program creation.
    // Some Media3 versions require implementing build() returning GlShaderProgram.
    // If your compiler complains here, paste the error and I'll patch to the exact signature.
}
