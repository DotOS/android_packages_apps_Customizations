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

import android.app.Activity
import android.graphics.*
import android.os.AsyncTask
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import com.android.settings.dotextras.custom.sections.clock.utils.CropRectRotator.rotateCropRectForExifOrientation
import java.io.IOException
import java.io.InputStream

/**
 * Represents Asset types for which bytes can be read directly, allowing for flexible bitmap
 * decoding.
 */
abstract class StreamableAsset : Asset() {
    private var mBitmapRegionDecoder: BitmapRegionDecoder? = null
    private var mDimensions: Point? = null
    override fun decodeBitmap(
        targetWidth: Int, targetHeight: Int,
        receiver: BitmapReceiver?,
    ) {
        val task = DecodeBitmapAsyncTask(targetWidth, targetHeight, receiver)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun decodeRawDimensions(activity: Activity?, receiver: DimensionsReceiver?) {
        val task = DecodeDimensionsAsyncTask(receiver)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    override fun decodeBitmapRegion(
        rect: Rect?, targetWidth: Int, targetHeight: Int,
        receiver: BitmapReceiver?,
    ) {
        runDecodeBitmapRegionTask(rect, targetWidth, targetHeight, receiver)
    }

    override fun supportsTiling(): Boolean {
        return true
    }

    /**
     * Fetches an input stream of bytes for the wallpaper image asset and provides the stream
     * asynchronously back to a [StreamReceiver].
     */
    fun fetchInputStream(streamReceiver: StreamReceiver) {
        object : AsyncTask<Void?, Void?, InputStream?>() {
            override fun doInBackground(vararg params: Void?): InputStream? {
                return openInputStream()
            }

            override fun onPostExecute(inputStream: InputStream?) {
                streamReceiver.onInputStreamOpened(inputStream)
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }

    /**
     * Returns an InputStream representing the asset. Should only be called off the main UI thread.
     */
    protected abstract fun openInputStream(): InputStream?// By default, assume that the EXIF orientation is normal (i.e., bitmap is rotated 0 degrees
    // from how it should be rendered to a viewer).
    /**
     * Gets the EXIF orientation value of the asset. This method should only be called off the main UI
     * thread.
     */
    protected open val exifOrientation: Int
        get() =// By default, assume that the EXIF orientation is normal (i.e., bitmap is rotated 0 degrees
            // from how it should be rendered to a viewer).
            ExifInterface.ORIENTATION_NORMAL

    /**
     * Decodes and downscales a bitmap region off the main UI thread.
     *
     * @param rect         Rect representing the crop region in terms of the original image's resolution.
     * @param targetWidth  Width of target view in physical pixels.
     * @param targetHeight Height of target view in physical pixels.
     * @param receiver     Called with the decoded bitmap region or null if there was an error decoding
     * the bitmap region.
     * @return AsyncTask reference so that the decoding task can be canceled before it starts.
     */
    fun runDecodeBitmapRegionTask(
        rect: Rect?, targetWidth: Int, targetHeight: Int,
        receiver: BitmapReceiver?,
    ): AsyncTask<*, *, *> {
        val task = DecodeBitmapRegionAsyncTask(rect, targetWidth, targetHeight, receiver)
        task.execute()
        return task
    }

    /**
     * Decodes the raw dimensions of the asset without allocating memory for the entire asset. Adjusts
     * for the EXIF orientation if necessary.
     *
     * @return Dimensions as a Point where width is represented by "x" and height by "y".
     */
    fun calculateRawDimensions(): Point? {
        if (mDimensions != null) {
            return mDimensions
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val inputStream = openInputStream() ?: return null
        // Input stream may be null if there was an error opening it.
        BitmapFactory.decodeStream(inputStream, null, options)
        closeInputStream(inputStream,
            "There was an error closing the input stream used to calculate "
                    + "the image's raw dimensions")
        val exifOrientation = exifOrientation
        // Swap height and width if image is rotated 90 or 270 degrees.
        mDimensions = if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90
            || exifOrientation == ExifInterface.ORIENTATION_ROTATE_270
        ) {
            Point(options.outHeight, options.outWidth)
        } else {
            Point(options.outWidth, options.outHeight)
        }
        return mDimensions
    }

    /**
     * Returns a BitmapRegionDecoder for the asset.
     */
    private fun openBitmapRegionDecoder(): BitmapRegionDecoder? {
        var inputStream: InputStream? = null
        var brd: BitmapRegionDecoder? = null
        try {
            inputStream = openInputStream()
            // Input stream may be null if there was an error opening it.
            if (inputStream == null) {
                return null
            }
            brd = BitmapRegionDecoder.newInstance(inputStream, true)
        } catch (e: IOException) {
            Log.w(TAG, "Unable to open BitmapRegionDecoder", e)
        } finally {
            closeInputStream(inputStream, "Unable to close input stream used to create "
                    + "BitmapRegionDecoder")
        }
        return brd
    }

    /**
     * Closes the provided InputStream and if there was an error, logs the provided error message.
     */
    private fun closeInputStream(inputStream: InputStream?, errorMessage: String) {
        try {
            inputStream!!.close()
        } catch (e: IOException) {
            Log.e(TAG, errorMessage)
        }
    }

    /**
     * Interface for receiving unmodified input streams of the underlying asset without any
     * downscaling or other decoding options.
     */
    interface StreamReceiver {
        /**
         * Called with an opened input stream of bytes from the underlying image asset. Clients must
         * close the input stream after it has been read. Returns null if there was an error opening the
         * input stream.
         */
        fun onInputStreamOpened(inputStream: InputStream?)
    }

    /**
     * AsyncTask which decodes a Bitmap off the UI thread. Scales the Bitmap for the target width and
     * height if possible.
     */
    private inner class DecodeBitmapAsyncTask(
        private var mTargetWidth: Int,
        private var mTargetHeight: Int,
        private val mReceiver: BitmapReceiver?,
    ) : AsyncTask<Void?, Void?, Bitmap?>() {
        override fun doInBackground(vararg unused: Void?): Bitmap? {
            val exifOrientation = exifOrientation
            // Switch target height and width if image is rotated 90 or 270 degrees.
            if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90
                || exifOrientation == ExifInterface.ORIENTATION_ROTATE_270
            ) {
                val tempHeight = mTargetHeight
                mTargetHeight = mTargetWidth
                mTargetWidth = tempHeight
            }
            val options = BitmapFactory.Options()
            val rawDimensions = calculateRawDimensions() ?: return null
            // Raw dimensions may be null if there was an error opening the underlying input stream.
            options.inSampleSize = BitmapUtils.calculateInSampleSize(
                rawDimensions.x, rawDimensions.y, mTargetWidth, mTargetHeight)
            options.inPreferredConfig = Bitmap.Config.HARDWARE
            val inputStream = openInputStream()
            var bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            closeInputStream(
                inputStream, "Error closing the input stream used to decode the full bitmap")

            // Rotate output bitmap if necessary because of EXIF orientation tag.
            val matrixRotation = getDegreesRotationForExifOrientation(exifOrientation)
            if (matrixRotation > 0) {
                val rotateMatrix = Matrix()
                rotateMatrix.setRotate(matrixRotation.toFloat())
                bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, rotateMatrix, false)
            }
            return bitmap
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            mReceiver!!.onBitmapDecoded(bitmap)
        }
    }

    /**
     * AsyncTask subclass which decodes a bitmap region from the asset off the main UI thread.
     */
    private inner class DecodeBitmapRegionAsyncTask(
        private var mCropRect: Rect?, private var mTargetWidth: Int, private var mTargetHeight: Int,
        private val mReceiver: BitmapReceiver?,
    ) : AsyncTask<Void?, Void?, Bitmap?>() {
        override fun doInBackground(vararg voids: Void?): Bitmap? {
            val exifOrientation = exifOrientation
            // Switch target height and width if image is rotated 90 or 270 degrees.
            if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90
                || exifOrientation == ExifInterface.ORIENTATION_ROTATE_270
            ) {
                val tempHeight = mTargetHeight
                mTargetHeight = mTargetWidth
                mTargetWidth = tempHeight
            }

            // Rotate crop rect if image is rotated more than 0 degrees.
            mCropRect = rotateCropRectForExifOrientation(
                calculateRawDimensions()!!, mCropRect!!, exifOrientation)
            val options = BitmapFactory.Options()
            options.inSampleSize = BitmapUtils.calculateInSampleSize(
                mCropRect!!.width(), mCropRect!!.height(), mTargetWidth, mTargetHeight)
            if (mBitmapRegionDecoder == null) {
                mBitmapRegionDecoder = openBitmapRegionDecoder()
            }

            // Bitmap region decoder may have failed to open if there was a problem with the underlying
            // InputStream.
            return if (mBitmapRegionDecoder != null) {
                try {
                    var bitmap = mBitmapRegionDecoder!!.decodeRegion(mCropRect, options)

                    // Rotate output bitmap if necessary because of EXIF orientation.
                    val matrixRotation = getDegreesRotationForExifOrientation(exifOrientation)
                    if (matrixRotation > 0) {
                        val rotateMatrix = Matrix()
                        rotateMatrix.setRotate(matrixRotation.toFloat())
                        bitmap = Bitmap.createBitmap(
                            bitmap, 0, 0, bitmap.width, bitmap.height, rotateMatrix, false)
                    }
                    bitmap
                } catch (e: OutOfMemoryError) {
                    Log.e(TAG, "Out of memory and unable to decode bitmap region", e)
                    null
                }
            } else null
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            mReceiver!!.onBitmapDecoded(bitmap)
        }
    }

    /**
     * AsyncTask subclass which decodes the raw dimensions of the asset off the main UI thread. Avoids
     * allocating memory for the fully decoded image.
     */
    private inner class DecodeDimensionsAsyncTask(private val mReceiver: DimensionsReceiver?) :
        AsyncTask<Void?, Void?, Point?>() {
        override fun doInBackground(vararg unused: Void?): Point? {
            return calculateRawDimensions()
        }

        override fun onPostExecute(dimensions: Point?) {
            mReceiver!!.onDimensionsDecoded(dimensions)
        }
    }

    companion object {
        private const val TAG = "StreamableAsset"

        /**
         * Scales and returns a new Rect from the given Rect by the given scaling factor.
         */
        fun scaleRect(rect: Rect, scale: Float): Rect {
            return Rect(
                Math.round(rect.left.toFloat() * scale),
                Math.round(rect.top.toFloat() * scale),
                Math.round(rect.right.toFloat() * scale),
                Math.round(rect.bottom.toFloat() * scale))
        }

        /**
         * Maps from EXIF orientation tag values to counterclockwise degree rotation values.
         */
        private fun getDegreesRotationForExifOrientation(exifOrientation: Int): Int {
            return when (exifOrientation) {
                ExifInterface.ORIENTATION_NORMAL -> 0
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> {
                    Log.w(TAG,
                        "Unsupported EXIF orientation $exifOrientation")
                    0
                }
            }
        }
    }
}