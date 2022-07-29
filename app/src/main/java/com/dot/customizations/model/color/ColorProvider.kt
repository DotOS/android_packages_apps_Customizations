package com.dot.customizations.model.color

import android.app.WallpaperColors
import android.content.Context
import android.content.res.ColorStateList
import android.provider.Settings
import android.util.Log
import androidx.core.graphics.ColorUtils
import com.android.internal.graphics.cam.Cam
import com.dot.customizations.compat.WallpaperManagerCompat
import com.dot.customizations.model.ResourcesApkProvider
import com.dot.customizations.model.color.ColorUtils.toColorString
import com.dot.customizations.module.InjectorProvider
import com.dot.customizations.monet.ACCENT3_HUE_SHIFT
import com.dot.customizations.monet.ColorScheme
import dev.kdrag0n.colorkt.cam.Zcam
import dev.kdrag0n.colorkt.cam.Zcam.Companion.toZcam
import dev.kdrag0n.colorkt.data.Illuminants
import dev.kdrag0n.colorkt.gamut.LchGamut.clipToLinearSrgb
import dev.kdrag0n.colorkt.rgb.LinearSrgb.Companion.toLinear
import dev.kdrag0n.colorkt.rgb.Srgb
import dev.kdrag0n.colorkt.tristimulus.CieXyz.Companion.toXyz
import dev.kdrag0n.colorkt.tristimulus.CieXyzAbs.Companion.toAbs
import dev.kdrag0n.colorkt.ucs.lab.CieLab
import dev.kdrag0n.colorkt.ucs.lab.Oklab.Companion.toOklab
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class ColorProvider(context: Context, stubPackageName: String) :
    ResourcesApkProvider(context, stubPackageName), ColorOptionsProvider {
    var colorBundles: List<ColorOption>? = null
    var customColorBundles: List<ColorOption>? = null
    var homeWallpaperColors: WallpaperColors? = null
    var lockWallpaperColors: WallpaperColors? = null
    private fun buildBundle(
        seed: Int,
        index: Int,
        mIsDefault: Boolean,
        source: String?,
        list: ArrayList<ColorOption>
    ) {

        val hashMap: HashMap<String?, String?> = HashMap()
        val colorScheme = ColorScheme(seed, false)
        val colorSchemeDark = ColorScheme(seed, true)
        val secondaryColorLight = intArrayOf(
            ColorUtils.setAlphaComponent(colorScheme.accent1[2], 255), ColorUtils.setAlphaComponent(
                colorScheme.accent1[2], 255
            ), ColorStateList.valueOf(
                colorScheme.accent3[6]
            ).withLStar(85.0f).colors[0], ColorUtils.setAlphaComponent(
                colorScheme.accent1[6], 255
            )
        )
        val secondaryColorDark = intArrayOf(
            ColorUtils.setAlphaComponent(colorSchemeDark.accent1[2], 255),
            ColorUtils.setAlphaComponent(
                colorSchemeDark.accent1[2], 255
            ),
            ColorStateList.valueOf(
                colorSchemeDark.accent3[6]
            ).withLStar(85.0f).colors[0],
            ColorUtils.setAlphaComponent(
                colorSchemeDark.accent1[6], 255
            )
        )
        var source3 = ""
        val source2 = if (mIsDefault) {
            source3
        } else {
            toColorString(seed)
        }
        hashMap["android.theme.customization.system_palette"] = source2
        if (!mIsDefault) {
            source3 = toColorString(seed)
        }
        hashMap["android.theme.customization.accent_color"] = source3
        list.add(
            ColorSeedOption(
                source,
                hashMap,
                mIsDefault,
                source!!,
                1 + index,
                ColorSeedOption.PreviewInfo(secondaryColorLight, secondaryColorDark)
            )
        )
    }

    private fun loadPreset() {
        val bundlesList = ArrayList<String>()
        val bundleNames = mStubApkResources.getStringArray(
            mStubApkResources.getIdentifier(
                "color_bundles",
                "array",
                mStubPackageName
            )
        )
        for (i in bundleNames.indices) {
            if (i == 4) break
            bundlesList.add(bundleNames[i])
        }
        val colorPresetBundles = ArrayList<ColorOption>()
        var position = 1;
        for (bundle in bundlesList) {
            val hashMap: HashMap<String?, String?> = HashMap()
            val bundleName = getItemStringFromStub("bundle_name_", bundle)
            val bundleColorPrimary = getItemColorFromStub("color_primary_", bundle)
            val bundleColorSecondary = getItemColorFromStub("color_secondary_", bundle)
            hashMap["android.theme.customization.system_palette"] =
                toColorString(bundleColorSecondary)
            hashMap["android.theme.customization.accent_color"] = toColorString(bundleColorPrimary)
            val accentColor = ColorScheme(bundleColorPrimary, false).accentColor
            val accentColor2 = ColorScheme(bundleColorPrimary, true).accentColor
            colorPresetBundles.add(
                ColorBundle(
                    bundleName,
                    hashMap,
                    false,
                    index = position,
                    mPreviewInfo = ColorBundle.PreviewInfo(
                        accentColor,
                        accentColor2
                    )
                )
            )
            position++
        }
        this.colorBundles = colorPresetBundles
    }

    fun buildColorSeeds(
        wallpaperColors: WallpaperColors,
        count: Int,
        source: String?,
        isDefault: Boolean,
        list: ArrayList<ColorOption>
    ) {
        val list2: List<Int>
        val list3: List<Int>
        val seedColors: List<Int> =
            if (Settings.Secure.getInt(mContext.contentResolver, "monet_engine_custom_color", 0) == 1) {
                getCustomSeeds()
            } else
                ColorScheme.getSeedColors(wallpaperColors)
        loadPreset()
        buildBundle(
            seedColors[0],
            0,
            isDefault,
            source,
            list
        )
        val size = seedColors.size - 1
        list2 = if (size <= 0) {
            ArrayList()
        } else if (size != 1) {
            val arrayList: ArrayList<Int> = ArrayList(size)
            val listIterator = seedColors.listIterator(1)
            while (listIterator.hasNext()) {
                arrayList.add(listIterator.next())
            }
            arrayList
        } else if (seedColors.isNotEmpty()) {
            listOf(seedColors[seedColors.size - 1])
        } else {
            throw NoSuchElementException("List is empty.")
        }
        val i3 = count - 1
        var index4 = 0
        if (i3 >= 0) {
            list3 = if (i3 == 0) {
                ArrayList()
            } else (if (i3 >= list2.size) {
                list2
            } else if (i3 == 1) {
                listOf(list2[0])
            } else {
                val arrayList2: ArrayList<Int> = ArrayList(i3)
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
            for (seed in list3) {
                index4++
                buildBundle(seed, index4, false, source, list)
            }
            return
        }
        throw IllegalArgumentException("Requested element count $i3 is less than zero.")
    }

    fun buildCustomColorSeeds(
        wallpaperColors: WallpaperColors,
        list: ArrayList<ColorOption>
    ) {
        val seedColors: ArrayList<Int> = ColorScheme.getSeedColors(wallpaperColors) as ArrayList<Int>
        val hueShifts = arrayListOf(
            -90f,
            -60f,
            -30f,
            20f,
            40f,
            60f,
            75f,
            90f,
            120f,
            160f,
            180f,
            200f,
            220f,
            -140f,
            240f,
            -100f
        )
        val tempList = ArrayList<Int>()
        for (j in 0 until hueShifts.size) {
            tempList.add(styleSeed(seedColors[0], 200.0, hueShifts[j]))
        }
        seedColors.clear()
        seedColors.addAll(tempList)
        for (i in seedColors.indices) {
            buildBundle(seedColors[i], i, false, toColorString(seedColors[i]), list)
        }
    }

    private fun styleSeed(seed: Int, luminance: Double = 200.0, hue: Float = 60f): Int {
        val cond = Zcam.ViewingConditions(
            surroundFactor = Zcam.ViewingConditions.SURROUND_AVERAGE,
            adaptingLuminance = 0.4 * luminance,
            backgroundLuminance = CieLab(50.0, 0.0, 0.0).toXyz().y * luminance,
            referenceWhite = Illuminants.D65.toAbs(luminance),
        )
        val src = Srgb(toColorString(seed))
        val zcam = src.toLinear().toXyz().toAbs(luminance).toZcam(cond)
        val colorful = zcam.copy(hue = wrapDegrees((zcam.hue + hue).roundToInt()).toDouble())
        return colorful.clipToLinearSrgb().toSrgb().toRgb8()
    }

    private fun wrapDegrees(degrees: Int): Int {
        return when {
            degrees < 0 -> {
                (degrees % 360) + 360
            }
            degrees >= 360 -> {
                degrees % 360
            }
            else -> {
                degrees
            }
        }
    }

    /**
     * @param limitedList is used to show max 4 color options on the main page
     */
    fun getCustomSeeds(limitedList: Boolean = true): List<Int> {
        val seeds = ArrayList<Int>()
        val prefs = mContext.getSharedPreferences("monet_custom_seeds", Context.MODE_PRIVATE)
        val colorOverride = Settings.Secure.getInt(mContext.contentResolver,
            "monet_engine_color_override", -1)
        prefs.edit().putInt("color_seed_$colorOverride", colorOverride).apply()
        if (prefs != null && prefs.all.isNotEmpty()) {
            for (seed in prefs.all) {
                if (limitedList && seeds.size == 4) break
                if (seed.key.contains("color_seed_")) {
                    seeds.add(seed.value as Int)
                }
            }
        }
        return seeds
    }

    companion object {

        fun loadSeedColors(
            colorProvider: ColorProvider,
            homeColors: WallpaperColors?,
            lockColors: WallpaperColors?
        ) {
            val arrayList = ArrayList<ColorOption>()
            if (homeColors != null) {
                val count = if (lockColors == null) 4 else 2
                if (lockColors != null) {
                    val wallpaperManagerCompat: WallpaperManagerCompat =
                        InjectorProvider.getInjector()
                            .getWallpaperManagerCompat(colorProvider.mContext)
                    var isDefault = true
                    if (wallpaperManagerCompat.getWallpaperId(WallpaperManagerCompat.FLAG_LOCK) <= wallpaperManagerCompat.getWallpaperId(
                            WallpaperManagerCompat.FLAG_SYSTEM
                        )
                    ) {
                        isDefault = false
                    }
                    colorProvider.buildColorSeeds(
                        if (isDefault) lockColors else homeColors,
                        count,
                        if (isDefault) "lock_wallpaper" else "home_wallpaper",
                        true,
                        arrayList
                    )
                    colorProvider.buildColorSeeds(
                        if (isDefault) homeColors else lockColors,
                        count,
                        if (isDefault) "home_wallpaper" else "lock_wallpaper",
                        false,
                        arrayList
                    )
                } else {
                    colorProvider.buildColorSeeds(
                        homeColors,
                        count,
                        "home_wallpaper",
                        true,
                        arrayList
                    )
                }
                val list = colorProvider.colorBundles
                val arrayList2 = ArrayList<ColorOption>()
                if (list != null) {
                    for (t in list) {
                        arrayList2.add(t)
                    }
                }
                arrayList.addAll(arrayList2)
                colorProvider.colorBundles = arrayList
                loadCustomSeedColors(colorProvider, homeColors, lockColors)
            }
        }

        private fun loadCustomSeedColors(
            colorProvider: ColorProvider,
            homeColors: WallpaperColors?,
            lockColors: WallpaperColors?
        ) {
            val arrayList = ArrayList<ColorOption>()
            if (homeColors != null) {
                if (lockColors != null) {
                    val wallpaperManagerCompat: WallpaperManagerCompat =
                        InjectorProvider.getInjector()
                            .getWallpaperManagerCompat(colorProvider.mContext)
                    var isDefault = true
                    if (wallpaperManagerCompat.getWallpaperId(WallpaperManagerCompat.FLAG_LOCK) <= wallpaperManagerCompat.getWallpaperId(
                            WallpaperManagerCompat.FLAG_SYSTEM
                        )
                    ) {
                        isDefault = false
                    }
                    colorProvider.buildCustomColorSeeds(
                        if (isDefault) lockColors else homeColors,
                        arrayList
                    )
                } else {
                    colorProvider.buildCustomColorSeeds(
                        homeColors,
                        arrayList
                    )
                }
                colorProvider.customColorBundles = arrayList
            }
        }
    }

}