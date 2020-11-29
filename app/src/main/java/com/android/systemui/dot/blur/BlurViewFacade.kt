package com.android.systemui.dot.blur

import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt

interface BlurViewFacade {
    /**
     * Enables/disables the blur. Enabled by default
     *
     * @param enabled true to enable, false otherwise
     * @return [BlurViewFacade]
     */
    fun setBlurEnabled(enabled: Boolean): BlurViewFacade?

    /**
     * Can be used to stop blur auto update or resume if it was stopped before.
     * Enabled by default.
     *
     * @return [BlurViewFacade]
     */
    fun setBlurAutoUpdate(enabled: Boolean): BlurViewFacade?

    /**
     * Can be set to true to optimize position calculation before blur.
     * By default, BlurView calculates its translation, rotation and scale before each draw call.
     * If you are not changing these properties (for example, during animation), this behavior can be changed
     * to calculate them only once during initialization.
     *
     * @param hasFixedTransformationMatrix indicates if this BlurView has fixed transformation Matrix.
     * @return [BlurViewFacade]
     */
    fun setHasFixedTransformationMatrix(hasFixedTransformationMatrix: Boolean): BlurViewFacade?

    /**
     * @param frameClearDrawable sets the drawable to draw before view hierarchy.
     * Can be used to draw Activity's window background if your root layout doesn't provide any background
     * Optional, by default frame is cleared with a transparent color.
     * @return [BlurViewFacade]
     */
    fun setFrameClearDrawable(frameClearDrawable: Drawable?): BlurViewFacade?

    /**
     * @param radius sets the blur radius
     * Default value is [BlurController.DEFAULT_BLUR_RADIUS]
     * @return [BlurViewFacade]
     */
    fun setBlurRadius(radius: Float): BlurViewFacade?

    /**
     * @param algorithm sets the blur algorithm
     * @return [BlurViewFacade]
     */
    fun setBlurAlgorithm(algorithm: BlurAlgorithm): BlurViewFacade

    /**
     * Sets the color overlay to be drawn on top of blurred content
     *
     * @param overlayColor int color
     * @return [BlurViewFacade]
     */
    fun setOverlayColor(@ColorInt overlayColor: Int): BlurViewFacade?
}