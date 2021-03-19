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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper

@SuppressLint("MissingPermission")
class AccentHeader(context: Context?, attributesSet: AttributeSet) : LinearLayout(
    context,
    attributesSet
) {

    init {
        val mContext = context!!
        LayoutInflater.from(mContext).inflate(
            R.layout.item_accent_header, this, true
        )
        val accentColor: Int = ResourceHelper.getAccent(mContext)

        val nightModeFlags: Int = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        var normalizedTextColor: Int =
            if (ResourceHelper.isDark(accentColor)) ResourceHelper.getTextColor(mContext) else ResourceHelper.getInverseTextColor(
                mContext
            )
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> normalizedTextColor =
                if (ResourceHelper.isDark(accentColor)) ResourceHelper.getTextColor(
                    mContext
                ) else ResourceHelper.getInverseTextColor(mContext)
            Configuration.UI_MODE_NIGHT_NO -> normalizedTextColor =
                if (ResourceHelper.isDark(accentColor)) ResourceHelper.getInverseTextColor(
                    mContext
                ) else ResourceHelper.getTextColor(mContext)
        }
        val button = findViewById<ImageView>(R.id.accButton)
        val image = findViewById<ImageView>(R.id.accHeader)
        val text = findViewById<ImageView>(R.id.accText)
        button.imageTintList = ColorStateList.valueOf(accentColor)
        image.imageTintList = ColorStateList.valueOf(accentColor)
        text.imageTintList = ColorStateList.valueOf(normalizedTextColor)
    }
}