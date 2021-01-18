package com.android.settings.dotextras.custom.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import com.android.internal.R.anim.slide_in_left
import com.android.internal.R.anim.slide_out_right
import com.android.settings.dotextras.R
import com.google.android.material.switchmaterial.SwitchMaterial


class DotMaterialPreference(context: Context?, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    var titleView: TextView? = null
    var summaryView: TextView? = null
    var iconView: ImageView? = null
    var switchView: SwitchMaterial? = null
    private var layoutView: LinearLayout? = null
    private var widgetFrame: LinearLayout? = null
    private var url: String? = null

    init {
        LayoutInflater.from(context).inflate(
            R.layout.preference_base, this, true
        )
        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.DotMaterialPreference)
            if (a.getBoolean(R.styleable.DotMaterialPreference_cardStyle,
                    false)
            ) {
                if (childCount != 0) {
                    removeAllViews()
                }
                LayoutInflater.from(context).inflate(
                    R.layout.preference_card_base, this, true)
            } else {
                if (childCount != 0) {
                    removeAllViews()
                }
                LayoutInflater.from(context).inflate(
                    R.layout.preference_base, this, true
                )
            }
            titleView = findViewById(android.R.id.title)
            titleView!!.typeface = Typeface.create("google-sans-medium", Typeface.NORMAL)
            summaryView = findViewById(android.R.id.summary)
            summaryView!!.typeface = Typeface.create("google-sans", Typeface.NORMAL)
            iconView = findViewById(android.R.id.icon)
            layoutView = findViewById(R.id.preference_layout)
            widgetFrame = findViewById(android.R.id.widget_frame)
            if (a.getBoolean(R.styleable.DotMaterialPreference_android_checkable, false)) {
                widgetFrame!!.addView(LayoutInflater.from(context)
                    .inflate(R.layout.preference_widget_switch, this, false))
                switchView = findViewById(android.R.id.switch_widget)
                switchView!!.isSelected =
                    a.getBoolean(R.styleable.DotMaterialPreference_android_checked, false)
            } else {
                if (widgetFrame!!.childCount != 0)
                    widgetFrame!!.removeAllViews()
            }
            titleView!!.text = a.getString(R.styleable.DotMaterialPreference_android_title)
            summaryView!!.text = a.getString(R.styleable.DotMaterialPreference_android_summary)
            titleView!!.visibility = if (titleView!!.text.isEmpty()) GONE else VISIBLE
            summaryView!!.visibility = if (summaryView!!.text.isEmpty()) GONE else VISIBLE
            iconView!!.setImageResource(a.getResourceId(R.styleable.DotMaterialPreference_android_icon,
                android.R.color.transparent))
            iconView!!.imageTintList =
                ColorStateList.valueOf(a.getColor(R.styleable.DotMaterialPreference_android_tint,
                    getContext().getColor(R.color.colorAccent)))
            if (a.getString(R.styleable.DotMaterialPreference_url) != null) {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                builder.setExitAnimations(mContext, slide_in_left, slide_out_right)
                url = a.getString(R.styleable.DotMaterialPreference_url)
                setOnClickPreference {
                    customTabsIntent.launchUrl(mContext, Uri.parse(url))
                }
            }
            a.recycle()
        }
    }

    fun setOnClickPreference(onClickListener: OnClickListener) {
        layoutView!!.setOnClickListener(onClickListener)
    }
}