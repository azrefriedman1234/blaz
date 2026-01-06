package com.pasiflonet.mobile.util

import android.content.Context
import androidx.media3.effect.GlShaderProgram
import androidx.media3.effect.RgbFilter


/**
 * Placeholder data-holder. Real Media3 GL blur effect will be added later.
 */
data class RegionBlurEffect(val rects: List<BlurRectN>)


    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram {
        // Delegate to built-in filter (acts as safe placeholder).
        return RgbFilter.createGrayscaleFilter().toGlShaderProgram(context, useHdr)
    }

