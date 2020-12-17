package com.android.settings.dotextras.custom.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.PreferenceFragmentCompat
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.batterystyles.BatteryStylesAdapter
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.PAGER
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWIPE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.custom.sections.cards.OnSlideChangedListener
import com.android.settings.dotextras.custom.utils.ResourceHelper

class StatusbarSection : GenericSection() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.section_statusbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list = ArrayList<ContextCards>()
        val perclist = ArrayList<ContextCards>()
        list.add(ContextCards(
            iconID = R.drawable.round_battery_full_white_36dp,
            title = getString(R.string.battery_styles),
            accentColor = R.color.colorAccent,
            feature = featureManager.System().STATUS_BAR_BATTERY_STYLE,
            featureType = SYSTEM,
            pagerAdapter = BatteryStylesAdapter(requireActivity())
        ))
        setupLayout(PAGER, list, R.id.sectionBS, 1,true)
        buildSwipeable(
            list = perclist,
            iconID = R.drawable.round_battery_unknown_white_36dp,
            subtitle = "Percentage Style",
            accentColor = R.color.teal_500,
            feature = featureManager.System().STATUS_BAR_SHOW_BATTERY_PERCENT,
            featureType = SYSTEM,
            min = 0,
            max = 2,
            default = 0,
            summary = "Status Bar",
            "Style"
        ) { position, title ->
            run {
                var newTitle = ""
                when (position) {
                    0->newTitle = "Hide"
                    1->newTitle = "Inside"
                    2->newTitle = "Outside"
                }
                title.text = newTitle
            }
        }
        buildSwipeable(
            list = perclist,
            iconID = R.drawable.round_battery_unknown_white_36dp,
            subtitle = "QS Percentage Style",
            accentColor = R.color.red_500,
            feature = featureManager.System().QS_SHOW_BATTERY_PERCENT,
            featureType = SYSTEM,
            min = 0,
            max = 1,
            default = 0,
            summary = "Quick Settings",
            "Style"
        ) { position, title ->
            run {
                var newTitle = ""
                when (position) {
                    0->newTitle = "Estimate"
                    1->newTitle = "Percentage"
                }
                title.text = newTitle
            }
        }
        setupLayout(SWIPE, perclist, R.id.sectionPercentage)
    }
}