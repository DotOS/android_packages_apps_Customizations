package com.android.settings.dotextras.custom.sections.wallpaper.colors

import android.app.WallpaperColors
import android.content.Context
import android.net.Uri
import android.provider.Settings
import com.android.settings.dotextras.custom.sections.wallpaper.Wallpaper
import com.dot.ui.utils.ResourceHelper
import com.dot.ui.utils.uriToDrawable
import com.dot.ui.utils.urlToDrawable
import com.kieronquinn.monetcompat.extensions.toArgb
import dev.kdrag0n.monet.colors.CieLab
import dev.kdrag0n.monet.colors.Illuminants
import dev.kdrag0n.monet.colors.Srgb
import dev.kdrag0n.monet.colors.Zcam
import dev.kdrag0n.monet.theme.ZcamDynamicColorScheme
import dev.kdrag0n.monet.theme.ZcamMaterialYouTargets
import kotlin.math.log10
import kotlin.math.pow

class MonetColors(val context: Context, val wallpaper: Wallpaper) {

    private val drawable = if (wallpaper.uri != null) {
        context.uriToDrawable(Uri.parse(wallpaper.uri))
    } else {
        context.urlToDrawable(wallpaper.url!!)
    }
    val colors = WallpaperColors.fromDrawable(drawable)
    private val chroma =
        Settings.Secure.getFloat(context.contentResolver, "monet_chroma", 1.0f).toDouble()
    private val lightness = Settings.Secure.getInt(context.contentResolver, "monet_lightness", 425)
    private val cond = createZcamViewingConditions(parseWhiteLuminanceUser(lightness))

    val wallpaperColors: ZcamDynamicColorScheme
        get() {
            return ZcamDynamicColorScheme(
                ZcamMaterialYouTargets(chroma, true, cond),
                Srgb(colors.primaryColor.toArgb()), chroma, cond)
        }

    val backgroundColor: Int get() {
        return if (ResourceHelper.isNightMode(context)) {
            wallpaperColors.neutral1[900]?.toArgb()!!
        } else {
            wallpaperColors.neutral1[50]?.toArgb()!!
        }
    }

    val backgroundSecondaryColor: Int get() {
        return if (ResourceHelper.isNightMode(context)) {
            wallpaperColors.neutral1[700]?.toArgb()!!
        } else {
            wallpaperColors.neutral1[100]?.toArgb()!!
        }
    }

    val accentColor: Int get() {
        return if (ResourceHelper.isNightMode(context)) {
            wallpaperColors.accent1[100]?.toArgb()!!
        } else {
            wallpaperColors.accent1[500]?.toArgb()!!
        }
    }

    companion object {
        private const val WHITE_LUMINANCE_MIN = 1.0
        private const val WHITE_LUMINANCE_MAX = 10000.0
        private const val WHITE_LUMINANCE_USER_MAX = 1000
        fun parseWhiteLuminanceUser(userValue: Int): Double {
            val userSrc = userValue.toDouble() / WHITE_LUMINANCE_USER_MAX
            val userInv = 1.0 - userSrc
            return (10.0).pow(userInv * log10(WHITE_LUMINANCE_MAX))
                .coerceAtLeast(WHITE_LUMINANCE_MIN)
        }

        fun createZcamViewingConditions(whiteLuminance: Double) = Zcam.ViewingConditions(
            Zcam.ViewingConditions.SURROUND_AVERAGE,
            0.4 * whiteLuminance,
            CieLab(50.0, 0.0, 0.0).toCieXyz().y * whiteLuminance,
            Illuminants.D65 * whiteLuminance, whiteLuminance
        )

        fun wallpaperColors(context: Context, color: Int): ZcamDynamicColorScheme {
            val chroma =
                Settings.Secure.getFloat(context.contentResolver, "monet_chroma", 1.0f).toDouble()
            val lightness = Settings.Secure.getInt(context.contentResolver, "monet_lightness", 425)
            val cond = createZcamViewingConditions(parseWhiteLuminanceUser(lightness))
            return ZcamDynamicColorScheme(
                ZcamMaterialYouTargets(chroma, true, cond),
                Srgb(color), chroma, cond)
        }

        fun backgroundColor(context: Context, color: Int): Int {
            val wallpaperColors = wallpaperColors(context, color)
            return if (ResourceHelper.isNightMode(context)) {
                wallpaperColors.neutral1[900]?.toArgb()!!
            } else {
                wallpaperColors.neutral1[50]?.toArgb()!!
            }
        }

        fun backgroundSecondaryColor(context: Context, color: Int): Int  {
            val wallpaperColors = wallpaperColors(context, color)
            return if (ResourceHelper.isNightMode(context)) {
                wallpaperColors.neutral1[700]?.toArgb()!!
            } else {
                wallpaperColors.neutral1[100]?.toArgb()!!
            }
        }

        fun accentColor(context: Context, color: Int): Int  {
            val wallpaperColors = wallpaperColors(context, color)
            return if (ResourceHelper.isNightMode(context)) {
                wallpaperColors.accent1[100]?.toArgb()!!
            } else {
                wallpaperColors.accent1[500]?.toArgb()!!
            }
        }
    }

}