package com.dot.customizations.model.color

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.SystemProperties
import android.util.Log
import androidx.annotation.ColorInt
import kotlin.jvm.internal.Intrinsics

fun IntArray.toColorInt(): Int {
    return Color.rgb(this[0], this[1], this[2])
}

object ColorUtils {
    var sFlagId = 0
    var sSysuiRes: Resources? = null
    fun isMonetEnabled(context: Context): Boolean {
        var systemUIFlag = 0
        val isMonetPropEnabled = SystemProperties.getBoolean("persist.systemui.flag_monet", false)
        if (isMonetPropEnabled) {
            return true
        }
        if (sSysuiRes == null) {
            try {
                val packageManager = context.packageManager
                val applicationInfo = packageManager.getApplicationInfo(
                    "com.android.systemui",
                    PackageManager.GET_META_DATA
                )
                sSysuiRes = packageManager.getResourcesForApplication(applicationInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.w("ColorUtils", "Couldn't read color flag, skipping section", e)
            }
        }
        if (sFlagId == 0) {
            val resources = sSysuiRes
            if (resources != null) {
                systemUIFlag = resources.getIdentifier("flag_monet", "bool", "com.android.systemui")
            }
            sFlagId = systemUIFlag
        }
        if (sFlagId <= 0) {
            return false
        }
        val resources2 = sSysuiRes
        Intrinsics.checkNotNull(resources2)
        return resources2!!.getBoolean(sFlagId)
    }

    fun toColorString(@ColorInt color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }
}