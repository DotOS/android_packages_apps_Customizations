/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.dot.customizations.model.color

import android.app.WallpaperColors
import android.content.Context
import android.widget.RemoteViews.ColorResources;
import android.util.SparseIntArray
import com.dot.customizations.monet.ColorScheme

/** A class to override colors in a [Context] with wallpaper colors.  */
class WallpaperColorResources(wallpaperColors: WallpaperColors) {
    val mColorOverlay = SparseIntArray()

    init {
        val wallpaperColorScheme = ColorScheme(wallpaperColors,  /* darkTheme= */false)
        addOverlayColor(wallpaperColorScheme.neutral1, android.R.color.system_neutral1_10)
        addOverlayColor(wallpaperColorScheme.neutral2, android.R.color.system_neutral2_10)
        addOverlayColor(wallpaperColorScheme.accent1, android.R.color.system_accent1_10)
        addOverlayColor(wallpaperColorScheme.accent2, android.R.color.system_accent2_10)
        addOverlayColor(wallpaperColorScheme.accent3, android.R.color.system_accent3_10)
    }

    /** Applies the wallpaper color resources to the `context`.  */
    fun apply(context: Context?) {
        ColorResources.create(context, mColorOverlay).apply(context)
    }

    private fun addOverlayColor(colors: List<Int>, firstResourceColorId: Int) {
        var resourceColorId = firstResourceColorId
        for (color in colors) {
            mColorOverlay.put(resourceColorId, color)
            resourceColorId++
        }
    }
}