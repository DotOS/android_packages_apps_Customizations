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

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect

/**
 * Collection of static utility methods for decoding and processing Bitmaps.
 */
class BitmapUtils private constructor() {
    companion object {
        private const val DEFAULT_CENTER_ALIGNMENT = 0.5f

        /**
         * Calculates the highest subsampling factor to scale the source image to the target view without
         * losing visible quality. Final result is based on powers of 2 because it should be set as
         * BitmapOptions#inSampleSize.
         *
         * @param srcWidth     Width of source image.
         * @param srcHeight    Height of source image.
         * @param targetWidth  Width of target view.
         * @param targetHeight Height of target view.
         * @return Highest subsampling factor as a power of 2.
         */
        fun calculateInSampleSize(
            srcWidth: Int, srcHeight: Int, targetWidth: Int, targetHeight: Int
        ): Int {
            var shift = 0
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both the result
            // bitmap's height and width at least as large as the target height and width.
            while (halfHeight shr shift >= targetHeight && halfWidth shr shift >= targetWidth) {
                shift++
            }
            return 1 shl shift
        }

        /**
         * Generates a hash code for the given bitmap. Computation starts with a nonzero prime number,
         * then for the integer values of height, width, and a selection of pixel colors, multiplies the
         * result by 31 and adds said integer value. Multiply by 31 because it is prime and conveniently 1
         * less than 32 which is 2 ^ 5, allowing the VM to replace multiplication by a bit shift and
         * subtraction for performance.
         *
         *
         * This method should be called off the UI thread.
         */
        fun generateHashCode(bitmap: Bitmap): Long {
            var result: Long = 17
            val width = bitmap.width
            val height = bitmap.height
            result = 31 * result + width
            result = 31 * result + height

            // Traverse pixels exponentially so that hash code generation scales well with large images.
            var x = 0
            while (x < width) {
                var y = 0
                while (y < height) {
                    result = 31 * result + bitmap.getPixel(x, y)
                    y = y * 2 + 1
                }
                x = x * 2 + 1
            }
            return result
        }

        /**
         * Calculates horizontal alignment of the rect within the supplied dimensions.
         *
         * @return A float value between 0 and 1 specifying horizontal alignment; 0 for left-aligned, 0.5
         * for horizontal center-aligned, and 1 for right-aligned.
         */
        fun calculateHorizontalAlignment(dimensions: Point, rect: Rect): Float {
            val paddingLeft = rect.left
            val paddingRight = dimensions.x - rect.right
            val totalHorizontalPadding = paddingLeft + paddingRight
            // Zero horizontal padding means that there is no room to crop horizontally so we just fall
            // back to a default center-alignment value.
            return if (totalHorizontalPadding == 0) DEFAULT_CENTER_ALIGNMENT else paddingLeft / (paddingLeft.toFloat() + paddingRight)
        }

        /**
         * Calculates vertical alignment of the rect within the supplied dimensions.
         *
         * @return A float value between 0 and 1 specifying vertical alignment; 0 for top-aligned, 0.5 for
         * vertical center-aligned, and 1 for bottom-aligned.
         */
        fun calculateVerticalAlignment(dimensions: Point, rect: Rect): Float {
            val paddingTop = rect.top
            val paddingBottom = dimensions.y - rect.bottom
            val totalVerticalPadding = paddingTop + paddingBottom
            // Zero vertical padding means that there is no room to crop vertically so we just fall back to
            // a default center-alignment value.
            return if (totalVerticalPadding == 0) DEFAULT_CENTER_ALIGNMENT else paddingTop / (paddingTop.toFloat() + paddingBottom)
        }
    }

    // Suppress default constructor for noninstantiability.
    init {
        throw AssertionError()
    }
}