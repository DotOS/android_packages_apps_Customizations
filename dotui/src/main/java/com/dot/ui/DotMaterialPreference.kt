package com.dot.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.browser.customtabs.CustomTabsIntent
import com.dot.ui.system.FeatureManager

class DotMaterialPreference(context: Context?, attrs: AttributeSet?) :
    LinearLayout(context!!, attrs) {

    lateinit var titleView: TextView
    var summaryView: TextView? = null
    lateinit var iconView: ImageView
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    var switchView: Switch? = null
    var seekBar: SeekBar? = null
    var countText: TextView? = null
    private var layoutView: LinearLayout? = null
    var widgetFrame: LinearLayout? = null
    private var url: String? = null

    private var isSeekable: Boolean = false
    private var isCheckable: Boolean = false
    private var newStyle: Boolean = false
    private var widgetLayoutResource: Int = -1
    lateinit var widgetLayout: View

    init {
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(
            R.layout.preference_base, this, true
        )
        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.DotMaterialPreference)
            isSeekable = a.getBoolean(R.styleable.DotMaterialPreference_seekable, false)
            isCheckable = a.getBoolean(R.styleable.DotMaterialPreference_android_checkable, false)
            newStyle = a.getBoolean(R.styleable.DotMaterialPreference_newStyle, false)
            val categoryLayout = a.getBoolean(R.styleable.DotMaterialPreference_categoryLayout, false)
            val footerLayout = a.getBoolean(R.styleable.DotMaterialPreference_footerLayout, false)
            val cardStyle = a.getBoolean(R.styleable.DotMaterialPreference_cardStyle, false)
            widgetLayoutResource =
                a.getResourceId(R.styleable.DotMaterialPreference_android_widgetLayout, -1)
            val footerLayoutResource =
                a.getResourceId(R.styleable.DotMaterialPreference_footerLayout_below, -1)
            when {
                cardStyle -> {
                    if (childCount != 0) removeAllViews()
                    if (newStyle) LayoutInflater.from(context).inflate(R.layout.preference_card_base2, this, true)
                    else LayoutInflater.from(context).inflate(R.layout.preference_card_base, this, true)
                }
                isSeekable -> {
                    if (childCount != 0) removeAllViews()
                    if (newStyle) LayoutInflater.from(context).inflate(R.layout.preference_seekbar2, this, true)
                    else LayoutInflater.from(context).inflate(R.layout.preference_seekbar, this, true)
                }
                footerLayout -> {
                    if (childCount != 0) removeAllViews()
                    LayoutInflater.from(context).inflate(R.layout.preference_footer, this, true)
                }
                categoryLayout -> {
                    if (childCount != 0) removeAllViews()
                    LayoutInflater.from(context).inflate(R.layout.preference_category, this, true)
                }
                else -> {
                    if (childCount != 0) removeAllViews()
                    if (newStyle) LayoutInflater.from(context).inflate(R.layout.preference_base2, this, true)
                    else LayoutInflater.from(context).inflate(R.layout.preference_base, this, true)
                }
            }
            titleView = requireViewById(android.R.id.title)
            titleView.typeface = Typeface.create("google-sans-medium", Typeface.NORMAL)
            titleView.text = a.getString(R.styleable.DotMaterialPreference_android_title)
            titleView.visibility = if (titleView.text.isEmpty()) GONE else VISIBLE
            isEnabled = true
            if (!categoryLayout) {
                iconView = requireViewById(android.R.id.icon)
                val icon = a.getResourceId(
                    R.styleable.DotMaterialPreference_android_icon,
                    android.R.color.transparent
                )
                iconView.setImageResource(icon)
                iconView.imageTintList = ColorStateList.valueOf(
                    a.getColor(
                        R.styleable.DotMaterialPreference_android_tint,
                        getContext().getColor(R.color.colorAccent)
                    )
                )
                if (icon == android.R.color.transparent) {
                    iconView.visibility = GONE
                }
                layoutView = requireViewById(R.id.preference_layout)
                if (!footerLayout) {
                    summaryView = findViewById(android.R.id.summary)
                    summaryView?.typeface = Typeface.create("google-sans", Typeface.NORMAL)
                    widgetFrame = requireViewById(android.R.id.widget_frame)
                    if (isCheckable) {
                        widgetLayout = LayoutInflater.from(context).inflate(
                            R.layout.preference_widget_switch,
                            this,
                            false
                        )
                        widgetFrame!!.addView(widgetLayout)
                        switchView = findViewById(android.R.id.switch_widget)
                        switchView!!.isChecked = a.getBoolean(
                            R.styleable.DotMaterialPreference_android_checked,
                            false
                        )
                    } else {
                        if (widgetFrame!!.childCount != 0)
                            widgetFrame!!.removeAllViews()
                    }
                    summaryView?.text =
                        a.getString(R.styleable.DotMaterialPreference_android_summary)
                    summaryView?.visibility = if (summaryView?.text?.isEmpty()!!) GONE else VISIBLE
                    if (a.getString(R.styleable.DotMaterialPreference_url) != null) {
                        val builder = CustomTabsIntent.Builder()
                        url = a.getString(R.styleable.DotMaterialPreference_url)
                        setOnClickPreference {
                            builder.build().launchUrl(context!!, Uri.parse(url))
                        }
                    }
                    if (isSeekable) {
                        seekBar = requireViewById(R.id.seekbar)
                        seekBar!!.min = a.getInt(R.styleable.DotMaterialPreference_android_min, 0)
                        seekBar!!.max = a.getInt(R.styleable.DotMaterialPreference_android_max, 0)
                        seekBar!!.setProgress(
                            a.getInt(
                                R.styleable.DotMaterialPreference_android_progress,
                                0
                            ), false
                        )
                        widgetLayout = LayoutInflater.from(context).inflate(
                            R.layout.preference_widget_count,
                            this,
                            false
                        )
                        widgetFrame!!.addView(widgetLayout)
                        countText = requireViewById(R.id.countText)
                        countText!!.text =
                            a.getInt(R.styleable.DotMaterialPreference_android_progress, 0)
                                .toString()
                    }
                    if (!isCheckable && !isSeekable && widgetLayoutResource != -1) {
                        widgetFrame!!.addView(
                            LayoutInflater.from(context).inflate(
                                widgetLayoutResource, this, false
                            )
                        )
                    }
                    val featureType = a.getString(R.styleable.DotMaterialPreference_featureType)
                    var featureDefault = a.getString(R.styleable.DotMaterialPreference_featureDefault)
                    val featureValue = a.getString(R.styleable.DotMaterialPreference_featureValue)
                    val feature = a.getString(R.styleable.DotMaterialPreference_feature)
                    if (featureDefault == "true") featureDefault = "1"
                    if (featureDefault == "false") featureDefault = "0"
                    if (!feature.isNullOrEmpty() && !featureType.isNullOrEmpty() && !featureDefault.isNullOrEmpty()) {
                        val fm = FeatureManager(context!!.contentResolver)
                        when (featureType) {
                            "secure" -> {
                                if (isCheckable) {
                                    setChecked(
                                        fm.Secure().getInt(feature, featureDefault.toInt()) == 1
                                    )
                                    setOnCheckListener { _, isChecked ->
                                        fm.Secure().setInt(feature, if (isChecked) 1 else 0)
                                    }
                                }
                            }
                            "system" -> {
                                if (isCheckable) {
                                    setChecked(
                                        fm.System().getInt(feature, featureDefault.toInt()) == 1
                                    )
                                    setOnCheckListener { _, isChecked ->
                                        fm.System().setInt(feature, if (isChecked) 1 else 0)
                                    }
                                }
                            }
                            else -> {
                                Log.e("DotPreferences", "Invalid feature type")
                            }
                        }
                    }
                }
                else {
                    if (footerLayoutResource != -1) {
                        val footerView = LayoutInflater.from(context).inflate(footerLayoutResource, this, false)
                        findViewById<LinearLayout>(R.id.preference_layout).addView(footerView)
                    }
                }
            }
            val showDivider = a.getBoolean(R.styleable.DotMaterialPreference_showDivider, false)
            if (showDivider) addView(
                LayoutInflater.from(context).inflate(R.layout.divider, this, false)
            )
            a.recycle()
        }
    }

    override fun isEnabled(): Boolean {
        val isEnabled = super.isEnabled()
        titleView.isEnabled = isEnabled
        summaryView?.isEnabled = isEnabled
        switchView?.isEnabled = isEnabled
        seekBar?.isEnabled = isEnabled
        return isEnabled
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (seekBar != null) {
            seekBar!!.isEnabled = enabled
        }
    }

    var summary: String?
        get() {
            return summaryView?.text.toString()
        }
        set(value) {
            summaryView?.text = value
        }

    var title: String?
        get() {
            return titleView.text.toString()
        }
        set(value) {
            titleView.text = value
        }


    fun setUrl(url: String) {
        this.url = url
        val builder = CustomTabsIntent.Builder()
        setOnClickPreference {
            builder.build().launchUrl(context, Uri.parse(url))
        }
    }

    fun setChecked(checked: Boolean) {
        switchView?.isChecked = checked
    }

    fun setOnCheckListener(onCheckedChangeListener: CompoundButton.OnCheckedChangeListener) {
        setOnClickPreference(null)
        setOnClickPreference {
            switchView?.isChecked = !switchView?.isChecked!!
        }
        switchView?.setOnCheckedChangeListener(null)
        switchView?.setOnCheckedChangeListener(onCheckedChangeListener)
    }

    fun setWidgetLayout(resID: Int) {
        if (!isCheckable && !isSeekable && widgetLayoutResource != -1) {
            widgetLayoutResource = resID
            widgetLayout = LayoutInflater.from(context).inflate(
                widgetLayoutResource, this, false
            )
            if (widgetFrame!!.childCount != 0) widgetFrame!!.removeAllViews()
            widgetFrame!!.addView(widgetLayout)
        } else {
            Log.e(
                "PreferenceManager",
                "Cannot set widget for preference $id. isCheckable or isSeekable is enabled or widgetLayoutResource is null"
            )
        }
    }

    fun setProgress(value: Int) {
        seekBar?.progress = value
        countText?.text = value.toString()
    }

    fun getProgress(): Int = seekBar?.progress!!

    fun setMax(value: Int) {
        seekBar?.max = value
    }

    fun setMin(value: Int) {
        seekBar?.min = value
    }


    fun setOnClickPreference(onClickListener: OnClickListener?) {
        layoutView!!.setOnClickListener(onClickListener)
    }

    fun setOnProgressChangedPreference(postUnit: (progress: Int) -> Unit) {
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                countText?.text = progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                postUnit(seekBar.progress)
            }

        })
    }

    fun setOnProgressChangedPreference(listener: SeekBar.OnSeekBarChangeListener) {
        seekBar?.setOnSeekBarChangeListener(listener)
    }
}