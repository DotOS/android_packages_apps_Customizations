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
package com.android.settings.dotextras.custom.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.android.settings.dotextras.custom.views.TwoToneAccentView.Shade
import com.android.settings.dotextras.custom.views.TwoToneAccentView.Shade.DARK
import com.android.settings.dotextras.custom.views.TwoToneAccentView.Shade.LIGHT
import com.android.settings.dotextras.system.FeatureManager
import kotlin.math.roundToInt

object ResourceHelper {

    fun isDark(color: Int): Boolean {
        val darkness: Double =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    /**
     * 0.0-0.5 = light
     * 0.5 = neutral
     * 0.5-1.0 = dark
     */
    fun getDarkness(color: Int): Double =
        1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255

    fun getLightCousinColor(darkColor: Int): Int {
        val darkness = getDarkness(darkColor)
        val light = darkness + (1-darkness) / 1.5
        return getColorByDarkness(darkColor, light)
    }

    fun getDarkCousinColor(lightColor: Int): Int {
        val lightness = getDarkness(lightColor)
        val dark = lightness + (1-lightness) * 1.5
        return getColorByDarkness(lightColor, dark)
    }

    private fun getColorByDarkness(initialColor: Int, darkness: Double): Int {
        val r = (Color.red(initialColor) * darkness).roundToInt()
        val g = (Color.green(initialColor) * darkness).roundToInt()
        val b = (Color.blue(initialColor) * darkness).roundToInt()
        return Color.argb(Color.alpha(initialColor),
            r.coerceAtMost(255),
            g.coerceAtMost(255),
            b.coerceAtMost(255))
    }

    fun lightsOut(context: Context): Boolean {
        val nightModeFlags: Int = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * LIGHT = suitable for light theme
     * DARK = suitable for dark theme
     */
    fun getToleratedShade(color: Int, shade: Shade): Shade {
        val tolerance = 0.3
        val colorDarkness = getDarkness(color)
        var toleratedDarkness = colorDarkness
        if (shade == LIGHT)
            toleratedDarkness-=tolerance
        if (shade == DARK)
            toleratedDarkness+=tolerance
        return if (toleratedDarkness in 0.0..0.5) LIGHT else DARK
    }

    /**
     * LIGHT = suitable for light theme
     * DARK = suitable for dark theme
     *
     * tolerance (Double), user-changeable
     * Preferred to be 0.3 - gives best results
     */
    fun getToleratedShade(color: Int, tolerance: Double, shade: Shade): Shade {
        val colorDarkness = getDarkness(color)
        var toleratedDarkness = colorDarkness
        if (shade == LIGHT) toleratedDarkness-=tolerance
        if (shade == DARK) toleratedDarkness+=tolerance
        return if (toleratedDarkness in 0.0..0.5) LIGHT else DARK
    }

    fun getAccent(context: Context): Int {
        val typedValue = TypedValue()
        val contextThemeWrapper = ContextThemeWrapper(
            context,
            android.R.style.Theme_DeviceDefault_DayNight
        )
        val nightModeFlags: Int = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        contextThemeWrapper.theme.resolveAttribute(
            android.R.attr.colorAccent,
            typedValue, true
        )
        val accentManager =
            FeatureManager(context.contentResolver).AccentManager()
        return when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> if (accentManager.getDark() == "-1") typedValue.data else Color.parseColor(
                "#" + accentManager.getDark()
            )
            Configuration.UI_MODE_NIGHT_NO -> if (accentManager.getLight() == "-1") typedValue.data else Color.parseColor(
                "#" + accentManager.getLight()
            )
            else -> typedValue.data
        }
    }

    fun getAccent(context: Context, config: Int): Int {
        val typedValue = TypedValue()
        var contextThemeWrapper = ContextThemeWrapper(
            context,
            android.R.style.Theme_DeviceDefault_DayNight
        )
        when (config) {
            Configuration.UI_MODE_NIGHT_YES -> {
                contextThemeWrapper = ContextThemeWrapper(
                    context,
                    android.R.style.Theme_DeviceDefault
                )
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                contextThemeWrapper = ContextThemeWrapper(
                    context,
                    android.R.style.Theme_DeviceDefault_Light
                )
            }
        }
        contextThemeWrapper.theme.resolveAttribute(
            android.R.attr.colorAccent,
            typedValue, true
        )
        val accentManager =
            FeatureManager(context.contentResolver).AccentManager()
        return when (config) {
            Configuration.UI_MODE_NIGHT_YES -> if (accentManager.getDark() == "-1") typedValue.data else Color.parseColor(
                "#" + accentManager.getDark()
            )
            Configuration.UI_MODE_NIGHT_NO -> if (accentManager.getLight() == "-1") typedValue.data else Color.parseColor(
                "#" + accentManager.getLight()
            )
            else -> typedValue.data
        }
    }

    @ColorInt
    fun getColorAttrDefaultColor(context: Context, attr: Int): Int {
        val ta = context.obtainStyledAttributes(intArrayOf(attr))
        @ColorInt val colorAccent = ta.getColor(0, 0)
        ta.recycle()
        return colorAccent
    }

    fun getTextColor(context: Context): Int {
        return context.resolveColorAttr(android.R.attr.textColorPrimary)
    }

    fun getInverseTextColor(context: Context): Int {
        return context.resolveColorAttr(android.R.attr.textColorPrimaryInverse)
    }

    fun getSecondaryTextColor(context: Context): Int {
        return context.resolveColorAttr(android.R.attr.textColorSecondary)
    }

    fun getInverseSecondaryTextColor(context: Context): Int {
        return context.resolveColorAttr(android.R.attr.textColorSecondaryInverse)
    }

    fun getCornerRadius(context: Context): Float {
        return context.resolveDimenAttr(android.R.attr.dialogCornerRadius)
    }

    fun getDrawable(context: Context, packageName: String, drawableName: String): Drawable? = try {
        val pm: PackageManager = context.packageManager
        val mApkResources: Resources = pm.getResourcesForApplication(packageName)
        ResourcesCompat.getDrawable(mApkResources, mApkResources.getIdentifier(
            drawableName,
            "drawable",
            packageName
        ), null)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }

    @Dimension
    fun Context.resolveDimenAttr(@AttrRes dimenAttr: Int): Float {
        val resolvedAttr = resolveThemeAttr(dimenAttr)
        val dimenRes =
            if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
        return resources.getDimension(dimenRes)
    }

    @ColorInt
    fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
        val resolvedAttr = resolveThemeAttr(colorAttr)
        val colorRes =
            if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
        return ContextCompat.getColor(this, colorRes)
    }

    fun Context.resolveThemeAttr(@AttrRes attrRes: Int): TypedValue {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue
    }

    fun shouldDisableNightLight(context: Context): Boolean {
        return context.resources.getBoolean(
            Resources.getSystem()
                .getIdentifier("disable_fod_night_light", "bool", "android")
        )
    }

    fun hasFodSupport(context: Context): Boolean {
        return context.resources.getBoolean(
            Resources.getSystem()
                .getIdentifier("config_supportsInDisplayFingerprint", "bool", "android")
        )
    }

    fun getFodAnimationPackage(context: Context): String {
        return context.resources.getString(
            Resources.getSystem()
                .getIdentifier("config_fodAnimationPackage", "string", "android")
        )
    }

    fun isPackageInstalled(context: Context, pkg: String, ignoreState: Boolean): Boolean {
        try {
            val pi = context.packageManager.getPackageInfo(pkg, 0)
            if (!pi.applicationInfo.enabled && !ignoreState) {
                return false
            }
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    fun isPackageInstalled(context: Context, pkg: String): Boolean {
        return isPackageInstalled(context, pkg, true)
    }
}