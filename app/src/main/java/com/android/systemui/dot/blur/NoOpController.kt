package com.android.systemui.dot.blur

import android.graphics.Canvas
import com.android.systemui.dot.blur.BlurViewFacade
import android.graphics.drawable.Drawable

//Used in edit mode and in case if no BlurController was set
internal class NoOpController : BlurController {
    override fun draw(canvas: Canvas): Boolean {
        return true
    }

    override fun updateBlurViewSize() {}
    override fun destroy() {}
    override fun setBlurRadius(radius: Float): BlurViewFacade {
        return this
    }

    override fun setBlurAlgorithm(algorithm: BlurAlgorithm): BlurViewFacade {
        return this
    }

    override fun setOverlayColor(overlayColor: Int): BlurViewFacade {
        return this
    }

    override fun setFrameClearDrawable(windowBackground: Drawable?): BlurViewFacade {
        return this
    }

    override fun setBlurEnabled(enabled: Boolean): BlurViewFacade {
        return this
    }

    override fun setBlurAutoUpdate(enabled: Boolean): BlurViewFacade {
        return this
    }

    override fun setHasFixedTransformationMatrix(hasFixedTransformationMatrix: Boolean): BlurViewFacade {
        return this
    }
}