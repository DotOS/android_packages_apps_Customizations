package com.android.settings.dotextras.custom.sections.themes

import android.graphics.Typeface

class FontPack(
    val headLineFont: Typeface?,
    val bodyFont: Typeface?,
    val packageName: String,
    val label: String
) {
    var selected = false
}