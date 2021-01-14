/*
 * Copyright (C) 2020 The dotOS Project
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
package com.android.settings.dotextras.custom.sections.batterystyles.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.batterystyles.FullCircleBatteryDrawable

class FilledBatteryStyle : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.layout_batterystyle, container, false)
        val batteryView: ImageView = view.findViewById(R.id.batteryView)
        val drawable = FullCircleBatteryDrawable(requireContext(), R.color.meter_background_color)
        drawable.setBatteryLevel(50)
        drawable.setColors(requireContext().getColor(R.color.colorAccent),
            requireContext().getColor(R.color.colorPrimary),
            requireContext().getColor(R.color.colorPrimary))
        batteryView.setImageDrawable(drawable)
        batteryView.invalidate(true)
        return view
    }

}