/*
 * Copyright (C) 2017 Valentin (github.com/byvlstr/blurdialog)
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
package com.android.systemui.dot.blur

import com.android.systemui.dot.blur.SizeScaler.Companion.ROUNDING_VALUE

/**
 * Scales width and height by [scaleFactor],
 * and then rounds the size proportionally so the width is divisible by [ROUNDING_VALUE]
 */
class SizeScaler(private val scaleFactor: Float) {
    fun scale(width: Int, height: Int): Size {
        val nonRoundedScaledWidth = downscaleSize(width.toFloat())
        val scaledWidth = roundSize(nonRoundedScaledWidth)
        //Only width has to be aligned to ROUNDING_VALUE
        val roundingScaleFactor = width.toFloat() / scaledWidth
        //Ceiling because rounding or flooring might leave empty space on the View's bottom
        val scaledHeight = Math.ceil((height / roundingScaleFactor).toDouble())
            .toInt()
        return Size(scaledWidth, scaledHeight, roundingScaleFactor)
    }

    fun isZeroSized(measuredWidth: Int, measuredHeight: Int): Boolean {
        return downscaleSize(measuredHeight.toFloat()) == 0 || downscaleSize(measuredWidth.toFloat()) == 0
    }

    /**
     * Rounds a value to the nearest divisible by [.ROUNDING_VALUE] to meet stride requirement
     */
    private fun roundSize(value: Int): Int {
        return if (value % ROUNDING_VALUE == 0) {
            value
        } else value - value % ROUNDING_VALUE + ROUNDING_VALUE
    }

    private fun downscaleSize(value: Float): Int {
        return Math.ceil((value / scaleFactor).toDouble()).toInt()
    }

    class Size(val width: Int, val height: Int, val scaleFactor: Float) {
        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val size = o as Size
            if (width != size.width) return false
            return if (height != size.height) false else java.lang.Float.compare(
                size.scaleFactor,
                scaleFactor
            ) == 0
        }

        override fun hashCode(): Int {
            var result = width
            result = 31 * result + height
            result = 31 * result + if (scaleFactor != +0.0f) java.lang.Float.floatToIntBits(
                scaleFactor
            ) else 0
            return result
        }

        override fun toString(): String {
            return "Size{" +
                    "width=" + width +
                    ", height=" + height +
                    ", scaleFactor=" + scaleFactor +
                    '}'
        }
    }

    companion object {
        // Bitmap size should be divisible by ROUNDING_VALUE to meet stride requirement.
        // This will help avoiding an extra bitmap allocation when passing the bitmap to RenderScript for blur.
        // Usually it's 16, but on Samsung devices it's 64 for some reason.
        private const val ROUNDING_VALUE = 64
    }
}