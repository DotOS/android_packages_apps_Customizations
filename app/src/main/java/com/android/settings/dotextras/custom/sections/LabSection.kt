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
package com.android.settings.dotextras.custom.sections

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.lab.MonetColorAdapter
import com.android.settings.dotextras.system.MonetManager
import kotlinx.android.synthetic.main.section_lab.*

class LabSection : GenericSection() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.section_lab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val monetManager = MonetManager(requireContext())
        val monetEnabled = monetManager.isEnabled()
        monetEnable.switchView!!.isChecked = monetEnabled
        monetEnable.setOnClickPreference {
            monetEnable.switchView!!.isChecked = !monetEnable.switchView!!.isChecked
            featureManager.Secure().setInt(featureManager.Secure().MONET_ENGINE, if (monetEnable.switchView!!.isChecked) 1 else 0)
            monetColors.isEnabled = monetEnabled
        }
        monetColors.isEnabled = monetEnabled
        monetColors.seekBar!!.progress =
            featureManager.Secure().getInt(featureManager.Secure().MONET_COLOR_GEN, 16)
        monetColors.countText!!.text = featureManager.Secure().getInt(featureManager.Secure().MONET_COLOR_GEN, 16).toString()
        monetColors.setOnProgressChangedPreference { progress ->
            featureManager.Secure().setInt(featureManager.Secure().MONET_COLOR_GEN, progress)
        }
        monetPaletteRecycler.adapter = MonetColorAdapter(monetManager.getPaletteColors())
        monetPaletteRecycler.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun isAvailable(context: Context): Boolean {
        return true
    }
}