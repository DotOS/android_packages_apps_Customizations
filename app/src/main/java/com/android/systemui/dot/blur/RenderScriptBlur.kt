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

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.annotation.RequiresApi

/**
 * Blur using RenderScript, processed on GPU.
 * Requires API 17+
 */
class RenderScriptBlur @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1) constructor(context: Context?) :
    BlurAlgorithm {
    private val renderScript: RenderScript
    private val blurScript: ScriptIntrinsicBlur
    private var outAllocation: Allocation? = null
    private var lastBitmapWidth = -1
    private var lastBitmapHeight = -1
    private fun canReuseAllocation(bitmap: Bitmap?): Boolean {
        return bitmap!!.height == lastBitmapHeight && bitmap.width == lastBitmapWidth
    }

    /**
     * @param bitmap     bitmap to blur
     * @param blurRadius blur radius (1..25)
     * @return blurred bitmap
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun blur(bitmap: Bitmap?, blurRadius: Float): Bitmap? {
        //Allocation will use the same backing array of pixels as bitmap if created with USAGE_SHARED flag
        val inAllocation = Allocation.createFromBitmap(renderScript, bitmap)
        if (!canReuseAllocation(bitmap)) {
            if (outAllocation != null) {
                outAllocation!!.destroy()
            }
            outAllocation = Allocation.createTyped(renderScript, inAllocation.type)
            lastBitmapWidth = bitmap!!.width
            lastBitmapHeight = bitmap.height
        }
        blurScript.setRadius(blurRadius)
        blurScript.setInput(inAllocation)
        //do not use inAllocation in forEach. it will cause visual artifacts on blurred Bitmap
        blurScript.forEach(outAllocation)
        outAllocation!!.copyTo(bitmap)
        inAllocation.destroy()
        return bitmap
    }

    override fun destroy() {
        blurScript.destroy()
        renderScript.destroy()
        if (outAllocation != null) {
            outAllocation!!.destroy()
        }
    }

    override fun canModifyBitmap(): Boolean {
        return true
    }

    override val supportedBitmapConfig: Bitmap.Config
        get() = Bitmap.Config.ARGB_8888

    /**
     * @param context Context to create the [RenderScript]
     */
    init {
        renderScript = RenderScript.create(context)
        blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    }
}