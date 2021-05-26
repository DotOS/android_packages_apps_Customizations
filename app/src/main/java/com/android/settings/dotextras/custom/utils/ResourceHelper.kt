/*
 * Copyright (C) 2020 The dotOS Project
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
package com.android.settings.dotextras.custom.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.SystemProperties
import android.util.TypedValue
import android.view.KeyCharacterMap
import android.view.KeyEvent
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.android.internal.os.RoSystemProperties
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.DeviceKeysConstants.*
import com.android.settings.dotextras.custom.views.TwoToneAccentView.Shade
import com.android.settings.dotextras.custom.views.TwoToneAccentView.Shade.DARK
import com.android.settings.dotextras.custom.views.TwoToneAccentView.Shade.LIGHT
import com.android.settings.dotextras.system.FeatureManager
import kotlin.math.roundToInt


object ResourceHelper {

    fun colorToHex(@ColorInt color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

    fun isDark(color: Int): Boolean {
        val darkness: Double =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    /**
     * 0.0-0.5 = light
     * 0.5 = neutral
     * 0.5-1.0 = dark
     */
    fun getDarkness(color: Int): Double =
        1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255

    fun getLightCousinColor(darkColor: Int): Int {
        val darkness = getDarkness(darkColor)
        val light = darkness + (1 - darkness) / 1.5
        return getColorByDarkness(darkColor, light)
    }

    fun getDarkCousinColor(lightColor: Int): Int {
        val lightness = getDarkness(lightColor)
        val dark = lightness + (1 - lightness) * 1.5
        return getColorByDarkness(lightColor, dark)
    }

    private fun getColorByDarkness(initialColor: Int, darkness: Double): Int {
        val r = (Color.red(initialColor) * darkness).roundToInt()
        val g = (Color.green(initialColor) * darkness).roundToInt()
        val b = (Color.blue(initialColor) * darkness).roundToInt()
        return Color.argb(
            Color.alpha(initialColor),
            r.coerceAtMost(255),
            g.coerceAtMost(255),
            b.coerceAtMost(255)
        )
    }

    fun lightsOut(context: Context): Boolean {
        val nightModeFlags: Int = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * LIGHT = suitable for light theme
     * DARK = suitable for dark theme
     */
    fun getToleratedShade(color: Int, shade: Shade): Shade {
        val tolerance = 0.3
        val colorDarkness = getDarkness(color)
        var toleratedDarkness = colorDarkness
        if (shade == LIGHT)
            toleratedDarkness -= tolerance
        if (shade == DARK)
            toleratedDarkness += tolerance
        return if (toleratedDarkness in 0.0..0.5) LIGHT else DARK
    }

    fun getAccent(context: Context): Int {
        val typedValue = TypedValue()
        val contextThemeWrapper = ContextThemeWrapper(
            context,
            android.R.style.Theme_DeviceDefault_DayNight
        )
        val nightModeFlags: Int = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        contextThemeWrapper.theme.resolveAttribute(
            android.R.attr.colorAccent,
            typedValue, true
        )
        val fm = FeatureManager(context.contentResolver)
        val accentManager = fm.AccentManager()
        if (accentManager.isMonetEnabled()) {
            return fm.Secure().getInt(fm.Secure().MONET_BASE_ACCENT, typedValue.data)
        }
        return when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> if (accentManager.getDark() == "-1") typedValue.data else Color.parseColor(
                "#" + accentManager.getDark()
            )
            Configuration.UI_MODE_NIGHT_NO -> if (accentManager.getLight() == "-1") typedValue.data else Color.parseColor(
                "#" + accentManager.getLight()
            )
            else -> typedValue.data
        }
    }

    fun getAccent(context: Context, config: Int): Int {
        val typedValue = TypedValue()
        var contextThemeWrapper = ContextThemeWrapper(
            context,
            android.R.style.Theme_DeviceDefault_DayNight
        )
        when (config) {
            Configuration.UI_MODE_NIGHT_YES -> {
                contextThemeWrapper = ContextThemeWrapper(
                    context,
                    android.R.style.Theme_DeviceDefault
                )
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                contextThemeWrapper = ContextThemeWrapper(
                    context,
                    android.R.style.Theme_DeviceDefault_Light
                )
            }
        }
        contextThemeWrapper.theme.resolveAttribute(
            android.R.attr.colorAccent,
            typedValue, true
        )
        val accentManager =
            FeatureManager(context.contentResolver).AccentManager()
        if (accentManager.isMonetEnabled()) {
            return typedValue.data
        }
        return when (config) {
            Configuration.UI_MODE_NIGHT_YES -> if (accentManager.getDark() == "-1") typedValue.data else Color.parseColor(
                "#" + accentManager.getDark()
            )
            Configuration.UI_MODE_NIGHT_NO -> if (accentManager.getLight() == "-1") typedValue.data else Color.parseColor(
                "#" + accentManager.getLight()
            )
            else -> typedValue.data
        }
    }

    fun isNightMode(context: Context): Boolean {
        val nightModeFlags: Int = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    @ColorInt
    fun getColorStateListDefaultColor(context: Context, resId: Int): Int {
        val list = context.resources.getColorStateList(resId, context.theme)
        return list.defaultColor
    }

    @ColorInt
    fun getColorAttrDefaultColor(context: Context, attr: Int): Int {
        val ta = context.obtainStyledAttributes(intArrayOf(attr))
        @ColorInt val colorAccent = ta.getColor(0, 0)
        ta.recycle()
        return colorAccent
    }

    fun getTextColor(context: Context): Int {
        return context.resolveColorAttr(android.R.attr.textColorPrimary)
    }

    fun getInverseTextColor(context: Context): Int {
        return context.resolveColorAttr(android.R.attr.textColorPrimaryInverse)
    }

    fun getSecondaryTextColor(context: Context): Int {
        return context.resolveColorAttr(android.R.attr.textColorSecondary)
    }

    fun getInverseSecondaryTextColor(context: Context): Int {
        return context.resolveColorAttr(android.R.attr.textColorSecondaryInverse)
    }

    fun getCornerRadius(context: Context): Float {
        return context.resolveDimenAttr(android.R.attr.dialogCornerRadius)
    }

    fun getDrawable(context: Context, packageName: String, drawableName: String): Drawable? = try {
        val pm: PackageManager = context.packageManager
        val mApkResources: Resources = pm.getResourcesForApplication(packageName)
        ResourcesCompat.getDrawable(
            mApkResources, mApkResources.getIdentifier(
                drawableName,
                "drawable",
                packageName
            ), null
        )
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }

    @Dimension
    fun Context.resolveDimenAttr(@AttrRes dimenAttr: Int): Float {
        val resolvedAttr = resolveThemeAttr(dimenAttr)
        val dimenRes =
            if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
        return resources.getDimension(dimenRes)
    }

    @ColorInt
    fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
        val resolvedAttr = resolveThemeAttr(colorAttr)
        val colorRes =
            if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
        return ContextCompat.getColor(this, colorRes)
    }

    fun Context.resolveThemeAttr(@AttrRes attrRes: Int): TypedValue {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue
    }

    fun isAndroidGo(): Boolean = RoSystemProperties.CONFIG_LOW_RAM

    fun hasRGBLed(context: Context): Boolean {
        return getInternalBool("config_multiColorBatteryLed", context)
    }

    fun hasNavbarByDefault(context: Context): Boolean {
        var needsNav = context.resources.getBoolean(com.android.internal.R.bool.config_showNavigationBar)
        val navBarOverride = SystemProperties.get("qemu.hw.mainkeys")
        if ("1" == navBarOverride) {
            needsNav = false
        } else if ("0" == navBarOverride) {
            needsNav = true
        }
        return needsNav
    }

    fun isNavbarEnabled(context: Context): Boolean {
        val featureManager = FeatureManager(context.contentResolver)
        return featureManager.System().getInt("navigation_bar_show", if (hasNavbarByDefault(context)) 1 else 0) != 0

    }

    fun setNavbarEnabled(context: Context, enabled: Boolean) {
        if (!canDisable(context)) {
            return
        }
        val featureManager = FeatureManager(context.contentResolver)
        featureManager.System().setInt("navigation_bar_show", if (enabled) 1 else 0)
    }

    fun canDisable(context: Context): Boolean {
        val canForceDisable = getInternalBool("config_canForceDisableNavigationBar", context)
        if (canForceDisable) {
            return true
        }
        val deviceKeys = getInternalInteger("config_deviceHardwareKeys", context)
        val hasHomeKey = deviceKeys and KEY_MASK_HOME != 0
        val hasBackKey = deviceKeys and KEY_MASK_BACK != 0
        return hasHomeKey && hasBackKey
    }

    /* returns whether the device supports button backlight adjusment or not. */
    fun hasButtonBacklightSupport(context: Context): Boolean {
        val buttonBrightnessControlSupported = getInternalInteger("config_deviceSupportsButtonBrightnessControl", context) != 0
        return (buttonBrightnessControlSupported && (hasHomeKey(context) || hasBackKey(context) || hasMenuKey(context) || hasAssistKey(context) || hasAppSwitchKey(context)))
    }

    /* returns whether the device supports keyboard backlight adjusment or not. */
    fun hasKeyboardBacklightSupport(context: Context): Boolean {
        return getInternalInteger("config_deviceSupportsKeyboardBrightnessControl", context) != 0
    }

    /* returns whether the device has home key or not. */
    fun hasHomeKey(context: Context): Boolean {
        return getDeviceKeys(context) and KEY_MASK_HOME != 0
    }

    /* returns whether the device has back key or not. */
    fun hasBackKey(context: Context): Boolean {
        return getDeviceKeys(context) and KEY_MASK_BACK != 0
    }

    /* returns whether the device has menu key or not. */
    fun hasMenuKey(context: Context): Boolean {
        return getDeviceKeys(context) and KEY_MASK_MENU != 0
    }

    /* returns whether the device has assist key or not. */
    fun hasAssistKey(context: Context): Boolean {
        return getDeviceKeys(context) and KEY_MASK_ASSIST != 0
    }

    /* returns whether the device has app switch key or not. */
    fun hasAppSwitchKey(context: Context): Boolean {
        return getDeviceKeys(context) and KEY_MASK_APP_SWITCH != 0
    }

    /* returns whether the device has camera key or not. */
    fun hasCameraKey(context: Context): Boolean {
        return getDeviceKeys(context) and KEY_MASK_CAMERA != 0
    }

    /* returns whether the device can be waken using the home key or not. */
    fun canWakeUsingHomeKey(context: Context): Boolean {
        return getDeviceWakeKeys(context) and KEY_MASK_HOME != 0
    }

    /* returns whether the device can be waken using the back key or not. */
    fun canWakeUsingBackKey(context: Context): Boolean {
        return getDeviceWakeKeys(context) and KEY_MASK_BACK != 0
    }

    /* returns whether the device can be waken using the menu key or not. */
    fun canWakeUsingMenuKey(context: Context): Boolean {
        return getDeviceWakeKeys(context) and KEY_MASK_MENU != 0
    }

    /* returns whether the device can be waken using the assist key or not. */
    fun canWakeUsingAssistKey(context: Context): Boolean {
        return getDeviceWakeKeys(context) and KEY_MASK_ASSIST != 0
    }

    /* returns whether the device can be waken using the app switch key or not. */
    fun canWakeUsingAppSwitchKey(context: Context): Boolean {
        return getDeviceWakeKeys(context) and KEY_MASK_APP_SWITCH != 0
    }

    /* returns whether the device can be waken using the camera key or not. */
    fun canWakeUsingCameraKey(context: Context): Boolean {
        return getDeviceWakeKeys(context) and KEY_MASK_CAMERA != 0
    }

    /* returns whether the device can be waken using the volume rocker or not. */
    fun canWakeUsingVolumeKeys(context: Context): Boolean {
        return getDeviceWakeKeys(context) and KEY_MASK_VOLUME != 0
    }

    /* returns whether the device has volume rocker or not. */
    fun hasVolumeKeys(context: Context): Boolean {
        return getDeviceKeys(context) and KEY_MASK_VOLUME != 0
    }

    /* returns whether the device has power key or not. */
    fun hasPowerKey(): Boolean {
        return KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_POWER)
    }

    fun getDeviceKeys(context: Context): Int {
        return getInternalInteger("config_deviceHardwareKeys", context)
    }

    fun getDeviceWakeKeys(context: Context): Int {
        return getInternalInteger("config_deviceHardwareWakeKeys", context)
    }

    fun getVolRockerSwap(context: Context): Int {
        return getInternalInteger("config_volumeRockerVsDisplayOrientation", context)
    }

    /*
     * A device has hardware keys if
     * it has at least a home button (1),
     * a back button (2),
     * an app switch (16)
     * and a volume rocker (64)
     * which equals 83
     */
    fun hasHardwareKeys(context: Context): Boolean {
        return getInternalInteger("config_deviceHardwareKeys", context) >= 83
    }

    fun hasAmbient(context: Context): Boolean {
        return getInternalBool("config_dozeAlwaysOnDisplayAvailable", context)
    }

    fun shouldDisableNightLight(context: Context): Boolean {
        return getInternalBool("disable_fod_night_light", context)
    }

    fun hasFodSupport(context: Context): Boolean {
        return getInternalBool("config_supportsInDisplayFingerprint", context)
    }

    fun getFodAnimationPackage(context: Context): String {
        return try {
            context.resources.getString(
                Resources.getSystem()
                    .getIdentifier("config_fodAnimationPackage", "string", "android")
            )
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }

    fun getDotWallsSupport(context: Context): Boolean {
        return isPackageInstalled(context, context.getString(R.string.dot_wallpapers_packagename))
    }

    fun getDotWalls(context: Context): ArrayList<Drawable> {
        val list = ArrayList<Drawable>()
        val walls = context.resources.getStringArray(R.array.dot_walls)
        for (wall in walls) {
            list.add(
                getDrawable(
                    context,
                    context.getString(R.string.dot_wallpapers_packagename),
                    wall
                )!!
            )
        }
        return list
    }

    fun getDotWallsUri(context: Context): ArrayList<Uri> {
        val list = ArrayList<Uri>()
        val walls = context.resources.getStringArray(R.array.dot_walls)
        for (wall in walls) {
            list.add(Uri.parse("android.resource://${context.getString(R.string.dot_wallpapers_packagename)}/drawable/$wall"))
        }
        return list
    }

    fun getInternalInteger(res: String, context: Context): Int {
        return try {
            context.resources.getInteger(Resources.getSystem().getIdentifier(res, "integer", "android"))
        } catch (e: Resources.NotFoundException) {
            -1
        }
    }

    fun getInternalFloat(res: String, context: Context): Float {
        return try {
            context.resources.getFloat(Resources.getSystem().getIdentifier(res, "dimen", "android"))
        } catch (e: Resources.NotFoundException) {
            -1f
        }
    }

    fun getInternalBool(res: String, context: Context): Boolean {
        return try {
            context.resources.getBoolean(Resources.getSystem().getIdentifier(res, "bool", "android"))
        } catch (e: Resources.NotFoundException) {
            false
        }
    }

    fun isPackageInstalled(context: Context, pkg: String, ignoreState: Boolean): Boolean {
        try {
            val pi = context.packageManager.getPackageInfo(pkg, 0)
            if (!pi.applicationInfo.enabled && !ignoreState) {
                return false
            }
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
        return true
    }

    fun isPackageInstalled(context: Context, pkg: String): Boolean {
        return isPackageInstalled(context, pkg, true)
    }
}