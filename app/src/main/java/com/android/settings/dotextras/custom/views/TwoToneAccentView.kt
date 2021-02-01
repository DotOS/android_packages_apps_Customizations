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
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.BalloonPump
import com.android.settings.dotextras.custom.utils.ColorSheetUtils
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.utils.SettingsConstants
import com.android.settings.dotextras.system.FeatureManager
import com.google.android.material.button.MaterialButton

typealias onTwoTonePressed = ((shade: TwoToneAccentView.Shade) -> Unit)?

class TwoToneAccentView(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private var whiteLayout: RelativeLayout
    private var whitePreview: AppCompatImageView
    private var whiteHex: TextView
    private var whiteCompatible: AppCompatImageView
    private var whiteApply: MaterialButton

    private var darkLayout: RelativeLayout
    private var darkPreview: AppCompatImageView
    private var darkHex: TextView
    private var darkCompatible: AppCompatImageView
    private var darkApply: MaterialButton

    private var smartPicker: LinearLayout
    private var resetAccents: LinearLayout

    private val featureManager: FeatureManager

    private var listener: onTwoTonePressed = null
    private var lightColor: Int? = null
    private var darkColor: Int? = null

    private var sharedPref: SharedPreferences? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.item_shade_control, this, true)

        whiteLayout = findViewById(R.id.accentLight)
        whitePreview = findViewById(R.id.accentPreviewLight)
        whiteHex = findViewById(R.id.accentHexLight)
        whiteCompatible = findViewById(R.id.accentLightCompatibleImage)
        whiteApply = findViewById(R.id.applyLight)

        darkLayout = findViewById(R.id.accentDark)
        darkPreview = findViewById(R.id.accentPreviewDark)
        darkHex = findViewById(R.id.accentHexDark)
        darkCompatible = findViewById(R.id.accentDarkCompatibleImage)
        darkApply = findViewById(R.id.applyDark)

        smartPicker = findViewById(R.id.smartPicker)
        resetAccents = findViewById(R.id.resetCustomAccents)

        featureManager = FeatureManager(context!!.contentResolver)

        if (ResourceHelper.lightsOut(context))
            requestFocus(Shade.DARK)
        else
            requestFocus(Shade.LIGHT)

        whiteLayout.setOnClickListener {
            if (!isInFocus(Shade.LIGHT))
                requestFocus(Shade.LIGHT)
        }
        whiteLayout.setOnLongClickListener {
            if (isInFocus(Shade.LIGHT))
                listener?.invoke(Shade.LIGHT)
            isInFocus(Shade.LIGHT)
        }
        darkLayout.setOnClickListener {
            if (!isInFocus(Shade.DARK))
                requestFocus(Shade.DARK)
        }
        darkLayout.setOnLongClickListener {
            if (isInFocus(Shade.DARK))
                listener?.invoke(Shade.DARK)
            isInFocus(Shade.DARK)
        }

        smartPicker.setOnClickListener {
            if (isInFocus(Shade.LIGHT) && lightColor != null)
                bindDarkColor(ResourceHelper.getDarkCousinColor(lightColor!!))
            if (isInFocus(Shade.DARK) && darkColor != null)
                bindWhiteColor(ResourceHelper.getLightCousinColor(darkColor!!))
        }
        resetAccents.setOnClickListener {
            featureManager.AccentManager().reset()
        }
    }

    fun setSharedPref(prefs: SharedPreferences) {
        sharedPref = prefs
        createBalloon(if (isInFocus(Shade.DARK)) darkLayout else whiteLayout)
    }

    private fun createBalloon(view: View) {
        val balloonPump = BalloonPump(mContext, sharedPref!!)
        balloonPump.create(R.string.ballon_twotone)
        balloonPump.show(view)
    }

    fun setOnTwoTonePressed(listener: onTwoTonePressed) {
        this.listener = listener
    }

    private fun requestFocus(shade: Shade) {
        when (shade) {
            Shade.LIGHT -> {
                whitePreview.contentDescription = "focused"
                darkPreview.contentDescription = ""
                whiteLayout.background = ContextCompat.getDrawable(context, R.drawable.rounded_fg)
                darkLayout.background = null
                whiteHex.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.colorPrimaryBackground, null))
                darkHex.backgroundTintList = null
            }
            Shade.DARK -> {
                darkPreview.contentDescription = "focused"
                whitePreview.contentDescription = ""
                whiteLayout.background = null
                darkLayout.background = ContextCompat.getDrawable(context, R.drawable.rounded_fg)
                whiteHex.backgroundTintList = null
                darkHex.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.colorPrimaryBackground, null))
            }
        }
    }

    private fun isInFocus(shade: Shade): Boolean = when (shade) {
        Shade.LIGHT -> whitePreview.contentDescription == "focused"
        Shade.DARK -> darkPreview.contentDescription == "focused"
    }

    fun bindWhiteColor(color: Int) {
        lightColor = color
        whitePreview.imageTintList = ColorStateList.valueOf(color)
        whitePreview.invalidate()
        whiteHex.text = ColorSheetUtils.colorToHex(color)
        whiteHex.invalidate()
        val suitableColor = ResourceHelper.getToleratedShade(color, Shade.LIGHT) == Shade.LIGHT
        whiteCompatible.imageTintList =
            if (!suitableColor) ColorStateList.valueOf(resources.getColor(R.color.red_500, null))
            else ColorStateList.valueOf(resources.getColor(R.color.green_500, null))
        whiteCompatible.setImageResource(if (!suitableColor) R.drawable.ic_error_round
        else R.drawable.ic_check_circle)
        whiteCompatible.invalidate()
        whiteApply.setOnClickListener {
            featureManager.AccentManager()
                .applyLight(ColorSheetUtils.colorToHex(color).replace("#", ""))
        }
    }

    fun bindDarkColor(color: Int) {
        darkColor = color
        darkPreview.imageTintList = ColorStateList.valueOf(color)
        darkHex.text = ColorSheetUtils.colorToHex(color)
        val suitableColor = ResourceHelper.getToleratedShade(color, Shade.DARK) == Shade.DARK
        darkCompatible.imageTintList =
            if (!suitableColor) ColorStateList.valueOf(resources.getColor(R.color.red_500, null))
            else ColorStateList.valueOf(resources.getColor(R.color.green_500, null))
        darkCompatible.setImageResource(if (!suitableColor) R.drawable.ic_error_round
        else R.drawable.ic_check_circle)
        darkApply.setOnClickListener {
            featureManager.AccentManager()
                .applyDark(ColorSheetUtils.colorToHex(color).replace("#", ""))
        }
    }

    enum class Shade {
        LIGHT, DARK
    }
}