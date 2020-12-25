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

import android.graphics.Bitmap

interface BlurAlgorithm {
    /**
     * @param bitmap     bitmap to be blurred
     * @param blurRadius blur radius
     * @return blurred bitmap
     */
    fun blur(bitmap: Bitmap?, blurRadius: Float): Bitmap?

    /**
     * Frees allocated resources
     */
    fun destroy()

    /**
     * @return true if this algorithm returns the same instance of bitmap as it accepted
     * false if it creates a new instance.
     *
     *
     * If you return false from this method, you'll be responsible to swap bitmaps in your
     * [BlurAlgorithm.blur] implementation
     * (assign input bitmap to your field and return the instance algorithm just blurred).
     */
    fun canModifyBitmap(): Boolean

    /**
     * Retrieve the [android.graphics.Bitmap.Config] on which the [BlurAlgorithm]
     * can actually work.
     *
     * @return bitmap config supported by the given blur algorithm.
     */
    val supportedBitmapConfig: Bitmap.Config
}