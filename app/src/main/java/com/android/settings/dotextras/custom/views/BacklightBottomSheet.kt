/*
 * Copyright (C) 2021 The dotOS Project
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
package com.android.settings.dotextras.custom.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.utils.ResourceHelper
import com.android.settings.dotextras.system.FeatureManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.hwkeys_light_bottomsheet.*
import kotlin.math.roundToInt

@SuppressLint("SetTextI18n")
class BacklightBottomSheet : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {

    private val DEFAULT_BUTTON_TIMEOUT = 5
    private val BUTTON_BRIGHTNESS = "button_brightness"

    private lateinit var fm: FeatureManager

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.hwkeys_light_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fm = FeatureManager(requireContext().contentResolver)
        illuminate_buttons.switchView!!.isChecked = fm.Secure().getFloat(BUTTON_BRIGHTNESS, 0.0f) != 0.0f
        illuminate_buttons.setOnClickPreference {
            illuminate_buttons.switchView!!.isChecked = !illuminate_buttons.switchView!!.isChecked
            illuminate_buttons_pressed.isEnabled = illuminate_buttons.switchView!!.isChecked
            if (!illuminate_buttons.switchView!!.isChecked) {
                illuminate_buttons_pressed.switchView!!.isChecked = false
                backlight_timeout.isEnabled = false
            }
            backlight_brightness.isEnabled = illuminate_buttons.switchView!!.isChecked
            backlight_brightness.progress = (getBrightness() * 100f).roundToInt()
        }

        backlight_brightness_layout.visibility = if (ResourceHelper.hasButtonBacklightSupport(requireContext())) View.VISIBLE else View.GONE

        backlight_brightness.isEnabled = illuminate_buttons.switchView!!.isChecked
        backlight_brightness.progress = (getBrightness() * 100f).roundToInt()
        backlight_brightness.setOnSeekBarChangeListener(this)
        backlight_brightness_value.text = "${(getBrightness() * 100f).roundToInt()}%"

        backlight_timeout.progress = getTimeout()
        backlight_timeout.setOnSeekBarChangeListener(this)
        backlight_timeout.isEnabled = illuminate_buttons_pressed.switchView!!.isChecked

        illuminate_buttons_pressed.isEnabled = illuminate_buttons.switchView!!.isChecked
        illuminate_buttons_pressed.switchView!!.isChecked = fm.System().getInt(fm.System().BUTTON_BACKLIGHT_ONLY_WHEN_PRESSED, 0) == 1
        illuminate_buttons_pressed.setOnClickPreference {
            if (illuminate_buttons.switchView!!.isChecked) {
                illuminate_buttons_pressed.switchView!!.isChecked = !illuminate_buttons_pressed.switchView!!.isChecked
                backlight_timeout.isEnabled = illuminate_buttons_pressed.switchView!!.isChecked
                backlight_timeout.progress = getTimeout()
                if (backlight_timeout.progress == 0)
                    backlight_timeout.progress = DEFAULT_BUTTON_TIMEOUT
            }
        }
        backlight_timeout_seconds.text = getString(R.string.backlight_seconds, getTimeout().toString())

        list_reset.setOnClickListener {
            backlight_timeout.progress = 1
            backlight_brightness.progress = (getDefaultBrightness() * 100f).roundToInt()
            setTimeout(backlight_timeout.progress)
            if (ResourceHelper.hasButtonBacklightSupport(requireContext())) applyBrightness(backlight_brightness.progress / 100f)
            illuminate_buttons_pressed.switchView!!.isChecked = false
            fm.System().setInt(fm.System().BUTTON_BACKLIGHT_ONLY_WHEN_PRESSED, 0)
        }

        list_apply.setOnClickListener {
            setTimeout(backlight_timeout.progress)
            if (ResourceHelper.hasButtonBacklightSupport(requireContext())) applyBrightness(backlight_brightness.progress / 100f)
            fm.System().setInt(fm.System().BUTTON_BACKLIGHT_ONLY_WHEN_PRESSED, if (illuminate_buttons_pressed.switchView!!.isChecked) 1 else 0)
            dismiss()
        }

        val bottomSheet: View = dialog!!.findViewById(R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
        behavior.peekHeight = BottomSheetBehavior.PEEK_HEIGHT_AUTO
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getBrightness(): Float {
        return fm.Secure().getFloat(BUTTON_BRIGHTNESS, getDefaultBrightness())
    }

    private fun getDefaultBrightness(): Float = ResourceHelper.getInternalFloat("config_buttonBrightnessSettingDefaultFloat", requireContext()) / 100f

    private fun applyBrightness(brightness: Float) {
        fm.Secure().setFloat(BUTTON_BRIGHTNESS, brightness)
        if (brightness == 0.0f) {
            illuminate_buttons.switchView!!.isChecked = false
            backlight_brightness.isEnabled = illuminate_buttons.switchView!!.isChecked
        }
        backlight_brightness_value.text = "${(brightness * 100f).roundToInt()}%"
        Log.d("Backlight", "Brightness : $brightness")
    }

    private fun getTimeout(): Int {
        return fm.Secure().getInt("button_backlight_timeout", DEFAULT_BUTTON_TIMEOUT * 1000) / 1000
    }

    private fun setTimeout(timeout: Int) {
        fm.Secure().setInt("button_backlight_timeout", timeout * 1000)
        if (timeout == 0) {
            illuminate_buttons_pressed.switchView!!.isChecked = false
            backlight_timeout.isEnabled = illuminate_buttons_pressed.switchView!!.isChecked
        }
        backlight_timeout_seconds.text = getString(R.string.backlight_seconds, timeout.toString())
        Log.d("Backlight", "Timeout : $timeout")
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        backlight_brightness_value.text = "${backlight_brightness.progress}%"
        backlight_timeout_seconds.text = getString(R.string.backlight_seconds, backlight_timeout.progress.toString())
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
}