/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.dotextras.custom.sections.clock

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.os.AsyncTask
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Interface representing an image asset.
 */
abstract class Asset {
    /**
     * Decodes a bitmap sized for the destination view's dimensions off the main UI thread.
     *
     * @param targetWidth  Width of target view in physical pixels.
     * @param targetHeight Height of target view in physical pixels.
     * @param receiver     Called with the decoded bitmap or null if there was an error decoding the
     * bitmap.
     */
    abstract fun decodeBitmap(targetWidth: Int, targetHeight: Int, receiver: BitmapReceiver?)

    /**
     * Decodes and downscales a bitmap region off the main UI thread.
     *
     * @param rect         Rect representing the crop region in terms of the original image's
     * resolution.
     * @param targetWidth  Width of target view in physical pixels.
     * @param targetHeight Height of target view in physical pixels.
     * @param receiver     Called with the decoded bitmap region or null if there was an error
     * decoding the bitmap region.
     */
    abstract fun decodeBitmapRegion(
        rect: Rect?, targetWidth: Int, targetHeight: Int,
        receiver: BitmapReceiver?
    )

    /**
     * Calculates the raw dimensions of the asset at its original resolution off the main UI thread.
     * Avoids decoding the entire bitmap if possible to conserve memory.
     *
     * @param activity Activity in which this decoding request is made. Allows for early termination
     * of fetching image data and/or decoding to a bitmap. May be null, in which
     * case the request is made in the application context instead.
     * @param receiver Called with the decoded raw dimensions of the whole image or null if there
     * was an error decoding the dimensions.
     */
    abstract fun decodeRawDimensions(
        activity: Activity?,
        receiver: DimensionsReceiver?
    )

    /**
     * Returns whether this asset has access to a separate, lower fidelity source of image data
     * (that may be able to be loaded more quickly to simulate progressive loading).
     */
    fun hasLowResDataSource(): Boolean {
        return false
    }

    /**
     * Loads the asset from the separate low resolution data source (if there is one) into the
     * provided ImageView with the placeholder color and bitmap transformation.
     *
     * @param transformation Bitmap transformation that can transform the thumbnail image
     * post-decoding.
     */
    fun loadLowResDrawable(
        activity: Activity?, imageView: ImageView?, placeholderColor: Int,
        transformation: BitmapTransformation?
    ) {
        // No op
    }

    /**
     * Returns whether the asset supports rendering tile regions at varying pixel densities.
     */
    abstract fun supportsTiling(): Boolean

    /**
     * Loads a Drawable for this asset into the provided ImageView. While waiting for the image to
     * load, first loads a ColorDrawable based on the provided placeholder color.
     *
     * @param context          Activity hosting the ImageView.
     * @param imageView        ImageView which is the target view of this asset.
     * @param placeholderColor Color of placeholder set to ImageView while waiting for image to
     * load.
     */
    fun loadDrawable(
        context: Context, imageView: ImageView,
        placeholderColor: Int
    ) {
        // Transition from a placeholder ColorDrawable to the decoded bitmap when the ImageView in
        // question is empty.
        val needsTransition = imageView.drawable == null
        val placeholderDrawable: Drawable = ColorDrawable(placeholderColor)
        if (needsTransition) {
            imageView.setImageDrawable(placeholderDrawable)
        }

        // Set requested height and width to the either the actual height and width of the view in
        // pixels, or if it hasn't been laid out yet, then to the absolute value of the layout
        // params.
        val width =
            if (imageView.width > 0) imageView.width else abs(imageView.layoutParams.width)
        val height =
            if (imageView.height > 0) imageView.height else abs(imageView.layoutParams.height)
        decodeBitmap(width, height, object : BitmapReceiver {
            override fun onBitmapDecoded(bitmap: Bitmap?) {
                if (!needsTransition) {
                    imageView.setImageBitmap(bitmap)
                    return
                }
                val resources = context.resources
                val layers = arrayOfNulls<Drawable>(2)
                layers[0] = placeholderDrawable
                layers[1] = BitmapDrawable(resources, bitmap)
                val transitionDrawable = TransitionDrawable(layers)
                transitionDrawable.isCrossFadeEnabled = true
                imageView.setImageDrawable(transitionDrawable)
                transitionDrawable.startTransition(
                    resources.getInteger(
                        android.R.integer.config_shortAnimTime
                    )
                )
            }
        })
    }

