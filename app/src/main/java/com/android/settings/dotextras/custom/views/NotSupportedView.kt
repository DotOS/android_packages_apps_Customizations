package com.android.settings.dotextras.custom.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import com.android.settings.dotextras.R

class NotSupportedView(context: Context?, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(
            R.layout.layout_not_supported, this, true
        )
        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.NotSupportedView)
            if (a.hasValue(R.styleable.NotSupportedView_featureTitle)) {
                findViewById<TextView>(R.id.notSupportedTitle).text =
                    String.format(
                        mContext.getString(R.string.feature_control_not_supported),
                        a.getString(R.styleable.NotSupportedView_featureTitle)
                    )
            }
            a.recycle()
        }
    }
}