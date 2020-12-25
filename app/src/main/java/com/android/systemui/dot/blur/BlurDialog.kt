/*
 * Copyright (C) 2020 The dotOS Project
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
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.content.ContextCompat
import com.android.settings.dotextras.R

class BlurDialog : BlurView {
    private var radius: Float = 0f
    private var duration = DURATION_INFINITE

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttributes(context, attrs)
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttributes(context, attrs)
        init()
    }

    private fun init() {
        setOverlayColor(ContextCompat.getColor(context, R.color.colorPrimaryTranslucent))
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
    }

    private fun initAttributes(context: Context, attrs: AttributeSet) {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BlurDialog,
            0, 0
        )
        try {
            radius = a.getDimension(R.styleable.BlurDialog_cornerRadius, 12f)
        } finally {
            a.recycle()
        }
        background = RoundedCornersDrawable(radius)
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
    }

    fun create(decorView: View, radius: Int, cornerRadius: Float) {
        val rootView = decorView.findViewById<View>(android.R.id.content) as ViewGroup
        background = RoundedCornersDrawable(cornerRadius)
        setBlurAutoUpdate(true)
        setupWith(rootView)
            .setBlurAlgorithm(RenderScriptBlur(context))
            .setFrameClearDrawable(background)
            ?.setBlurAutoUpdate(true)
        setBlurRadius(radius.toFloat())
    }

    companion object {
        const val DURATION_INFINITE = -1
    }

    class RoundedCornersDrawable(cornerRadius: Float) : GradientDrawable(
        Orientation.BOTTOM_TOP, intArrayOf(
            Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT
        )
    ) {
        init {
            cornerRadii = floatArrayOf(
                // top left
                cornerRadius,
                cornerRadius,
                // top right
                cornerRadius,
                cornerRadius,
                // bottom right
                0f,
                0f,
                // bottom left
                0f,
                0f
            )
        }
    }
}