    /**
     * Loads a Drawable for this asset into the provided ImageView, providing a crossfade transition
     * with the given duration from the Drawable previously set on the ImageView.
     *
     * @param context                  Activity hosting the ImageView.
     * @param imageView                ImageView which is the target view of this asset.
     * @param transitionDurationMillis Duration of the crossfade, in milliseconds.
     * @param drawableLoadedListener   Listener called once the transition has begun.
     * @param placeholderColor         Color of the placeholder if the provided ImageView is empty
     * before the
     */
    fun loadDrawableWithTransition(
        context: Context,
        imageView: ImageView,
        transitionDurationMillis: Int,
        drawableLoadedListener: DrawableLoadedListener?,
        placeholderColor: Int
    ) {
        val imageViewDimensions = getViewDimensions(imageView)

        // Transition from a placeholder ColorDrawable to the decoded bitmap when the ImageView in
        // question is empty.
        val needsPlaceholder = imageView.drawable == null
        if (needsPlaceholder) {
            imageView.setImageDrawable(
                getPlaceholderDrawable(context, imageView, placeholderColor)
            )
        }
        decodeBitmap(
            imageViewDimensions.x,
            imageViewDimensions.y,
            object : BitmapReceiver {
                override fun onBitmapDecoded(bitmap: Bitmap?) {
                    val resources = context.resources
                    CenterCropBitmapTask(bitmap!!, imageView, object : BitmapReceiver {
                        override fun onBitmapDecoded(bitmap: Bitmap?) {
                            val layers = arrayOfNulls<Drawable>(2)
                            val existingDrawable = imageView.drawable
                            if (existingDrawable is TransitionDrawable) {
                                // Take only the second layer in the existing TransitionDrawable so
                                // we don't keep
                                // around a reference to older layers which are no longer shown (this
                                // way we avoid a
                                // memory leak).
                                val existingTransitionDrawable = existingDrawable
                                val id = existingTransitionDrawable.getId(1)
                                layers[0] = existingTransitionDrawable.findDrawableByLayerId(id)
                            } else {
                                layers[0] = existingDrawable
                            }
                            layers[1] = BitmapDrawable(resources, bitmap)
                            val transitionDrawable = TransitionDrawable(layers)
                            transitionDrawable.isCrossFadeEnabled = true
                            imageView.setImageDrawable(transitionDrawable)
                            transitionDrawable.startTransition(transitionDurationMillis)
                            drawableLoadedListener?.onDrawableLoaded()
                        }
                    }).execute()
                }
            })
    }

    /**
     * Interface for receiving decoded Bitmaps.
     */
    interface BitmapReceiver {
        /**
         * Called with a decoded Bitmap object or null if there was an error decoding the bitmap.
         */
        fun onBitmapDecoded(bitmap: Bitmap?)
    }

    /**
     * Interface for receiving raw asset dimensions.
     */
    interface DimensionsReceiver {
        /**
         * Called with raw dimensions of asset or null if the asset is unable to decode the raw
         * dimensions.
         *
         * @param dimensions Dimensions as a Point where width is represented by "x" and height by
         * "y".
         */
        fun onDimensionsDecoded(dimensions: Point?)
    }

    /**
     * Interface for being notified when a drawable has been loaded.
     */
    interface DrawableLoadedListener {
        fun onDrawableLoaded()
    }

    /**
     * Custom AsyncTask which returns a copy of the given bitmap which is center cropped and scaled
     * to fit in the given ImageView.
     */
    class CenterCropBitmapTask(
        private val mBitmap: Bitmap, view: View,
        private val mBitmapReceiver: BitmapReceiver
    ) : AsyncTask<Void?, Void?, Bitmap>() {
        private val mImageViewWidth: Int
        private val mImageViewHeight: Int
        override fun doInBackground(vararg unused: Void?): Bitmap {
            val measuredWidth = mImageViewWidth
            val measuredHeight = mImageViewHeight
            val bitmapWidth = mBitmap.width
            val bitmapHeight = mBitmap.height
            val scale =
                (bitmapWidth.toFloat() / measuredWidth).coerceAtMost(bitmapHeight.toFloat() / measuredHeight)
            val scaledBitmap = Bitmap.createScaledBitmap(
                mBitmap, (bitmapWidth / scale).roundToInt(), (bitmapHeight / scale).roundToInt(),
                true
            )
            val horizontalGutterPx = 0.coerceAtLeast((scaledBitmap.width - measuredWidth) / 2)
            val verticalGutterPx = 0.coerceAtLeast((scaledBitmap.height - measuredHeight) / 2)
            return Bitmap.createBitmap(
                scaledBitmap,
                horizontalGutterPx,
                verticalGutterPx,
                scaledBitmap.width - 2 * horizontalGutterPx,
                scaledBitmap.height - 2 * verticalGutterPx
            )
        }

        override fun onPostExecute(newBitmap: Bitmap) {
            mBitmapReceiver.onBitmapDecoded(newBitmap)
        }

        init {
            val imageViewDimensions = getViewDimensions(view)
            mImageViewWidth = imageViewDimensions.x
            mImageViewHeight = imageViewDimensions.y
        }
    }

    companion object {
        /**
         * Creates and returns a placeholder Drawable instance sized exactly to the target ImageView and
         * filled completely with pixels of the provided placeholder color.
         */
        protected fun getPlaceholderDrawable(
            context: Context, imageView: ImageView, placeholderColor: Int
        ): Drawable {
            val imageViewDimensions = getViewDimensions(imageView)
            val placeholderBitmap = Bitmap.createBitmap(
                imageViewDimensions.x,
                imageViewDimensions.y,
                Bitmap.Config.ARGB_8888
            )
            placeholderBitmap.eraseColor(placeholderColor)
            return BitmapDrawable(context.resources, placeholderBitmap)
        }

        /**
         * Returns the visible height and width in pixels of the provided ImageView, or if it hasn't
         * been laid out yet, then gets the absolute value of the layout params.
         */
        private fun getViewDimensions(view: View): Point {
            val width = if (view.width > 0) view.width else abs(view.layoutParams.width)
            val height = if (view.height > 0) view.height else abs(view.layoutParams.height)
            return Point(width, height)
        }
    }
}