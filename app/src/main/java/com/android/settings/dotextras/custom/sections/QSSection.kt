package com.android.settings.dotextras.custom.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWIPE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWITCH
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.system.FeatureManager
import com.android.settings.dotextras.custom.sections.batterystyles.CircleBatteryDrawable
import com.android.settings.dotextras.custom.sections.batterystyles.FullCircleBatteryDrawable
import com.android.settings.dotextras.custom.sections.batterystyles.ThemedBatteryDrawable

open class QSSection : GenericSection() {

    private var qsList: ArrayList<ContextCards> = ArrayList()
    private var rows_columnsList: ArrayList<ContextCards> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.section_qs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val featureManager = FeatureManager(requireActivity().contentResolver)
        /**
         * Clean
         */
        qsList.clear()
        rows_columnsList.clear()
        /**
         * Options
         */
        qsList.add(
            ContextCards(
                iconID = R.drawable.ic_qs_title,
                title = getString(R.string.disabled),
                subtitle = getString(R.string.qs_title),
                accentColor = R.color.dot_red,
                feature = featureManager.System().QS_TILE_TITLE_VISIBILITY,
                featureType = SYSTEM,
                enabled = true
            )
        )
        setupLayout(SWITCH, qsList, R.id.sectionQS)
        /**
         * Rows & Columns
         */
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = "QS Columns",
            accentColor = R.color.dot_red,
            feature = featureManager.System().QS_COLUMNS_PORTRAIT,
            featureType = SYSTEM,
            min = 1,
            max = 7,
            default = 4,
            summary = "Portrait",
            extraTitle = "Column(s)"
        )
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = "QS Columns",
            accentColor = R.color.dot_pink,
            feature = featureManager.System().QS_COLUMNS_LANDSCAPE,
            featureType = SYSTEM,
            min = 1,
            max = 9,
            default = 4,
            summary = "Landscape",
            extraTitle = "Column(s)"
        )
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = "QS Rows",
            accentColor = R.color.dot_violet,
            feature = featureManager.System().QS_ROWS_PORTRAIT,
            featureType = SYSTEM,
            min = 1,
            max = 5,
            default = 3,
            summary = "Portrait",
            extraTitle = "Row(s)"
        )
        buildSwipeable(
            rows_columnsList,
            iconID = R.drawable.ic_add,
            subtitle = "QS Rows",
            accentColor = R.color.dot_green,
            feature = featureManager.System().QS_ROWS_LANDSCAPE,
            featureType = SYSTEM,
            min = 1,
            max = 5,
            default = 1,
            summary = "Landscape",
            extraTitle = "Row(s)"
        )
        setupLayout(SWIPE, rows_columnsList, R.id.sectionRows)
    }
}