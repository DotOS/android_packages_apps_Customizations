package com.dot.customizations.model.color

import android.app.WallpaperColors
import android.util.SparseIntArray
import com.dot.customizations.monet.ColorScheme

class WallpaperColorResources(wallpaperColors: WallpaperColors?) {
    var mColorOverlay = SparseIntArray()
    fun addOverlayColor(list: List<Int>, i: Int) {
        var i = i
        for (num in list) {
            mColorOverlay.put(i, num)
            i++
        }
    }

    init {
        val colorScheme = ColorScheme(wallpaperColors!!, false)
        addOverlayColor(colorScheme.neutral1, android.R.color.system_neutral1_10)
        addOverlayColor(colorScheme.neutral2, android.R.color.system_neutral2_10)
        addOverlayColor(colorScheme.accent1, android.R.color.system_accent1_10)
        addOverlayColor(colorScheme.accent2, android.R.color.system_accent2_10)
        addOverlayColor(colorScheme.accent3, android.R.color.system_accent3_10)
    }
}