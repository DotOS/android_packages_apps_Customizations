/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.dot.customizations.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import com.android.settingslib.R

/**
 * MainSwitchBar is a View with a customized Switch.
 * This component is used as the main switch of the page
 * to enable or disable the prefereces on the page.
 */
@SuppressLint("CustomViewStyleable", "UseSwitchCompatOrMaterialCode", "ClickableViewAccessibility")
open class MainSwitchBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes),
    CompoundButton.OnCheckedChangeListener {

    private var mSwitchChangeListener: OnMainSwitchChangeListener? = null

    private var mTextView: TextView?

    /**
     * Return the Switch
     */
    var switch: Switch?
        protected set
    private val mBackgroundOn: Drawable?
    private val mBackgroundOff: Drawable?
    private val mBackgroundDisabled: Drawable?
    private val mFrameView: View

    var isChecked: Boolean
        /**
         * Return the status of the Switch
         */
        get() = switch!!.isChecked
        /**
         * Update the switch status
         */
        set(checked) {
            if (switch != null) {
                switch!!.isChecked = checked
            }
            setBackground(checked)
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.settingslib_main_switch_bar, this)
        isFocusable = true
        isClickable = true
        mFrameView = findViewById(R.id.frame)
        mTextView = findViewById(R.id.switch_text)
        switch = findViewById(R.id.switch_widget)
        mBackgroundOn =
            AppCompatResources.getDrawable(context, R.drawable.settingslib_switch_bar_bg_on)
        mBackgroundOff =
            AppCompatResources.getDrawable(context, R.drawable.settingslib_switch_bar_bg_off)
        mBackgroundDisabled = AppCompatResources.getDrawable(
            context,
            R.drawable.settingslib_switch_bar_bg_disabled
        )
        isChecked = switch!!.isChecked
        setBackground(isChecked)
        show()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        this@MainSwitchBar.isChecked = isChecked
        propagateChecked(isChecked)
    }

    override fun performClick(): Boolean {
        return switch!!.performClick()
    }

    /**
     * Set the title text
     */
    fun setTitle(text: CharSequence?) {
        if (mTextView != null) {
            mTextView!!.text = text
        }
    }

    /**
     * Show the MainSwitchBar
     */
    fun show() {
        visibility = VISIBLE
        switch!!.setOnCheckedChangeListener(this)
    }

    /**
     * Hide the MainSwitchBar
     */
    fun hide() {
        if (isShowing) {
            visibility = GONE
            switch!!.setOnCheckedChangeListener(null)
        }
    }

    /**
     * Return the displaying status of MainSwitchBar
     */
    val isShowing: Boolean
        get() = visibility == VISIBLE

    /**
     * Adds a listener for switch changes
     */
    fun setSwitchChangeListener(listener: OnMainSwitchChangeListener) {
        mSwitchChangeListener = listener
    }

    /**
     * Remove a listener for switch changes
     */
    fun removeOnSwitchChangeListener() {
        mSwitchChangeListener = null
    }

    /**
     * Enable or disable the text and switch.
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mTextView!!.isEnabled = enabled
        switch!!.isEnabled = enabled
        if (enabled) {
            mFrameView.background = if (isChecked) mBackgroundOn else mBackgroundOff
        } else {
            mFrameView.background = mBackgroundDisabled
        }
    }

    private fun propagateChecked(isChecked: Boolean) {
        setBackground(isChecked)
        mSwitchChangeListener?.onSwitchChanged(switch, isChecked)
    }

    private fun setBackground(isChecked: Boolean) {
        mFrameView.background = if (isChecked) mBackgroundOn else mBackgroundOff
    }

    internal class SavedState : BaseSavedState {
        var mChecked = false
        var mVisible = false

        constructor(superState: Parcelable?) : super(superState)

        /**
         * Constructor called from [.CREATOR]
         */
        private constructor(`in`: Parcel) : super(`in`) {
            mChecked = (`in`.readValue(javaClass.classLoader) as Boolean?)!!
            mVisible = (`in`.readValue(javaClass.classLoader) as Boolean?)!!
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeValue(mChecked)
            out.writeValue(mVisible)
        }

        override fun toString(): String {
            return ("MainSwitchBar.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked=" + mChecked
                    + " visible=" + mVisible + "}")
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.mChecked = switch!!.isChecked
        ss.mVisible = isShowing
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        switch!!.isChecked = ss.mChecked
        isChecked = ss.mChecked
        setBackground(ss.mChecked)
        visibility = if (ss.mVisible) VISIBLE else GONE
        switch!!.setOnCheckedChangeListener(if (ss.mVisible) this else null)
        requestLayout()
    }
}