package com.android.settings.dotextras.custom.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.android.settings.dotextras.system.FeatureManager


object ResourceHelper {

    fun isDark(color: Int): Boolean {
        val darkness: Double =
            1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }

    fun getAccent(context: Context): Int {
        val typedValue = TypedValue()
        val contextThemeWrapper = ContextThemeWrapper(
            context,
            android.R.style.Theme_DeviceDefault_DayNight
        )
        contextThemeWrapper.theme.resolveAttribute(
            android.R.attr.colorAccent,
            typedValue, true
        )
        val accentManager =
            FeatureManager(context.contentResolver).AccentManager()
        return if (accentManager.get() == "-1" || accentManager.get() == "") typedValue.data else Color.parseColor(
            "#" + accentManager.get()
        )
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


    @SuppressLint("UseCompatLoadingForDrawables")
    fun getDrawable(context: Context, packageName: String, drawableName: String): Drawable? = try {
        val pm: PackageManager = context.packageManager
        val mApkResources: Resources = pm.getResourcesForApplication(packageName)
        mApkResources.getDrawable(
            mApkResources.getIdentifier(
                drawableName,
                "drawable",
                packageName
            )
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

    fun shouldDisableNightLight(context: Context): Boolean {
        return context.resources.getBoolean(
            Resources.getSystem()
                .getIdentifier("disable_fod_night_light", "bool", "android")
        )
    }

    fun hasFodSupport(context: Context): Boolean {
        return context.resources.getBoolean(
            Resources.getSystem()
                .getIdentifier("config_supportsInDisplayFingerprint", "bool", "android")
        )
    }

    fun getFodAnimationPackage(context: Context): String {
        return context.resources.getString(
            Resources.getSystem()
                .getIdentifier("config_fodAnimationPackage", "string", "android")
        )
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