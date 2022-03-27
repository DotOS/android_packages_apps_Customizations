package com.dot.customizations.model.color

import android.app.WallpaperColors
import android.util.SparseIntArray
import com.android.internal.graphics.cam.Cam
import com.dot.customizations.monet.ColorScheme.Companion.getSeedColors
import com.dot.customizations.monet.Shades

class WallpaperColorResources(wallpaperColors: WallpaperColors?) {
    val mColorOverlay = SparseIntArray()
    fun addOverlayColor(list: List<IntArray>, i: Int) {
        var i = i
        for (num in list) {
            mColorOverlay.put(i, num.toColorInt())
            i++
        }
    }

    init {
        val intValue = (getSeedColors(wallpaperColors!!)[0] as Number).toInt()
        val fromInt = Cam.fromInt(if (intValue == 0) -14979341 else intValue)
        val hue = fromInt.hue
        val chroma = fromInt.chroma
        val list: List<IntArray> =
            arrayListOf(Shades.of(hue, if (chroma < 48.0f) 48.0f else chroma))
        val list2: List<IntArray> = arrayListOf(Shades.of(hue, 16.0f))
        val list3: List<IntArray> = arrayListOf(Shades.of(60.0f + hue, 32.0f))
        val list4: List<IntArray> = arrayListOf(Shades.of(hue, 4.0f))
        val list5: List<IntArray> = arrayListOf(Shades.of(hue, 8.0f))
        addOverlayColor(list4, 17170462)
        addOverlayColor(list5, 17170475)
        addOverlayColor(list, 17170488)
        addOverlayColor(list2, 17170501)
        addOverlayColor(list3, 17170514)
    }
}