package com.android.settings.dotextras.custom.sections.batterystyles.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.batterystyles.ThemedBatteryDrawable

class DefaultBatteryStyle : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_batterystyle, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val batteryView: ImageView = view.findViewById(R.id.batteryView)
        val drawable = ThemedBatteryDrawable(requireContext(), R.color.meter_background_color)
        drawable.setBatteryLevel(50)
        drawable.setColors(requireContext().getColor(R.color.colorAccent), requireContext().getColor(R.color.colorPrimary), requireContext().getColor(R.color.colorAccent))
        batteryView.setImageDrawable(drawable)
    }

}