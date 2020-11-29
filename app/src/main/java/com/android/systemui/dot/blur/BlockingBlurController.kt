package com.android.systemui.dot.blur

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.ColorInt

/**
 * Blur Controller that handles all blur logic for the attached View.
 * It honors View size changes, View animation and Visibility changes.
 *
 *
 * The basic idea is to draw the view hierarchy on a bitmap, excluding the attached View,
 * then blur and draw it on the system Canvas.
 *
 *
 * It uses [ViewTreeObserver.OnPreDrawListener] to detect when
 * blur should be updated.
 *
 *
 * Blur is done on the main thread.
 */
internal class BlockingBlurController(
    val blurView: View,
    private val rootView: ViewGroup,
    @param:ColorInt private var overlayColor: Int
) : BlurController {
    private var blurRadius = BlurController.DEFAULT_BLUR_RADIUS
    private var blurAlgorithm: BlurAlgorithm
    private var internalCanvas: Canvas? = null
    private var internalBitmap: Bitmap? = null
    private val rootLocation = IntArray(2)
    private val blurViewLocation = IntArray(2)
    private val sizeScaler = SizeScaler(BlurController.DEFAULT_SCALE_FACTOR)
    private var scaleFactor = 1f
    private val drawListener =
        ViewTreeObserver.OnPreDrawListener { // Not invalidating a View here, just updating the Bitmap.
            // This relies on the HW accelerated bitmap drawing behavior in Android
            // If the bitmap was drawn on HW accelerated canvas, it holds a reference to it and on next
            // drawing pass the updated content of the bitmap will be rendered on the screen
            updateBlur()
            true
        }
    private var blurEnabled = true
    private var initialized = false
    private var frameClearDrawable: Drawable? = null
    private var hasFixedTransformationMatrix = false
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)
    fun init(measuredWidth: Int, measuredHeight: Int) {
        if (sizeScaler.isZeroSized(measuredWidth, measuredHeight)) {
            // Will be initialized later when the View reports a size change
            blurView.setWillNotDraw(true)
            return
        }
        blurView.setWillNotDraw(false)
        allocateBitmap(measuredWidth, measuredHeight)
        internalCanvas = Canvas(internalBitmap)
        initialized = true
        if (hasFixedTransformationMatrix) {
            setupInternalCanvasMatrix()
        }
    }

    fun updateBlur() {
        if (!blurEnabled || !initialized) {
            return
        }
        if (frameClearDrawable == null) {
            internalBitmap!!.eraseColor(Color.TRANSPARENT)
        } else {
            frameClearDrawable!!.draw(internalCanvas)
        }
        if (hasFixedTransformationMatrix) {
            rootView.draw(internalCanvas)
        } else {
            internalCanvas!!.save()
            setupInternalCanvasMatrix()
            rootView.draw(internalCanvas)
            internalCanvas!!.restore()
        }
        blurAndSave()
    }

    private fun allocateBitmap(measuredWidth: Int, measuredHeight: Int) {
        val bitmapSize = sizeScaler.scale(measuredWidth, measuredHeight)
        scaleFactor = bitmapSize.scaleFactor
        internalBitmap = Bitmap.createBitmap(
            bitmapSize.width,
            bitmapSize.height,
            blurAlgorithm.supportedBitmapConfig
        )
    }

    /**
     * Set up matrix to draw starting from blurView's position
     */
    private fun setupInternalCanvasMatrix() {
        rootView.getLocationOnScreen(rootLocation)
        blurView.getLocationOnScreen(blurViewLocation)
        val left = blurViewLocation[0] - rootLocation[0]
        val top = blurViewLocation[1] - rootLocation[1]
        val scaledLeftPosition = -left / scaleFactor
        val scaledTopPosition = -top / scaleFactor
        internalCanvas!!.translate(scaledLeftPosition, scaledTopPosition)
        internalCanvas!!.scale(1 / scaleFactor, 1 / scaleFactor)
    }

    override fun draw(canvas: Canvas): Boolean {
        if (!blurEnabled || !initialized) {
            return true
        }
        // Not blurring own children
        if (canvas === internalCanvas) {
            return false
        }
        updateBlur()
        canvas.save()
        canvas.scale(scaleFactor, scaleFactor)
        canvas.drawBitmap(internalBitmap, 0f, 0f, paint)
        canvas.restore()
        if (overlayColor != TRANSPARENT) {
            canvas.drawColor(overlayColor)
        }
        return true
    }

    private fun blurAndSave() {
        internalBitmap = blurAlgorithm.blur(internalBitmap, blurRadius)
        if (!blurAlgorithm.canModifyBitmap()) {
            internalCanvas!!.setBitmap(internalBitmap)
        }
    }

    override fun updateBlurViewSize() {
        val measuredWidth = blurView.measuredWidth
        val measuredHeight = blurView.measuredHeight
        init(measuredWidth, measuredHeight)
    }

    override fun destroy() {
        setBlurAutoUpdate(false)
        blurAlgorithm.destroy()
        initialized = false
    }

    override fun setBlurRadius(radius: Float): BlurViewFacade {
        blurRadius = radius
        return this
    }

    override fun setBlurAlgorithm(algorithm: BlurAlgorithm): BlurViewFacade {
        blurAlgorithm = algorithm
        return this
    }

    override fun setFrameClearDrawable(frameClearDrawable: Drawable?): BlurViewFacade {
        this.frameClearDrawable = frameClearDrawable
        return this
    }

    override fun setBlurEnabled(enabled: Boolean): BlurViewFacade {
        blurEnabled = enabled
        setBlurAutoUpdate(enabled)
        blurView.invalidate()
        return this
    }

    override fun setBlurAutoUpdate(enabled: Boolean): BlurViewFacade {
        blurView.viewTreeObserver.removeOnPreDrawListener(drawListener)
        if (enabled) {
            blurView.viewTreeObserver.addOnPreDrawListener(drawListener)
        }
        return this
    }

    override fun setHasFixedTransformationMatrix(hasFixedTransformationMatrix: Boolean): BlurViewFacade {
        this.hasFixedTransformationMatrix = hasFixedTransformationMatrix
        return this
    }

    override fun setOverlayColor(overlayColor: Int): BlurViewFacade {
        if (this.overlayColor != overlayColor) {
            this.overlayColor = overlayColor
            blurView.invalidate()
        }
        return this
    }

    companion object {
        @ColorInt
        val TRANSPARENT = 0
    }

    /**
     * @param blurView View which will draw it's blurred underlying content
     * @param rootView Root View where blurView's underlying content starts drawing.
     * Can be Activity's root content layout (android.R.id.content)
     * or some of your custom root layouts.
     */
    init {
        blurAlgorithm = NoOpBlurAlgorithm()
        val measuredWidth = blurView.measuredWidth
        val measuredHeight = blurView.measuredHeight
        init(measuredWidth, measuredHeight)
    }
}