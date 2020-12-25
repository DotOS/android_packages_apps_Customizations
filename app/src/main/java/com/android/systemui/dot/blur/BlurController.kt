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

import android.graphics.Canvas

interface BlurController : BlurViewFacade {
    /**
     * Draws blurred content on given canvas
     *
     * @return true if BlurView should proceed with drawing itself and its children
     */
    fun draw(canvas: Canvas): Boolean

    /**
     * Must be used to notify Controller when BlurView's size has changed
     */
    fun updateBlurViewSize()

    /**
     * Frees allocated resources
     */
    fun destroy()

    companion object {
        const val DEFAULT_SCALE_FACTOR = 8f
        const val DEFAULT_BLUR_RADIUS = 16f
    }
}