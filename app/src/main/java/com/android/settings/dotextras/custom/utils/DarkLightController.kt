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

import android.annotation.ColorInt
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat

@ColorInt
fun Context.getNormalizedColor(reference: Drawable, isSelected: Boolean): Int {
    val nightModeFlags: Int = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    var normalizedTextColor: Int =
        if (ResourceHelper.isDark((reference as ColorDrawable).color)) ResourceHelper.getTextColor(
            this
        ) else ResourceHelper.getInverseTextColor(this)
    when (nightModeFlags) {
        Configuration.UI_MODE_NIGHT_YES -> normalizedTextColor =
            if (ResourceHelper.isDark(reference.color)) ResourceHelper.getTextColor(
                this
            ) else ResourceHelper.getInverseTextColor(this)
        Configuration.UI_MODE_NIGHT_NO -> normalizedTextColor = (if (isSelected) {
            if (ResourceHelper.isDark((reference).color)) ResourceHelper.getInverseTextColor(
                this
            ) else ResourceHelper.getTextColor(this)
        } else ResourceHelper.getTextColor(this))
    }
    return normalizedTextColor
}

@ColorInt
fun Context.getNormalizedColor(reference: Drawable): Int {
    val nightModeFlags: Int = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    var normalizedTextColor: Int =
        if (ResourceHelper.isDark((reference as ColorDrawable).color)) ResourceHelper.getTextColor(
            this
        ) else ResourceHelper.getInverseTextColor(this)
    when (nightModeFlags) {
        Configuration.UI_MODE_NIGHT_YES -> normalizedTextColor =
            if (ResourceHelper.isDark(reference.color)) ResourceHelper.getTextColor(
                this
            ) else ResourceHelper.getInverseTextColor(this)
        Configuration.UI_MODE_NIGHT_NO -> normalizedTextColor = ResourceHelper.getTextColor(this)
    }
    return normalizedTextColor
}

@ColorInt
fun Context.getNormalizedColor(reference: Int): Int {
    val nightModeFlags: Int = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    var normalizedTextColor: Int =
        if (ResourceHelper.isDark(reference)) ResourceHelper.getTextColor(this)
        else ResourceHelper.getInverseTextColor(this)
    when (nightModeFlags) {
        Configuration.UI_MODE_NIGHT_YES -> normalizedTextColor =
            if (ResourceHelper.isDark(reference)) ResourceHelper.getTextColor(this)
            else ResourceHelper.getInverseTextColor(this)
        Configuration.UI_MODE_NIGHT_NO -> normalizedTextColor =
            ResourceHelper.getTextColor(this)
    }
    return normalizedTextColor
}

@ColorInt
fun Context.getNormalizedSecondaryColor(reference: Int): Int {
    val nightModeFlags: Int = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK
    var normalizedTextColor: Int =
        if (ResourceHelper.isDark(reference)) ResourceHelper.getSecondaryTextColor(this)
        else ResourceHelper.getInverseSecondaryTextColor(this)
    when (nightModeFlags) {
        Configuration.UI_MODE_NIGHT_YES -> normalizedTextColor =
            if (ResourceHelper.isDark(reference)) ResourceHelper.getSecondaryTextColor(this)
            else ResourceHelper.getInverseSecondaryTextColor(this)
        Configuration.UI_MODE_NIGHT_NO -> normalizedTextColor =
            ResourceHelper.getSecondaryTextColor(this)
    }
    return normalizedTextColor
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@ColorInt
internal fun resolveColor(context: Context, @ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(context, colorRes)
}

@ColorInt
internal fun resolveColorAttr(
    context: Context,
    @AttrRes attrRes: Int,
): Int {
    val a = context.theme.obtainStyledAttributes(intArrayOf(attrRes))
    return a.getColor(0, 0)
}

fun Int.isColorDark(threshold: Double = 0.5): Boolean {
    if (this == Color.TRANSPARENT) {
        return false
    }
    val darkness =
        1 - (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255
    return darkness >= threshold
}