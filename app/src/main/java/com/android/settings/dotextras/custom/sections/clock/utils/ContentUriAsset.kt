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
package com.android.settings.dotextras.custom.sections.clock.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

/**
 * Represents an asset located via an Android content URI.
 */
class ContentUriAsset @JvmOverloads constructor(
    context: Context, val uri: Uri, requestOptions: RequestOptions,
    uncached: Boolean =  /* uncached */false,
) : StreamableAsset() {
    private val mContext: Context = context.applicationContext
    private var mRequestOptions: RequestOptions? = null
    private var mExifCompat: ExifInterfaceCompat? = null
    private var mExifOrientation: Int = ExifInterfaceCompat.EXIF_ORIENTATION_UNKNOWN
    /**
     * @param context The application's context.
     * @param uri     Content URI locating the asset.
     * @param uncached If true, [.loadDrawable] and
     * [.loadDrawableWithTransition]
     * will not cache data, and fetch it each time.
     */
    /**
     * @param context The application's context.
     * @param uri     Content URI locating the asset.
     */
    @JvmOverloads
    constructor(
        context: Context,
        uri: Uri,
        uncached: Boolean =  /* uncached */false,
    ) : this(context, uri, RequestOptions.centerCropTransform(), uncached)

    override fun decodeBitmapRegion(
        rect: Rect?, targetWidth: Int, targetHeight: Int,
        receiver: BitmapReceiver?,
    ) {
        // BitmapRegionDecoder only supports images encoded in either JPEG or PNG, so if the content
        // URI asset is encoded with another format (for example, GIF), then fall back to cropping a
        // bitmap region from the full-sized bitmap.
        if (isJpeg || isPng) {
            super.decodeBitmapRegion(rect, targetWidth, targetHeight, receiver)
            return
        }
        decodeRawDimensions(null /* activity */, object : DimensionsReceiver {
            override fun onDimensionsDecoded(dimensions: Point?) {
                if (dimensions == null) {
                    Log.e(TAG, "There was an error decoding the asset's raw dimensions with " +
                            "content URI: " + uri)
                    receiver!!.onBitmapDecoded(null)
                    return
                }
                decodeBitmap(dimensions.x, dimensions.y, object : BitmapReceiver {
                    override fun onBitmapDecoded(fullBitmap: Bitmap?) {
                        if (fullBitmap == null) {
                            Log.e(TAG, "There was an error decoding the asset's full bitmap with " +
                                    "content URI: " + uri)
                            receiver!!.onBitmapDecoded(null)
                            return
                        }
                        val task = BitmapCropTask(fullBitmap, rect, receiver)
                        task.execute()
                    }
                })
            }
        })
    }

    /**
     * Returns whether this image is encoded in the JPEG file format.
     */
    val isJpeg: Boolean
        get() {
            val mimeType = mContext.contentResolver.getType(uri)
            return mimeType != null && mimeType == JPEG_MIME_TYPE
        }

    /**
     * Returns whether this image is encoded in the PNG file format.
     */
    val isPng: Boolean
        get() {
            val mimeType = mContext.contentResolver.getType(uri)
            return mimeType != null && mimeType == PNG_MIME_TYPE
        }

    /**
     * Reads the EXIF tag on the asset. Automatically trims leading and trailing whitespace.
     *
     * @return String attribute value for this tag ID, or null if ExifInterface failed to read tags
     * for this asset, if this tag was not found in the image's metadata, or if this tag was
     * empty (i.e., only whitespace).
     */
    fun readExifTag(tagId: String?): String? {
        ensureExifInterface()
        if (mExifCompat == null) {
            Log.w(TAG, "Unable to read EXIF tags for content URI asset")
            return null
        }
        val attribute = mExifCompat!!.getAttribute(tagId)
        return if (attribute == null || attribute.trim { it <= ' ' }.isEmpty()) {
            null
        } else attribute.trim { it <= ' ' }
    }

    private fun ensureExifInterface() {
        if (mExifCompat == null) {
            try {
                openInputStream().use { inputStream ->
                    if (inputStream != null) {
                        mExifCompat = ExifInterfaceCompat(inputStream)
                    }
                }
            } catch (e: IOException) {
                Log.w(TAG, "Couldn't read stream for " + uri, e)
            }
        }
    }

    override fun openInputStream(): InputStream? {
        return try {
            mContext.contentResolver.openInputStream(uri)
        } catch (e: FileNotFoundException) {
            Log.w(TAG, "Image file not found", e)
            null
        }
    }


    /**
     * Returns the EXIF rotation for the content URI asset. This method should only be called off
     * the main UI thread.
     */
    private fun readExifOrientation(): Int {
        ensureExifInterface()
        if (mExifCompat == null) {
            Log.w(TAG, "Unable to read EXIF rotation for content URI asset with content URI: "
                    + uri)
            return ExifInterfaceCompat.EXIF_ORIENTATION_NORMAL
        }
        return mExifCompat!!.getAttributeInt(ExifInterfaceCompat.TAG_ORIENTATION,
            ExifInterfaceCompat.EXIF_ORIENTATION_NORMAL)
    }

    override fun loadDrawable(
        context: Context, imageView: ImageView,
        placeholderColor: Int,
    ) {
        Glide.with(context)
            .asDrawable()
            .load(uri)
            .apply(mRequestOptions!!.placeholder(ColorDrawable(placeholderColor)))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }

    override fun loadDrawableWithTransition(
        context: Context, imageView: ImageView,
        transitionDurationMillis: Int, drawableLoadedListener: DrawableLoadedListener?,
        placeholderColor: Int,
    ) {
        Glide.with(context)
            .asDrawable()
            .load(uri)
            .apply(mRequestOptions!!.placeholder(ColorDrawable(placeholderColor)))
            .transition(DrawableTransitionOptions.withCrossFade(transitionDurationMillis))
            .listener(object : RequestListener<Drawable?> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any,
                    target: Target<Drawable?>, isFirstResource: Boolean,
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any,
                    target: Target<Drawable?>, dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    drawableLoadedListener?.onDrawableLoaded()
                    return false
                }
            })
            .into(imageView)
    }

    /**
     * Custom AsyncTask which crops a bitmap region from a larger bitmap.
     */
    private class BitmapCropTask(
        private val mFromBitmap: Bitmap?,
        private val mCropRect: Rect?,
        private val mReceiver: BitmapReceiver?,
    ) : AsyncTask<Void?, Void?, Bitmap?>() {
        override fun doInBackground(vararg unused: Void?): Bitmap? {
            return if (mFromBitmap == null) {
                null
            } else Bitmap.createBitmap(
                mFromBitmap, mCropRect!!.left, mCropRect.top, mCropRect.width(),
                mCropRect.height())
        }

        override fun onPostExecute(bitmapRegion: Bitmap?) {
            mReceiver!!.onBitmapDecoded(bitmapRegion)
        }
    }

    companion object {
        private const val TAG = "ContentUriAsset"
        private const val JPEG_MIME_TYPE = "image/jpeg"
        private const val PNG_MIME_TYPE = "image/png"
    }
    /**
     * @param context The application's context.
     * @param uri     Content URI locating the asset.
     * @param requestOptions [RequestOptions] to be applied when loading the asset.
     * @param uncached If true, [.loadDrawable] and
     * [.loadDrawableWithTransition]
     * will not cache data, and fetch it each time.
     */
    /**
     * @param context The application's context.
     * @param uri     Content URI locating the asset.
     * @param requestOptions [RequestOptions] to be applied when loading the asset.
     */
    init {
        mRequestOptions = if (uncached) {
            requestOptions.apply(RequestOptions
                .diskCacheStrategyOf(DiskCacheStrategy.NONE)
                .skipMemoryCache(true))
        } else {
            requestOptions
        }
    }
}