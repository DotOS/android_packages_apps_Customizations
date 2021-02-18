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
package com.android.settings.dotextras.custom.sections

import android.content.Context
import android.content.om.IOverlayManager
import android.os.Bundle
import android.os.Handler
import android.os.ServiceManager
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.settings.dotextras.BaseActivity
import com.android.settings.dotextras.R
import com.android.settings.dotextras.custom.SectionInterface
import com.android.settings.dotextras.custom.sections.cards.*
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.PAGER
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.RGB
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWIPE
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type.SWITCH
import com.android.settings.dotextras.custom.utils.BalloonPump
import com.android.settings.dotextras.custom.utils.GridSpacingItemDecoration
import com.android.settings.dotextras.custom.utils.SettingsConstants
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
    var useInitUI = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (useInitUI) (requireActivity() as BaseActivity).enableSettingsLauncher(false)
        SPACER = resources.getDimension(R.dimen.recyclerSpacer).toInt()
        featureManager = FeatureManager(requireActivity().contentResolver)
        overlayManager = IOverlayManager.Stub
            .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE))
    }

    override fun isAvailable(context: Context): Boolean = true

    fun createBalloon(resID: Int, view: View) {
        val balloonPump = BalloonPump(requireContext(), requireActivity().getSharedPreferences(
            SettingsConstants.SETTINGS_PREF, Context.MODE_PRIVATE))
        balloonPump.create(resID)
        balloonPump.show(view)
    }

    fun createBalloon(string: String, view: View) {
        val balloonPump = BalloonPump(requireContext(), requireActivity().getSharedPreferences(
            SettingsConstants.SETTINGS_PREF, Context.MODE_PRIVATE))
        balloonPump.create(string)
        balloonPump.show(view)
    }

    fun createBalloon(resID: Int, pos: Int, layoutID: Int) {
        val balloonPump = BalloonPump(requireContext(), requireActivity().getSharedPreferences(
            SettingsConstants.SETTINGS_PREF, Context.MODE_PRIVATE))
        balloonPump.create(resID)
        Handler().postDelayed({ balloonPump.show(getContextView(pos, layoutID)) }, 500)
    }

    fun createBalloon(string: String, pos: Int, layoutID: Int) {
        val balloonPump = BalloonPump(requireContext(), requireActivity().getSharedPreferences(
            SettingsConstants.SETTINGS_PREF, Context.MODE_PRIVATE))
        balloonPump.create(string)
        Handler().postDelayed({ balloonPump.show(getContextView(pos, layoutID)) }, 500)
    }

    fun getContextView(pos: Int, layoutID: Int): View =
        requireView().findViewById<ContextSectionLayout>(layoutID).getViewByPos(pos)

    fun setupLayout(list: ArrayList<ContextCards>, layoutID: Int) {
        val contextLayout = requireView().findViewById<ContextSectionLayout>(layoutID)
        contextLayout.setupAdapter(ContextCardsAdapter(requireActivity().contentResolver, list))
        contextLayout.addDecoration(
            GridSpacingItemDecoration(
                GRID_COLUMNS,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        contextLayout.setLayoutManger(GridLayoutManager(requireContext(), GRID_COLUMNS))
    }

    fun setupLayout(list: ArrayList<ContextCards>, layoutID: Int, columns: Int) {
        val contextLayout = requireView().findViewById<ContextSectionLayout>(layoutID)
        contextLayout.setupAdapter(ContextCardsAdapter(requireActivity().contentResolver, list))
        contextLayout.addDecoration(
            GridSpacingItemDecoration(
                columns,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        contextLayout.setLayoutManger(GridLayoutManager(requireContext(), columns))
    }

    fun setupLayout(list: ArrayList<ContextCards>, layoutID: Int, hideTitle: Boolean) {
        val contextLayout = requireView().findViewById<ContextSectionLayout>(layoutID)
        contextLayout.hideTitle(hideTitle)
        contextLayout.setupAdapter(ContextCardsAdapter(requireActivity().contentResolver, list))
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
        list: ArrayList<ContextCards>,
        layoutID: Int,
        columns: Int,
        hideTitle: Boolean,
    ) {
        val contextLayout = requireView().findViewById<ContextSectionLayout>(layoutID)
        contextLayout.hideTitle(hideTitle)
        contextLayout.setupAdapter(ContextCardsAdapter(requireActivity().contentResolver, list))
        contextLayout.addDecoration(
            GridSpacingItemDecoration(
                columns,
                resources.getDimension(R.dimen.recyclerSpacer).toInt(),
                true
            )
        )
        contextLayout.setLayoutManger(GridLayoutManager(requireContext(), columns))
    }

    fun buildPager(
        list: ArrayList<ContextCards>,
        iconID: Int,
        title: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        pagerAdapter: FragmentStateAdapter,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = title,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType,
            pagerAdapter = pagerAdapter
        )
        contextCards.viewType = PAGER
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = title,
            subtitle = subtitle,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = "",
            subtitle = subtitle,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        accentColor: Int,
        feature: String,
        featureType: Int,
        pagerAdapter: FragmentStateAdapter,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = "",
            accentColor = accentColor,
            feature = feature,
            featureType = featureType,
            pagerAdapter = pagerAdapter
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        summary: String,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = "",
            subtitle = subtitle,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType,
            summary = summary
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        summary: String,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = title,
            subtitle = subtitle,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType,
            summary = summary
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        enabled: Boolean,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = title,
            subtitle = subtitle,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType,
            enabled = enabled
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        enabled: Boolean,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = "",
            subtitle = subtitle,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType,
            enabled = enabled
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        summary: String,
        enabled: Boolean,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = title,
            subtitle = subtitle,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType,
            summary = summary,
            enabled = enabled
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        summary: String,
        listener: ContextCardsListener,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = title,
            subtitle = subtitle,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType,
            summary = summary,
            listener = listener
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
    }

    fun buildSwitch(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        summary: String,
        enabled: Boolean,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            title = "",
            subtitle = subtitle,
            accentColor = accentColor,
            feature = feature,
            featureType = featureType,
            summary = summary,
            enabled = enabled
        )
        contextCards.viewType = SWITCH
        list.add(contextCards)
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
    ) {
        val contextCards = ContextCards(
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
        contextCards.viewType = SWIPE
        list.add(contextCards)
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
        slideListener: OnSlideChangedListener,
    ) {
        val contextCards = ContextCards(
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
        contextCards.viewType = SWIPE
        list.add(contextCards)
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
    ) {
        val contextCards = ContextCards(
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
        contextCards.viewType = SWIPE
        list.add(contextCards)
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
        sliderListener: OnSlideChangedListener,
    ) {
        val contextCards = ContextCards(
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
        contextCards.viewType = SWIPE
        list.add(contextCards)
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
    ) {
        val contextCards = ContextCards(
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
        contextCards.viewType = SWIPE
        list.add(contextCards)
    }

    fun buildRGB(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        feature: String,
        featureType: Int,
        colorChangedListener: OnColorChangedListener,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            subtitle = subtitle,
            feature = feature,
            featureType = featureType,
            fragmentManager = requireActivity().supportFragmentManager,
            colorChangedListener = colorChangedListener
        )
        contextCards.viewType = RGB
        list.add(contextCards)
    }

    fun buildRGB(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        feature: String,
        featureType: Int,
        summary: String,
        colorChangedListener: OnColorChangedListener,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            subtitle = subtitle,
            feature = feature,
            featureType = featureType,
            summary = summary,
            fragmentManager = requireActivity().supportFragmentManager,
            colorChangedListener = colorChangedListener
        )
        contextCards.viewType = RGB
        list.add(contextCards)
    }

    fun buildRGB(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        feature: String,
        featureType: Int,
        defaultColor: Int,
        colorChangedListener: OnColorChangedListener,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            subtitle = subtitle,
            feature = feature,
            featureType = featureType,
            defaultColor = defaultColor,
            fragmentManager = requireActivity().supportFragmentManager,
            colorChangedListener = colorChangedListener
        )
        contextCards.viewType = RGB
        list.add(contextCards)
    }

    fun buildRGB(
        list: ArrayList<ContextCards>,
        iconID: Int,
        subtitle: String,
        feature: String,
        featureType: Int,
        summary: String,
        defaultColor: Int,
        colorChangedListener: OnColorChangedListener,
    ) {
        val contextCards = ContextCards(
            iconID = iconID,
            subtitle = subtitle,
            feature = feature,
            featureType = featureType,
            summary = summary,
            defaultColor = defaultColor,
            fragmentManager = requireActivity().supportFragmentManager,
            colorChangedListener = colorChangedListener
        )
        contextCards.viewType = RGB
        list.add(contextCards)
    }
}