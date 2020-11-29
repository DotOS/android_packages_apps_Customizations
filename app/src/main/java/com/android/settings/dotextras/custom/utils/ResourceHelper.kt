package com.android.settings.dotextras.custom.utils

import android.content.Context
import android.graphics.Color
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
        return if (accentManager.get() == "-1") typedValue.data else Color.parseColor("#" + accentManager.get())
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

    fun getCornerRadius(context: Context): Float {
        return context.resolveDimenAttr(android.R.attr.dialogCornerRadius)
    }

    @Dimension
    fun Context.resolveDimenAttr(@AttrRes dimenAttr: Int): Float {
        val resolvedAttr = resolveThemeAttr(dimenAttr)
        val dimenRes = if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
        return resources.getDimension(dimenRes)
    }

    @ColorInt
    fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
        val resolvedAttr = resolveThemeAttr(colorAttr)
        val colorRes =
            if (resolvedAttr.resourceId != 0) resolvedAttr.resourceId else resolvedAttr.data
        return ContextCompat.getColor(this, colorRes)
    }

    private fun Context.resolveThemeAttr(@AttrRes attrRes: Int): TypedValue {
        val typedValue = TypedValue()
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue
    }
}