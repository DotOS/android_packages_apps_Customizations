package com.android.settings.dotextras.custom.monet

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import com.kieronquinn.monetcompat.extensions.toArgb
import dev.kdrag0n.monet.colors.Srgb
import dev.kdrag0n.monet.theme.DynamicColorScheme
import dev.kdrag0n.monet.theme.TargetColors

class MonetPack(val context: Context, val wallpaperColor: Int) {

    private val colorScheme: DynamicColorScheme
    var selected = false

    init {
        val chromaMultiplier =
            Settings.Secure.getFloat(context.contentResolver, "monet_chroma").toDouble()
        colorScheme = DynamicColorScheme(
            TargetColors(chromaMultiplier),
            Srgb(wallpaperColor),
            chromaMultiplier
        )
    }

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

}