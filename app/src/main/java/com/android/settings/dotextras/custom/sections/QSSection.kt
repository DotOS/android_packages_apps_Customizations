package com.android.settings.dotextras.custom.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWIPE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SYSTEM
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.system.FeatureManager

open class QSSection : Fragment() {

    private val GRID_COLUMNS = 2
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
        rows_columnsList.clear()
        /**
         * Rows & Columns
         */
        buildSwipeable(
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
        val recyclerViewRC: RecyclerView = view.findViewById(R.id.contextswRecycler)
        val adapterRC =
            ContextCardsAdapter(requireActivity().contentResolver, SWIPE, rows_columnsList)
        recyclerViewRC.adapter = adapterRC
        recyclerViewRC.addItemDecoration(
            GridSpacingItemDecoration(
                GRID_COLUMNS,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        recyclerViewRC.layoutManager = GridLayoutManager(requireContext(), GRID_COLUMNS)
    }

    fun buildSwipeable(
        iconID: Int,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
        summary: String,
        extraTitle: String
    ) {
        rows_columnsList.add(
            ContextCards(
                iconID = iconID,
                title = "",
                subtitle = subtitle,
                accentColor = accentColor,
                feature = feature,
                featureType = featureType,
                min = min,
                max = max,
                default = default,
                summary = summary,
                extraTitle = extraTitle
            )
        )
    }

    fun buildSwipeable(
        iconID: Int,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
        summary: String
    ) {
        rows_columnsList.add(
            ContextCards(
                iconID = iconID,
                title = "",
                subtitle = subtitle,
                accentColor = accentColor,
                feature = feature,
                featureType = featureType,
                min = min,
                max = max,
                default = default,
                summary = summary
            )
        )
    }
}