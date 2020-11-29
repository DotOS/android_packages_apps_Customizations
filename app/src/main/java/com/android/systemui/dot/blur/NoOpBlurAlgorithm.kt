package com.android.systemui.dot.blur

import android.graphics.Bitmap

internal class NoOpBlurAlgorithm : BlurAlgorithm {
    override fun blur(bitmap: Bitmap?, blurRadius: Float): Bitmap? {
        return bitmap
    }

    override fun destroy() {}
    override fun canModifyBitmap(): Boolean {
        return true
    }

    override val supportedBitmapConfig: Bitmap.Config
        get() = Bitmap.Config.ARGB_8888
}