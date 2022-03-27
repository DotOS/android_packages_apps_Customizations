package com.dot.customizations.model.color

import android.app.Activity
import android.app.WallpaperColors
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dot.customizations.R
import com.dot.customizations.model.CustomizationManager
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.model.WallpaperColorsViewModel
import com.dot.customizations.model.mode.OverlayManagerCompat
import com.dot.customizations.module.CustomizationInjector
import com.dot.customizations.module.InjectorProvider
import com.dot.customizations.module.ThemesUserEventLogger
import com.dot.customizations.picker.color.ColorSectionView
import com.dot.customizations.widget.OptionSelectorController
import com.dot.customizations.widget.SeparatedTabLayout
import com.dot.customizations.widget.ViewPager2OSS
import com.google.common.collect.FluentIterable
import kotlinx.coroutines.launch
import java.util.*


class ColorSectionController(
    activity: Activity?,
    wallpaperColorsViewModel: WallpaperColorsViewModel,
    lifecycleOwner: LifecycleOwner,
    bundle: Bundle?
) : CustomizationSectionController<ColorSectionView?> {

    val mColorManager: ColorCustomizationManager
    var mColorSectionView: ColorSectionView? = null
    private val mColorSectionAdapter = ColorSectionAdapter()
    private lateinit var mColorViewPager: ViewPager2OSS
    val mEventLogger: ThemesUserEventLogger
    var mHomeWallpaperColors: WallpaperColors? = null
    private var mHomeWallpaperColorsReady = false
    private val mLifecycleOwner: LifecycleOwner
    var mLockWallpaperColors: WallpaperColors? = null
    private var mLockWallpaperColorsReady = false
    val mPresetColorOptions: MutableList<ColorOption?> = ArrayList()
    var mSelectedColor: ColorOption? = null
    val mWallpaperColorOptions: MutableList<ColorOption?> = ArrayList()
    private val mWallpaperColorsViewModel: WallpaperColorsViewModel
    var mLastColorApplyingTime: Long = 0


    inner class ColorSectionAdapter :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val mItemCounts = 2

        inner class ColorOptionsViewHolder(view: View) :
            RecyclerView.ViewHolder(
                view
            )

        override fun getItemCount(): Int {
            return mItemCounts
        }

        override fun getItemViewType(i: Int): Int {
            return R.layout.color_options_view
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            val view = viewHolder.itemView as? RecyclerView ?: return
            val colorSectionController = this@ColorSectionController
            val colorOptions =
                if (position == 0) colorSectionController.mWallpaperColorOptions
                else colorSectionController.mPresetColorOptions
            val optionSelectorController = OptionSelectorController(
                view,
                colorOptions,
                true,
                OptionSelectorController.CheckmarkStyle.CENTER
            )
            optionSelectorController.initOptions(colorSectionController.mColorManager)
            colorSectionController.setUpColorOptionsController(optionSelectorController)
        }

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): RecyclerView.ViewHolder {
            return ColorOptionsViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(i, viewGroup, false)
            )
        }
    }

    override fun createView(context: Context): ColorSectionView {
        mColorSectionView = LayoutInflater.from(context)
            .inflate(R.layout.color_section_view, null as ViewGroup?) as ColorSectionView
        mColorViewPager = mColorSectionView!!.requireViewById<ViewPager2OSS>(R.id.color_view_pager)
        mColorViewPager.mRecyclerView.adapter = mColorSectionAdapter
        mColorViewPager.setCurrentItem(0, false)
        mColorViewPager.restorePendingState()
        mColorViewPager.mUserInputEnabled = false
        mWallpaperColorsViewModel.homeWallpaperColors.observe(mLifecycleOwner) {
            mHomeWallpaperColors = it
            mHomeWallpaperColorsReady = true
            maybeLoadColors()
        }

        mWallpaperColorsViewModel.lockWallpaperColors.observe(mLifecycleOwner) {
            mLockWallpaperColors = it
            mLockWallpaperColorsReady = true
            maybeLoadColors()
        }

        return mColorSectionView!!
    }

    override fun isAvailable(context: Context?): Boolean {
        return context != null && ColorUtils.isMonetEnabled(context)
    }

    private fun maybeLoadColors() {
        if (mHomeWallpaperColorsReady && mLockWallpaperColorsReady) {
            val wallpaperColors = mHomeWallpaperColors
            var wallpaperColors2 = mLockWallpaperColors
            mColorManager.mHomeWallpaperColors = wallpaperColors
            mColorManager.mLockWallpaperColors = wallpaperColors2
            val optionsFetcher =
                object : CustomizationManager.OptionsFetchedListener<ColorOption?> {
                    override fun onError(th: Throwable?) {
                        if (th != null) {
                            Log.e("ColorSectionController", "Error loading theme bundles", th)
                        }
                    }

                    override fun onOptionsLoaded(list: List<ColorOption?>) {
                        var colorOption: ColorOption?
                        val colorOption2: ColorOption
                        mWallpaperColorOptions.clear()
                        mPresetColorOptions.clear()
                        for (colorOption3 in list) {
                            if (colorOption3 is ColorSeedOption) {
                                mWallpaperColorOptions.add(colorOption3)
                            } else if (colorOption3 is ColorBundle) {
                                mPresetColorOptions.add(colorOption3)
                            }
                        }
                        val allColors = ArrayList<ColorOption?>()
                        allColors.addAll(mWallpaperColorOptions)
                        allColors.addAll(mPresetColorOptions)
                        val iterator = FluentIterable.from(allColors).iterator()
                        while (true) {
                            if (!iterator.hasNext()) {
                                colorOption = null
                                break
                            }
                            colorOption = iterator.next()
                            if (colorOption!!.isActive(mColorManager)) {
                                break
                            }
                        }
                        if (colorOption == null) {
                            colorOption2 =
                                (if (mWallpaperColorOptions.isEmpty()) mPresetColorOptions[0]!! else mWallpaperColorOptions[0]!!)
                            colorOption = colorOption2
                        }
                        mSelectedColor = colorOption
                        mColorViewPager.post {
                            mColorViewPager.adapter?.notifyDataSetChanged()
                            /*if (mTabLayout != null && mTabLayout!!.tabCount == 0) {
                                val newTab = mTabLayout!!.newTab()
                                newTab.setText(R.string.wallpaper_color_tab)
                                mTabLayout!!.addTab(newTab, 0, mTabLayout!!.tabCount == 0)
                                val newTab2 = mTabLayout!!.newTab()
                                newTab2.setText(R.string.preset_color_tab)
                                mTabLayout!!.addTab(newTab2, 1, mTabLayout!!.tabCount == 0)
                            }*/
                            if (mWallpaperColorOptions.isEmpty()) {
                                //mTabLayout!!.getTabAt(0)!!.view.isEnabled = false
                                mColorViewPager.setCurrentItem(1, false)
                            }
                            mColorViewPager.setCurrentItem(
                                if ("preset" == mColorManager.currentColorSource) 1 else 0,
                                false
                            )
                            mColorViewPager.mUserInputEnabled = false
                        }
                    }
                }
            if (wallpaperColors2 != null && wallpaperColors2 == wallpaperColors) {
                wallpaperColors2 = null
            }
            val wallpaperColors3 = mColorManager.mHomeWallpaperColors
            val colorProvider = mColorManager.mProvider as ColorProvider
            val wallpapersColorsChanged = (colorProvider.homeWallpaperColors == wallpaperColors3
                    ) || (colorProvider.lockWallpaperColors == wallpaperColors2)
            if (wallpapersColorsChanged) {
                colorProvider.homeWallpaperColors = wallpaperColors3
                colorProvider.lockWallpaperColors = wallpaperColors2
            }
            val list = colorProvider.colorBundles
            if (list == null || wallpapersColorsChanged) {
                mLifecycleOwner.lifecycleScope.launch {
                    if (wallpapersColorsChanged) {
                        ColorProvider.loadSeedColors(
                            colorProvider,
                            wallpaperColors3,
                            wallpaperColors2
                        )
                    }
                    optionsFetcher.onOptionsLoaded(colorProvider.colorBundles!!)
                }
            } else {
                optionsFetcher.onOptionsLoaded(list)
            }
        }
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        val viewPager2 = mColorViewPager
        bundle.putInt("COLOR_TAB_POSITION", viewPager2.currentItem)
    }

    fun setUpColorOptionsController(optionSelectorController: OptionSelectorController<ColorOption>) {
        if (mSelectedColor != null && optionSelectorController.containsOption(mSelectedColor)) {
            optionSelectorController.setSelectedOption(mSelectedColor)
        }
        optionSelectorController.addListener {
            if (mSelectedColor != it) {
                mSelectedColor = (it as ColorOption?)!!
                Handler(Looper.getMainLooper()).postDelayed({
                    mColorManager.setThemeBundle(this, mSelectedColor!!)
                }, 100L)
                return@addListener
            }
        }
    }

    init {
        mEventLogger =
            (InjectorProvider.getInjector() as CustomizationInjector).getUserEventLogger(activity) as ThemesUserEventLogger
        mColorManager =
            ColorCustomizationManager.getInstance(activity!!, OverlayManagerCompat(activity))!!
        mWallpaperColorsViewModel = wallpaperColorsViewModel
        mLifecycleOwner = lifecycleOwner
        /*if (bundle != null && bundle.containsKey("COLOR_TAB_POSITION")) {
            mTabPositionToRestore =
                Optional.of(Integer.valueOf(bundle.getInt("COLOR_TAB_POSITION")))
        }*/
    }
}