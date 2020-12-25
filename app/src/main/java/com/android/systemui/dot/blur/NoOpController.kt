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