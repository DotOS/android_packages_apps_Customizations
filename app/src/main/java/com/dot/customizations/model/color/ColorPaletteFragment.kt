package com.dot.customizations.model.color

import android.app.WallpaperColors
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.android.settingslib.collapsingtoolbar.databinding.CollapsingToolbarBaseLayoutBinding
import com.dot.customizations.databinding.FragmentColorPaletteBinding
import com.dot.customizations.model.CustomizationManager
import com.dot.customizations.model.WallpaperColorsViewModel
import com.dot.customizations.module.CustomizationInjector
import com.dot.customizations.module.InjectorProvider
import com.dot.customizations.module.ThemesUserEventLogger
import com.dot.customizations.picker.AppbarFragment
import com.dot.customizations.widget.OptionSelectorController
import com.google.common.collect.FluentIterable
import kotlinx.coroutines.launch

class ColorPaletteFragment: AppbarFragment() {

    var mLastColorApplyingTime: Long = 0
    lateinit var mColorManager: ColorCustomizationManager
    lateinit var mEventLogger: ThemesUserEventLogger
    var mHomeWallpaperColors: WallpaperColors? = null
    private var mHomeWallpaperColorsReady = false
    var mLockWallpaperColors: WallpaperColors? = null
    private var mLockWallpaperColorsReady = false
    var mSelectedColor: ColorOption? = null
    val mWallpaperColorOptions: MutableList<ColorOption?> = ArrayList()
    private lateinit var mWallpaperColorsViewModel: WallpaperColorsViewModel

    private lateinit var mainRecycler: RecyclerView
    private lateinit var secondaryRecycler: RecyclerView
    private var _rootbinding: CollapsingToolbarBaseLayoutBinding? = null
    private val rootbinding get() = _rootbinding!!
    private var _binding: FragmentColorPaletteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _rootbinding = CollapsingToolbarBaseLayoutBinding.inflate(inflater)
        mEventLogger =
            (InjectorProvider.getInjector() as CustomizationInjector).getUserEventLogger(requireActivity()) as ThemesUserEventLogger
        mColorManager =
            ColorCustomizationManager.getInstance(requireActivity())!!
        mWallpaperColorsViewModel = ViewModelProvider(requireActivity()).get(WallpaperColorsViewModel::class.java)
        val parent = rootbinding.root.findViewById<ViewGroup>(com.android.settingslib.collapsingtoolbar.R.id.content_frame)
        parent?.removeAllViews()
        _binding = FragmentColorPaletteBinding.inflate(LayoutInflater.from(rootbinding.root.context), parent, true)
        binding.root.setOnApplyWindowInsetsListener { v: View, windowInsets: WindowInsets ->
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                windowInsets.systemWindowInsetBottom
            )
            windowInsets.consumeSystemWindowInsets()
        }
        arguments = createArguments("More styles")
        setUpToolbar(rootbinding.root, true)
        return rootbinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launchWhenCreated {
            with(binding) {
                mainRecycler = mainPaletteRecycler
                secondaryRecycler = otherPaletteRecycler
                mWallpaperColorsViewModel.homeWallpaperColors.observe(viewLifecycleOwner) {
                    mHomeWallpaperColors = it
                    mHomeWallpaperColorsReady = true
                    maybeLoadColors()
                }

                mWallpaperColorsViewModel.lockWallpaperColors.observe(viewLifecycleOwner) {
                    mLockWallpaperColors = it
                    mLockWallpaperColorsReady = true
                    maybeLoadColors()
                }
            }
        }
    }

    private fun maybeLoadColors() {
        if (mHomeWallpaperColorsReady && mLockWallpaperColorsReady) {
            val wallpaperColors = mHomeWallpaperColors
            var wallpaperColors2 = mLockWallpaperColors
            mColorManager.mHomeWallpaperColors = wallpaperColors
            mColorManager.mLockWallpaperColors = wallpaperColors2

            if (wallpaperColors2 != null && wallpaperColors2 == wallpaperColors) {
                wallpaperColors2 = null
            }
            val wallpaperColors3 = mColorManager.mHomeWallpaperColors
            val colorProvider = mColorManager.mProvider as ColorProvider
            val wallpapersColorsChanged = (colorProvider.homeWallpaperColors != wallpaperColors3
                    ) || (colorProvider.lockWallpaperColors != wallpaperColors2)
            if (wallpapersColorsChanged) {
                colorProvider.homeWallpaperColors = wallpaperColors3
                colorProvider.lockWallpaperColors = wallpaperColors2
            }
            val list = colorProvider.colorBundles

            val customlist = colorProvider.customColorBundles
            if (list == null || customlist == null  || wallpapersColorsChanged) {
                lifecycleScope.launch {
                    if (wallpapersColorsChanged) {
                        ColorProvider.loadSeedColors(
                            colorProvider,
                            colorProvider.homeWallpaperColors,
                            colorProvider.lockWallpaperColors
                        )
                    }
                    colorProvider.colorBundles?.let { ColorFetch(mainRecycler).fetcher.onOptionsLoaded(it) }
                    colorProvider.customColorBundles?.let { ColorFetch(secondaryRecycler).fetcher.onOptionsLoaded(it) }
                }
            } else {
                ColorFetch(mainRecycler).fetcher.onOptionsLoaded(list)
                ColorFetch(secondaryRecycler).fetcher.onOptionsLoaded(customlist)
            }
        }
    }

    private inner class ColorFetch(val recyclerView: RecyclerView) {
        val fetcher =
            object : CustomizationManager.OptionsFetchedListener<ColorOption?> {
                override fun onError(th: Throwable?) {
                    if (th != null) {
                        Log.e("ColorSectionController", "Error loading theme bundles", th)
                    }
                }

                override fun onOptionsLoaded(list: List<ColorOption?>) {
                    if (list.isNotEmpty()) {
                        var colorOption: ColorOption?
                        mWallpaperColorOptions.clear()
                        for (color in list) {
                            if (color is ColorSeedOption) {
                                mWallpaperColorOptions.add(color)
                            }
                        }
                        val iterator = FluentIterable.from(mWallpaperColorOptions).iterator()
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
                            colorOption = mWallpaperColorOptions[0]!!
                        }
                        mSelectedColor = colorOption
                        recyclerView.attachPalette(list as MutableList<ColorOption?>)
                    }
                }
            }

    }

    private fun RecyclerView.attachPalette(colorOptions: MutableList<ColorOption?>) {
        val optionSelectorController = OptionSelectorController(
            this,
            colorOptions,
            true,
            OptionSelectorController.CheckmarkStyle.CENTER
        )
        optionSelectorController.initOptions(mColorManager)
        setUpColorOptionsController(optionSelectorController)
    }

    private fun setUpColorOptionsController(optionSelectorController: OptionSelectorController<ColorOption>) {
        if (mSelectedColor != null && optionSelectorController.containsOption(mSelectedColor)) {
            optionSelectorController.setSelectedOption(mSelectedColor)
        }
        optionSelectorController.addListener {
            if (mSelectedColor != it) {
                mSelectedColor = (it as ColorOption)
                Handler(Looper.getMainLooper()).postDelayed({
                    mColorManager.setThemeBundle(this, it)
                }, 100L)
                return@addListener
            }
        }
    }

    override fun getToolbarId(): Int {
        return com.android.settingslib.collapsingtoolbar.R.id.action_bar
    }


}