package com.android.settings.dotextras.custom.sections

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter

class HardwareKeysSection : GenericSection() {

    private var hwkeys0List: ArrayList<ContextCards> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.section_hwkeys, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hwkeys0List.clear()
        buildSwitch(
            hwkeys0List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.disabled),
            subtitle = "Show navigation bar",
            accentColor = R.color.dot_sky,
            feature = featureManager.System().NAVIGATION_BAR_SHOW,
            featureType = ContextCardsAdapter.Type.SYSTEM,
            enabled = false
        )
        buildSwitch(
            hwkeys0List,
            iconID = R.drawable.ic_menu,
            title = getString(R.string.disabled),
            subtitle = getString(R.string.hold_to_torch),
            accentColor = R.color.dot_blue,
            feature = featureManager.System().BUTTON_BACKLIGHT_ONLY_WHEN_PRESSED,
            featureType = ContextCardsAdapter.Type.SYSTEM,
            summary = getString(R.string.hold_to_torch_summary),
            enabled = false
        )
    }

    override fun isAvailable(context: Context): Boolean {
        return super.isAvailable(context)
    }
}