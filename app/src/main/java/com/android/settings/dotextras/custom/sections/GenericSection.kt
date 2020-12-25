package com.android.settings.dotextras.custom.sections

import android.content.Context
import android.content.om.IOverlayManager
import android.os.Bundle
import android.os.ServiceManager
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.SectionInterface
import com.android.settings.dotextras.custom.sections.cards.ContextCards
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter
import com.android.settings.dotextras.custom.sections.cards.ContextCardsListener
import com.android.settings.dotextras.custom.sections.cards.OnSlideChangedListener
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.custom.views.ContextSectionLayout
import com.android.settings.dotextras.system.FeatureManager
import kotlin.properties.Delegates

open class GenericSection : Fragment(), SectionInterface {

    val GRID_COLUMNS = 2
    val GRID_FOD_COLUMNS = 3
    val GRID_OPT_COLUMNS = 2
    var SPACER by Delegates.notNull<Int>()
    lateinit var featureManager: FeatureManager
    lateinit var overlayManager: IOverlayManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SPACER = resources.getDimension(R.dimen.recyclerSpacer).toInt()
        featureManager = FeatureManager(requireActivity().contentResolver)
        overlayManager = IOverlayManager.Stub
            .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
    }

    override fun isAvailable(context: Context): Boolean = true

    fun setupLayout(type: Int, list: ArrayList<ContextCards>, layoutID: Int) {
        val contextLayout = requireView().findViewById<ContextSectionLayout>(layoutID)
        contextLayout.setupAdapter(
            ContextCardsAdapter(
                requireActivity().contentResolver,
                type,
                list
            )
        )
        contextLayout.addDecoration(
            GridSpacingItemDecoration(
                GRID_COLUMNS,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        contextLayout.setLayoutManger(GridLayoutManager(requireContext(), GRID_COLUMNS))
    }

    fun setupLayout(type: Int, list: ArrayList<ContextCards>, layoutID: Int, columns: Int) {
        val contextLayout = requireView().findViewById<ContextSectionLayout>(layoutID)
        contextLayout.setupAdapter(
            ContextCardsAdapter(
                requireActivity().contentResolver,
                type,
                list
            )
        )
        contextLayout.addDecoration(
            GridSpacingItemDecoration(
                columns,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        contextLayout.setLayoutManger(GridLayoutManager(requireContext(), columns))
    }

    fun setupLayout(type: Int, list: ArrayList<ContextCards>, layoutID: Int, hideTitle: Boolean) {
        val contextLayout = requireView().findViewById<ContextSectionLayout>(layoutID)
        contextLayout.hideTitle(hideTitle)
        contextLayout.setupAdapter(
            ContextCardsAdapter(
                requireActivity().contentResolver,
                type,
                list
            )
        )
        contextLayout.addDecoration(
            GridSpacingItemDecoration(
                GRID_COLUMNS,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        contextLayout.setLayoutManger(GridLayoutManager(requireContext(), GRID_COLUMNS))
    }

    fun setupLayout(
        type: Int,
        list: ArrayList<ContextCards>,
        layoutID: Int,
        columns: Int,
        hideTitle: Boolean
    ) {
        val contextLayout = requireView().findViewById<ContextSectionLayout>(layoutID)
        contextLayout.hideTitle(hideTitle)
        contextLayout.setupAdapter(
            ContextCardsAdapter(
                requireActivity().contentResolver,
                type,
                list
            )
        )
        contextLayout.addDecoration(
            GridSpacingItemDecoration(
                columns,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        contextLayout.setLayoutManger(GridLayoutManager(requireContext(), columns))
    }

    fun buildSwipeable(
        list: ArrayList<ContextCards>,
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
        list.add(
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
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
        summary: String,
        extraTitle: String,
        slideListener: OnSlideChangedListener
    ) {
        list.add(
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
                extraTitle = extraTitle,
                slideListener = slideListener
            )
        )
    }

    fun buildSwipeable(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
        summary: String,
        extraTitle: String,
        listener: ContextCardsListener
    ) {
        list.add(
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
                extraTitle = extraTitle,
                listener = listener
            )
        )
    }

    fun buildSwipeable(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
        summary: String,
        extraTitle: String,
        listener: ContextCardsListener,
        sliderListener: OnSlideChangedListener
    ) {
        list.add(
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
                extraTitle = extraTitle,
                listener = listener,
                slideListener = sliderListener
            )
        )
    }

    fun buildSwipeable(
        list: ArrayList<ContextCards>,
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
        list.add(
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