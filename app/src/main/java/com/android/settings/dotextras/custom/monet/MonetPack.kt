package com.android.settings.dotextras.custom.monet

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import com.kieronquinn.monetcompat.extensions.toArgb
import dev.kdrag0n.monet.colors.CieLab
import dev.kdrag0n.monet.colors.Illuminants
import dev.kdrag0n.monet.colors.Srgb
import dev.kdrag0n.monet.colors.Zcam
import dev.kdrag0n.monet.theme.ZcamDynamicColorScheme
import dev.kdrag0n.monet.theme.ZcamMaterialYouTargets
import kotlin.math.log10
import kotlin.math.pow

class MonetPack(val context: Context, val wallpaperColor: Int) {

    private val colorScheme: ZcamDynamicColorScheme
    var selected = false

    init {
        val chromaMultiplier =
            Settings.Secure.getFloat(context.contentResolver, "monet_chroma", 1.0f).toDouble()
        val lightness =
            Settings.Secure.getInt(context.contentResolver, "monet_lightness", 425)
        val cond = createZcamViewingConditions(parseWhiteLuminanceUser(lightness))
        colorScheme = ZcamDynamicColorScheme(
            ZcamMaterialYouTargets(chromaMultiplier, true, cond),
            Srgb(wallpaperColor),
            chromaMultiplier, cond
        )
    }

    private fun parseWhiteLuminanceUser(userValue: Int): Double {
        val userSrc = userValue.toDouble() / WHITE_LUMINANCE_USER_MAX
        val userInv = 1.0 - userSrc
        return (10.0).pow(userInv * log10(WHITE_LUMINANCE_MAX))
            .coerceAtLeast(WHITE_LUMINANCE_MIN)
    }

    private fun createZcamViewingConditions(whiteLuminance: Double) = Zcam.ViewingConditions(
        F_s = Zcam.ViewingConditions.SURROUND_AVERAGE,
        // sRGB
        L_a = 0.4 * whiteLuminance,
        // Gray world
        Y_b = CieLab(
            L = 50.0,
            a = 0.0,
            b = 0.0,
        ).toCieXyz().y * whiteLuminance,
        referenceWhite = Illuminants.D65 * whiteLuminance,
        whiteLuminance = whiteLuminance,
    )

    fun getAccentPrimaryColor(): Int? {
        return if (context.isDarkMode) {
            colorScheme.accent1[100]?.toArgb()
        } else {
            colorScheme.accent1[500]?.toArgb()
        }
    }

    fun getAccentSecondaryColor(): Int? {
        return if (context.isDarkMode) {
            colorScheme.accent2[100]?.toArgb()
        } else {
            colorScheme.accent2[500]?.toArgb()
        }
    }

    fun getAccentTertiaryColor(): Int? {
        return if (context.isDarkMode) {
            colorScheme.accent2[100]?.toArgb()
        } else {
            colorScheme.accent2[500]?.toArgb()
        }
    }

    fun getBackgroundColor(): Int? {
        return if (context.isDarkMode) {
            colorScheme.neutral1[900]?.toArgb()
        } else {
            colorScheme.neutral1[50]?.toArgb()
        }
    }

    private val Context.isDarkMode: Boolean
        get() {
            return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> true
                Configuration.UI_MODE_NIGHT_NO -> false
                Configuration.UI_MODE_NIGHT_UNDEFINED -> false
                else -> false
            }
        }

    companion object {
        private const val WHITE_LUMINANCE_MIN = 1.0
        private const val WHITE_LUMINANCE_MAX = 10000.0
        private const val WHITE_LUMINANCE_USER_MAX = 1000
    }

}