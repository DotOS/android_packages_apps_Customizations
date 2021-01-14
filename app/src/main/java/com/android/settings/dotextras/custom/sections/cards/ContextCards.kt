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
package com.android.settings.dotextras.custom.sections.cards

import android.annotation.ColorInt
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.settings.dotextras.custom.sections.cards.ContextCardsAdapter.Type
import com.android.settings.dotextras.custom.utils.ColorSheetUtils

typealias ContextCardsListener = ((value: Int) -> Unit)?
typealias OnColorChangedListener = ((colorInt: Int) -> Unit)?
typealias OnSlideChangedListener = ((position: Int, title: TextView) -> Unit)?

class ContextCards(
    var iconID: Int,
    var title: String,
    var subtitle: String,
    var accentColor: Int,
    val feature: String,
    val featureType: Int,
) {

    //Common variables
    var summary: String? = null
    var default: Int = -1
    var listener: ContextCardsListener = null
    var viewType: Int = Type.SWITCH

    //Switch variables
    var isCardChecked: Boolean = false

    //Swipe variables
    var extraTitle: String? = null
    var value: Int = -1
    var max: Int = -1
    var min: Int = -1
    var slideListener: OnSlideChangedListener = null

    //Pager variables
    var pagerAdapter: FragmentStateAdapter? = null

    //RGB variables
    @ColorInt
    var defaultColor: Int = 0xFFFFFF

    @ColorInt
    var colorInt: Int = defaultColor
    var colorListener: OnColorChangedListener = null
    var fragmentManager: FragmentManager? = null

    constructor(
        iconID: Int,
        title: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        pagerAdapter: FragmentStateAdapter,
    ) : this(iconID, title, "", accentColor, feature, featureType) {
        this.pagerAdapter = pagerAdapter
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        summary: String,
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.summary = summary
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        enabled: Boolean,
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.isCardChecked = enabled
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        summary: String,
        enabled: Boolean,
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.summary = summary
        this.isCardChecked = enabled
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        summary: String,
        listener: ContextCardsListener,
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.summary = summary
        this.listener = listener
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.min = min
        this.max = max
        this.default = default
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
        summary: String,
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.min = min
        this.max = max
        this.default = default
        this.summary = summary
    }

    constructor(
        iconID: Int,
        title: String,
        subtitle: String,
        accentColor: Int,
        feature: String,
        featureType: Int,
        min: Int,
        max: Int,
        default: Int,
        summary: String,
        extraTitle: String,
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.min = min
        this.max = max
        this.default = default
        this.summary = summary
        this.extraTitle = extraTitle
    }

    constructor(
        iconID: Int,
        title: String,
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
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.min = min
        this.max = max
        this.default = default
        this.summary = summary
        this.extraTitle = extraTitle
        this.slideListener = slideListener
    }

    constructor(
        iconID: Int,
        title: String,
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
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.min = min
        this.max = max
        this.default = default
        this.summary = summary
        this.extraTitle = extraTitle
        this.listener = listener
    }

    constructor(
        iconID: Int,
        title: String,
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
        slideListener: OnSlideChangedListener,
    ) : this(iconID, title, subtitle, accentColor, feature, featureType) {
        this.min = min
        this.max = max
        this.default = default
        this.summary = summary
        this.extraTitle = extraTitle
        this.listener = listener
        this.slideListener = slideListener
    }

    constructor(
        iconID: Int,
        subtitle: String,
        feature: String,
        featureType: Int,
        fragmentManager: FragmentManager,
        colorChangedListener: OnColorChangedListener,
    ) : this(iconID, "", subtitle, 0xFFFFFF, feature, featureType) {
        this.fragmentManager = fragmentManager
        this.title = ColorSheetUtils.colorToHex(colorInt)
        this.colorListener = colorChangedListener
    }

    constructor(
        iconID: Int,
        subtitle: String,
        feature: String,
        featureType: Int,
        defaultColor: Int,
        fragmentManager: FragmentManager,
        colorChangedListener: OnColorChangedListener,
    ) : this(iconID, "", subtitle, 0xFFFFFF, feature, featureType) {
        this.fragmentManager = fragmentManager
        this.defaultColor = defaultColor
        this.title = ColorSheetUtils.colorToHex(colorInt)
        this.colorListener = colorChangedListener
    }

    constructor(
        iconID: Int,
        subtitle: String,
        feature: String,
        featureType: Int,
        summary: String,
        fragmentManager: FragmentManager,
        colorChangedListener: OnColorChangedListener,
    ) : this(iconID, "", subtitle, 0xFFFFFF, feature, featureType) {
        this.fragmentManager = fragmentManager
        this.summary = summary
        this.title = ColorSheetUtils.colorToHex(colorInt)
        this.colorListener = colorChangedListener
    }

    constructor(
        iconID: Int,
        subtitle: String,
        feature: String,
        featureType: Int,
        summary: String,
        defaultColor: Int,
        fragmentManager: FragmentManager,
        colorChangedListener: OnColorChangedListener,
    ) : this(iconID, "", subtitle, 0xFFFFFF, feature, featureType) {
        this.fragmentManager = fragmentManager
        this.defaultColor = defaultColor
        this.summary = summary
        this.title = ColorSheetUtils.colorToHex(colorInt)
        this.colorListener = colorChangedListener
    }

}