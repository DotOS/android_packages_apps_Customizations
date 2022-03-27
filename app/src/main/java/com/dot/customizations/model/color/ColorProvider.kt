package com.dot.customizations.model.color

import android.app.WallpaperColors
import android.content.Context
import android.content.res.ColorStateList
import androidx.core.graphics.ColorUtils
import com.dot.customizations.compat.WallpaperManagerCompat
import com.dot.customizations.model.ResourcesApkProvider
import com.dot.customizations.model.color.ColorUtils.toColorString
import com.dot.customizations.module.InjectorProvider
import com.dot.customizations.monet.ColorScheme
import kotlinx.coroutines.CoroutineScope
import java.util.*

class ColorProvider(context: Context, stubPackageName: String) :
    ResourcesApkProvider(context, stubPackageName), ColorOptionsProvider {
    var colorBundles: List<ColorOption>? = null
    var homeWallpaperColors: WallpaperColors? = null
    var lockWallpaperColors: WallpaperColors? = null
    val scope: CoroutineScope? = null
    fun buildBundle(i: Int, i2: Int, z: Boolean, str: String?, list: ArrayList<ColorSeedOption>) {

        val hashMap: HashMap<String, String> = HashMap()
        val colorScheme = ColorScheme(i, false)
        val colorScheme2 = ColorScheme(i, true)
        val iArr = intArrayOf(
            ColorUtils.setAlphaComponent(colorScheme.accent1[2], 255), ColorUtils.setAlphaComponent(
                colorScheme.accent1[2], 255
            ), ColorStateList.valueOf(
                colorScheme.accent3[6]
            ).withLStar(85.0f).getColors().get(0), ColorUtils.setAlphaComponent(
                colorScheme.accent1[6], 255
            )
        )
        val iArr2 = intArrayOf(
            ColorUtils.setAlphaComponent(colorScheme2.accent1[2], 255),
            ColorUtils.setAlphaComponent(
                colorScheme2.accent1[2], 255
            ),
            ColorStateList.valueOf(
                colorScheme2.accent3[6]
            ).withLStar(85.0f).getColors().get(0),
            ColorUtils.setAlphaComponent(
                colorScheme2.accent1[6], 255
            )
        )
        var str3 = ""
        val str2 = if (z) {
            str3
        } else {
            toColorString(i)
        }
        hashMap["android.theme.customization.system_palette"] = str2
        if (!z) {
            str3 = toColorString(i)
        }
        hashMap["android.theme.customization.accent_color"] = str3
        list.add(
            ColorSeedOption(
                str,
                hashMap,
                z,
                str,
                1 + i2,
                ColorSeedOption.PreviewInfo(iArr, iArr2)
            )
        )
    }

    fun buildColorSeeds(
        wallpaperColors: WallpaperColors,
        i: Int,
        str: String?,
        z: Boolean,
        list: ArrayList<ColorSeedOption>
    ) {
        val list2: List<Number>
        val list3: List<Number>
        val seedColors: List<Int> = ColorScheme.getSeedColors(wallpaperColors)
        buildBundle(
            (seedColors[0] as Number).toInt(),
            0,
            z,
            str,
            list
        )
        val size = seedColors.size - 1
        list2 = if (size <= 0) {
            ArrayList()
        } else if (size != 1) {
            val arrayList: ArrayList<Int> = ArrayList(size)
            if (seedColors is RandomAccess) {
                val size2 = seedColors.size
                for (i2 in 1 until size2) {
                    arrayList.add(seedColors[i2])
                }
            } else {
                val listIterator = seedColors.listIterator(1)
                while (listIterator.hasNext()) {
                    arrayList.add(listIterator.next())
                }
            }
            arrayList
        } else if (seedColors.isNotEmpty()) {
            listOf(seedColors[seedColors.size - 1])
        } else {
            throw NoSuchElementException("List is empty.")
        }
        val i3 = i - 1
        var i4 = 0
        if (i3 >= 0) {
            list3 = if (i3 == 0) {
                ArrayList()
            } else (if (i3 >= list2.size) {
                list2
            } else if (i3 == 1) {
                listOf(list2[0])
            } else {
                val arrayList2: ArrayList<Number> = ArrayList(i3)
                var i5 = 0
                for (obj in list2) {
                    arrayList2.add(obj)
                    i5++
                    if (i5 == i3) {
                        break
                    }
                }
                arrayList2
            })
            for (number in list3) {
                i4++
                buildBundle(number.toInt(), i4, false, str, list)
            }
            return
        }
        throw IllegalArgumentException("Requested element count $i3 is less than zero.")
    }

    companion object {
        fun getItemColorFromStub(
            colorProvider: ColorProvider,
            str: String?,
            str2: String?
        ): Int {
            return colorProvider.mStubApkResources.getColor(
                colorProvider.mStubApkResources.getIdentifier(
                    String.format("%s%s", str, str2), "color", colorProvider.mStubPackageName
                ), null
            )
        }

        fun loadSeedColors(
            colorProvider: ColorProvider,
            wallpaperColors: WallpaperColors?,
            wallpaperColors2: WallpaperColors?
        ) {
            var emptyList: ArrayList<ColorSeedOption>?
            Objects.requireNonNull(colorProvider)
            if (wallpaperColors != null) {
                val arrayList = ArrayList<ColorSeedOption>()
                val i = if (wallpaperColors2 == null) 4 else 2
                if (wallpaperColors2 != null) {
                    val wallpaperManagerCompat: WallpaperManagerCompat =
                        InjectorProvider.getInjector()
                            .getWallpaperManagerCompat(colorProvider.mContext)
                    var z = true
                    if (wallpaperManagerCompat.getWallpaperId(WallpaperManagerCompat.FLAG_LOCK) <= wallpaperManagerCompat.getWallpaperId(
                            WallpaperManagerCompat.FLAG_SYSTEM
                        )
                    ) {
                        z = false
                    }
                    colorProvider.buildColorSeeds(
                        if (z) wallpaperColors2 else wallpaperColors,
                        i,
                        if (z) "lock_wallpaper" else "home_wallpaper",
                        true,
                        arrayList
                    )
                    colorProvider.buildColorSeeds(
                        if (z) wallpaperColors else wallpaperColors2,
                        i,
                        if (z) "home_wallpaper" else "lock_wallpaper",
                        false,
                        arrayList
                    )
                } else {
                    colorProvider.buildColorSeeds(
                        wallpaperColors,
                        i,
                        "home_wallpaper",
                        true,
                        arrayList
                    )
                }
                val list = colorProvider.colorBundles
                emptyList = if (list == null) {
                    null
                } else {
                    val arrayList2 = ArrayList<ColorSeedOption>()
                    for (t in list) {
                        if (t !is ColorSeedOption) {
                            arrayList2.add(t as ColorSeedOption)
                        }
                    }
                    arrayList2
                }
                if (emptyList == null) {
                    emptyList = ArrayList()
                }
                arrayList.addAll(emptyList)
                colorProvider.colorBundles = arrayList
            }
        }
    }

}