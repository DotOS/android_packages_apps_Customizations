package com.android.settings.dotextras.custom.views

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.custom.utils.getNormalizedColor
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.chip.Chip
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.switchmaterial.SwitchMaterial


class AccentHeader(context: Context?, attributesSet: AttributeSet) : LinearLayout(
    context,
    attributesSet
) {

    init {
        val layoutBase = LayoutInflater.from(mContext).inflate(
            R.layout.item_accent_header, this, false
        )
        val accentColor: Int = ResourceHelper.getAccent(mContext)
        val acc0 = layoutBase.findViewById<MaterialRadioButton>(R.id.acc0)
        val acc1 = layoutBase.findViewById<MaterialCheckBox>(R.id.acc1)
        val acc2 = layoutBase.findViewById<SwitchMaterial>(R.id.acc2)
        val acc3 = layoutBase.findViewById<ProgressBar>(R.id.acc3)
        val acc4 = layoutBase.findViewById<ProgressBar>(R.id.acc4)
        val acc5 = layoutBase.findViewById<Chip>(R.id.acc5)
        val acc6 = layoutBase.findViewById<MaterialButton>(R.id.acc6)
        val colorStateList = ColorStateList(
            arrayOf(
                intArrayOf(-android.R.attr.state_checked), intArrayOf(
                    android.R.attr.state_checked
                )
            ), intArrayOf(
                accentColor, accentColor
            )
        )

        val nightModeFlags: Int = resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        var normalizedTextColor: Int =
            if (ResourceHelper.isDark(accentColor)) ResourceHelper.getTextColor(mContext) else ResourceHelper.getInverseTextColor(
                mContext
            )
        when (nightModeFlags) {
            Configuration.UI_MODE_NIGHT_YES -> normalizedTextColor =
                if (ResourceHelper.isDark(accentColor)) ResourceHelper.getTextColor(
                    mContext
                ) else ResourceHelper.getInverseTextColor(mContext)
            Configuration.UI_MODE_NIGHT_NO -> normalizedTextColor =
                if (ResourceHelper.isDark(accentColor)) ResourceHelper.getInverseTextColor(
                    mContext
                ) else ResourceHelper.getTextColor(mContext)
        }
        acc0.buttonTintList = colorStateList
        acc1.buttonTintList = colorStateList
        acc2.thumbDrawable.setColorFilter(accentColor, PorterDuff.Mode.MULTIPLY)
        acc2.trackDrawable.setColorFilter(accentColor, PorterDuff.Mode.MULTIPLY)
        acc3.indeterminateDrawable.setColorFilter(accentColor, PorterDuff.Mode.MULTIPLY)
        acc4.progressDrawable.setColorFilter(accentColor, PorterDuff.Mode.MULTIPLY)
        acc5.setTextColor(normalizedTextColor)
        acc5.chipBackgroundColor = colorStateList
        acc6.setTextColor(normalizedTextColor)
        acc6.setBackgroundColor(accentColor)
        addView(layoutBase)
    }
}