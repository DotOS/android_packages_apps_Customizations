/**
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dot.customizations.model.color

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import androidx.annotation.ColorInt
import com.dot.customizations.model.ResourceConstants.*
import com.dot.customizations.model.color.ColorUtils.toColorString
import com.dot.customizations.monet.ColorScheme
import com.dot.customizations.monet.Style

/**
 * Utility class to read all the details of a color bundle for previewing it
 * (eg, actual color values)
 */
internal class ColorBundlePreviewExtractor(context: Context) {
    private val mPackageManager: PackageManager

    init {
        mPackageManager = context.packageManager
    }

    fun addSecondaryColor(builder: ColorBundle.Builder, @ColorInt color: Int) {
        val darkColorScheme = ColorScheme(color, true)
        val lightColorScheme = ColorScheme(color, false)
        val lightSecondary: Int = lightColorScheme.accentColor
        val darkSecondary: Int = darkColorScheme.accentColor
        builder.addOverlayPackage(OVERLAY_CATEGORY_COLOR, toColorString(color))
            .setColorSecondaryLight(lightSecondary)
            .setColorSecondaryDark(darkSecondary)
    }

    fun addPrimaryColor(builder: ColorBundle.Builder, @ColorInt color: Int) {
        val darkColorScheme = ColorScheme(color, true)
        val lightColorScheme = ColorScheme(color, false)
        val lightPrimary: Int = lightColorScheme.accentColor
        val darkPrimary: Int = darkColorScheme.accentColor
        builder.addOverlayPackage(OVERLAY_CATEGORY_SYSTEM_PALETTE, toColorString(color))
            .setColorPrimaryLight(lightPrimary)
            .setColorPrimaryDark(darkPrimary)
    }

    fun addColorStyle(builder: ColorBundle.Builder, styleName: String) {
        var s: Style = Style.TONAL_SPOT
        if (!TextUtils.isEmpty(styleName)) {
            try {
                s = Style.valueOf(styleName)
            } catch (e: IllegalArgumentException) {
                Log.i(TAG, "Unknown style : $styleName. Will default to TONAL_SPOT.")
            }
        }
        builder.setStyle(s)
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun addAndroidIconOverlay(builder: ColorBundle.Builder) {
        addSystemDefaultIcons(builder, *ICONS_FOR_PREVIEW)
    }

    fun addSystemDefaultIcons(builder: ColorBundle.Builder, vararg previewIcons: String?) {
        try {
            for (iconName in previewIcons) {
                builder.addIcon(loadIconPreviewDrawable(iconName))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Didn't find android package icons, will skip preview", e)
        } catch (e: Resources.NotFoundException) {
            Log.w(TAG, "Didn't find android package icons, will skip preview", e)
        }
    }

    @Throws(PackageManager.NameNotFoundException::class, Resources.NotFoundException::class)
    fun loadIconPreviewDrawable(drawableName: String?): Drawable {
        val packageRes: Resources = mPackageManager.getResourcesForApplication(ANDROID_PACKAGE)
        val res = Resources.getSystem()
        return res.getDrawable(
            packageRes.getIdentifier(
                drawableName, "drawable",
                ANDROID_PACKAGE
            ), null
        )
    }

    companion object {
        private const val TAG = "ColorBundlePreviewExtractor"
    }
}