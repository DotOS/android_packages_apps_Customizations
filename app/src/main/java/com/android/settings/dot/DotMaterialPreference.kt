package com.android.settings.dot

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.android.settings.dotextras.R
import com.google.android.material.card.MaterialCardView

class DotMaterialPreference(context: Context?, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    val isChecked: Boolean = false

    val titleView: TextView
    val summaryView: TextView
    val iconView: ImageView
    val cardView: MaterialCardView
    private val layoutView: LinearLayout

    init {
        LayoutInflater.from(context).inflate(
            R.layout.dot_material_preference, this, true
        )
        titleView = findViewById(android.R.id.title)
        summaryView = findViewById(android.R.id.summary)
        iconView = findViewById(android.R.id.icon)
        cardView = findViewById(R.id.preferenceCard)
        layoutView = findViewById(R.id.preferenceLayout)

    }
}