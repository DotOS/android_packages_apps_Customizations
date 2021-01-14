/*
 * Copyright (C) 2020 The dotOS Project
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
package com.android.settings.dotextras.custom.views

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import com.android.settings.dotextras.R

class FodPreview(context: Context?, attributesSet: AttributeSet) : RelativeLayout(
    context,
    attributesSet
) {

    var fodPreview: AppCompatImageView
    var fodAnimPreview: AppCompatImageView

    init {
        LayoutInflater.from(context).inflate(
            R.layout.item_fod_preview, this, true
        )
        fodPreview = findViewById(R.id.fodPreviewSrc)
        fodAnimPreview = findViewById(R.id.fodAnimPreviewSrc)
    }

    fun setPreview(drawable: Drawable?) {
        fodPreview.setImageDrawable(drawable)
    }

    fun setPreviewAnimation(anim: AnimationDrawable, start: Boolean) {
        if (fodAnimPreview.drawable != null && (fodAnimPreview.drawable as AnimationDrawable).isRunning)
            (fodAnimPreview.drawable as AnimationDrawable).stop()
        fodAnimPreview.setImageDrawable(anim)
        if (start) {
            anim.start()
        }
    }

    fun isAnimating(): Boolean = (fodAnimPreview.drawable!! as AnimationDrawable).isRunning

    fun setAnimationState(running: Boolean) {
        if (running)
            (fodAnimPreview.drawable!! as AnimationDrawable).start()
        else
            (fodAnimPreview.drawable!! as AnimationDrawable).stop()
    }
}