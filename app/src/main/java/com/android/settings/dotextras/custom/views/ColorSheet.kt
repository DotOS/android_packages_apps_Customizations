/*
 *   ColorSheet
 *
 *   Copyright (c) 2019. Sasikanth Miriyampalli
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.android.settings.dotextras.custom.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentManager
import com.android.settings.dotextras.R
import com.android.settings.dotextras.system.FeatureManager
import com.android.systemui.dot.blur.BlurDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.item_colorpicker.*

typealias ColorPickerListener = ((color: Int) -> Unit)?

class ColorSheet : BottomSheetDialogFragment() {

    companion object {
        private const val TAG = "ColorSheet"
        const val NO_COLOR = -1
    }

    private var colorAdapter: ColorAdapter? = null

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState != null) dismiss()
        return inflater.inflate(R.layout.item_colorpicker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val blurDialog: BlurDialog = view.findViewById(R.id.sheetBlur)
        blurDialog.create(
            requireActivity().window.decorView, 10, resources.getDimension(
                R.dimen.default_dialog_radius
            )
        )
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val dialog = dialog as BottomSheetDialog? ?: return
                val behavior = dialog.behavior
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.peekHeight = 0
                behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    }

                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                            dismiss()
                        }
                    }
                })
            }
        })

        view.findViewById<MaterialButton>(R.id.resetAccent).setOnClickListener {
            FeatureManager(requireActivity().contentResolver).AccentManager().reset()
            dismiss()
        }

        if (colorAdapter != null) {
            colorSheetList.adapter = colorAdapter
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        colorAdapter = null
    }

    /**
     * Config color picker
     *
     * @param colors: Array of colors to show in color picker
     * @param selectedColor: Pass in the selected color from colors list, default value is null. You can pass [ColorSheet.NO_COLOR]
     * to select noColorOption in the sheet.
     * @param noColorOption: Gives a option to set the [selectedColor] to [NO_COLOR]
     * @param listener: [ColorPickerListener]
     */
    fun colorPicker(
        colors: IntArray,
        @ColorInt selectedColor: Int? = null,
        noColorOption: Boolean = false,
        listener: ColorPickerListener
    ): ColorSheet {
        colorAdapter = ColorAdapter(this, colors, selectedColor, noColorOption, listener)
        return this
    }

    fun show(fragmentManager: FragmentManager) {
        this.show(fragmentManager, TAG)
    }
}
