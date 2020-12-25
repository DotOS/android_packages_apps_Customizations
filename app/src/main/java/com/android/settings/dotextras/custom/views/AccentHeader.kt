package com.android.settings.dotextras.custom.views

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
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
        LayoutInflater.from(mContext).inflate(
            R.layout.item_accent_header, this, true
        )
        val accentColor: Int = ResourceHelper.getAccent(mContext)

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
        val button = findViewById<ImageView>(R.id.accButton)
        val image = findViewById<ImageView>(R.id.accHeader)
        val text = findViewById<ImageView>(R.id.accText)
        button.imageTintList = ColorStateList.valueOf(accentColor)
        image.imageTintList = ColorStateList.valueOf(accentColor)
        text.imageTintList = ColorStateList.valueOf(normalizedTextColor)
    }
}