/*
 * Copyright (C) 2022 The Android Open Source Project
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
package com.dot.customizations.model.color

import android.app.Activity
import android.app.WallpaperColors
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.stats.style.StyleEnums
import android.text.TextUtils
import android.util.LayoutDirection
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationManager
import com.dot.customizations.model.theme.OverlayManagerCompat
import com.dot.customizations.module.CustomizationInjector
import com.dot.customizations.module.ThemesUserEventLogger
import com.dot.customizations.picker.color.ColorSectionView
import com.dot.customizations.widget.OptionSelectorController
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.model.WallpaperColorsViewModel
import com.dot.customizations.module.InjectorProvider
import com.dot.customizations.module.LargeScreenMultiPanesChecker
import com.dot.customizations.widget.PageIndicator
import com.dot.customizations.widget.SeparatedTabLayout

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import java.util.*
import kotlin.math.min

/**
 * Color section view's controller for the logic of color customization.
 */
class ColorSectionController(
    activity: Activity?, viewModel: WallpaperColorsViewModel,
    lifecycleOwner: LifecycleOwner, savedInstanceState: Bundle?
) : CustomizationSectionController<ColorSectionView?> {
    private val mEventLogger: ThemesUserEventLogger
    private val mColorManager: ColorCustomizationManager?
    private val mWallpaperColorsViewModel: WallpaperColorsViewModel
    private val mLifecycleOwner: LifecycleOwner
    private val mColorSectionAdapter = ColorSectionAdapter()
    private var mWallpaperColorOptions: List<ColorOption?> = ArrayList()
    private var mPresetColorOptions: List<ColorOption?> = ArrayList()
    private val mColorSectionViewPager by lazy { mColorSectionView!!.findViewById<ViewPager2>(R.id.color_section_view_pager) }
    private var mSelectedColor: ColorOption? = null
    private val mTabLayout by lazy { mColorSectionView!!.findViewById<SeparatedTabLayout>(R.id.separated_tabs) }
    private var mHomeWallpaperColors: WallpaperColors? = null
    private var mLockWallpaperColors: WallpaperColors? = null

    // Uses a boolean value to indicate whether wallpaper color is ready because WallpaperColors
    // maybe be null when it's ready.
    private var mHomeWallpaperColorsReady = false
    private var mLockWallpaperColorsReady = false
    private var mTabPositionToRestore = Optional.empty<Int>()
    private val mPagePositionToRestore: Array<Optional<Int>> =
        arrayOf(Optional.empty<Int>(), Optional.empty<Int>())
    private var mLastColorApplyingTime = 0L
    private var mColorSectionView: ColorSectionView? = null
    private val mIsMultiPane: Boolean

    init {
        val injector: CustomizationInjector =
            InjectorProvider.getInjector() as CustomizationInjector
        mEventLogger = injector.getUserEventLogger(activity) as ThemesUserEventLogger
        mColorManager = ColorCustomizationManager.getInstance(
            activity!!,
            OverlayManagerCompat(activity)
        )
        mWallpaperColorsViewModel = viewModel
        mLifecycleOwner = lifecycleOwner
        mIsMultiPane = LargeScreenMultiPanesChecker().isMultiPanesEnabled(activity)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_COLOR_TAB_POSITION)) {
                mTabPositionToRestore = Optional.of(
                    savedInstanceState.getInt(KEY_COLOR_TAB_POSITION)
                )
            }
            for (i in mPagePositionToRestore.indices) {
                val keyColorPage = getPagePositionKey(i)
                if (savedInstanceState.containsKey(keyColorPage)) {
                    setPagePositionToRestore(i, savedInstanceState.getInt(keyColorPage))
                }
            }
        }
    }

    private fun getPagePositionKey(index: Int): String {
        return String.format(Locale.US, "%s_%d", KEY_COLOR_PAGE_POSITION, index)
    }

    private fun setPagePositionToRestore(pagePositionKeyIndex: Int, pagePosition: Int) {
        if (pagePositionKeyIndex >= 0 && pagePositionKeyIndex < mPagePositionToRestore.size) {
            mPagePositionToRestore[pagePositionKeyIndex] = Optional.of(pagePosition)
        }
    }

    private fun getPagePositionToRestore(pagePositionKeyIndex: Int, defaultPagePosition: Int): Int {
        return if (pagePositionKeyIndex >= 0 && pagePositionKeyIndex < mPagePositionToRestore.size) {
            mPagePositionToRestore[pagePositionKeyIndex].orElse(defaultPagePosition)
        } else 0
    }

    override fun isAvailable(context: Context?): Boolean {
        return context != null && ColorUtils.isMonetEnabled(context) && mColorManager!!.isAvailable
    }

    override fun createView(context: Context): ColorSectionView {
        mColorSectionView = LayoutInflater.from(context).inflate(
            R.layout.color_section_view,  /* root= */null
        ) as ColorSectionView
        mColorSectionViewPager.setAccessibilityDelegate(createAccessibilityDelegate(ID_VIEWPAGER))
        mColorSectionViewPager.adapter = mColorSectionAdapter
        mColorSectionViewPager.isUserInputEnabled = false
        if (ColorProvider.themeStyleEnabled) {
            mColorSectionViewPager!!.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        mColorSectionAdapter.setNumColors(
            context.resources.getInteger(
                R.integer.options_grid_num_columns
            )
        )
        // TODO(b/202145216): Use just 2 views when tapping either button on top.
        mTabLayout.setViewPager(mColorSectionViewPager)
        mWallpaperColorsViewModel.homeWallpaperColors.observe(
            mLifecycleOwner
        ) { homeColors ->
            mHomeWallpaperColors = homeColors
            mHomeWallpaperColorsReady = true
            maybeLoadColors()
        }
        mWallpaperColorsViewModel.lockWallpaperColors.observe(
            mLifecycleOwner
        ) { lockColors ->
            mLockWallpaperColors = lockColors
            mLockWallpaperColorsReady = true
            maybeLoadColors()
        }
        return mColorSectionView as ColorSectionView
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (mColorSectionViewPager != null) {
            savedInstanceState.putInt(
                KEY_COLOR_TAB_POSITION,
                mColorSectionViewPager.currentItem
            )
            for (i in mPagePositionToRestore.indices) {
                savedInstanceState.putInt(getPagePositionKey(i), getPagePositionToRestore(i, 0))
            }
        }
    }

    private fun maybeLoadColors() {
        if (mHomeWallpaperColorsReady && mLockWallpaperColorsReady) {
            mColorManager!!.setWallpaperColors(mHomeWallpaperColors, mLockWallpaperColors)
            loadColorOptions( /* reload= */false)
        }
    }

    private fun loadColorOptions(reload: Boolean) {
        mColorManager!!.fetchOptions(object :
            CustomizationManager.OptionsFetchedListener<ColorOption?> {
            override fun onOptionsLoaded(options: List<ColorOption?>) {
                val wallpaperColorOptions: MutableList<ColorOption?> = ArrayList()
                val presetColorOptions: MutableList<ColorOption?> = ArrayList()
                for (option in options) {
                    if (option is ColorSeedOption) {
                        wallpaperColorOptions.add(option)
                    } else if (option is ColorBundle) {
                        presetColorOptions.add(option)
                    }
                }
                mWallpaperColorOptions = wallpaperColorOptions
                mPresetColorOptions = presetColorOptions
                mSelectedColor = findActiveColorOption(
                    mWallpaperColorOptions,
                    mPresetColorOptions
                )
                mTabLayout.post { setUpColorViewPager() }
            }

            override fun onError(throwable: Throwable?) {
                if (throwable != null) {
                    Log.e(TAG, "Error loading theme bundles", throwable)
                }
            }
        }, reload)
    }

    private fun setUpColorViewPager() {
        mColorSectionAdapter.notifyDataSetChanged()
        if (mTabLayout != null && mTabLayout.tabCount == 0) {
            mTabLayout.addTab(
                mTabLayout.newTab().setText(R.string.wallpaper_color_tab),
                WALLPAPER_TAB_INDEX
            )
            mTabLayout.addTab(
                mTabLayout.newTab().setText(R.string.preset_color_tab),
                PRESET_TAB_INDEX
            )
        }
        if (mWallpaperColorOptions.isEmpty()) {
            // Select preset tab and disable wallpaper tab.
            mTabLayout.getTabAt(WALLPAPER_TAB_INDEX)!!.view.isEnabled = false
            mColorSectionViewPager.setCurrentItem(PRESET_TAB_INDEX,  /* smoothScroll= */false)
            return
        }
        mColorSectionViewPager.setCurrentItem(
            mTabPositionToRestore.orElseGet { if (ColorOptionsProvider.COLOR_SOURCE_PRESET == mColorManager!!.currentColorSource) PRESET_TAB_INDEX else WALLPAPER_TAB_INDEX },  /* smoothScroll= */
            false
        )

        // Disable "wallpaper colors" and "basic colors" swiping for new color style.
        mColorSectionViewPager.isUserInputEnabled = !ColorProvider.themeStyleEnabled
    }

    private fun setupColorPages(
        container: ViewPager2, colorsPerPage: Int, sectionPosition: Int,
        options: List<ColorOption?>, pageIndicator: PageIndicator
    ) {
        container.adapter = ColorPageAdapter(
            options,  /* pageEnabled= */true,
            colorsPerPage
        )
        if (ColorProvider.themeStyleEnabled) {
            // Update page index to show selected items.
            val selectedIndex = options.indexOf(mSelectedColor)
            if (colorsPerPage != 0) {
                val pageIndex = selectedIndex / colorsPerPage
                val position = getPagePositionToRestore(sectionPosition, pageIndex)
                container.setCurrentItem(position,  /* smoothScroll= */false)
            }
            pageIndicator.setNumPages(getNumPages(colorsPerPage, options.size))
            registerOnPageChangeCallback(sectionPosition, container, pageIndicator)
        }
    }

    private fun registerOnPageChangeCallback(
        sectionPosition: Int, container: ViewPager2,
        pageIndicator: PageIndicator
    ) {
        container.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (mColorSectionViewPager.currentItem == sectionPosition) {
                    pageIndicator.setLocation(getPagePosition(pageIndicator, position).toFloat())
                    setPagePositionToRestore(sectionPosition, position)
                }
            }

            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                if (mColorSectionViewPager.currentItem == sectionPosition) {
                    pageIndicator.setLocation(getPagePosition(pageIndicator, position).toFloat())
                    setPagePositionToRestore(sectionPosition, position)
                }
            }

            private fun getPagePosition(pageIndicator: PageIndicator, position: Int): Int {
                return if (pageIndicator.layoutDirection == LayoutDirection.RTL) pageIndicator.childCount - 1 - position else position
            }
        })
    }

    private fun setupColorOptions(
        container: RecyclerView, colorOptions: List<ColorOption?>,
        pageEnabled: Boolean, index: Int, colorsPerPage: Int
    ) {
        val totalSize = colorOptions.size
        if (totalSize == 0) {
            return
        }
        val subOptions: List<ColorOption?> = if (pageEnabled && ColorProvider.themeStyleEnabled) {
            colorOptions.subList(
                colorsPerPage * index,
                min(colorsPerPage * (index + 1), totalSize)
            )
        } else {
            colorOptions
        }
        val adaptiveController: OptionSelectorController<ColorOption> = OptionSelectorController(
            container, subOptions,  /* useGrid= */true, OptionSelectorController.CheckmarkStyle.CENTER
        )
        adaptiveController.initOptions(mColorManager)
        setUpColorOptionsController(adaptiveController)
    }

    private fun findActiveColorOption(
        wallpaperColorOptions: List<ColorOption?>,
        presetColorOptions: List<ColorOption?>
    ): ColorOption? {
        var activeColorOption: ColorOption? = null
        for (colorOption in Lists.newArrayList(
            Iterables.concat(wallpaperColorOptions, presetColorOptions)
        )) {
            if (colorOption!!.isActive(mColorManager!!)) {
                activeColorOption = colorOption
                break
            }
        }
        // Use the first one option by default. This should not happen as above should have an
        // active option found.
        if (activeColorOption == null) {
            activeColorOption =
                if (wallpaperColorOptions.isEmpty()) presetColorOptions[0] else wallpaperColorOptions[0]
        }
        return activeColorOption
    }

    private fun setUpColorOptionsController(
        optionSelectorController: OptionSelectorController<ColorOption>
    ) {
        if (mSelectedColor != null && optionSelectorController.containsOption(mSelectedColor)) {
            optionSelectorController.setSelectedOption(mSelectedColor)
        }
        optionSelectorController.addListener { selectedOption ->
            val selectedColor = selectedOption as ColorOption
            if (mSelectedColor == selectedColor) {
                return@addListener
            }
            mSelectedColor = selectedOption
            // Post with delay for color option to run ripple.
            Handler().postDelayed({ applyColor(mSelectedColor) },  /* delayMillis= */100)
        }
    }

    private fun applyColor(colorOption: ColorOption?) {
        if (SystemClock.elapsedRealtime() - mLastColorApplyingTime < MIN_COLOR_APPLY_PERIOD) {
            return
        }
        mLastColorApplyingTime = SystemClock.elapsedRealtime()
        mColorManager!!.apply(colorOption!!, object : CustomizationManager.Callback {
            override fun onSuccess() {
                mColorSectionView!!.announceForAccessibility(
                    mColorSectionView!!.getContext().getString(R.string.color_changed)
                )
            }

            override fun onError(throwable: Throwable?) {
                Log.w(TAG, "Apply theme with error: $throwable")
            }
        })
    }

    private fun getColorAction(colorOption: ColorOption?): Int {
        var action: Int = StyleEnums.DEFAULT_ACTION
        val isForBoth = mLockWallpaperColors == null || mLockWallpaperColors == mHomeWallpaperColors
        if (TextUtils.equals(colorOption!!.source, ColorOptionsProvider.COLOR_SOURCE_PRESET)) {
            action = StyleEnums.COLOR_PRESET_APPLIED
        } else if (isForBoth) {
            action = StyleEnums.COLOR_WALLPAPER_HOME_LOCK_APPLIED
        } else {
            when (colorOption.source) {
                ColorOptionsProvider.COLOR_SOURCE_HOME -> action =
                    StyleEnums.COLOR_WALLPAPER_HOME_APPLIED
                ColorOptionsProvider.COLOR_SOURCE_LOCK -> action =
                    StyleEnums.COLOR_WALLPAPER_LOCK_APPLIED
            }
        }
        return action
    }

    private fun createAccessibilityDelegate(id: String): View.AccessibilityDelegate {
        return object : View.AccessibilityDelegate() {
            override fun onInitializeAccessibilityNodeInfo(
                host: View,
                info: AccessibilityNodeInfo
            ) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                //info.setUniqueId(id)
            }
        }
    }

    private inner class ColorSectionAdapter : RecyclerView.Adapter<ColorSectionAdapter.ColorPageViewHolder>() {
        private val mItemCounts: Int = intArrayOf(WALLPAPER_TAB_INDEX, PRESET_TAB_INDEX).size
        private var mNumColors = 0
        override fun getItemCount(): Int {
            return mItemCounts
        }

        override fun onBindViewHolder(viewHolder: ColorPageViewHolder, position: Int) {
            when (position) {
                WALLPAPER_TAB_INDEX -> setupColorPages(
                    viewHolder.mContainer, mNumColors, position,
                    mWallpaperColorOptions, viewHolder.mPageIndicator
                )
                PRESET_TAB_INDEX -> setupColorPages(
                    viewHolder.mContainer, mNumColors, position,
                    mPresetColorOptions, viewHolder.mPageIndicator
                )
                else -> {}
            }
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ColorPageViewHolder {
            return ColorPageViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(
                    viewType, viewGroup, false
                )
            )
        }

        override fun getItemViewType(position: Int): Int {
            return R.layout.color_pages_view
        }

        fun setNumColors(numColors: Int) {
            mNumColors = numColors
        }

        private inner class ColorPageViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val mContainer: ViewPager2
            val mPageIndicator: PageIndicator

            init {
                mContainer = itemView.findViewById(R.id.color_page_container)
                // Correct scrolling goes under collapsing toolbar while scrolling oclor options.
                mContainer.getChildAt(0).isNestedScrollingEnabled = false
                /**
                 * Sets page transformer with margin to separate color pages and
                 * sets color pages' padding to not scroll to window boundary if multi-pane case
                 */
                if (mIsMultiPane) {
                    val padding = itemView.context.resources.getDimensionPixelSize(
                        R.dimen.section_horizontal_padding
                    )
                    mContainer.setPageTransformer(MarginPageTransformer(padding * 2))
                    mContainer.setPadding(padding,  /* top= */0, padding,  /* bottom= */0)
                }
                mPageIndicator = itemView.findViewById(R.id.color_page_indicator)
                if (ColorProvider.themeStyleEnabled) {
                    mPageIndicator.visibility = View.VISIBLE
                }
                itemView.setAccessibilityDelegate(createAccessibilityDelegate(ID_ITEMVIEW))
                mContainer.setAccessibilityDelegate(createAccessibilityDelegate(ID_CONTAINER))
            }
        }
    }

    private inner class ColorPageAdapter(
        private val mColorOptions: List<ColorOption?>, private val mPageEnabled: Boolean,
        private val mColorsPerPage: Int
    ) : RecyclerView.Adapter<ColorPageAdapter.ColorOptionViewHolder>() {
        override fun getItemCount(): Int {
            return if (!mPageEnabled || !ColorProvider.themeStyleEnabled) {
                1
            } else getNumPages(mColorsPerPage, mColorOptions.size)
            // Color page size.
        }

        override fun onBindViewHolder(viewHolder: ColorOptionViewHolder, position: Int) {
            setupColorOptions(
                viewHolder.mContainer, mColorOptions, mPageEnabled, position,
                mColorsPerPage
            )
        }

        override fun onCreateViewHolder(
            viewGroup: ViewGroup,
            viewType: Int
        ): ColorOptionViewHolder {
            return ColorOptionViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(
                    viewType, viewGroup,
                    false
                )
            )
        }

        override fun getItemViewType(position: Int): Int {
            return R.layout.color_options_view
        }

        private inner class ColorOptionViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val mContainer: RecyclerView

            init {
                mContainer = itemView.findViewById(R.id.color_option_container)
                // Sets layout with margins for non multi-pane case to separate color options.
                if (!mIsMultiPane) {
                    val layoutParams = FrameLayout.LayoutParams(
                        mContainer.layoutParams
                    )
                    val margin = itemView.context.resources.getDimensionPixelSize(
                        R.dimen.section_horizontal_padding
                    )
                    layoutParams.setMargins(margin,  /* top= */0, margin,  /* bottom= */0)
                    mContainer.layoutParams = layoutParams
                }
            }
        }
    }

    companion object {
        private const val TAG = "ColorSectionController"
        private const val KEY_COLOR_TAB_POSITION = "COLOR_TAB_POSITION"
        private const val KEY_COLOR_PAGE_POSITION = "COLOR_PAGE_POSITION"
        private const val ID_VIEWPAGER = "ColorSectionController_colorSectionViewPager"
        private const val ID_ITEMVIEW = "ColorSectionController_itemView"
        private const val ID_CONTAINER = "ColorSectionController_container"
        private const val MIN_COLOR_APPLY_PERIOD = 500L
        private const val WALLPAPER_TAB_INDEX = 0
        private const val PRESET_TAB_INDEX = 1
        private fun getNumPages(optionsPerPage: Int, totalOptions: Int): Int {
            return Math.ceil((totalOptions.toFloat() / optionsPerPage).toDouble()).toInt()
        }
    }
}